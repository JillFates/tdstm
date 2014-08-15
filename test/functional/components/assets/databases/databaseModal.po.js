'use strict';
var ApplicationModal = require('../createApp.po.js');
var DatabaseModal = function(){
  var extendAppModal = new ApplicationModal();
  this.createTitle = extendAppModal.createAppTitle;
  this.createModal  = extendAppModal.createAppModal;
  this.createModalButtons = this.createModal.$$('span.button input');
  this.saveBtn = this.createModal.$('input[value="Save"]');
  this.isCOpened = extendAppModal.isCreateModalOpened();
  this.nameLabel = extendAppModal.nameLabel;
  this.nameField = extendAppModal.nameField;
  this.typeLabel = extendAppModal.typeLabel;
  this.typeField = extendAppModal.typeField;
  this.formatLabel = $('[for="dbFormat"]');
  this.formatField = $('#dbFormat');
  this.sizeScaleLabel = this.createModal.$('[for="size"]');
  this.sizeField = $('#size');
  this.scaleField= $('#scale');
  this.scaleOptions= this.scaleField.$$('option');
  this.scaleSelected = this.scaleField.$('option:checked');
  this.setName = function(name){
    this.nameField.clear();
    this.nameField.sendKeys(name);
  };
};
module.exports = DatabaseModal;