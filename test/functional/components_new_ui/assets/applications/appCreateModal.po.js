'use strict';
var CreateAsset = require('../modals/assetCreate.po.js');

var AppCreateModal = function(){
  this.appOwnerLabel = this.createModal.$('[for="appOwnerId"]');
  this.appOwnerField = this.createModal.element(by.id('appOwner'));
  this.saveBtn = this.createModal.$('[onclick="EntityCrud.saveToShow($(this),\'APPLICATION\')"]');
};
AppCreateModal.prototype = new CreateAsset();
AppCreateModal.prototype.createApp = function(name){
  this.nameField.sendKeys(name);
  this.saveBtn.click();
};

module.exports = AppCreateModal;