# Security Audit — VEFA Validator

- **Date:** 2026-04-22
- **Commit:** 9341abe030c7797a882052b0ebdbd382e7a9f4a7
- **Java version:** OpenJDK 21.0.10 (Temurin)
- **Build tool:** Maven 3, parent POM `com.helger:parent-pom:3.0.3`
- **Modules audited:** validator-api, validator-core, validator-build, validator-tester, validator-dist

## Executive Summary

The VEFA Validator is an XML document validation library with a Guice-based plugin architecture. It processes XML, ZIP, and ASiC-E archives from potentially untrusted sources. The audit identified **2 High-severity** and **5 Medium-severity** findings. The most significant issues are: (1) XML parsers (`XMLInputFactory`, `TransformerFactory`, `SchemaFactory`, JAXB `Unmarshaller`) are instantiated without XXE hardening, exposing the library to XML External Entity attacks; (2) ZIP archive entry names from `ZipDeclaration.children()` are not sanitized, enabling Zip Slip path traversal. No Critical-severity findings were identified because the library does not directly write extracted files to disk (the Zip Slip risk materializes only if a caller uses the filenames for file I/O), and the XXE risk is partially mitigated by `BlockingURIResolver` in some code paths. The codebase does not claim a "security first" design philosophy; security posture is **Partial** — some defenses exist (e.g., `BlockingURIResolver`, 50KB read limit) but they are inconsistently applied.

## Security-First Principle Scorecard

| Principle | Verdict | Evidence |
|---|---|---|
| Secure defaults | **Weak** | XML parsers use factory defaults (XXE-enabled). Callers cannot opt in to hardening. |
| Input validation at trust boundaries | **Weak** | No validation of ZIP entry names, artifact filenames, URI resolver paths, or JAXB-unmarshalled content. |
| Least privilege | **Partial** | Many core classes are package-private. But `ValidatorEngine` internals (mutable maps) are accessible via getters. |
| Fail securely | **Partial** | Exceptions produce FATAL flags in reports. Some `catch` blocks swallow exceptions silently (e.g., `ZipDeclaration.children()` returns null). |
| Defense in depth | **Weak** | `BlockingURIResolver` exists but only covers `":/"`-containing URIs. No layered validation. |
| No secrets in code or logs | **Strong** | No hardcoded credentials, keys, or secrets found. Logging does not emit sensitive data. |
| Auditability | **Partial** | Validation actions are logged (INFO-level). No structured security event logging. |
| Dependency hygiene | **Partial** | Dependencies are reasonably current. No security scanning configured. Versions are pinned. |

## Findings

### F-01 — XMLInputFactory without XXE protection [Severity: High]

- **CWE:** CWE-611 (Improper Restriction of XML External Entity Reference)
- **Location:**
  - `validator-core/src/main/java/no/difi/vefa/validator/declaration/AbstractXmlDeclaration.java:12`
  - `validator-core/src/main/java/no/difi/vefa/validator/checker/XsdChecker.java:28`
- **Description:** Two `XMLInputFactory` instances are created via `newFactory()` / `newInstance()` without disabling external entity support or DTD processing. These factories are used to parse XML content submitted for validation — content that is untrusted by nature.
- **Impact:** An attacker submitting a crafted XML document for validation could exfiltrate local files (via `file://` entity references), perform SSRF (via `http://` entity references), or cause denial of service (via entity expansion / "Billion Laughs"). The `AbstractXmlDeclaration` factory is used by all XML-based declaration detectors (`NoblDeclaration`, `EspdDeclaration`, `UnCefactDeclaration`, `ValidatorTestDeclaration`, `AsiceXmlDeclaration`).
- **Evidence:** Verified via `XxeProtectionTest.testXmlInputFactoryXxeDefault()` — the default factory reports `IS_SUPPORTING_EXTERNAL_ENTITIES=true` and `SUPPORT_DTD=true`.
- **Remediation:**
  ```java
  protected static final XMLInputFactory XML_INPUT_FACTORY;
  static {
      XML_INPUT_FACTORY = XMLInputFactory.newFactory ();
      XML_INPUT_FACTORY.setProperty (XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
      XML_INPUT_FACTORY.setProperty (XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
  }
  ```
  Apply the same hardening in `XsdChecker.java:28`. This disables DTD processing entirely, which is safe since validation artifacts are XSD/Schematron — not DTD-based.
