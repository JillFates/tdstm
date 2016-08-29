'use strict';
var suites = require('./suites.js');
exports.config = {

  allScriptsTimeout: 30000,
  suites: {
    test : suites.test1.concat(suites.test2),
    projects:suites.projects,
    menu : suites.menu,
    admin : suites.admin,
    regression : suites.regression,
    tasks : suites.tasks,
    dashboards : suites.dashboards,
    planning: suites.planning,
    reports : suites.reports,
    assets : suites.assets,
    importExport : suites.importExport,
		cw: suites.cw
  },

  // seleniumAddress: 'http://localhost:4444/wd/hub',
  capabilities: {
    'browserName': process.env.BROWSER_NAME
  },

  rootElement: 'div[ng-app]',

  onPrepare: function() {

    var SpecReporter = require('jasmine-spec-reporter');
    jasmine.getEnv().addReporter(new SpecReporter({
      displayStacktrace: true,
      displayPendingSpec: true
    }));

     /* var ScreenShotReporter = require('protractor-screenshot-reporter');
      var path = require('path');
      jasmine.getEnv().addReporter(new ScreenShotReporter({
         baseDirectory: './tmp/screenshots',
         pathBuilder: function pathBuilder(spec, descriptions, results, capabilities) {
      // Return '<browser>/<specname>' as path for screenshots:
      // Example: 'firefox/list-should work'.
      return path.join(capabilities.caps_.browser, descriptions.join('-'));
        }
      }));
*/
		browser.driver.get(process.env.BASE_URL+'/tdstm/auth/login');
		var usernameInput = browser.driver.findElement(by.id('usernameid'));
		usernameInput.clear();
		usernameInput.sendKeys(process.env.USER_NAME);
		var passwordInput = browser.driver.findElement(by.name('password'));
		passwordInput.clear();
		passwordInput.sendKeys(process.env.PASSWORD);
		browser.driver.findElement(by.id('submitButton')).click();
		/*
    var username= browser.driver.findElement(by.id('usernameId'));
    username.sendKeys(process.env.USER_NAME);

    browser.driver.findElement(by.css('input[type="password"][name="password"]')).sendKeys(process.env.PASSWORD);
    browser.driver.findElement(by.css('.buttonR input')).click();*/
  },

  framework: 'jasmine2',

  jasmineNodeOpts: {
    showColors: true,
    // includeStackTrace: true,
    defaultTimeoutInterval: 80000,
    print: function() {}
    // grep:'*',
    // invertGrep: false
  }
};
