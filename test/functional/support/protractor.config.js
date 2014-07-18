'use strict';
var baseUrl =  process.env.BASE_URL;
exports.config = {

  allScriptsTimeout: 11000,
  suites: {
    regression: [
    '../components/clientProject/listProjects.spec.js',
    '../components/clientProject/createProject.spec.js',
    '../components/eventsBundles/bundles.spec.js',
    '../components/assets/createApp.spec.js',
    '../components/assets/addTask.spec.js',
    '../components/assets/addComments.spec.js', // replace for comments/listapplication
    '../components/assets/serverList.spec.js',
    '../components/clientProject/deleteProject.spec.js',
    '../components/userMenu/signOut.spec.js',
    '../components/login/login.spec.js'
    ]
  },

  capabilities: {
    'browserName': 'chrome'
  },

  rootElement: 'div[ng-app]',

  onPrepare: function() {
    // At this point, global 'protractor' object will be set up, and jasmine
    // will be available. For example, you can add a Jasmine reporter with:
    //     jasmine.getEnv().addReporter(new jasmine.JUnitXmlReporter(
    //         'outputdir/', true, true));
    browser.driver.get(baseUrl+'/tdstm/auth/login');
    browser.driver.findElement(by.id('usernameId')).sendKeys(process.env.USERNAME);
    browser.driver.findElement(by.name('password')).sendKeys(process.env.PASSWORD);
    browser.driver.findElement(by.css('.buttonR input')).click();
  },

  framework: 'jasmine',
  
  jasmineNodeOpts: {
    isVerbose: true,
    showColors: true,
    includeStackTrace: true,
    defaultTimeoutInterval: 30000
  },

  // // ----- The cleanup step -----
  // //
  // // A callback function called once the tests have finished running and
  // // the webdriver instance has been shut down. It is passed the exit code
  // // (0 if the tests passed or 1 if not).
  // onCleanUp: function() {}
};