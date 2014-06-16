Run Functional test suite
1. install node
2. on test/functional and run  npm install
2. on test/functional run 
    ln -s node_modules/protractor/bin/webdriver-manager webdriver-manager
    ln -s node_modules/protractor/bin/protractor protractor
4. run 
   webdriver-manager update
5. add the following process.env variables
    export BASE_URL=http://localhost:8080
    export USERNAME= <-- add your userName 
    export PASSWORD= <-- add password
6. run the test  with "protractor support/protractor.config.js"
