'use strict';
var CreateAsset = require('../modals/assetCreate.po.js');
var DeviceCreateModal = function(){
  this.altNameLabel = this.createModal.$('[for="shortName"]');
  this.altNameField = this.createModal.element(by.id('shortName'));
  this.modelTypeField = this.createModal.element(by.id('assetTypeSelect'));
  this.modelTypeFieldInput = this.createModal.$('#modelEditId input[id^="s2id_autogen"]');
  this.modelTypeOptions = this.modelTypeField.$$('option');
  this.modelTypeOptionSelected = this.modelTypeField.$('option:checked');
  this.saveBtn = this.createModal.$('[onclick="EntityCrud.saveToShow($(this), \'DEVICE\'); "]');
};
DeviceCreateModal.prototype = new CreateAsset();
DeviceCreateModal.prototype.createServer = function(appName,deciveType){
  var field = this.nameField;
  field.sendKeys(appName);
  this.modelTypeFieldInput.sendKeys(deciveType);
  this.modelTypeField.$('[value='+deciveType+']').click();
  this.saveBtn.click();
};
DeviceCreateModal.prototype.getModelTypeSelected = function(){
  if(process.env.BROWSER_NAME ==='phantomjs'){
    return browser.executeScript('return $("#assetTypeSelectContainer option:checked").text()');
  }else{
    return this.MmdelTypeOptionSelected.getText();
  }
};
module.exports = DeviceCreateModal;