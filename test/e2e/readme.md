# Geb and Gradle Project

[![Build Status][build_status]](https://gitlab.com/gebish/geb-example-gradle/pipelines)

##Required setup

The following needs to be installed:
- Google Chrome
- Firefox
- Latest Java SDK
- Cygwin
- Groovy
- Gradle
- Git
- Intellij Idea


## Description

This is an example of incorporating Geb into a Gradle build. It shows the use of Spock and JUnit 4 tests.

The build is setup to work with Firefox, Chrome and PhantomJS. Have a look at the `build.gradle` and the `src/test/resources/GebConfig.groovy` files.

We use Gradle as our build automation tool. Project dependencies are managed through the build.gradle file in the dependencies section, much alike Maven's xml file.

##Example: Updating a driver version.

To update a driver version in the project all the user needs to do is change the version number in the file (chromeDriverVersion for an instance).

Gradle will then download and install the new version. Take into consideration that this will impact all the project once merged.


## Usage

The following commands will launch the tests with the individual browsers:

    ./gradlew chromeTest
    ./gradlew firefoxTest
    ./gradlew phantomJsTest

To run with all, you can run:

    ./gradlew test
    
Run a single test locally:

    ./gradlew <browserTest> --tests specs.<TestFolder>.<TestName> -Dbrowser.location=local 
    
    --info and -- debug parameters are optional
    
    -Dtm.creds.username=<userName> -Dtm.creds.password=<Password> these parameters allow
    the user to run the tests using non-default credentials.
    
The ./gradlew clean command:

    This command will remove already-compiled files in the project so a complete fresh rebuild can be done.

Replace `./gradlew` with `gradlew.bat` or `gradlew` in the above examples if you're on Windows.

[Gradle User Manual]

##Keeping the Chrome and ChromeDriver versions up-to-date:

Latest chromeDriver Versions and the Chrome version each one supports are to be found [here].

## Questions and issues

Please ask questions on [Geb user mailing list][mailing_list] and raise issues in [Geb issue tracker][issue_tracker].



[build_status]: https://gitlab.com/gebish/geb-example-gradle/badges/master/build.svg "Build Status"
[mailing_list]: https://groups.google.com/forum/#!forum/geb-user
[issue_tracker]: https://github.com/geb/issues/issues
[Gradle User Manual]:https://docs.gradle.org/current/userguide/command_line_interface.html
[here]:https://sites.google.com/a/chromium.org/chromedriver/downloads