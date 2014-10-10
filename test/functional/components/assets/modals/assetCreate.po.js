'use strict';
var CreateAsset = function(){
  this.createModal = $('[aria-labelledby="ui-id-1"]');
  this.createModalTitle = this.createModal.element(by.id('ui-id-1'));
  this.closeCreateModalBtn = this.createModal.$('.ui-dialog-titlebar-close');
  this.createModalButtons = this.createModal.$$('span.button input');
  this.saveLogStgBtn = this.createModal.$('[onclick="saveToShow($(this),\'Logical Storage\')"]');
  this.cancelBtn = this.createModal.$('[onclick="$(\'#createEntityView\').dialog(\'close\');"]');
  //Fields
  this.bundleLabel =this.createModal.$('[for="moveBundle"]');
  this.bundleField = this.createModal.element(by.id('moveBundle'));
  this.bundleOptions = this.bundleField.$$('option');
  
  this.descriptionLabel = $('[for="description"]');
  this.descriptionField = element(by.id('description'));
  
  this.environmentLabel =this.createModal.$('[for="environment"]');
  this.environmentField = this.createModal.element(by.id('environment'));
  this.environmentOptions=this.environmentField.$$('option');
  
  this.externalRefIdLabel = $('[for="externalRefId"]');
  this.externalRefIdField = element(by.id('externalRefId'));
  
  this.nameLabel = this.createModal.$('[for="assetName"]');
  this.nameField = this.createModal.element(by.id('assetName'));
  
  this.planStatusLabel = this.createModal.$('[for="planStatus"]');
  this.planStatusField = this.createModal.element(by.id('planStatus'));
  this.planStatusOptions = this.planStatusField.$$('option');
  
  this.typeLabel = this.createModal.$('[for="assetType"]');
  this.typeField = this.createModal.element(by.id('assetType'));
  
  this.validationLabel = this.createModal.$('[for="validation"]');
  this.validationField = this.createModal.element(by.id('validation'));
  this.validationOptions=this.validationField.$$('option');
  this.validationSelected = this.validationField.$('option:checked');
  
  this.custom1Label = $('[for="custom1"]');
  this.custom1Field = element(by.id('custom1'));
  this.custom2Label = $('[for="custom2"]');
  this.custom2Field= element(by.id('custom2'));
};
CreateAsset.prototype ={};
CreateAsset.prototype.setName = function(name){
  this.nameField.clear();
  this.nameField.sendKeys(name);
};
CreateAsset.prototype.getBundleSelected = function(){
  if(process.env.BROWSER_NAME ==='phantomjs'){
    return browser.executeScript('return $("#moveBundle option:checked").text()');
  }else{
    return this.bundleField.$('option:checked').getText();
  }
};
CreateAsset.prototype.getEnvironmentSelected = function(){
  if(process.env.BROWSER_NAME ==='phantomjs'){
    return  browser.executeScript('return $("#environment option:checked").text()');
  }else{
    return this.environmentField.$('option:checked').getText();
  }
};
CreateAsset.prototype.getPlanStatusSelected = function(){
  if(process.env.BROWSER_NAME ==='phantomjs'){
    return browser.executeScript('return $("#planStatus option:checked").text()');
  }else{
    return this.planStatusField.$('option:checked').getText();
  }
};
CreateAsset.prototype.isCreateModalOpened = function(){
  var that = this;
  return browser.wait(function(){
    return that.createModal.getAttribute('style').then(function(style){
      return style.search('display: block;') !== -1;
    });
  }).then(function(){
     return true;
  });
};
CreateAsset.prototype.isCreateModalClosed = function(){
  var that = this;
  return browser.wait(function(){
    return that.createModal.getAttribute('style').then(function(style){
      return style.search('display: none;') !== -1;
    });
  }).then(function(){
     return true;
  });
};

module.exports = CreateAsset;