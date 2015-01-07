'use strict';
var suites = require('./suites.js');

exports.config = {

  allScriptsTimeout: 80000,
  suites: {
    test1 : suites.test1,
    test2 : suites.test2,
    menu : suites.menu,
    regression : suites.regression, 
    tasks : suites.tasks,
    dashboards : suites.dashboards,
    reports : suites.reports,
    assets : suites.assets,
    admin : suites.admin
    /*// Phantomjd does not support file download yet 
    , importExport:[
    '../components/assets/importExport/exportEmpty.spec.js'
    ]*/
  },

  // seleniumAddress:'http://localhost:4444/wd/hub',

  capabilities: {
    'browserName': 'phantomjs',
    'phantomjs.binary.path':'./node_modules/phantomjs/bin/phantomjs',
  },
  
  rootElement: 'div[ng-app]',

  onPrepare: function() {
    require('jasmine-reporters');
    jasmine.getEnv().addReporter(new jasmine.JUnitXmlReporter(
            'outputdir/', true, true));
    browser.driver.manage().window().setSize(1280, 1024);
    browser.driver.get(process.env.BASE_URL+'/tdstm/auth/login');
    browser.driver.findElement(by.id('usernameId')).sendKeys(process.env.USER_NAME);
    browser.driver.findElement(by.name('password')).sendKeys(process.env.PASSWORD);
    browser.driver.findElement(by.css('.buttonR input')).click();
  },

  framework: 'jasmine',
  
  jasmineNodeOpts: {
    isVerbose: false,
    showColors: false,
    includeStackTrace: true,
    defaultTimeoutInterval: 80000
  }
};