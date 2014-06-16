'use strict';
var baseUrl =  process.env.BASE_URL;
exports.config = {

  allScriptsTimeout: 11000,
  specs: [
    '../components/clientProject/listProjects.spec.js',
    '../components/clientProject/createProject.spec.js',
    '../components/clientProject/deleteProject.spec.js',
    '../components/userMenu/signOut.spec.js',
    '../components/login/login.spec.js'
  ],

  // Patterns to exclude.
  // exclude: [],

  // Alternatively, suites may be used. When run without a commad line parameter,
  // all suites will run. If run with --suite=smoke, only the patterns matched
  // by that suite will run.
  // suites: {
  //   test: '../spec/*.js'
        // login:[file 1, file 2],
  //   // smoke: 'spec/smoketests/*.js',
  //   // full: 'spec/*.js'
  // },

  capabilities: {
    'browserName': 'chrome'
  },

  // If you would like to run more than one instance of webdriver on the same
  // tests, use multiCapabilities, which takes an array of capabilities.
  // If this is specified, capabilities will be ignored.
  // multiCapabilities: [],

  // ----- More information for your tests ----
  // baseUrl: 'tmdev.tdsops.com',
  // baseUrl:process.env.BASE_URL,
  rootElement: 'div[ng-app]',

  onPrepare: function() {
    // global.dvr = browser.driver;
    // At this point, global 'protractor' object will be set up, and jasmine
    // will be available. For example, you can add a Jasmine reporter with:
    //     jasmine.getEnv().addReporter(new jasmine.JUnitXmlReporter(
    //         'outputdir/', true, true));
    browser.driver.get(baseUrl+'/tdstm/auth/login');
    browser.driver.findElement(by.id('usernameId')).sendKeys(process.env.USERNAME);
    browser.driver.findElement(by.name('password')).sendKeys(process.env.PASSWORD);
    browser.driver.findElement(by.css('.buttonR input')).click();
  },

  // The params object will be passed directly to the protractor instance,
  // and can be accessed from your test. It is an arbitrary object and can
  // contain anything you may need in your test.
  // This can be changed via the command line as:
  //   --params.login.user 'Joe'
  // params: {
  //   login: {
  //     user: '',
  //     password: ''
  //   }
  // },

  framework: 'jasmine',
  
  jasmineNodeOpts: {
    isVerbose: false,
    showColors: true,
    // If true, include stack traces in failures.
    // includeStackTrace: true,
    defaultTimeoutInterval: 30000
  },

  // // ----- The cleanup step -----
  // //
  // // A callback function called once the tests have finished running and
  // // the webdriver instance has been shut down. It is passed the exit code
  // // (0 if the tests passed or 1 if not).
  // onCleanUp: function() {}
};