[![Maven Central](https://img.shields.io/maven-central/v/no.difi.vefa/validator-parent.svg)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22no.difi.vefa%22%20AND%20validator)
[![Docker](https://img.shields.io/docker/pulls/difi/vefa-validator.svg)](https://hub.docker.com/r/difi/vefa-validator/)

# VEFA Validator 2.x

This repository contains the code of the validation library for Java of which may be used to validate document related to eProcurement. The library is intended to be included in your software where you need support for document validation, it is not prossible to perform validation by simply compiling the project.

This library does not contain validation rules for any of the eProcurement documents supported. If you have issues related to specific types of documents, please make sure to create whose issues in the respective repository, e.g. [ehf-postaward-g3](https://github.com/anskaffelser/ehf-postaward-g3) for Post-Award documents or [eforms-sdk-nor](https://github.com/anskaffelser/eforms-sdk-nor) for eForms.

## Fork Philip

What was changed:

v2.3.1
* Changed baseline to Java 11+
* Updated to JAXB 4.x
* Using JUnit instead of TestNG
* Improved log messages
* Get rid of Lombok
* Read sample of 50K instead of 10K to allow for real-world instances as well
* Default output to "info" level


## Features

* **Very easy to use.**
* Supports **rendering documents**.
* Very **low footprint** in your code.
* **Pooling** of resources.
* Supports **different lifecycles** of validation artifacts.
* **[Configurable](https://github.com/anskaffelser/vefa-validator/blob/master/doc/configurations.md)** to fit multiple sizes.


## Getting started

Include dependency in your pom.xml:

```xml
<dependency>
  <groupId>no.difi.vefa</groupId>
  <artifactId>validator-core</artifactId>
  <version>2.1.0</version>
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
