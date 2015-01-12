'use strict';
var File = require('./file.po.js');
var ExportPage = function exportPage () {
  // this.applicationLabel = $('[for="applicationId"]');
  // this.deviceLabel = $('[for="assetId"]');
  // this.databaseLabel = $('[for="databaseId"]');
  // this.storageLabel = $('[for="filesId"]');
  // this.roomLabel = $('[for="roomId"]');
  // this.rackLabel = $('[for="rackId"]');
  // this.dependencyLabel = $('[for="dependencyId"]');
  // this.cablingLabel = $('[for="cablingId"]');
  // this.commentsLabel = $('[for="commentId"]');
};
ExportPage.prototype ={};

ExportPage.prototype.getTitle = function() {
  return browser.driver.findElement(by.css('h1'));
};

ExportPage.prototype.getApplicationCheckbox = function() {
  return browser.driver.findElement(by.id('applicationId'));
};
ExportPage.prototype.getDevicesCheckbox = function() {
  return browser.driver.findElement(by.id('assetId'));
};
ExportPage.prototype.getDatabaseCheckbox = function() {
  return browser.driver.findElement(by.id('databaseId'));
};
ExportPage.prototype.getStorageCheckbox = function() {
  return browser.driver.findElement(by.id('filesId'));
};
ExportPage.prototype.getRoomCheckbox = function() {
  return browser.driver.findElement(by.id('roomId'));
};
ExportPage.prototype.getRackCheckbox = function() {
  return browser.driver.findElement(by.id('rackId'));
};
ExportPage.prototype.getDependencyCheckbox = function() {
  return browser.driver.findElement(by.id('dependencyId'));
};
ExportPage.prototype.getCablingCheckbox = function() {
  return browser.driver.findElement(by.id('cablingId'));
};
ExportPage.prototype.getCommentCheckbox = function() {
  return browser.driver.findElement(by.id('commentId'));
};

ExportPage.prototype.getProgressStatus = function() {
  return browser.driver.findElement(by.id('progressStatus'));
};

ExportPage.prototype.selectCheckboxToExport = function(toExport) {
  for (var outerKey in toExport) {
    var that = this;
    (function (key) {
      that['get'+key+'Checkbox']().getAttribute('checked').then(function(current){
    if ((toExport[key] !== current) && !(toExport[key]==='false' && current===null)){
      that['get'+key+'Checkbox']().click();
    }
  });
    })(outerKey);
  }
};

ExportPage.prototype.enableDevices = function() {
  var that = this;
  this.getDevicesCheckbox().getAttribute('checked').then(function(valor){
    if(!valor){
      that.getDevicesCheckbox().click();
    }
  });
};
ExportPage.prototype.getExportBtn = function() {
  return browser.driver.findElement(by.css('input[value="Export to Excel"]'));
};

ExportPage.prototype.isExportingModalOpened = function() {
  return browser.wait(function(){
      return browser.executeScript('return $("#progressBar").text()').then(function(text){
        console.log(text);
        return text!=='';
      });
  }).then(function(){
     return true;
  });
};

ExportPage.prototype.isExportingModalClosed = function(filePath) {
  var file = new File();
  return browser.driver.wait(function(){
    return browser.driver.findElement(by.id('progressBar')).getText().then(function(text){
      return text==='' && file.existsSync(filePath);
    });
  }).then(function(){
     return true;
  });
};

module.exports = ExportPage;