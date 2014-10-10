'use strict';
var CreateAsset = require('../modals/assetCreate.po.js');

var DeviceCreateModal = function(){
  this.altNameLabel = this.createModal.$('[for="shortName"]');
  this.altNameField = this.createModal.element(by.id('shortName'));
  this.saveBtn = this.createModal.$('[onclick="saveToShow($(this),\'AssetEntity\')"]');

};
DeviceCreateModal.prototype = new CreateAsset();
DeviceCreateModal.prototype.createServer = function(appName){
    //needs to be open the create server modal
    // var that = this;
    var field = this.nameField;
    field.sendKeys(appName);
    expect(field.getAttribute('value')).toEqual(appName);
    this.saveBtn.click();
  };

module.exports = DeviceCreateModal;