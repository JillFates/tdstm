'use strict';
var baseUrl =  process.env.BASE_URL;
var suites = require('./suites.js');
exports.config = {

  // seleniumAddress: 'http://localhost:4444/wd/hub',
  allScriptsTimeout: 15000,
  suites: {
    test : suites.test,
    menu : suites.menu,
    regression : suites.regression, 
    tasks : suites.tasks,
    dashboards : suites.dashboards,
    reports : suites.reports,
    assets : suites.assets,
    importExport : suites.importExport
  },

  capabilities: {
    // 'browserName': 'internet explorer'
    'browserName':'chrome'
  },

  rootElement: 'div[ng-app]',

  onPrepare: function() {
    browser.driver.get(baseUrl+'/tdstm/auth/login');
    browser.driver.findElement(by.id('usernameId')).sendKeys(process.env.USER_NAME);
    browser.driver.findElement(by.name('password')).sendKeys(process.env.PASSWORD);
    browser.driver.findElement(by.css('.buttonR input')).click();
  },

  framework: 'jasmine',
  
  jasmineNodeOpts: {
    isVerbose: true,
    showColors: true,
    includeStackTrace: true,
    defaultTimeoutInterval: 80000
  }
};