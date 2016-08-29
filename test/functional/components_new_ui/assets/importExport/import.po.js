'use strict';
var ImportPage = function () {
  this.importTypeSelectId = 'dataTransferSet';
};
ImportPage.prototype = {};

ImportPage.prototype.getTitle = function() {
  return browser.driver.findElement(by.css('h1'));
};

ImportPage.prototype.getImportTypeSelected = function() {
  var that = this;
  if(process.env.BROWSER_NAME ==='phantomjs'){
    return browser.driver.executeScript('return $("#'+that.importTypeSelectId+' option:checked").text()');
  }else{
    return browser.driver.findElement(by.css('#'+that.importTypeSelectId+' option:checked')).getText();
  }
};

ImportPage.prototype.getApplicationCheckbox = function() {
  return browser.driver.findElement(by.id('applicationId'));
};

ImportPage.prototype.getDevicesCheckbox = function() {
  return browser.driver.findElement(by.id('assetId'));
};

ImportPage.prototype.getDatabaseCheckbox = function() {
  return browser.driver.findElement(by.id('databaseId'));
};

ImportPage.prototype.getStorageCheckbox = function() {
  return browser.driver.findElement(by.id('storageId'));
};

ImportPage.prototype.getDependencyCheckbox = function() {
  return browser.driver.findElement(by.id('dependencyId'));
};

ImportPage.prototype.getCablingCheckbox = function() {
  return browser.driver.findElement(by.id('cablingId'));
};

ImportPage.prototype.getCommentCheckbox = function() {
  return browser.driver.findElement(by.id('commentId'));
};

ImportPage.prototype.selectCheckboxToImport = function(toImport) {
  var that = this;
  for (var outerKey in toImport){
    (function (key) {
      that['get'+key+'Checkbox']().getAttribute('checked').then(function(current){
        if ((toImport[key] !== current) && !(toImport[key]==='false' && current===null)){
          that['get'+key+'Checkbox']().click();
        }
      });
    })(outerKey);
  }
};

ImportPage.prototype.getFileField = function() {
  return browser.driver.findElement(by.id('file'));
};

ImportPage.prototype.getImportBtn = function() {
  return browser.driver.findElement(by.css('input[type="submit"]#run'));
};

ImportPage.prototype.isProgressBarDisplayed = function() {
  return browser.wait(function(){
    return browser.driver.findElement(by.id('progressBar')).getText().then(function(text){
      return text!=='';
    });
  },9000).then(function(){
     return true;
  });
};

ImportPage.prototype.getResultMessage = function() {
  return browser.wait(function(){
    return browser.executeScript('return $(".body div").attr("class")').then(function (la) {
      return la === 'message';
    });
  },9000).then(function(){
    return browser.driver.findElement(by.css('.body .message')).getText().then(function (text) {
      return text;
    }); 
  });
};

ImportPage.prototype.getExpectedSucessMessage = function(devices,application,databases,logicalStorage,dependency,cables,comments) {
  var resultMessage = [
    'Spreadsheet import was successful',
    'Please click the Manage Batches below to review and post these changes',
    'Results:',
    devices+' Devices loaded',
    application+' Applications loaded',
    databases+' Databases loaded',
    logicalStorage+' Logical Storage loaded',
    dependency+' Dependency loaded',
    cables+' Cables loaded',
    comments+' Comments loaded'
  ].join('\n');
  return resultMessage;
};

ImportPage.prototype.getManageBatchesLink = function() {
  return browser.driver.findElement(by.css('.buttonR a[href="/tdstm/dataTransferBatch/index"]'));
};


module.exports = ImportPage;