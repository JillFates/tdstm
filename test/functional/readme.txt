What to do tu run Functional test suite
1. install node 

2. cd  ./test/functional and run
    $ npm install

3. cd  ./test/functional and run 
    $ ln -s node_modules/protractor/bin/webdriver-manager webdriver-manager
    $ ln -s node_modules/protractor/bin/protractor protractor

4. run 
    $ webdriver-manager update

5. Check svn ignore list run

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
  
  exit and save it.

6. add the following process.env variables
    $ export BASE_URL=http://localhost:8080
    $ export USER_NAME= "username" <-- add your userName 
    $ export PASSWORD= "password" <-- add your password
    $ export BROWSER_NAME = <-- chrome, firefox, phantomjs
7. run the test  with "protractor support/protractor.config.js"
