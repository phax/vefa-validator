[![Maven Central](https://img.shields.io/maven-central/v/no.difi.vefa/validator-parent.svg)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22no.difi.vefa%22%20AND%20validator)
[![Docker](https://img.shields.io/docker/pulls/difi/vefa-validator.svg)](https://hub.docker.com/r/difi/vefa-validator/)

# VEFA Validator 2.x

This repository contains the code of the validation library for Java of which may be used to validate document related to eProcurement. The library is intended to be included in your software where you need support for document validation, it is not prossible to perform validation by simply compiling the project.

This library does not contain validation rules for any of the eProcurement documents supported. If you have issues related to specific types of documents, please make sure to create whose issues in the respective repository, e.g. [ehf-postaward-g3](https://github.com/anskaffelser/ehf-postaward-g3) for Post-Award documents or [eforms-sdk-nor](https://github.com/anskaffelser/eforms-sdk-nor) for eForms.

## Fork Philip

What was changed:

Docker image - see https://hub.docker.com/r/phelger/vefa-validator/tags
* `phelger/vefa-validator:latest` - latest release version
* `phelger/vefa-validator:edge` - latest snapshot version

v2.4.4 - work in progress
* Extended the README with a user guide covering the supported document formats, their detection constraints and all default values
* Removed Google Guice as the dependency injection framework — the validator is now wired with plain Java constructor calls in `ValidatorBuilder.build()` and `ValidatorFactory`. The public `ValidatorBuilder` API (`newValidator().setProperties(...).setSource(...).build()`) is unchanged.
* Deleted the `no.difi.vefa.validator.module` package (`ValidatorModule`, `CacheModule`, `SbdhModule`, `SourceModule`, `PropertiesModule`).
* Replaced Guava's `LoadingCache` with `com.helger.cache.impl.ProviderCache` from ph-cache for the checker cache. Soft values, `maximumSize` and time-based eviction (`pools.checker.expire`) are preserved. Note the semantic change: ph-cache uses `expireAfterWrite` (fixed lifetime from put) instead of Guava's `expireAfterAccess` (sliding window reset on read).
* Dropped the `com.google.inject:guice` and `com.google.guava:guava` dependencies.
* Bumped ph-commons to 12.3.0-SNAPSHOT for the new `CacheBuilder` expiration API.

v2.4.3 - 2026-04-23
* Fixed an error in serializing certain XSLT documents to disk
* Some performance and security improvements

v2.4.2 - 2026-04-22
* Renewed expired internal certificate

v2.4.1 - 2025-11-16
* Updated to ph-commons 12.1.0
* Using JSpecify annotations

v2.4.0 - 2025-09-12
* Changed baseline to Java 17+
* The Docker image is now based on `eclipse-temurin:21-jre`

v2.3.1 - 2024-09-04
* Changed baseline to Java 11+
* Updated to JAXB 4.x
* Using JUnit instead of TestNG
* Improved log messages
* Get rid of Lombok
* Read sample of 50K instead of 10K to allow for real-world instances as well
* Default output to "info" level
* Changed Maven group ID to `com.helger.vefa`
* Changed Docker group to `phelger`

v2.1.0
* Support for PEPPOL BIS/EHF Billing 3.0.
* Changed visibility of no.difi.vefa.validator.ValidationImpl (earlier Validation).
* Allow multiple directories when using DirectorySource and SimpleDirectorySource.
* Refactored ValidatorBuilder to use ValidatorPlugin to declare functionality.
* Nested validation for document types supporting embedded content (currently SBDH and ASiC-E).
* Introducing flags FUTURE_ERROR (not currently in use) and UNKNOWN.
* Allow overriding properties used during validation per validation.
* Adding EspdDeclaration.
* Support for ASiC-E.
* Support for triggers.
* Support for namespace in single quotes.
* Support for test scope.
* Support for unit tests.
* Support for infourl.

v2.0.2
* Rewrite of Declaration.
* Rewrite of Expectation.
* no.difi.vefa.validator.api.Document.getDeclaration() returns a string, not an object.
* Fixing methods in ValidatorBuilder returning void.
* Adding support for Piwik in sample application. Not turned on by default.
* Refactoring of validator-build.
* Adding SbdhDeclaration.
* Updating to [Saxon 9.7](http://www.saxonica.com/products/latest.xml#saxon9-7).
* Close Jimfs when closing the validator. [#22](https://github.com/difi/vefa-validator/pull/22)

v2.0.1
* Adding annotation XmlTransient to flagFilterer in no.difi.vefa.validator.api.Section. [#14](https://github.com/difi/vefa-validator/issues/14)
* XsltChecker changed name to SvrlXsltChecker.
* Changed signature of no.difi.vefa.validator.api.Source.createInstance(...).
* Loading necessary modules when initiating JimFS. [#9](https://github.com/difi/vefa-validator/issues/9)

v2.0.0
* Allow tracing successful tests.
* Adding FlagType.SUCCESS in API.
* Added more logging.
* Updating version of no.difi.commons:commons-schematron.
* Functionality to sign validation artifacts defined by parameters.
* Fixing UTF-8 thing in presentation of rendered documents in sample application.
* Allow override of implementations of checker og renderer to use.

v2.0.0-RC2
* DirectorySourceInstance looks for artifacts.xml in directory.
* Testing and fixing of detection of CustomizationID and ProfileID.
* Functionality for configuring validator.
* Supporting parameters for stylesheets.
* Better defaults in the sample application (validator-web).
* ValidatorException is moved from no.difi.vefa.validator to no.difi.vefa.validator.api.
* Cleaner use of exceptions in exposed classes.
* Renaming 'Presenter' and associated to 'Renderer' for better communication. 
* Moving no.difi.vefa.validator.api to module validator-api.
* More Javadoc in code.

v2.0.0-RC1
* Initial publishing of validator.

## Features

* **Very easy to use.**
* Supports **rendering documents**.
* Very **low footprint** in your code.
* **Pooling** of resources.
* Supports **different lifecycles** of validation artifacts.
* **[Configurable](https://github.com/anskaffelser/vefa-validator/blob/master/doc/configurations.md)** to fit multiple sizes.


## Getting started

Include dependency in your `pom.xml`, where `x.y.z` denotes the latest release version:

```xml
<dependency>
  <groupId>com.helger.vefa</groupId>
  <artifactId>validator-core</artifactId>
  <version>x.y.z</version>
</dependency>
```

Start validating business documents:

```java
// Create a new validator using validation artifacts from DFØ.
Validator validator = ValidatorBuilder.newValidator().build();

// Validate business document.
Validation validation = validator.validate(Paths.get("/path/to/document.xml"));

// Print result of validation.
System.out.println(validation.getReport().getFlag());
```

The validator is expensive to create, one instance should be enough.


### New repositories

Repositories referenced in the code was moved as of September 1st 2020. To switch to the new repository, adding source in the ValidatorBuilder is required. Example of how it may look like:

```java
Validator validator = ValidatorBuilder.newValidator()
    .setSource(RepositorySource.of("https://anskaffelser.dev/repo/validator/current/"))
    .build();
```

More information on the change and link to the new test repository may be found in the [announcment of the new repositories](https://anskaffelser.dev/service/announcement/2020-08-31-changed-urls-for-validator/).


## User guide

This guide describes the behaviour of the validator engine itself: which input formats are recognised, how a document is matched to a set of validation rules, which constraints apply, and which defaults are used.
The validation rules ("validation artifacts") are **not** part of this repository — they are loaded from an external source at runtime.

### Validation flow

1. `ValidatorBuilder.newValidator().build()` reads **all** validation artifacts from the configured source into memory. This is expensive — create one `Validator` and share it, it is thread safe. Call `close()` when done.
2. `validator.validate(...)` reads the **complete** document into a byte array in memory.
3. The first 50 KB are offered to the *declarations*, which recognise the document in a two-step handshake: `verify(...)` (cheap check on the sample) and `detect(...)` (extract the identifying values).
4. Detection produces a list of *declaration identifiers*, most specific first, e.g. `xml.ubl::urn:fdc:peppol.eu:2017:poacc:billing:01:1.0#urn:cen.eu:en16931:2017`.
5. The identifiers are looked up in the configurations of the loaded artifacts. The **first** identifier with a matching configuration wins; the configuration is then normalized (all `inherit` references are resolved).
6. Every `file` of the configuration is executed by a *checker* (XSD or Schematron), every `trigger` by a *trigger*. The result is a `Report` containing `Section`s of `Assertion`s.

If no declaration matches, or if no configuration exists for any of the identifiers, the report has the title `Unknown document type`, flag `UNKNOWN` and a single `SYSTEM-003` assertion.

### Supported input formats

Declaration types form a hierarchy: `xml.*` types are only tried after the generic `xml` type matched, `zip.asice` only after `zip` matched.
The rightmost column shows the identifier as used for matching a configuration (`{type}::{value}`).

| Format | Type | Recognised by | Declaration identifier(s) |
|---|---|---|---|
| Generic XML | `xml` | Root element in an XML namespace | `xml::{namespace}::{localName}` |
| OASIS UBL 2.x | `xml.ubl` | Root namespace matches `urn:oasis:names:specification:ubl:schema:xsd:(.+)-2` | Up to 6 identifiers, see below |
| UN/CEFACT CII | `xml.uncefact` | Root namespace matches `urn:un:unece:uncefact:data:standard:(.+)` | `xml.uncefact::{rootLocalName}::{businessProcessID}::{guidelineID}` |
| ESPD | `xml.espd` | Root element is `ESPDRequest` in `urn:grow:names:specification:ubl:schema:xsd:ESPDRequest-1` or `ESPDResponse` in `…:ESPDResponse-1` | `xml.espd::{namespace}::{rootLocalName}::{CustomizationID / VersionID}` |
| NOBL | `xml.nobl` | Root namespace matches `urn:fdc:difi.no:2018:nobl:(.+)-1` | `xml.nobl::{rootLocalName}::{CustomizationID}`, `…::{ProfileID}`, `…::{CustomizationID}::{ProfileID}` |
| SBDH | `xml.sbdh` | Root namespace starts with `http://www.unece.org/cefact/namespaces/StandardBusinessDocumentHeader` | `xml.sbdh::{namespace}::{rootLocalName}` and `xml.sbdh::SBDH:1.0` |
| ASiC-E | `zip.asice` | ZIP whose first entry is `mimetype` containing `application/vnd.etsi.asic-e+zip` | `zip.asice::application/vnd.etsi.asic-e+zip` |
| ASiC-E embedded in XML | `xml.asice` | Root element `asic` in `urn:etsi.org:specification:02918:v1.2.1` | `xml.asice::application/vnd.etsi.asic-e+zip` |
| Generic ZIP | `zip` | File starts with `PK\x03\x04` | `zip::{mimetype}` or `zip::application/zip` |
| Validator test | `xml.test` | Root element `test` in `http://difi.no/xsd/vefa/validator/1.0` | `configuration::{value of @configuration}` |
| Validator test set | `xml.testset` | Root element `testSet` in `http://difi.no/xsd/vefa/validator/1.0` | `xml.testset::http://difi.no/xsd/vefa/validator/1.0::testSet` |

Formats not listed here (EDIFACT, JSON, PDF, plain text, …) are not recognised and result in `Unknown document type`.

#### Generic XML

* The root namespace and root local name are extracted with **regular expressions** from the first 50 KB decoded as UTF-8 — not with an XML parser. Comments are removed first; XML declarations, processing instructions and `DOCTYPE` are skipped.
* The prefix of the root element must be bound **on the root element itself**; namespace declarations inherited from elsewhere are not considered.
* A document whose root element is **not** in a namespace is not recognised at all (e.g. `<test></test>` results in `unknown`).
* A configuration may reference this via `<standardId>{namespace}::{localName}</standardId>` or an explicit `<declaration type="xml">…</declaration>`.

#### UBL

* Detection transforms the **whole** document (not just the sample) with an XSLT, so very large documents cost more here than other formats.
* `CustomizationID` and `ProfileID` must be **direct children of the root element** and in the namespace `urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2`. Values are `normalize-space()`d; attributes such as `schemeID` are ignored.
* The identifiers are produced in this order (each prefixed with `xml.ubl::`), and the first one with a configuration wins:
  1. `{ProfileID}#{CustomizationID}`
  2. `{CustomizationID}`
  3. `{rootLocalName}::{ProfileID}#{CustomizationID}`
  4. `{rootLocalName}::{CustomizationID}`
  5. `{namespace}::{rootLocalName}`
  6. `{rootLocalName}`
* Entries 1, 3 require both IDs; 2, 4 require `CustomizationID`. A UBL document without `CustomizationID` is therefore only matched by namespace/local name.
* A configuration containing `<profileId>` and `<customizationId>` but no `<declaration>` is registered automatically as `xml.ubl::{profileId}#{customizationId}`.

#### UN/CEFACT CII

* Only the first 50 KB are scanned.
* The values are taken from the `ID` element **immediately following** `BusinessProcessSpecifiedDocumentContextParameter` and `GuidelineSpecifiedDocumentContextParameter` (matched by local name, in document order).
* Scanning stops at the end of `ExchangedDocumentContext`. If that element is not found within the sample, detection falls back to the generic identifier `xml::{namespace}::{rootLocalName}`.

#### ESPD

* Namespace **and** root local name must match exactly — `ESPDRequest-1` / `ESPDRequest` or `ESPDResponse-1` / `ESPDResponse`.
* The first 50 KB are scanned for `CustomizationID` and `VersionID` (by local name, anywhere in the document). Empty elements are ignored.
* If neither is found, the identifier is the plain `xml.espd::{namespace}::{rootLocalName}`.

#### SBDH

* Detection only looks at the root namespace — the header content itself is not inspected.
* A configuration with `<standardId>SBDH:…</standardId>` is registered automatically as `xml.sbdh::{standardId}`.
* With nesting enabled, the payload (everything except the `StandardBusinessDocumentHeader` element) is extracted by XSLT and validated as a child document. An empty payload (extraction result of 38 bytes or less) yields no children.

#### ASiC-E

* The `mimetype` entry must be the **first** entry of the ZIP, and the local file header of that entry must not use an extra field (byte 29 of the file must be `0`) — this is the same constraint the ASiC-E specification imposes.
* Built-in configuration `asice-archive` runs the `asice` trigger, which verifies the container signature: one `ASICE-001` assertion (`INFO`) per signing certificate, or `ASICE-002` (`FATAL`) when verification fails.
* With nesting enabled, all files inside the container are validated individually.
* `xml.asice` is the Base64-encoded variant: the **complete text content** of the document is Base64-decoded into an ASiC-E container. Built-in configuration `asice-archive-xml` — no signature trigger.

#### Generic ZIP

* If the first entry is named `mimetype`, its content becomes the identifier (which is how ODF/OOXML-style containers are identified), otherwise `application/zip` is used.
* There is **no built-in configuration** for generic ZIP files. Without a matching configuration from an artifact, validation ends as `Unknown document type` — and consequently no nested validation of the entries takes place.

#### Validator test files

* `xml.test` selects the configuration **directly** through its `configuration` attribute (identifier `configuration::{value}`); the wrapped business document is unwrapped before validation. Without the attribute, detection falls back to the generic `xml::…` identifier.
* `xml.testset` (built-in configuration `vefa-testset`) produces one child validation per `test` element and requires `feature.nesting`. `configuration`, `id` and `assert` values of the set are inherited by the individual tests.

### Nested validation

Nested (recursive) validation is **off by default** and is enabled with `feature.nesting`. It applies to:

| Type | Children |
|---|---|
| `xml.sbdh` | The SBDH payload |
| `zip.asice`, `xml.asice` | All files in the container |
| `zip` | All ZIP entries |
| `xml.testset` | One document per `test` element |

Children appear as nested `Report` elements (`report/report`) and via `IValidation.getChildren()`; `getChildren()` returns `null` when there are none.
Nesting only happens when the outer document itself has a matching configuration and has not already failed fatally.

### Matching a configuration

* The declaration identifiers are tried in order; the first one with a configuration wins.
* Configurations declare their identifiers explicitly with `<declaration type="…">value</declaration>`, matched as `{type}::{value}`.
* Shorthands used when no `<declaration>` is given: `profileId` + `customizationId` → `xml.ubl::{profileId}#{customizationId}`; `standardId` → `xml.sbdh::{standardId}` if it starts with `SBDH:`, otherwise `xml::{standardId}`.
* Every configuration is additionally reachable as `configuration::{identifier}` — this is what the validator test files use.
* If several artifacts declare the same identifier, the configuration with the **higher `weight`** wins. The default weight is `0`; the built-in configurations use `Long.MIN_VALUE` so artifact-provided configurations always take precedence.
* `inherit` merges `rule`, `file` and `trigger` entries of the referenced configurations **before** the own entries, so inherited checks run first. Unresolvable references produce a `SYSTEM-007` warning per reference (suppressible with `feature.suppress_notloaded`).

### Checkers

The checker is chosen by the **file extension** of the artifact path (case insensitive suffix match):

| Extension | Implementation | Notes |
|---|---|---|
| `.xsd` | XSD validation | Imports/includes are resolved from inside the same artifact |
| `.sch` | Schematron | Compiled to XSLT at load time — considerably slower to prepare |
| `.xsl`, `.xslt`, `.svrl.xsl`, `.svrl.xslt`, `.sch.xslt` | Schematron as pre-compiled XSLT | Produces SVRL, which is converted into assertions |

Constraints:

* **XSD validation reports only the first error** and always with flag `FATAL`, because validation stops at the first `SAXParseException`.
* As soon as a section results in `FATAL`, the remaining `file` entries of the configuration are skipped. Triggers are still executed.
* Compiled checkers are cached across validations, see `pools.checker.*` below.
* Failing to prepare or run a checker yields `SYSTEM-008` (`ERROR`), a failing trigger `SYSTEM-010` (`ERROR`).

### Result model and flags

`Report` → `Section` → `Assertion`. A section's flag is the highest flag of its assertions, and the report's flag is the highest flag of its sections. Messages produced by the validator itself are collected in a section titled `Validator`, inserted as the first section.

Flags, ordered from lowest to highest:

`SUCCESS` < `INFO` < `OK` < `EXPECTED` < `WARNING` < `FUTURE_ERROR` < `ERROR` < `FATAL` < `UNKNOWN`

A configuration can change the flag of individual rules with `<rule identifier="…" action="…"/>`, where action is one of `suppress`, `setWarning`, `setError`, `setFutureError`, `setFatal` or `none`. Suppressed assertions are dropped from the report entirely.

Assertions produced by the engine itself:

| Identifier | Flag | Meaning |
|---|---|---|
| `SYSTEM-001` | `FATAL` | Unexpected error while loading or configuring the document |
| `SYSTEM-003` | `UNKNOWN` | Document type could not be detected, or no configuration found |
| `SYSTEM-004` / `-005` / `-006` | `ERROR` | Expected `FATAL` / `ERROR` / `WARNING` rule did not fire (expectations only) |
| `SYSTEM-007` | `WARNING` | Referenced validation artifact was not loaded |
| `SYSTEM-008` | `ERROR` | Checker failed |
| `SYSTEM-009` | `ERROR` | Rule expected to succeed did fire (expectations only) |
| `SYSTEM-010` | `ERROR` | Trigger failed |
| `XSD` | `FATAL` | XSD validation error |
| `ASICE-001` | `INFO` | Signing certificate of an ASiC-E container |
| `ASICE-002` | `FATAL` | ASiC-E container verification failed |

### Expectations

With `feature.expectation` enabled, expected results are read from the document itself and used to filter flags — an expected `ERROR` becomes `EXPECTED`, an unfired expectation becomes an `ERROR`. This is meant for testing validation artifacts, not for production validation.

* For `xml.test` documents the expectations come from the `assert` element.
* For UBL, UN/CEFACT, ESPD, NOBL and SBDH documents they are read from the **first XML comment** in the first 50 KB. Blocks are separated by blank lines, keys are `description`/`content`, `success(es)`, `warning(s)`, `error(s)`, `fatal(s)` and `scope`; each rule is listed on its own line with an optional count:

```xml
<!--
Description: Invoice with two known errors

Errors:
EUGEN-T10-R008
BII2-T10-R039 2 times
-->
```

* Generic `xml`, `zip`, `zip.asice`, `xml.asice` and `xml.testset` documents do not support expectations.
* `scope` restricts reporting to the listed rule identifiers — everything else is dropped from the report.

### Configuration properties and defaults

Properties are resolved in this order, first hit wins: properties of the individual validation → properties given to `ValidatorBuilder.setProperties(...)` → `ValidatorDefaults`.

| Property | Type | Default | Effect |
|---|---|---|---|
| `feature.expectation` | boolean | `false` | Read expected results from the document (see above) |
| `feature.nesting` | boolean | `false` | Validate embedded documents (SBDH payload, ASiC-E/ZIP entries, test sets) |
| `feature.suppress_notloaded` | boolean | `false` | Do not report `SYSTEM-007` for artifacts that could not be loaded |
| `feature.infourl` | boolean | `false` | Copy `file/@infoUrl` to the assertions, replacing `{}` with the rule identifier |
| `pools.checker.size` | int | `250` | Maximum number of compiled checkers kept in the cache |
| `pools.checker.expire` | int (minutes) | `1440` (1 day) | Lifetime of a cached checker, counted **from insertion** (`expireAfterWrite`) |

Unknown keys return `false` / `0` / `null` depending on the accessor used.

```java
// Validator-wide properties
Validator validator = ValidatorBuilder.newValidator ()
    .setProperties (new SimpleProperties ().set ("feature.nesting", true)
                                           .set ("pools.checker.size", 500))
    .setSource (RepositorySource.forProduction ())
    .build ();

// Properties for a single validation
IValidation validation = validator.validate (resource,
                                             new SimpleProperties ().set ("feature.expectation", true));
```

Other defaults:

| Setting | Default |
|---|---|
| Validation artifacts source | `RepositorySource.forProduction()` = `https://anskaffelser.dev/repo/validator/current/` |
| Test/draft repository | `RepositorySource.forTest()` = `https://anskaffelser.dev/repo/validator/draft/` |
| Detection sample size | 50 KB (UBL reads the whole document) |
| Configuration `weight` | `0` (built-in configurations: `Long.MIN_VALUE`) |
| `file/@type` when omitted | `xml.xsd` for `.xsd` paths, otherwise `xml.schematron.xslt` |
| `stylesheet/@type` when omitted | `xml.xslt` |
| Report title when nothing matched | `Unknown document type` |

### Validation artifact sources

| Source | Behaviour |
|---|---|
| `RepositorySource` | Reads `artifacts.xml` from each given URI and downloads every referenced `.asice` artifact. Several URIs may be combined. |
| `DirectorySource` | Uses `artifacts.xml` in the directory if present (lower memory footprint), otherwise loads **all** `*.asice` files found in the directory. Several directories may be combined. |

All artifacts are read into memory when the `Validator` is built; updates in the repository or directory only take effect after building a new `Validator`.

### General constraints

* **Java 17 or later.** CI builds and tests on Java 17, 21 and 25.
* `Validator` is thread safe and intended to be created once. `ValidationInstance` objects (the results) are per validation.
* Documents are held **completely in memory** during validation; converted documents (`xml.asice`, `xml.test`) exist twice in memory. Nested validation multiplies this per embedded document.
* Detection is heuristic and works on the first 50 KB only — a document whose identifying elements appear later is only recognised at the generic `xml` level. The exception is UBL, which processes the whole document.
* Validation artifacts are external. Without a matching artifact even a perfectly well-formed document ends in `Unknown document type`.
* Security relevant defaults: the shared StAX factory used for detection has DTD support and external entity resolution disabled, the shared Saxon processor disallows external function calls, and XSD imports/includes are resolved from within the artifact rather than from the network.
