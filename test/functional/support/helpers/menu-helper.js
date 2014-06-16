'use strict';
function MenuHelper(){
  this.title =  '.title';
  //userMenu
  this.userMenuLinkCss = 'a #loginUserId';
  this.signOutLinkCss = 'a[href="/tdstm/auth/signOut"].mmlink';
  this.accountDetailsCss= 'a[href^="/tdstm/person/getPersonDetails/"]';
  this.editPreferencesCss = '#resetPreferenceId';
  this.useMobileSiteCss = 'a[href="/tdstm/clientTeams/listTasks?viewMode=mobile&tab="]';
 // LoginPage
  this.signInbtnCss = '.buttonR input';
  this.errorMessageCss = 'form div.message';
  // menu
  //Admin
  this.adminCss = '#adminMenuId a';
    //Administration
  this.adminPortalCss='a[href="/tdstm/auth/home"]';
  this.rolePermissionsCss ='a[href="/tdstm/permissions/show"]';
  this.assetOptionsCss ='a[href="/tdstm/assetEntity/assetOptions"]';
  this.releaseNotesCss ='a[]';
    //Manage Clients
  //ClientProjects
  this.clientProjectCss = '#projectMenuId a';
  this.listProjectsCss = 'a[href="/tdstm/project/list?active=active"]';
  //Rooms/Racks
  this.roomRackCss = '#roomMenuId a';
  //Assets
  this.assetsCss = '#assetMenuId a';
  this.summaryCss = 'a[href="/tdstm/assetEntity/assetSummary"]';
  this.listAppsCss='a[href="/tdstm/application/list"]';
  this.listServersCss = 'a[href="/tdstm/assetEntity/index?listType=server"]';
  this.listPhysicalCss = 'a[href="/tdstm/assetEntity/index?listType=physical"]';
  this.listDBsCss='a[href="/tdstm/database/index"]';
  this.listStorageCss='a[href="/tdstm/files/index"]';
  this.listDependenciesCss = 'a[href="/tdstm/assetEntity/listDependencies"]';
  this.dependencyAnalizerCss='a[href="/tdstm/moveBundle/dependencyConsole"]';
  this.assetCommentsCss='a[href="/tdstm/assetEntity/listComment"]';
  //Events/Bundles
  this.eventsBundlesCss = '#eventMenuId a';
  //Tasks
  this.tasksCss = '#teamMenuId a';
  //Console
  this.consoleCss = '#consoleMenuId a';
  //Dashboard
  this.dashboardsCss = '#dashboardMenuId a';
  //Reports
  this.reportsCss = '#reportsMenuId a';
  //timezonee
  this.timezoneCss = '.tzmenu';
} 

MenuHelper.prototype = {};
MenuHelper.prototype.getTitle = function getTitle(){
  return browser.driver.findElement(by.css(this.title));
};

// MenuHelper.prototype.signOut = function signOut(){
//   var signOutLink = browser.driver.findElement(by.css(this.signOutLinkCss));
//   signOutLink.click();
// };
  
MenuHelper.prototype.selectopt = function selectopt(option,suboption){
  var menu = new MenuHelper();
  var d = {
    'admin' : function () {
      $(menu.adminCss).click(); 
      var s ={
        'adminPortal':function(){

        },
        'rolePermissions':function(){

        },
        'assetOptions':function(){

        },
        'releaseNotes':function(){

        }
      };
      s[suboption]();
    },
    'clientProject' : function () {
      browser.driver.findElement(by.css(menu.clientProjectCss)).click(); 
      var s = {
        'listProjects':function(){
          browser.driver.findElement(by.css(menu.listProjectsCss)).click();
        },
        'projDetails':function(){

        },
        'projectStaff':function(){

        },
        'fieldSettings':function(){

        },
        'help':function(){

        }
      };
      s[suboption]();
    },
    'roomsRacks' : function () {
      $(menu.roomRackCss).click(); 
    },
    'assets' : function () {
      browser.driver.findElement(by.css(menu.assetsCss)).click(); 
      var s = {
        'summary':function(){
          browser.driver.findElement(by.css(menu.summaryCss)).click();
        },
        'listApps':function(){
          browser.driver.findElement(by.css(menu.listAppsCss)).click();
        },
        'listServers':function(){
          browser.driver.findElement(by.css(menu.listServersCss)).click();
        },
        'listPhysical':function(){
          browser.driver.findElement(by.css(menu.listPhysicalCss)).click();
        },
        'listDBs':function(){
          browser.driver.findElement(by.css(menu.listDBsCss)).click();
        },
        'listStorage':function(){
          browser.driver.findElement(by.css(menu.listStorageCss)).click();
        },
        'listDependencies':function(){
          browser.driver.findElement(by.css(menu.listDependenciesCss)).click();

        },
        'dependencyAnalizer':function(){
          browser.driver.findElement(by.css(menu.summaryCss)).click();

        },
        'assetsHelp':function(){
          browser.driver.findElement(by.css(menu.summaryCss)).click();

        },
        'import':function(){
          browser.driver.findElement(by.css(menu.summaryCss)).click();

        },
        'manageBatches':function(){
          browser.driver.findElement(by.css(menu.summaryCss)).click();

        },
        'export':function(){
          browser.driver.findElement(by.css(menu.summaryCss)).click();

        },
        'assetComments':function(){
          browser.driver.findElement(by.css(menu.summaryCss)).click();

        },
        'manageDataHelp':function(){
          browser.driver.findElement(by.css(menu.summaryCss)).click();

        },
      };
      s[suboption]();
    },
    'eventsBundles' : function () {
      $(menu.eventsBundlesCss).click(); 
    },
    'tasks' : function () {
      $(menu.tasksCss).click(); 
    },
    'console' : function () {
      $(menu.consoleCss).click(); 
    },
    'dashboards' : function () {
      $(menu.dashboardsCss).click(); 
    },
    'reports' : function () {
      $(menu.reportsCss).click(); 
    },
    'user' : function (suboption) {
      browser.driver.findElement(by.css(menu.userMenuLinkCss)).click().then(function(){
        var s ={
          'signOut':function(){
            browser.driver.findElement(by.css(menu.signOutLinkCss)).click();
          },
          'accountDetails': function(){
            browser.driver.findElement(by.css(menu.accountDetailsCss)).click();
          },
          'editPreferences': function(){
            browser.driver.findElement(by.css(menu.editPreferencesCss)).click();
          },
          'useMobileSite': function(){
            browser.driver.findElement(by.css(menu.useMobileSiteCss)).click();
          }
        };
        s[suboption]();
      });
      
    },
    'time' : function () {
      $(menu.timezoneCss).click(); 
    }
  };
  d[option](suboption);

};



exports.MenuHelper = MenuHelper;