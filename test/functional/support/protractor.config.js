'use strict';
var baseUrl =  process.env.BASE_URL;
exports.config = {

  allScriptsTimeout: 11000,
  specs: [
    // '../spec/clientProject/listProjects.js',
    // '../spec/tasks/cookbook.js',
    // '../spec/login-out.js',
    '../spec/assets/listApp.js',
    // '../spec/clientProject/deleteProject.js'
  ],

  // Patterns to exclude.
  // exclude: [],

  // Alternatively, suites may be used. When run without a commad line parameter,
  // all suites will run. If run with --suite=smoke, only the patterns matched
  // by that suite will run.
  // suites: {
  //   test: '../spec/*.js'
  //   // smoke: 'spec/smoketests/*.js',
  //   // full: 'spec/*.js'
  // },

  // ----- Capabilities to be passed to the webdriver instance ----
  //
  // For a full list of available capabilities, see
  // https://code.google.com/p/selenium/wiki/DesiredCapabilities
  // and
  // https://code.google.com/p/selenium/source/browse/javascript/webdriver/capabilities.js
  capabilities: {
    'browserName': 'chrome'
  },

  // If you would like to run more than one instance of webdriver on the same
  // tests, use multiCapabilities, which takes an array of capabilities.
  // If this is specified, capabilities will be ignored.
  // multiCapabilities: [],

  // ----- More information for your tests ----
  // baseUrl: 'http://localhost:' + (process.env.HTTP_PORT || '8080'),
  // baseUrl: 'tmdev.tdsops.com',

  // Selector for the element housing the angular app - this defaults to
  // body, but is necessary if ng-app is on a descendant of <body>  
  rootElement: 'div[ng-app]',

  // A callback function called once protractor is ready and available, and
  // before the specs are executed
  // You can specify a file containing code to run by setting onPrepare to
  // the filename string.
  onPrepare: function() {

    // At this point, global 'protractor' object will be set up, and jasmine
    // will be available. For example, you can add a Jasmine reporter with:
    //     jasmine.getEnv().addReporter(new jasmine.JUnitXmlReporter(
    //         'outputdir/', true, true));
    browser.driver.get(baseUrl+'/tdstm/auth/login');
    // browser.driver.get('http://tmdev.tdsops.com/tdstm/auth/login');
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

  // ----- The test framework -----
  //
  // Jasmine and Cucumber are fully supported as a test and assertion framework.
  // Mocha has limited beta support. You will need to include your own
  // assertion framework if working with mocha.
  framework: 'jasmine',
  
  // ----- Options to be passed to minijasminenode -----
  //
  // See the full list at https://github.com/juliemr/minijasminenode
  jasmineNodeOpts: {
    // onComplete will be called just before the driver quits.
    // onComplete: null,
    // If true, display spec names.
    isVerbose: true,
    // If true, print colors to the terminal.
    showColors: true,
    // If true, include stack traces in failures.
    // includeStackTrace: true,
    // Default time to wait in ms before a test fails.
    // defaultTimeoutInterval: 30000
  },

  // ----- Options to be passed to cucumber -----
  // cucumberOpts: {
  //   // Require files before executing the features.
  //   require: 'cucumber/stepDefinitions.js',
  //   // Only execute the features or scenarios with tags matching @dev.
  //   // This may be an array of strings to specify multiple tags to include.
  //   tags: '@dev',
  //   // How to format features (default: progress)
  //   format: 'summary'  
  // },

  // // ----- The cleanup step -----
  // //
  // // A callback function called once the tests have finished running and
  // // the webdriver instance has been shut down. It is passed the exit code
  // // (0 if the tests passed or 1 if not).
  // onCleanUp: function() {}
};