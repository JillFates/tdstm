'use strict';
var ApplicationModal = function(){
  this.createAppTitle = element(by.id('ui-id-1'));
  // this.nameLabel = $('[class="label C  highField"] [for="assetName"]');
  this.createAppModal = $('[aria-labelledby="ui-id-1"]');
  this.nameLabel = $('[for="assetName"]');
  this.nameField = element(by.id('assetName'));
  this.saveBtn = $('[onclick="saveToShow($(this),\'Application\')"]');
  this.typeLabel = $('[for="assetType"]');
  this.typeField = element(by.id('assetType'));
  this.boundlefield = element(by.id('moveBundle'));
  this.boundleOptions = this.boundlefield.$$('option');
  this.boundleSelected = this.boundlefield.$('option:checked');
  this.viewModal = $('[aria-labelledby="ui-id-2"]');
  this.viewAppTitle = element(by.id('ui-id-2'));
  this.editBtn = $('[onclick^="editEntity"]');
  this.deleteBtn = $('[onclick="return confirm(\'Are you sure?\');"]');
  this.addTaskCommentBtnList = $$('a[href^="javascript:createIssue"]');
  // this.addTaskBtn = $$('a[href^="javascript:createIssue"]')[0];
  // this.addCommentBtn = $$('a[href^="javascript:createIssue"]')[1];
  this.closeBtn = $('[aria-labelledby="ui-id-2"] .ui-dialog-titlebar-close');

  this.setName = function(name){
    this.nameField.sendKeys(name);
  };

  this.createApp = function(name){
    this.nameField.sendKeys(name);
    this.saveBtn.click();
  };
  
  this.isCreateModalClosed = function(){
    var that = this;
    return browser.wait(function(){
      return that.createAppModal.getAttribute('style').then(function(style){
        return style.search('display: none;') !== -1;
      });
    }).then(function(){
       return true;
    });
  };

  this.isCreateModalOpened = function(){
    var that = this;
    return browser.wait(function(){
      return that.createAppModal.getAttribute('style').then(function(style){
        return style.search('display: block;') !== -1;
      });
    }).then(function(){
       return true;
    });
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

};
module.exports = ApplicationModal;