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

ImportPage.prototype.setApplicationCheckValue = function(expected) {
  var that = this;
  this.getApplicationCheckbox().getAttribute('checked').then(function(current){
    if ((expected !== current) && !(expected==='false' && current===null)){
      that.getApplicationCheckbox().click();
    }
  });
};

ImportPage.prototype.setDevicesCheckValue = function(expected) {
  var that = this;
  this.getDevicesCheckbox().getAttribute('checked').then(function(current){
    if ((expected !== current) && !(expected==='false' && current===null)){
      that.getDevicesCheckbox().click();
    }
  });
};

ImportPage.prototype.setDatabaseCheckValue = function(expected) {
  var that = this;
  this.getDatabaseCheckbox().getAttribute('checked').then(function(current){
    if ((expected !== current) && !(expected==='false' && current===null)){
      that.getDatabaseCheckbox().click();
    }
  });
};

ImportPage.prototype.setStorageCheckValue = function(expected) {
  var that = this;
  this.getStorageCheckbox().getAttribute('checked').then(function(current){
    if ((expected !== current) && !(expected==='false' && current===null)){
      that.getStorageCheckbox().click();
    }
  });
};

ImportPage.prototype.setDependencyCheckValue = function(expected) {
  var that = this;
  this.getDependencyCheckbox().getAttribute('checked').then(function(current){
    if ((expected !== current) && !(expected==='false' && current===null)){
      that.getDependencyCheckbox().click();
    }
  });
};

ImportPage.prototype.setCablingCheckValue = function(expected) {
  var that = this;
  this.getCablingCheckbox().getAttribute('checked').then(function(current){
    if ((expected !== current) && !(expected==='false' && current===null)){
      that.getCablingCheckbox().click();
    }
  });
};

ImportPage.prototype.setCommentCheckValue = function(expected) {
  var that = this;
  this.getCommentCheckbox().getAttribute('checked').then(function(current){
    if ((expected !== current) && !(expected==='false' && current===null)){
      that.getCommentCheckbox().click();
    }
  });
};

ImportPage.prototype.selectCheckboxToImport = function(toImport) {
 this.setApplicationCheckValue(toImport['application']);
 this.setDevicesCheckValue(toImport['devices']);
 this.setDatabaseCheckValue(toImport['database']);
 this.setStorageCheckValue(toImport['storage']);
 this.setDependencyCheckValue(toImport['dependency']);
 this.setCablingCheckValue(toImport['cabling']);
 this.setCommentCheckValue(toImport['comment']);
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
  }).then(function(){
     return true;
  });
};

ImportPage.prototype.getResultMessage = function() {
  return browser.wait(function(){
    return browser.driver.findElement(by.css('.body .message')).isDisplayed();
  }).then(function(){
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