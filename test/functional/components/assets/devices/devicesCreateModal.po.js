'use strict';
var CreateAsset = require('../modals/assetCreate.po.js');
var DeviceCreateModal = function(){
  this.altNameLabel = this.createModal.$('[for="shortName"]');
  this.altNameField = this.createModal.element(by.id('shortName'));
  this.modelTypeField = this.createModal.element(by.id('assetTypeSelect'));
  this.modelTypeFieldInput = this.createModal.$('#modelEditId input[id^="s2id_autogen"]');
  this.currentAssetType = this.createModal.$('#currentAssetType');
  this.modelTypeOptions = this.modelTypeField.$$('option');
  this.modelTypeOptionSelected = this.modelTypeField.$('option:checked');
  this.saveBtn = this.createModal.$('[onclick="EntityCrud.saveToShow($(this), \'DEVICE\'); "]');
  this.rackCabinetLabel = this.createModal.$('[for="sourceRackId"]');
  this.chassisBladeLabel = this.createModal.$('[for="sourceChassisId"]');
};
DeviceCreateModal.prototype = new CreateAsset();
// DeviceCreateModal.prototype.createModal = function(){

// }; 
DeviceCreateModal.prototype.createDevice = function(appName,deviceType){
  this.nameField.sendKeys(appName);
  this.modelTypeFieldInput.sendKeys(deviceType);
  // this.modelTypeField.$('[value='+deviceType+']').click();
  browser.actions().sendKeys(protractor.Key.ENTER).perform();
  browser.executeScript('return $("#currentAssetType").val("'+deviceType+'")');
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