- **References:** [OWASP XXE Prevention Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/XML_External_Entity_Prevention_Cheat_Sheet.html), CWE-611

---

### F-02 — JAXB Unmarshaller without XXE protection [Severity: High]

- **CWE:** CWE-611 (Improper Restriction of XML External Entity Reference)
- **Location:**
  - `validator-core/src/main/java/no/difi/vefa/validator/ValidatorEngine.java:127-129`
  - `validator-core/src/main/java/no/difi/vefa/validator/source/RepositorySourceInstance.java:33`
  - `validator-core/src/main/java/no/difi/vefa/validator/source/DirectorySourceInstance.java:59`
  - `validator-core/src/main/java/no/difi/vefa/validator/declaration/ValidatorTestDeclaration.java:116`
- **Description:** JAXB `Unmarshaller` instances are created via `JAXB_CONTEXT.createUnmarshaller()` and used to deserialize XML from artifact sources (remote HTTP, local directories, and input streams) without configuring XXE protections. The `StreamSource` passed to `unmarshaller.unmarshal()` does not prevent external entity resolution.
- **Impact:** A malicious `config.xml` or `artifacts.xml` file (from a compromised repository, a MITM attack on the HTTP fetch, or a crafted local directory) could trigger XXE. In `RepositorySourceInstance.java:33`, the unmarshaller directly fetches and parses a remote URL: `unmarshaller.unmarshal(artifactsUri.toURL())` — this is the highest-risk call site because it combines network fetch with insecure parsing.
- **Evidence:** Code inspection confirms no `XMLInputFactory`, `SAXParserFactory`, or `XMLReader` is configured before unmarshalling.
- **Remediation:**
  ```java
  final SAXParserFactory spf = SAXParserFactory.newInstance ();
  spf.setNamespaceAware (true);
  spf.setFeature ("http://apache.org/xml/features/disallow-doctype-decl", true);
  spf.setFeature ("http://xml.org/sax/features/external-general-entities", false);
  spf.setFeature ("http://xml.org/sax/features/external-parameter-entities", false);
  final XMLReader xmlReader = spf.newSAXParser ().getXMLReader ();
  final SAXSource saxSource = new SAXSource (xmlReader, new InputSource (inputStream));
  unmarshaller.unmarshal (saxSource, Configurations.class);
  ```
  Create a shared utility method for safe unmarshalling to avoid duplication.
