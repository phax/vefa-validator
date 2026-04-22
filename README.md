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
