module.exports = {
  test1: [
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
  projects:[
    '../components/projects/listProjects.spec.js',
    '../components/projects/createProjectPage.spec.js',
    '../components/projects/projectStaff.spec.js',
    '../components/projects/fieldSettings.spec.js'
    ],
  menu:[ //Do not change the order of these files since some test depends on others.
    '../components/menu/adminMenu.spec.js',
    '../components/menu/projectsMenu.spec.js',
    '../components/menu/dataCentersMenu.spec.js',
    '../components/menu/assetsMenu.spec.js',
    '../components/menu/planningMenu.spec.js',
    '../components/menu/tasksMenu.spec.js',
    '../components/menu/dashboardsMenu.spec.js',
    '../components/menu/reportsMenu.spec.js',
    '../components/userMenu/userMenu.spec.js'
  ],
  admin:[
    '../components/admin/adminPortal.spec.js',
    // '../components/admin/adminPortal/roleType.spec.js',
    // '../components/admin/staff/merge.spec.js'//need database configuration
  ],
  regression:[ //Do not change the order of these files since some test depends on others.
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
    '../components/dataCenters/rackElevations.spec.js',
    // '../components/assets/dependencyAnalyzer.spec.js',
    '../components/userMenu/signOut.spec.js',
    '../components/login/login.spec.js'
    ], 
    tasks:[
    '../components/tasks/myTasks.spec.js',
    '../components/tasks/taskManager.spec.js',
    '../components/tasks/taskGraph.spec.js',
    '../components/tasks/timeline.spec.js'
    // '../components/tasks/cookbook.spec.js'
    ],
    dashboards:[
    '../components/dashboards/eventDashboard.spec.js'
    ],
    planning:[
    '../components/planning/listEventNews.spec.js'
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
    '../components/assets/devices/storageDevicesList.spec.js',
    '../components/assets/storage/storageList.spec.js',
    '../components/assets/devices/devices.spec.js'
    ],
    importExport:[     // Phantomjd does not support file download yet 
    '../components/assets/importExport/impExp-devices-bladeChassis.spec.js',
    // '../components/assets/importExport/exportEmpty.spec.js',    
    // '../components/assets/importExport/impExp-devices.spec.js'
    ]
};