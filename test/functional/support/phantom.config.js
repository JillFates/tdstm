'use strict';
var baseUrl =  process.env.BASE_URL;
exports.config = {

  allScriptsTimeout: 80000,
  suites: {
    test1:[
    '../components/projects/createProject.spec.js',
    '../components/planning/bundles.spec.js',
    '../components/assets/applications/createApp.spec.js',
    '../components/assets/applications/addTask.spec.js',
    '../components/assets/applications/addComments.spec.js'
    ],
    test2:[
    '../components/assets/devices/createServer.spec.js',
    '../components/assets/databases/createDatabase.spec.js',
    '../components/projects/deleteProject.spec.js'
    ],
    menu:[
    '../components/projects/listProjects.spec.js',
    '../components/projects/fieldSettings.spec.js',
    '../components/menu/adminMenu.spec.js',
    '../components/menu/projectsMenu.spec.js',
    '../components/menu/dataCentersMenu.spec.js',
    '../components/menu/assetsMenu.spec.js',
    '../components/menu/planningMenu.spec.js',
    '../components/menu/tasksMenu.spec.js',
    '../components/menu/dashboardsMenu.spec.js',
    '../components/menu/reportsMenu.spec.js'
    ],
    regression:[ //Do not change the order of these files since some test depends on others.
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
    '../components/dataCenters/rooms.spec.js',
    '../components/dataCenters/racks.spec.js',
    '../components/assets/dependencyAnalyzer.spec.js',
    '../components/userMenu/signOut.spec.js',
    '../components/login/login.spec.js'
    ], 
    tasks:[
    '../components/tasks/myTasks.spec.js',
    '../components/tasks/taskManager.spec.js',
    '../components/tasks/taskGraph.spec.js',
    '../components/tasks/timeline.spec.js',
    // '../components/tasks/cookbook.spec.js'
    ],
    dashboards:[
    '../components/dashboards/eventDashboard.spec.js'
    ],
    reports:[
    '../components/reports/applicationProfiles.spec.js',
    '../components/reports/cablingConflict.spec.js',
    '../components/reports/cablingData.spec.js',
    '../components/reports/applicationConflicts.spec.js',
    '../components/reports/serverConflicts.spec.js',
    '../components/reports/databaseConflicts.spec.js',
    '../components/reports/taskReport.spec.js',
    '../components/reports/reportSummary.spec.js',
    '../components/reports/preEventChecklist.spec.js',
    '../components/reports/loginBadges.spec.js',
    '../components/reports/assetTags.spec.js',
    '../components/reports/transportWorksheets.spec.js',
    '../components/reports/applicationMigration.spec.js',
    '../components/reports/issueReport.spec.js',
    '../components/reports/eventResults.spec.js',
    '../components/reports/cablingQA.spec.js'
    ],
    assets:[
    '../components/assets/applications/applicationList.spec.js',
    '../components/assets/databases/dbList.spec.js',
    '../components/assets/devices/serverList.spec.js',
    '../components/assets/devices/allDevicesList.spec.js',
    '../components/assets/storage/storageList.spec.js',
    '../components/assets/devices/storageDevicesList.spec.js',
    '../components/assets/devices/devices.spec.js'
    ] 
    /*// Phantomjd does not support file download yet 
    , importExport:[
    '../components/assets/importExport/exportEmpty.spec.js'
    ]*/
  },

  // seleniumAddress:'http://localhost:4444/wd/hub',

  capabilities: {
    'browserName': "phantomjs",
    'phantomjs.binary.path':'./node_modules/phantomjs/bin/phantomjs',
  },
  
  rootElement: 'div[ng-app]',

  onPrepare: function() {
    require('jasmine-reporters');
    jasmine.getEnv().addReporter(new jasmine.JUnitXmlReporter(
            'outputdir/', true, true));
    browser.driver.manage().window().setSize(1280, 1024);
    browser.driver.get(baseUrl+'/tdstm/auth/login');
    browser.driver.findElement(by.id('usernameId')).sendKeys(process.env.USER_NAME);
    browser.driver.findElement(by.name('password')).sendKeys(process.env.PASSWORD);
    browser.driver.findElement(by.css('.buttonR input')).click();
  },

  framework: 'jasmine',
  
  jasmineNodeOpts: {
    isVerbose: false,
    showColors: false,
    includeStackTrace: true,
    defaultTimeoutInterval: 30000
  }
};