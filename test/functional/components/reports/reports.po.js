'use strict';
var Reports = function(){
  this.getPageTitle = function(){
    return browser.driver.findElement(by.css('h1')).getText();
  };
//Report summary Starts  
  this.getPageTitlesLength = function(){
    if(process.env.BROWSER_NAME==='phantomjs'){
      return browser.driver.executeScript('return $("h1").length');
    }else{
      return browser.driver.findElements(by.css('h1')).then(function(list){
        return list.length;
      });
    }
  };
  this.getPageTitlesList = function(){
    if(process.env.BROWSER_NAME==='phantomjs'){
      return browser.driver.executeScript('return $("h1")');
    }else{
      return browser.driver.findElements(by.css('h1'));
    }
  };
  this.getReportsListLength = function(){
    if(process.env.BROWSER_NAME==='phantomjs'){
      return browser.driver.executeScript('return $("table[style=\'border: 0\'] table tbody td a").length');
    }else{
      return browser.driver.findElements(by.css('table[style="border: 0"] table tbody td a')).then(function(list){
        return list.length;
      });
    }
  };
  this.getReportsList = function(){
    if(process.env.BROWSER_NAME==='phantomjs'){
      return browser.driver.executeScript('return $("table[style=\'border: 0\'] table tbody td a")');
    }else{
      return browser.driver.findElements(by.css('table[style="border: 0"] table tbody td a'));
    }
  };
  //Report summary ends
//Generate Reports  Buttons Starts
  this.getGenerateCablingConflictBtn = function(){
    return browser.driver.findElement(by.css('[onclick="return submit_CablingQAReport(this)"]'));
  };
  this.generateApplicationConflictsBtn = function(){
    return browser.driver.findElement(by.id('applicationConflictsButton'));
  };
  this.generateServerConflictsBtn = function(){
    return browser.driver.findElement(by.id('serverConflictsButton'));
  };
  this.generateCablingDataBtn = function(){
    return browser.driver.findElement(by.css('[onclick="return submit_CablingDataReport(this)"]'));
  };
  this.generateApplicationProfilesBtn = function(){
    return browser.driver.findElement(by.id('applicationProfilesButton'));
  };
  this.generateDatabaseConflictsBtn = function(){
    return browser.driver.findElement(by.id('databaseConflictsButton'));
  };
// Generate Reports Buttons Ends
  this.getAppProfileGeneratedHeader = function(){
    return browser.driver.findElement(by.css('[ng-app="tdsAssets"] div b'));
  };
  this.getMessage = function(){
    return browser.driver.findElement(by.css('.main_bottom .body .message'));
  };
  this.getBundleLabel = function(){
    return browser.driver.findElement(by.css('#bundleRow label')).getText();
  };
  this.getBundlesDropdown = function(){
    return browser.driver.findElement(by.css('#moveBundleId'));
  };
  this.getBundleSelected = function(){
    if(process.env.BROWSER_NAME==='phantomjs'){
     return browser.driver.executeScript('return $("#moveBundleId option:checked").text()');
    }else{
     return browser.driver.findElement(by.css('#moveBundleId option:checked')).getText();
    }
  };
  this.getBundlesOptionsLength = function(){
    if(process.env.BROWSER_NAME==='phantomjs'){
      return browser.driver.executeScript('return $("#moveBundleId option").length');
    }else{
      return browser.driver.findElements(by.css('#moveBundleId option')).then(function(list){
        return list.length;
      });
    }
  };
  this.getBundlesOptions = function(){
    if(process.env.BROWSER_NAME==='phantomjs'){
      return browser.driver.executeScript('return $("#moveBundleId option")');
    }else{
      return browser.driver.findElements(by.css('#moveBundleId option'));
    }
  };
  this.getCableTypeLabel = function(){
    return browser.driver.findElements(by.css('label')).then(function(list){
      return list[1].getText();
    });
  };
  this.getCableTypeSelected = function(){
    if(process.env.BROWSER_NAME==='phantomjs'){
     return browser.driver.executeScript('return $("#cableTypeId option:checked").text()');
    }else{
     return browser.driver.findElement(by.css('#cableTypeId option:checked')).getText();
    }
  };
  this.getCableTypeOptionsLength = function(){
    if(process.env.BROWSER_NAME==='phantomjs'){
      return browser.driver.executeScript('return $("#cableTypeId option").length');
    }else{
      return browser.driver.findElements(by.css('#cableTypeId option')).then(function(list){
        return list.length;
      });
    }
  };
  this.getCableTypeOptions = function(){
    if(process.env.BROWSER_NAME==='phantomjs'){
      return browser.driver.executeScript('return $("#cableTypeId option")');
    }else{
      return browser.driver.findElements(by.css('#cableTypeId option'));
    }
  };
  //Application conflicts
  this.getMissingInput = function(){
    return browser.driver.findElement(by.id('missing'));
  };
  this.getUnresolvedInput = function(){
    return browser.driver.findElement(by.id('unresolved'));
  };
  this.getConflictsInput = function(){
    return browser.driver.findElement(by.id('unresolved'));
  };
  //Server Conflicts
  this.getBundleConflictInput = function(){
    return browser.driver.findElement(by.id('bundleConflicts'));
  };
  this.getUnresolvedDependenciesInput = function(){
    return browser.driver.findElement(by.id('unresolvedDep'));
  };  
  this.getNoRunsOnInput = function(){
    return browser.driver.findElement(by.id('noRuns'));
  };  
  this.getVMWithNoSupportInput = function(){
    return browser.driver.findElement(by.id('vmWithNoSupport'));
  };
  //Database Conflict
  this.getNoApplicationInput = function(){
    return browser.driver.findElement(by.id('noApps'));
  };
  this.getDbWithNoSupport = function(){
    return browser.driver.findElement(by.id('dbWithNoSupport'));
  };
//Task Report
  this.getEventsLabel = function(){
    return browser.driver.findElement(by.css('form .name label'));
  };
  this.getEventsSelected = function(){
    if(process.env.BROWSER_NAME==='phantomjs'){
     return browser.driver.executeScript('return $("#moveEventId option:checked").text()');
    }else{
     return browser.driver.findElement(by.css('#moveEventId option:checked')).getText();
    }
  };
  this.getEventsOptionsLength = function(){
    if(process.env.BROWSER_NAME==='phantomjs'){
      return browser.driver.executeScript('return $("#moveEventId option").length');
    }else{
      return browser.driver.findElements(by.css('#moveEventId option')).then(function(list){
        return list.length;
      });
    }
  };
  this.getEventsOptions = function(){
    if(process.env.BROWSER_NAME==='phantomjs'){
      return browser.driver.executeScript('return $("#moveEventId option")');
    }else{
      return browser.driver.findElements(by.css('#moveEventId option'));
    }
  };
  this.getIncludeCommentsCheckbox = function(){
    return browser.driver.findElement(by.css('[name="wComment"]'));
  };
  this.getIncludeOnlyRemainingCheckbox = function(){
    return browser.driver.findElement(by.css('[name="wUnresolved"]'));
  };
  this.getIncludeUnpublishedCheckbox = function(){
    return browser.driver.findElement(by.css('[name="viewUnpublished"]'));
  };
  this.getButtons = function(){
    if(process.env.BROWSER_NAME==='phantomjs'){
      return browser.driver.executeScript('return $(".buttonR input")');
    }else{
      return browser.driver.findElements(by.css('.buttonR input'));
    }
  };
  this.generateTaskReport = function(option){
    var op = {
      'web':0,
      'xls':1,
      'pdf':2
    };
    this.getButtons().then(function(buttons){
      buttons[op[option]].click();
    });
  };
  //pre-Event Checklist
  this.getOutputWeb = function(){
    return browser.driver.findElement(by.id('web'));
  };
  this.generatePreEventChecklistBtn = function(){
    return browser.driver.findElement(by.css('[onclick="return verifyEvent();"]'));
  };
  this.getPreEventCheckGeneratedReportHeader = function(){
    return browser.driver.findElement(by.css('.main_bottom div div b'));
  };
  // transport worksheet
  this.generateTransportWorksheetBtn = function(){
    return browser.driver.findElement(by.css('[onclick="return submit_transportationAssetReport(this)"]'));
  };
  // Application Migration Results
  this.generateApplicationMigrationBtn = function(){
    return browser.driver.findElement(by.id('applicationMigrationButton'));
  };
  this.getMoveBundleLabel = function(){
    return browser.driver.findElement(by.css('[for="moveBundle"]'));
  };
};
module.exports = Reports;