Introduction

This document is used to guide Developers and QA Engineers on how to install, configure and run the Function Test Suite for the 
TransitionManager application. The suite leverages the following 3rd party tools.

* Node.js - Javascrip Runtime environment
* Protractor
* WebDriver

Installation Steps

What to do tu run Functional test suite

1. Download and install Node.js (http://nodejs.org/)

The website has an INSTALL button that when clicked will download an msi (Windows) or pkg (Mac) automatically to provide the 
appropriate installer for your OS.

For Windows the install will default to c:\Program Files\nodejs, for Mac/Linux? it installs into /usr/local/bin

2. Install Version Control Client to access TM Functional Testing Scripts

https://subversion.apache.org/packages.html  (SlikSVN for Windows provides non-registration download). Note that version 8 was incompatible with TDS's older version repository.

3. Create development/testing directory where testing scripts will reside

    mkdir c:\tm\qa
    cd c:\tm\qa

4. Checkout working copy of TransitionManager Functional Testing Scripts

    svn co http://svn.tdsops.com/tdstm/trunk/test/functional

This command will prompt for your SVN credentials and then download the source code into a new directory 'functional'


5. Download the Node packages for testing

    Windows:
        1. Make sure that the MSDOS shell is running in Admin mode

        2. Before running npm you need to create a directory 
            mkdir c:\Users\USERNAME\AppData\Roaming\npm

The Node Package Manager (npm) will examine the package.json file in the test/functional directory to determine what
    packages need to be downloaded. To download and install, enter the following command (in the ./test/functional directory)

    $ npm install

6. Setup symbolic links to several packages

    While in the ./test/functional directory perform the following commands:

    Windows:
    MKLINK .\node_modules\protractor\bin\webdriver-manager webdriver-manager
    MKLINK .\node_modules\protractor\bin\protractor protractor

    Linux/Mac:
    $ ln -s node_modules/protractor/bin/webdriver-manager webdriver-manager
    $ ln -s node_modules/protractor/bin/protractor protractor

7. Webdriver Manager Update

    Windows:

    Linux: 
    $ ./webdriver-manager update

8. Check svn ignore list run

    $ svn proplist -Rv

Should be include on that list:  
    node_module
    protractor
    webdriver-manager

If those are not included, run  
    $ svn propedit svn:ignore .
  
  Add this to the file 
    node_module
    protractor
    webdriver-manager
    propedit
  exit and save it.

9. Add the following process.env variables(under runTests.sh file)
    $ export BASE_URL=http://localhost:8080
    $ export USER_NAME= "username" <-- add your userName 
    $ export PASSWORD= "password" <-- add your password
    $ export BROWSER_NAME = <-- chrome, firefox, phantomjs
    $ export DOWNLOAD_PATH=/path to browser downloads/ <-- browser download path
    
10. Run the test  with "protractor support/protractor.config.js" or run them with ./runTests.sh

=====================================
  Win 7 - Lab Machine - Cygwin
=====================================

1. Install cywing <version downloaded: http://cygwin.com/setup-x86_64.exe> and ran it 
In select packages section search for subversion and choose the package under Devel Default -> subversion: A version Control system.
leave the other settings as default and continue the installation.

2. Install node <version downloaded: http://nodejs.org/dist/v0.10.36/x64/node.exe>
  * If this folder was not created , add it: "c:\Users\USERNAME\AppData\Roaming\npm"

3. Opened cygwin 
    I created a project folder
    $ cd  project
    $ svn co http://svn.tdsops.com/svn/tdstm/trunk/
    $ cd trunk/test/functional
    $ npm install
    $ node_modules/protractor/bin/webdriver-manager update

4. Add the following process.env variables
    $ export BASE_URL=http://localhost:8080
    $ export USER_NAME= "username" <-- add your userName 
    $ export PASSWORD= "password" <-- add your password
    $ export BROWSER_NAME = <-- chrome, firefox, phantomjs
    
5. Run the test  with "node_modules/protractor/bin/protractor support/protractor.config.js"
