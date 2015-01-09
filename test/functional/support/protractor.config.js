'use strict';
var suites = require('./suites.js');
exports.config = {

  allScriptsTimeout: 15000,
  suites: {
    test : suites.test1.concat(suites.test2),
    menu : suites.menu,
    regression : suites.regression, 
    tasks : suites.tasks,
    dashboards : suites.dashboards,
    reports : suites.reports,
    assets : suites.assets,
    importExport : suites.importExport,
    admin : suites.admin
  },

  // seleniumAddress: 'http://localhost:4444/wd/hub',
  capabilities: {
    // 'browserName': 'internet explorer'
    'browserName':'chrome'
    // 'browserName':'firefox'
  },

  rootElement: 'div[ng-app]',

  onPrepare: function() {
    browser.driver.get(process.env.BASE_URL+'/tdstm/auth/login');
    var username= browser.driver.findElement(by.id('usernameId'));
    username.sendKeys(process.env.USER_NAME);
    
    browser.driver.findElement(by.css('input[type="password"][name="password"]')).sendKeys(process.env.PASSWORD);
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