- **References:** CWE-611, [OWASP XXE Prevention - JAXB](https://cheatsheetseries.owasp.org/cheatsheets/XML_External_Entity_Prevention_Cheat_Sheet.html#jaxb-unmarshaller)

---

### F-03 — Zip Slip: unsanitized ZIP entry names [Severity: Medium]

- **CWE:** CWE-22 (Improper Limitation of a Pathname to a Restricted Directory)
- **Location:**
  - `validator-core/src/main/java/no/difi/vefa/validator/declaration/ZipDeclaration.java:75`
  - `validator-core/src/main/java/no/difi/vefa/validator/declaration/AsiceDeclaration.java:85`
  - `validator-core/src/main/java/no/difi/vefa/validator/declaration/AsiceXmlDeclaration.java:101`
  - `validator-core/src/main/java/no/difi/vefa/validator/util/ArtifactHolderImpl.java:61`
- **Description:** `ZipDeclaration.children()` iterates ZIP entries and stores them as `CachedFile` objects using `zipEntry.getName()` directly, with no validation for path traversal sequences (`../`), absolute paths, or null bytes. The same pattern exists in `AsiceDeclaration.children()` and `AsiceXmlDeclaration.children()` (via `asicReader.getNextFile()`), and in `ArtifactHolderImpl.loadAsic()`.
- **Impact:** Although this library stores extracted content in memory (not to disk), the `CachedFile.getFilename()` value is passed to `ValidationInstance._addChildValidation()` at line 280 and set as `report.setFilename()` at line 289. A caller using this filename for file I/O would be vulnerable to path traversal. Within the library itself, the filename is used as a map key in `ArtifactHolderImpl`, which could cause key collisions or map pollution. Severity is Medium rather than High because no direct file write occurs within the library.
- **Evidence:** Verified via `ZipSlipTest.testZipSlipPathTraversal()` — a ZIP with `../../etc/passwd` entry name is accepted without sanitization.
- **Remediation:**
  ```java
  final String name = zipEntry.getName ();
  if (name.contains ("..") || name.startsWith ("/") || name.startsWith ("\\"))
    continue; // or throw
  ```
  Apply to all `children()` implementations and `ArtifactHolderImpl.loadAsic()`.
- **References:** [Snyk Zip Slip](https://security.snyk.io/research/zip-slip-vulnerability), CWE-22

---

### F-04 — TransformerFactory without external access restrictions [Severity: Medium]

- **CWE:** CWE-611 (Improper Restriction of XML External Entity Reference)
- **Location:** `validator-core/src/main/java/no/difi/vefa/validator/declaration/ValidatorTestDeclaration.java:42`
- **Description:** `TransformerFactory.newInstance()` is created without setting `ACCESS_EXTERNAL_DTD` or `ACCESS_EXTERNAL_STYLESHEET` to empty strings. This factory is used in `convert()` (line 102) to transform JAXB-deserialized DOM nodes.
- **Impact:** A crafted test document could include external stylesheet or DTD references that trigger SSRF or file disclosure during the `transform()` call. The risk is partially mitigated because the input goes through JAXB unmarshalling first, and Saxon's `TransformerFactoryImpl` (which is selected due to classpath priority) does not support the `ACCESS_EXTERNAL_*` attributes. However, if Saxon is removed from the classpath, the default JDK `TransformerFactory` would be used without restrictions.
- **Evidence:** `TransformerFactoryXxeTest.testDefaultTransformerFactoryAllowsExternalAccess()` confirms Saxon's factory does not support these attributes.
- **Remediation:** Since Saxon's `TransformerFactory` does not support `ACCESS_EXTERNAL_*` attributes, use `setURIResolver(new BlockingURIResolver())` on the factory to prevent external resource resolution:
  ```java
  private static final TransformerFactory TRANSFORMER_FACTORY;
  static {
      TRANSFORMER_FACTORY = TransformerFactory.newInstance ();
      TRANSFORMER_FACTORY.setURIResolver (new BlockingURIResolver ());
  }
  ```
- **References:** CWE-611

---

### F-05 — HolderURIResolver incomplete path traversal sanitization [Severity: Medium]

- **CWE:** CWE-22 (Improper Limitation of a Pathname to a Restricted Directory)
- **Location:** `validator-core/src/main/java/no/difi/vefa/validator/util/HolderURIResolver.java:25-27`
- **Description:** `HolderURIResolver.resolve()` constructs a path from caller-supplied `href` and `base` values. Line 27 attempts to remove `..` sequences via regex: `.replaceAll("/(.+?)/\\.\\.", "")`. This approach is insufficient — it only removes a single level of traversal per match and does not handle repeated traversal (`../../..`), encoded sequences (`%2e%2e`), or backslash variants on Windows. The same pattern appears in `HolderLSResolveResource.java:35`.
- **Impact:** An attacker who controls the XSD/XSLT content (via a compromised artifact source) could reference paths outside the intended artifact holder scope. The impact is limited because `ArtifactHolderImpl.get()` does a map lookup by exact string — a traversal path would simply not match any key and return `null` (causing an NPE downstream). However, if the holder implementation changes, the traversal could succeed.
- **Evidence:** `UriResolverPathTraversalTest.testBlockingResolverAllowsRelativeTraversal()` confirms `BlockingURIResolver` does not block `../` paths (returns `null`, deferring to the caller's resolver chain).
- **Remediation:** Normalize the resolved path and validate it does not escape the root:
  ```java
  Path resolved = rootPath.resolve (href).normalize ();
  if (!resolved.startsWith (rootPath))
      throw new TransformerException ("Path traversal blocked: " + href);
  String newPath = rootPath.relativize (resolved).toString ().replace ('\\', '/');
  ```
- **References:** CWE-22

---

### F-06 — SchemaFactory without XXE protection [Severity: Medium]

- **CWE:** CWE-611 (Improper Restriction of XML External Entity Reference)
- **Location:** `validator-core/src/main/java/no/difi/vefa/validator/checker/XsdCheckerFactory.java:27-29`
- **Description:** `SchemaFactory.newInstance()` is created per-call without disabling external DTD access or external schema access. While a custom `LSResourceResolver` (`HolderLSResolveResource`) is set, this only controls schema imports/includes — it does not prevent the schema itself from containing DTD declarations with external entities.
- **Impact:** A malicious XSD file in the validation artifact source could reference external entities. The risk is mitigated by the fact that XSD files come from the artifact source (not from the user-submitted document), but if the artifact source is compromised, this becomes exploitable.
- **Remediation:**
  ```java
  final SchemaFactory schemaFactory = SchemaFactory.newInstance (XMLConstants.W3C_XML_SCHEMA_NS_URI);
  schemaFactory.setProperty (XMLConstants.ACCESS_EXTERNAL_DTD, "");
  schemaFactory.setProperty (XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
  schemaFactory.setResourceResolver (new HolderLSResolveResource (artifactHolder, path));
  ```
- **References:** CWE-611

---

### F-07 — Unbounded ZIP/ASiC-E archive extraction (Zip bomb) [Severity: Medium]

- **CWE:** CWE-409 (Improper Handling of Highly Compressed Data)
- **Location:**
  - `validator-core/src/main/java/no/difi/vefa/validator/declaration/ZipDeclaration.java:66-85`
  - `validator-core/src/main/java/no/difi/vefa/validator/declaration/AsiceDeclaration.java:75-95`
  - `validator-core/src/main/java/no/difi/vefa/validator/util/ArtifactHolderImpl.java:53-68`
- **Description:** All archive extraction code reads entry contents into byte arrays via `StreamHelper.getAllBytes()` with no size limit. A malicious ZIP archive with a highly compressed entry (zip bomb) could cause `OutOfMemoryError` by decompressing to gigabytes.
- **Impact:** Denial of service. An attacker submitting a zip-bomb document for validation could crash the JVM or exhaust heap memory. The 50KB read limit in `StreamUtils.read50KAndReset()` only applies to the initial document detection phase, not to archive extraction.
- **Remediation:** Implement a size-limited stream wrapper that throws an exception when a configurable maximum (e.g., 100MB) is exceeded:
  ```java
  InputStream limited = new BoundedInputStream (zipInputStream, MAX_ENTRY_SIZE);
  byte[] content = StreamHelper.getAllBytes (new NonClosingInputStream (limited));
  ```
- **References:** CWE-409, [OWASP Zip Bomb](https://owasp.org/www-community/vulnerabilities/Zip_bomb)

---

### F-08 — Mutable internal state exposed via getters [Severity: Info]

- **CWE:** CWE-374 (Passing Mutable Objects to an Untrusted Method)
- **Location:**
  - `validator-core/src/main/java/no/difi/vefa/validator/ValidatorEngine.java:286-289` (`getPackages()` returns mutable `ArrayList`)
  - `validator-api/src/main/java/no/difi/vefa/validator/api/CachedFile.java:29` (`getContent()` returns mutable `byte[]`)
  - `validator-api/src/main/java/no/difi/vefa/validator/api/VefaDocument.java:62` (`getDeclarations()` returns mutable `List`)
  - `validator-core/src/main/java/no/difi/vefa/validator/util/ArtifactHolderImpl.java:48` (`getFilenames()` returns backing `keySet()`)
- **Description:** Several internal-state getters return mutable references. Callers can modify the library's internal data structures.
- **Impact:** Low in practice — `ValidatorEngine` is package-private and the main consumers are internal. `CachedFile.getContent()` is more concerning as it's a public API class, but modifications would only affect the caller's own validation run.
- **Remediation:** Return defensive copies or unmodifiable wrappers:
  ```java
  public List<PackageType> getPackages () { return Collections.unmodifiableList (packages); }
  public byte[] getContent () { return m_aContent.clone (); }
  ```

---

### F-09 — Silent exception swallowing in archive extraction [Severity: Info]

- **CWE:** CWE-390 (Detection of Error Condition Without Action)
- **Location:**
  - `validator-core/src/main/java/no/difi/vefa/validator/declaration/ZipDeclaration.java:81-83` (returns `null` on `IOException`)
  - `validator-core/src/main/java/no/difi/vefa/validator/declaration/AsiceDeclaration.java:91-93` (returns `null` on `IOException`)
  - `validator-core/src/main/java/no/difi/vefa/validator/declaration/AsiceXmlDeclaration.java:107-109` (returns `null` on `IOException`)
- **Description:** The `children()` methods catch `IOException` and return `null`, hiding archive processing errors. The caller (`ValidationInstance._nestedValidation()` at line 275) iterates the result without null-checking, which would cause a `NullPointerException`.
- **Impact:** An attacker could craft a partially corrupt archive where extraction fails silently. The `NullPointerException` would crash the validation but would be caught by the outer try-catch in `ValidationInstance` constructor (line 99), resulting in a silent failure with an incomplete report.
- **Remediation:** Throw `VefaValidatorException` instead of returning `null`, or return `Collections.emptyList()`.

## Dependency Risks

| Dependency | Version | Status | Notes |
|---|---|---|---|
| Saxon-HE | 12.9 | Current | No known CVEs |
| Guice | 7.0.0 | Current | No known CVEs |
| Guava | 33.6.0-jre | Current | No known CVEs |
| GSON | 2.13.2 | Current | No known CVEs |
| Logback | 1.5.32 | Current | No known CVEs |
| Commons CLI | 1.11.0 | Current | No known CVEs |
| Commons IO | 2.21.0 | Current | No known CVEs |
| ph-commons | 12.2.1 | Current | No known CVEs |
| ph-schematron | 9.1.1 | Current | No known CVEs |
| ph-asic | 4.1.0 | Current | Verify ASiC reader performs path validation |
| Jakarta JAXB API | 4.0.5 | Current | No known CVEs |
| JAXB Runtime | 4.0.6 | Current | No known CVEs |
| Mockito | 5.23.0 (test) | Current | No known CVEs |
| JUnit | 4.x (test) | Outdated | No security impact (test-only) |

No critically vulnerable dependencies were identified. All production dependencies use pinned, current versions. No SNAPSHOT dependencies in production code (only the project version itself is SNAPSHOT).

## Recommended Next Steps

### This week (High priority)
1. **Harden XMLInputFactory** (F-01): Add `IS_SUPPORTING_EXTERNAL_ENTITIES=false` and `SUPPORT_DTD=false` to both instances in `AbstractXmlDeclaration` and `XsdChecker`. Two-line fix each.
2. **Harden JAXB unmarshalling** (F-02): Create a `SafeUnmarshaller` utility that wraps a `SAXParserFactory` with XXE disabled. Use it in `ValidatorEngine`, `RepositorySourceInstance`, `DirectorySourceInstance`, and `ValidatorTestDeclaration`.
3. **Sanitize ZIP entry names** (F-03): Add path traversal validation in `ZipDeclaration.children()`, `AsiceDeclaration.children()`, `AsiceXmlDeclaration.children()`, and `ArtifactHolderImpl.loadAsic()`.

### This month (Medium priority)
4. **Harden SchemaFactory** (F-06): Set `ACCESS_EXTERNAL_DTD` and `ACCESS_EXTERNAL_SCHEMA` to empty strings.
5. **Harden TransformerFactory** (F-04): Set `BlockingURIResolver` on the factory instance.
6. **Fix HolderURIResolver path handling** (F-05): Replace regex-based `..` removal with `Path.normalize()` + bounds check.
7. **Add zip bomb protection** (F-07): Implement size limits on archive entry extraction.

### This quarter (Improvements)
8. **Add OWASP Dependency-Check** to the Maven build: `org.owasp:dependency-check-maven` plugin. No such tooling is currently configured.
9. **Add SpotBugs + FindSecBugs** to CI for static analysis of security anti-patterns.
10. **Fix silent exception handling** (F-09): Replace `return null` with proper error propagation.
11. **Add defensive copies** (F-08) on public API return values, especially `CachedFile.getContent()`.

## Appendix: Verification Tests Added

All tests are under `validator-core/src/test/java/no/difi/vefa/validator/security/`:

| Test file | Tests | Proves |
|---|---|---|
| `XxeProtectionTest.java` | 3 tests | F-01: Default `XMLInputFactory` has XXE enabled; hardened factory properly disables it; XXE payload is accepted by default factory |
| `ZipSlipTest.java` | 1 test | F-03: `ZipDeclaration.children()` preserves path-traversal entry names (`../../etc/passwd`) without sanitization |
| `UriResolverPathTraversalTest.java` | 3 tests | F-05: `BlockingURIResolver` blocks `://` URLs but allows relative `../` traversal paths (returns null) |
| `TransformerFactoryXxeTest.java` | 2 tests | F-04: Saxon's `TransformerFactory` does not support `ACCESS_EXTERNAL_*` attributes; documents the need for `URIResolver`-based hardening |

**Total: 9 tests, all passing (0 failures, 0 errors).**
