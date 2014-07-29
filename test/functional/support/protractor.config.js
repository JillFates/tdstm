'use strict';
var baseUrl =  process.env.BASE_URL;
exports.config = {

  allScriptsTimeout: 11000,
  suites: {
    regression: [ //Do not change the order of these files since some test depends on others.
    '../components/clientProject/listProjects.spec.js',
    '../components/clientProject/showProjects.spec.js',
    '../components/clientProject/createProject.spec.js',
    '../components/clientProject/fieldSettings.spec.js',
    '../components/eventsBundles/bundles.spec.js',
    '../components/admin/adminPortal.spec.js',
    '../components/admin/rolePermission.spec.js',
    '../components/admin/assetOptions.spec.js',
    '../components/admin/listCompanies.spec.js',
    '../components/admin/listStaff.spec.js',
    '../components/admin/listUsers.spec.js',
    '../components/admin/importAccounts.spec.js',
    '../components/admin/listWorkflow.spec.js',
    '../components/admin/listManufacturers.spec.js',
    '../components/admin/listModel.spec.js',
    '../components/admin/syncLibraries.spec.js',
    '../components/assets/createApp.spec.js',
    '../components/assets/addTask.spec.js',
    '../components/assets/addComments.spec.js',
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