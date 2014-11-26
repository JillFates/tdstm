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
ExportPage.prototype.setApplicationCheckValue = function(value) {
  var that = this;
  this.getApplicationCheckbox().getAttribute('checked').then(function(valor){
    if(valor !==value){
      that.getApplicationCheckbox().click();
    }
  });
};
ExportPage.prototype.setDevicesCheckValue = function(value) {
  var that = this;
  this.getDevicesCheckbox().getAttribute('checked').then(function(valor){
    if(valor !==value){
      that.getDevicesCheckbox().click();
    }
  });
};
ExportPage.prototype.setDatabaseCheckValue = function(value) {
  var that = this;
  this.getDatabaseCheckbox().getAttribute('checked').then(function(valor){
    if(valor !==value){
      that.getDatabaseCheckbox().click();
    }
  });
};
ExportPage.prototype.setStorageCheckValue = function(value) {
  var that = this;
  this.getStorageCheckbox().getAttribute('checked').then(function(valor){
    if(valor !==value){
      that.getStorageCheckbox().click();
    }
  });
};
ExportPage.prototype.setRoomCheckValue = function(value) {
  var that = this;
  this.getRoomCheckbox().getAttribute('checked').then(function(valor){
    if(valor !==value){
      that.getRoomCheckbox().click();
    }
  });
};
ExportPage.prototype.setRackCheckValue = function(value) {
  var that = this;
  this.getRackCheckbox().getAttribute('checked').then(function(valor){
    if(valor !==value){
      that.getRackCheckbox().click();
    }
  });
};
ExportPage.prototype.setDependencyCheckValue = function(value) {
  var that = this;
  this.getDependencyCheckbox().getAttribute('checked').then(function(valor){
    if(valor !==value){
      that.getDependencyCheckbox().click();
    }
  });
};
ExportPage.prototype.setCablingCheckValue = function(value) {
  var that = this;
  this.getCablingCheckbox().getAttribute('checked').then(function(valor){
    if(valor !==value){
      that.getCablingCheckbox().click();
    }
  });
};
ExportPage.prototype.setCommentCheckValue = function(value) {
  var that = this;
  this.getCommentCheckbox().getAttribute('checked').then(function(valor){
    if(valor !==value){
      that.getCommentCheckbox().click();
    }
  });
};
ExportPage.prototype.getProgressStatus = function() {
  return browser.driver.findElement(by.id('progressStatus'));
};
ExportPage.prototype.selectCheckboxToExport = function(toExport) {
 this.setApplicationCheckValue(toExport['application']);
 this.setDevicesCheckValue(toExport['devices']);
 this.setDatabaseCheckValue(toExport['database']);
 this.setStorageCheckValue(toExport['storage']);
 this.setRoomCheckValue(toExport['room']);
 this.setRackCheckValue(toExport['rack']);
 this.setDependencyCheckValue(toExport['dependency']);
 this.setCablingCheckValue(toExport['cabling']);
 this.setCommentCheckValue(toExport['comment']);
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
  // var message = '';
  return browser.wait(function(){
    return browser.driver.findElement(by.id('progressBar')).getText().then(function(text){
      // if(text !== message){
      //   console.log('valor en open', text);
      //   message = text;
      // }
      return text!=='';
    });
  }).then(function(){
     return true;
  });
};

ExportPage.prototype.isExportingModalClosed = function(filePath) {
  var file = new File();
  // var message = '';
  return browser.driver.wait(function(){
    return browser.driver.findElement(by.id('progressBar')).getText().then(function(text){
      // if(text !== message){
      //   console.log('valor en close', text);
      //   message = text;
      // }
      return text==='' && file.existsSync(filePath);
    });
  }).then(function(){
     return true;
  });
};

module.exports = ExportPage;