'use strict';
var CreateAsset = require('../modals/assetCreate.po.js');

var DBCreateModal = function(){
  // this.saveBtn = this.createModal.$('[onclick="saveToShow($(this),\'Database\')"]');
  this.saveBtn = this.createModal.$('[onclick="EntityCrud.saveToShow($(this),\'DATABASE\')"]');
  this.formatLabel = $('[for="dbFormat"]');
  this.formatField = $('#dbFormat');
  this.sizeScaleLabel = this.createModal.$('[for="size"]');
  this.sizeField = $('#size');
  this.scaleField= $('#scale');
  this.scaleOptions= this.scaleField.$$('option');
  this.scaleSelected = this.scaleField.$('option:checked');
  this.rateOfChangeLabel= $('[for="rateOfChange"]');
  this.rateOfChangeField = element(by.id('rateOfChange'));
  this.descriptionLabel = $('[for="description"]');
  this.descriptionField = element(by.id('description'));
  this.supportLabel = $('[for="supportType"]');
  this.supportField = element(by.id('supportType'));
  this.retireDateLabel = $('[for="retireDate"]');
  this.retireDateField = element(by.id('retireDate'));
  this.maintExpLabel = $('[for="maintExpDate"]');
  this.maintExpField = element(by.id('maintExpDate'));
};
DBCreateModal.prototype = new CreateAsset();
DBCreateModal.prototype.getScaleSelected = function(){
   return browser.executeScript('return $("#scale option:checked").text()');
};
DBCreateModal.prototype.getValidationSelected = function(){
  return browser.executeScript('return $("#validation option:checked").text()');
};
module.exports = DBCreateModal;