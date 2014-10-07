'use strict';
var Menu = function(){
//Main Menu
  this.titleCss =  '.title';
  this.getCurrentUrl = function(){
    return browser.driver.getCurrentUrl();
  };
  
  this.getHeaderTitle = function(){
    return browser.driver.findElement(by.css(this.titleCss));
  };
  
  //Admin
  this.getAdmin = function(){
    return browser.driver.findElement
      (by.css('#adminMenuId a.home')); 
  };
  this.getAdminSubmenu = function(){
    return browser.driver.findElements
      (by.css('#adminMenuId a.mmlink'));
  };  
  this.getAdminPortal = function(){
    return browser.driver.findElement
      (by.css('a[href="/tdstm/auth/home"]'));
  };
  this.getRolePermissions = function(){
    return browser.driver.findElement
      (by.css('a[href="/tdstm/permissions/show"]'));
  };
  this.getAssetOptions = function(){
    return browser.driver.findElement
      (by.css('a[href="/tdstm/assetEntity/assetOptions"]'));
  };
  this.getReleaseNotes = function(){
    return browser.driver.findElement
      (by.css('a[href="javascript:window.open(\'https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TMReleaseNotes?cover=print\',\'help\');"]'));
  };
  this.getListCompanies = function(){
    return browser.driver.findElement
      (by.css('a[href^="/tdstm/partyGroup/list"]'));
  };
  this.getListStaff = function(){
    return browser.driver.findElement
      (by.css('a[href^="/tdstm/person/index"]'));
  };
  this.getListUsers = function(){
    return browser.driver.findElement
      (by.css('a[href^="/tdstm/userLogin/index"]'));
  };
  this.getImportAccounts = function(){
    return browser.driver.findElement
      (by.css('a[href="/tdstm/admin/importAccounts"]'));
  };
  this.getListWorkflows = function(){
    return browser.driver.findElement
      (by.css('a[href="/tdstm/workflow/home"]'));
  };
  this.getListManufacturers = function(){
    return browser.driver.findElement
      (by.css('a[href^="/tdstm/manufacturer/index"]'));
  };
  this.getListModels = function(){
    return browser.driver.findElement
      (by.css('a[href^="/tdstm/model/index"]'));
  };
  this.getSyncLibraries = function(){
    return browser.driver.findElement
      (by.css('a[href="/tdstm/model/importExport"]'));
  };
  this.getAdministrationHelp = function(){
    return browser.driver.findElement
      (by.css('a[href="javascript:window.open(\'https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TMAdminPortal?cover=print\',\'help\');"]'));
  };
  this.getManageClientsHelp = function(){
    return browser.driver.findElement
      (by.css('a[href="javascript:window.open(\'https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TMCreatePerson?cover=print\',\'help\');"]'));
  };
  this.getManageWorkflowsHelp = function(){
    return browser.driver.findElement
      (by.css('a[href="javascript:window.open(\'https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TMManageWorkflows?cover=print\',\'help\');"]'));
  };
  this.getMngModelHelp = function(){
    return browser.driver.findElement
    (by.css('a[href="javascript:window.open(\'https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TMModelLibrary?cover=print\',\'help\');"]'));
  };

  //timezonee
  this.timezoneCss = '.tzmenu';

//Admin
  this.goToAdmin = function(option){
    var that = this;
    this.getAdmin().click(); 
    var d ={
      'adminPortal':function(){
        that.getAdminPortal().click();
      },
      'rolePermissions':function(){
        that.getRolePermissions().click();
      },
      'assetOptions':function(){
        that.getAssetOptions().click();
      },
      'releaseNotes':function(){
         that.getReleaseNotes().click();
      },
      'listCompanies':function(){
        that.getListCompanies().click();
      },
      'listStaff':function(){
        that.getListStaff().click();
      },
      'listUsers':function(){
        that.getListUsers().click();
      },
      'importAccounts':function(){
        that.getImportAccounts().click();
      },
      'listWorkflow':function(){
        that.getListWorkflows().click();
      },
      'listManufacturer':function(){
        that.getListManufacturers().click();
      },
      'listModel': function(){
        that.getListModels().click();
      },
      'syncLibraries':function(){
        that.getSyncLibraries().click();
      }
    };
    d[option]();
  };
// Projects

  this.getProjects = function(){
    return browser.driver.findElement(by.css('#projectMenuId a.home'));
  };
  this.getProjectsSubmenu = function(){
    return browser.driver.findElements(by.css('#projectMenuId a.mmlink'));
  };
  //Projects
  this.getListProjects = function(){
    return browser.driver.findElement(by.css('a[href="/tdstm/project/list?active=active"]'));
  };
  this.getProjectDetails = function(){
    return browser.driver.findElement(by.css('a[href="/tdstm/projectUtil/index"]'));
  };
  this.getProjectStaff = function(){
    return browser.driver.findElement(by.css('a[href="/tdstm/person/manageProjectStaff"]'));
  };
  this.getFieldSettings = function(){
    return browser.driver.findElement(by.css('a[href="/tdstm/project/fieldImportance"]'));
  };
  this.getProjectsHelp = function(){
    return browser.driver.findElement(by.css('a[href="javascript:window.open(\'https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TMProjectStaff?cover=print\',\'help\');"]'));
  };
  this.goToProjects = function(option){
    var that = this;
    this.getProjects().click(); 
    var d = {
      'listProjects':function(){
        that.getListProjects().click();
      },
      'projDetails':function(){
        that.getProjectDetails().click();
      },
      'projectStaff':function(){
        that.getProjectStaff().click();
      },
      'fieldSettings':function(){
        that.getFieldSettings().click();
      },
      'help':function(){
        that.getProjectsHelp().click();
      }
    };
    d[option]();
  };
  this.getDataCenters = function(){
    return browser.driver.findElement(by.css('#roomMenuId a.home'));
  };
  this.getDataCentersSubmenu = function(){
    return browser.driver.findElements(by.css('#roomMenuId a.mmlink'));
  };
  this.getListRooms = function(){
    return browser.driver.findElement(by.css('a[href="/tdstm/room/index?viewType=list"]'));
  };
  this.getRacks = function(){
    return browser.driver.findElement(by.css('a[href="/tdstm/rackLayouts/create"]'));
  };
  this.getDataCentersHelp = function(){
    return browser.driver.findElement(by.css('a[href="javascript:window.open(\'https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TMRooms?cover=print\',\'help\');"]'));
  };
  this.goToDataCentars= function(option){
    this.getDataCenters().click();
    var d = {
    };
    d[option]();
  };
//Assets
  this.getAssets = function(){
    return browser.driver.findElement(by.css('#assetMenuId a.home'));
  };
  this.getAssetsSubmenu = function(){
    return browser.driver.findElements(by.css('#assetMenuId a.mmlink'));
  };
  this.getSummaryTable = function(){
    return browser.driver.findElement(by.css('a[href="/tdstm/assetEntity/assetSummary"]'));
  };
  this.getApplications = function(){
    return browser.driver.findElement(by.css('a[href="/tdstm/application/list"]'));
  };
  this.getServers = function(){
    return browser.driver.findElement(by.css('a[href="/tdstm/assetEntity/list?filter=server"]'));
  };
  this.getAllDevices = function(){
    return browser.driver.findElement(by.css('a[href="/tdstm/assetEntity/list?filter=all"]'));
  };
  this.getDatabases = function(){
    return browser.driver.findElement(by.css('a[href="/tdstm/database/list"]'));
  };
  this.getStorageDevices = function(){
    return browser.driver.findElement(by.css('a[href="/tdstm/assetEntity/list?filter=storage"]'));
  };
  this.getLogicalStorage = function(){
    return browser.driver.findElement(by.css('a[href="/tdstm/files/list"]'));
  };
  this.getComments = function(){
    return browser.driver.findElement(by.css('a[href="/tdstm/assetEntity/listComment"]'));
  };
  this.getDependencies = function(){
    return browser.driver.findElement(by.css('a[href="/tdstm/assetEntity/listDependencies"]'));
  };
  this.getDependencyAnalizer = function(){
    return browser.driver.findElement(by.css('a[href="/tdstm/moveBundle/dependencyConsole"]'));
  };
  this.getAssetsHelp = function(){
    return browser.driver.findElement(by.css('a[href="javascript:window.open(\'https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TMAssetOverview?cover=print\',\'help\');"]'));
  };
  this.getImportAssets = function(){
    return browser.driver.findElement(by.css('a[href="/tdstm/assetEntity/assetImport"]'));
  };
  this.getManageBatches = function(){
    return browser.driver.findElement(by.css('a[href="/tdstm/dataTransferBatch/index"]'));
  };
  this.getExportAssets = function(){
    return browser.driver.findElement(by.css('a[href="/tdstm/assetEntity/exportAssets"]'));
  };
  this.getManageDataHelp = function(){
    return browser.driver.findElement(by.css('a[href="javascript:window.open(\'https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TMImportExport?cover=print\',\'help\');"]'));
  };

  this.goToAssets = function(option){
    var that = this;
    this.getAssets().click();
    var d = {
      'summary':function(){
        that.getSummaryTable().click();
      },
      'applications':function(){
        that.getApplications().click();
      },
      'servers':function(){
        that.getServers().click();
      },
      'allDevices':function(){
        that.getAllDevices().click();
      },
      'databases':function(){
        that.getDatabases().click();
      },
      'storageDevices':function(){
        that.getStorageDevices().click();
      },
      'logicalStorage':function(){
        that.getLogicalStorage().click();
      },
      'comments':function(){
        that.getComments().click();
      },
      'dependencies':function(){
        that.getDependencies().click();
      },
      'dependencyAnalizer':function(){
        that.getDependencyAnalizer().click();
      },
      'assetsHelp':function(){
        that.getAssetsHelp().click();
      },
      'importAssets':function(){
        that.getImportAssets().click();
      },
      'manageBatches':function(){
        that.getManageBatches().click();
      },
      'exportAssets':function(){
        that.getExportAssets().click();
      },
      'manageDataHelp':function(){
        that.getManageDataHelp().click();
      },
    };
    d[option]();
  };
//Planning
  this.getPlanning = function(){
    return browser.driver.findElement(by.css('#eventMenuId a.home'));
  };
  this.getPlanningSubmenu = function(){
    return browser.driver.findElements(by.css('#eventMenuId a.mmlink'));
  };
  this.getListEvents = function(){
    return browser.driver.findElement(by.css('a[href="/tdstm/moveEvent/list"]'));
  };
  this.getListEventNews = function(){
    return browser.driver.findElement(by.css('a[href^="/tdstm/newsEditor/index"]'));
  };
  this.getPreEventChecklist = function(){
    return browser.driver.findElement(by.css('a[href="/tdstm/reports/preMoveCheckList"]'));
  };
  this.getExportRunbook = function(){
    return browser.driver.findElement(by.css('a[href="/tdstm/moveEvent/exportRunbook"]'));
  };
  this.getEventHelp = function(){
    return browser.driver.findElement(by.css('a[href="javascript:window.open(\'https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TMEvents?cover=print\',\'help\');"]'));
  };
  this.getListBundles = function(){
    return browser.driver.findElement(by.css('a[href="/tdstm/moveBundle/list"]'));
  };
  this.getAssignAssets = function(){
    return browser.driver.findElement(by.css('a[href="/tdstm/moveBundleAsset/bundleTeamAssignment?bundleId=&rack=UnrackPlan"]'));
  };
  this.getBundlesHelp = function(){
    return browser.driver.findElement(by.css('a[href="javascript:window.open(\'https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TMBundles?cover=print\',\'help\');"]'));
  };
  
  this.goToPlanning= function(option){
    var that = this;
    this.getPlanning().click();
    var d = {
      'listEvents':function(){

      },
      'listBundles':function(){
        that.getListBundles().click();
      }
    };
    d[option]();
  };
//Tasks
  this.getTasks = function(){
    return browser.driver.findElement(by.css('#teamMenuId a.home'));
  };
  this.getTasksSubmenu = function(){
    return browser.driver.findElements(by.css('#teamMenuId a.mmlink'));
  };
  this.getMyTasks = function(){
    return browser.driver.findElement(by.css('a[href="/tdstm/clientTeams/listTasks"]'));
  };
  this.getTaskManager = function(){
    return browser.driver.findElement(by.css('a[href="/tdstm/assetEntity/listTasks?initSession=true"]'));
  };
  this.getTaskGraph = function(){
    return browser.driver.findElement(by.css('a[href="/tdstm/task/taskGraph?initSession=true"]'));
  };
  this.getTaskTimeline = function(){
    return browser.driver.findElement(by.css('a[href="/tdstm/task/taskTimeline"]'));
  };
  this.getCookbook = function(){
    return browser.driver.findElement(by.css('a[href="/tdstm/cookbook/index"]'));
  };
  this.getTasksHelp = function(){
    return browser.driver.findElement(by.css
      ('a[href="javascript:window.open(\'https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TMTaskOverview?cover=print\',\'help\');"]'));
  };
  this.goToTasks= function(option){
    var that = this;
    this.getTasks().click();
    var d = {
      'myTasks':function(){
        that.getMyTasks().click();
      },
      'taskManager':function(){
        that.getTaskManager().click();
      },
      'taskGraph':function(){
        that.getTaskGraph().click();
      },
      'timeline':function(){
        that.getTaskTimeline().click();
      }, 
      'cookbook':function(){
        that.getCookbook().click();
      }
    };
    d[option]();
  };
//Dashboards
  this.getDashboards = function(){
    return browser.driver.findElement(by.css('#dashboardMenuId a.home'));
  };
  this.getDashboardsSubmenu = function(){
    return browser.driver.findElements(by.css('#dashboardMenuId a.mmlink'));
  };
  this.getUserDashboard = function(){
    return browser.driver.findElement(by.css('a[href="/tdstm/dashboard/userPortal"]'));
  };
  this.getPlanningDashboard = function(){
    return browser.driver.findElement(by.css('a[href="/tdstm/moveBundle/planningStats"]'));
  };
  this.getEventDashboard = function(){
    return browser.driver.findElement(by.css('a[href="/tdstm/dashboard/index"]'));
  };
  this.getCartTraker = function(){
    return browser.driver.findElement(by.css('a[href="/tdstm/cartTracking/cartTracking"]'));
  };
  this.getDashboardHelp = function(){
    return browser.driver.findElement(by.css('a[href="javascript:window.open(\'https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TMDashboardOverview?cover=print\',\'help\');"]'));
  };
  this.goToDashboards= function(option){
    // var that = this;
    this.getDashboards().click();
    var d = {

    };
    d[option]();
  };

//Reports

  this.getReports = function(){
    return browser.driver.findElement(by.css('#reportsMenuId a.home'));
  };
  this.getReportsMegaMenu = function(){
    return browser.driver.findElement(by.id('reportsMegaMenu'));
  };
  this.getReportsSubmenu = function(){
    return browser.driver.findElements(by.css('#reportsMenuId a.mmlink'));
  };
  this.getCablingConflict = function(){
      return browser.driver.findElement(by.css('a[href="/tdstm/reports/getBundleListForReportDialog?reportId=CablingConflict"]'));
  };
  this.getCablingData = function(){
    return browser.driver.findElement
      (by.css('a[href="/tdstm/reports/getBundleListForReportDialog?reportId=CablingData"]'));
  };
  this.getPower = function(){
      return browser.driver.findElement(by.css('a[href="/tdstm/reports/powerReport"]'));
  };
  this.getDiscoveryHelp = function(){
    return browser.driver.findElement(by.css('a[href="javascript:window.open(\'https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TranManHelp?cover=print\',\'help\');"]'));
  };
  this.getApplicationProfiles = function(){
    return browser.driver.findElement(by.css('a[href="/tdstm/reports/applicationProfiles"]'));
  };
  this.getApplicationConflicts = function(){
    return browser.driver.findElement(by.css('a[href="/tdstm/reports/applicationConflicts"]'));
  };
  this.getServerConflicts = function(){
    return browser.driver.findElement(by.css('a[href="/tdstm/reports/serverConflicts"]'));
  };
  this.getDatabaseConflicts = function(){
    return browser.driver.findElement(by.css('a[href="/tdstm/reports/databaseConflicts"]'));
  };
  this.getTaskReport = function(){
    return browser.driver.findElement(by.css('a[href="/tdstm/reports/getBundleListForReportDialog?reportId=Task+Report"]'));
  };
  this.getReportSummary = function(){
    return browser.driver.findElement(by.css('a[href^="/tdstm/reports/index?projectId="]'));
  };
  this.getReportPreEventChecklist = function(){
    return browser.driver.findElement(by.css('a[href="/tdstm/reports/preMoveCheckList"].home'));
  };
  this.getLoginBadges = function(){
    return browser.driver.findElement(by.css('a[href="/tdstm/reports/getBundleListForReportDialog?reportId=Login+Badges"]'));
  };
  this.getAssetTags = function(){
    return browser.driver.findElement(by.css('a[href="/tdstm/reports/getBundleListForReportDialog?reportId=Asset+Tag"]'));
  };
  this.getLogisticsTeamWorksheets = function(){
    return browser.driver.findElement(by.css('a[href="/tdstm/reports/getBundleListForReportDialog?reportId=cart+Asset"]'));
  };
  this.getTransportWorksheets =function(){
    return browser.driver.findElement(by.css('a[href="/tdstm/reports/getBundleListForReportDialog?reportId=Transportation+Asset+List"]'));
  };
  this.getEventPrepHelp = function(){
    return browser.driver.findElement(by.css('a[href="javascript:window.open(\'https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TranManHelp?cover=print\',\'help\');"]'));
  };
  this.getApplicationMigrationResults = function(){
    return browser.driver.findElement(by.css('a[href="/tdstm/reports/applicationMigrationReport"]'));
  };
  this.getIssueReport = function(){
    return browser.driver.findElement(by.css('a[href="/tdstm/reports/getBundleListForReportDialog?reportId=Issue+Report"]'));
  };
  this.getEventResults = function(){
    // return browser.driver.findElement(by.css('a[href="/tdstm/reports/retrieveBundleListForReportDialog?reportId=MoveResults"]'));
    return browser.driver.findElement(by.css('a[href="/tdstm/reports/getBundleListForReportDialog?reportId=MoveResults"]'));
  };
  this.getCablingQA = function(){
    return browser.driver.findElement(by.css('a[href="/tdstm/reports/getBundleListForReportDialog?reportId=CablingQA"]'));
  };
  this.getEventDayHelp = function(){
    return browser.driver.findElement(by.css('a[href="javascript:window.open(\'https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TranManHelp?cover=print\',\'help\');"]'));
  };
  this.goToReports= function(option){
    var that = this;
    this.getReports().click();
    var d = {
      'cablingConflict':function(){
          that.getCablingConflict().click();
      },
      'cablingData':function(){
        that.getCablingData().click();
      },
      'power': function(){
        that.getPower().click();
      }, 
      'applicationProfiles': function(){
        that.getApplicationProfiles().click();
      },
      'applicationConflicts':function(){
        that.getApplicationConflicts().click();
      },
      'serverConflicts':function(){
        that.getServerConflicts().click();
      }, 
      'databaseConflicts':function(){
        that.getDatabaseConflicts().click();
      }, 
      'taskReport':function(){
        that.getTaskReport().click();
      },
      'reportSummary':function(){
        that.getReportSummary().click();
      },
      'preEventCheckList':function(){
        that.getReportPreEventChecklist().click();
      },
      'loginBadges':function(){
        that.getLoginBadges().click();
      },
      'assetTags':function(){
        that.getAssetTags().click();
      },
      'logisticsTeamWorksheets':function(){
        that.getLogisticsTeamWorksheets().click();
      },
      'transportWorksheets':function(){
        that.getTransportWorksheets().click();
      },
      'applicationMigrationResults':function(){
        that.getApplicationMigrationResults().click();
      },
      'issueReport':function(){
        that.getIssueReport().click();
      },
      'eventResults':function(){
        that.getEventResults().click();
      },
      'cablingQA':function(){
        that.getCablingQA().click();
      }
    };
    d[option]();
  };

  this.goToTime= function(option){
    browser.driver.findElement(by.css(this.timezoneCss)).click();
    var d = {

    };
    d[option]();
  };

};

module.exports = Menu;