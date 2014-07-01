'use strict';
var Menu = function(){
  this.titleCss =  '.title';
  this.adminCss = '#adminMenuId a';
  
  this.clientProjectCss = '#projectMenuId a';
  this.listProjectsCss = 'a[href="/tdstm/project/list?active=active"]';
  
  this.roomRackCss = '#roomMenuId a';

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
  this.listBundlesCss = 'a[href="/tdstm/moveBundle/list"]';
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

  this.getCurrentUrl = function(){
    return browser.driver.getCurrentUrl();
  };

  
  this.getHeaderTitle = function(){
    return browser.driver.findElement(by.css(this.titleCss));
  };

  this.goToAdmin = function(option){
    browser.driver.findElement(by.css(this.adminCss)).click(); 
    var d ={
      'adminPortal':function(){

      },
      'rolePermissions':function(){

      },
      'assetOptions':function(){

      },
      'releaseNotes':function(){

      }
    };
    d[option]();
  };

  this.goToClientProject = function(option){
    var that = this;
    browser.driver.findElement(by.css(this.clientProjectCss)).click(); 
    var d = {
      'listProjects':function(){
        browser.driver.findElement(by.css(that.listProjectsCss)).click();
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
    d[option]();
  };
  
  this.goToRoomsRack= function(option){
    browser.driver.findElement(by.css(this.roomRackCss)).click();
    var d = {

    };
    d[option]();
  };

  this.goToAssets = function(option){
    var that = this;
    browser.driver.findElement(by.css(this.assetsCss)).click();
    var d = {
      'summary':function(){
        browser.driver.findElement(by.css(that.summaryCss)).click();
      },
      'listApps':function(){
        browser.driver.findElement(by.css(that.listAppsCss)).click();
      },
      'listServers':function(){
        browser.driver.findElement(by.css(that.listServersCss)).click();
      },
      'listPhysical':function(){
        browser.driver.findElement(by.css(that.listPhysicalCss)).click();
      },
      'listDBs':function(){
        browser.driver.findElement(by.css(that.listDBsCss)).click();
      },
      'listStorage':function(){
        browser.driver.findElement(by.css(that.listStorageCss)).click();
      },
      'listDependencies':function(){
        browser.driver.findElement(by.css(that.listDependenciesCss)).click();
      },
      'dependencyAnalizer':function(){
        browser.driver.findElement(by.css(that.dependencyAnalizerCss)).click();
      },
      'assetsHelp':function(){
      },
      'import':function(){
      },
      'manageBatches':function(){
      },
      'export':function(){
      },
      'assetComments':function(){
      },
      'manageDataHelp':function(){
      },
    };
    d[option]();
  };

  this.goToEventsBundles= function(option){
    var that = this;
    browser.driver.findElement(by.css(this.eventsBundlesCss)).click();
    var d = {
      'listEvents':function(){

      },
      'listBundles':function(){
        browser.driver.findElement(by.css(that.listBundlesCss)).click();


      }
    };
    d[option]();
  };
  
  this.goToTasks= function(option){
    browser.driver.findElement(by.css(this.tasksCss)).click();
    var d = {

    };
    d[option]();
  };
  
  this.goToConsole= function(option){
    browser.driver.findElement(by.css(this.consoleCss)).click();
    var d = {

    };
    d[option]();
  };
  
  this.goToDashboards= function(option){
    browser.driver.findElement(by.css(this.dashboardsCss)).click();
    var d = {

    };
    d[option]();
  };

  this.goToReports= function(option){
    browser.driver.findElement(by.css(this.reportsCss)).click();
    var d = {

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