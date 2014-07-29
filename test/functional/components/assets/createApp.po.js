'use strict';
var CreateAppModal = function(){
  this.createAppTitle = element(by.id('ui-id-1'));
  this.nameLabel = $('[class="label C  highField"] [for="assetName"]');
  this.nameField = element(by.id('assetName'));
  this.saveCloseBtn = $('input[value="Save/Close"]');
  this.saveShowBtn = $('[data-redirect][onclick="saveToShow($(this),\'AssetEntity\')"]');
  this.createAppModal = $('[aria-labelledby="ui-id-1"]');
  this.viewModal = $('[aria-labelledby="ui-id-2"]');
  this.typeLabel = $('[for="assetType"]');
  this.typeField = element(by.id('assetType'));
  this.boundlefield = element(by.id('moveBundle'));
  this.boundleOptions = this.boundlefield.$$('option');
  this.boundleSelected = this.boundlefield.$('option:checked');

  this.setName = function(name){
    this.nameField.sendKeys(name);
  };

  this.createApp = function(name,save){
    this.nameField.sendKeys(name);
    if(save === 'close'){
      this.saveCloseBtn.click();
    }else {
      this.saveShowBtn.click();
    }
  };
  
  this.isCreateModalClosed = function(){
    var that = this;
    return browser.wait(function(){
      return that.createAppModal.getAttribute('style').then(function(style){
        return  style === 'position: absolute; height: auto; width: auto; top: 0px; left: 0px; display: none;';
      });
    }).then(function(){
       return true;
    });
  };

  this.isCreateModalOpened = function(){
    var that = this;
    return browser.wait(function(){
      return that.createAppModal.getAttribute('style').then(function(style){
        return  style === 'position: absolute; height: auto; width: auto; top: 0px; left: 0px; display: block;';
      });
    }).then(function(){
       return true;
    });
  };

  this.isViewModalOpened = function(){
    var that = this;
    return browser.wait(function(){
      return that.viewModal.getAttribute('style').then(function(style){
        return  style === 'position: absolute; height: auto; width: auto; top: 0px; left: 3px; display: block;';
      });
    }).then(function(){
       return module;
    });
  };
};
module.exports = CreateAppModal;