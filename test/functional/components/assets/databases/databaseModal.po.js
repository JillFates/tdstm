'use strict';
var ApplicationModal = require('../createApp.po.js');
var DatabaseModal = function(){
  var extendAppModal = new ApplicationModal();
  this.createTitle = extendAppModal.createAppTitle;
  this.createModal  = extendAppModal.createAppModal;
  this.viewModal = extendAppModal.viewModal;
  this.createModalButtons = this.createModal.$$('span.button input');
  this.saveBtn = this.createModal.$('input[value="Save"]');
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
  this.rateOfChangeLabel= $('[for="rateOfChange"]');
  this.rateOfChangeField = element(by.id('rateOfChange'));
  //
  this.custom1Label = $('[for="custom1"]');
  this.custom1Field = element(by.id('custom1'));
  this.custom2Label = $('[for="custom2"]');
  this.custom2Field= element(by.id('custom2'));
  
  this.descriptionLabel = $('[for="description"]');
  this.descriptionField = element(by.id('description'));
  this.supportLabel = $('[for="supportType"]');
  this.supportField = element(by.id('supportType'));
  this.retireDateLabel = $('[for="retireDate"]');
  this.retireDateField = element(by.id('retireDate'));
  this.maintExpLabel = $('[for="maintExpDate"]');
  this.maintExpField = element(by.id('maintExpDate'));
  this.externalRefIdLabel = $('[for="externalRefId"]');
  this.externalRefIdField = element(by.id('externalRefId'));
  this.environmentLabel =$('[for="environment"]');
  this.environmentField = element(by.id('environment'));
  this.environmentOptions=this.environmentField.$$('option');
  // this.environmentSelected=this.environmentField.$('option:checked');
  this.environmentSelected=$('#environment option:checked');
  this.bundleLabel =$('[for="moveBundle"]');
  this.bundleField =element(by.id('moveBundle'));
  this.bundleOptions= this.bundleField.$$('option');
  this.bundleSelected=this.bundleField.$('option:checked');
  this.planStatusLabel=$('[for="planStatus"]');
  this.planStatusField=element(by.id('planStatus'));
  this.planStatusOptions = this.planStatusField.$$('option');
  this.planStatusSelected= this.planStatusField.$('option:checked');
  this.validationLabel = $('[for="validation"]');
  this.validationField=element(by.id('validation'));
  this.validationOptions=this.validationField.$$('option');
  this.validationSelected = this.validationField.$('option:checked');

  this.setName = function(name){
    this.nameField.clear();
    this.nameField.sendKeys(name);
  };
  this.getScaleSelected = function(){
     return browser.executeScript('return $("#scale option:checked").text()');
  };
  this.getEnvironmentSelected = function(){
    return browser.executeScript('return $("#environment option:checked").text()');
  };
  this.getBundleSelected = function(){
    return browser.executeScript('return $("#moveBundle option:checked").text()');
  };
  this.getPlanStatusSelected = function(){
    return browser.executeScript('return $("#planStatus option:checked").text()');
  };
  this.getValidationSelected = function(){
    return browser.executeScript('return $("#validation option:checked").text()');
  };

  this.isViewModalOpened = function(){
    var that = this;
    return browser.wait(function(){
      return that.viewModal.getAttribute('style').then(function(style){
        return style.search('display: block;') !== -1;
      });
    }).then(function(){
       return true;
    });
  };
  this.isViewModalClosed = function(){
    var that = this;
    return browser.wait(function(){
      return that.viewModal.getAttribute('style').then(function(style){
        return style.search('display: none;') !== -1;
      });
    }).then(function(){
       return true;
    });
  };

  this.isCreateModalClosed = function(){
    var that = this;
    return browser.wait(function(){
      return that.createModal.getAttribute('style').then(function(style){
        // return  style === 'position: absolute; height: auto; width: auto; top: 0px; left: 0px; display: none;';
        return style.search('display: none;') !== -1;

      });
    }).then(function(){
       return true;
    });
  };

  this.isCreateModalOpened = function(){
    var that = this;
    return browser.wait(function(){
      return that.createModal.getAttribute('style').then(function(style){
        return style.search('display: block;') !== -1;
      });
    }).then(function(){
       return true;
    });
  };

};
module.exports = DatabaseModal;