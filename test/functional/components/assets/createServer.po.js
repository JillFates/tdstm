'use strict';
var CreateServerModal = function(){
  this.createServerTitle = element(by.id('ui-id-1'));
  this.nameLabel = $('[class="label C highField"] [for="assetName"]');
  this.nameField = element(by.id('assetName'));
  this.saveCloseBtn = $('input[value="Save/Close"]');
  this.saveShowBtn = $('[data-redirect][onclick="saveToShow($(this),\'AssetEntity\')"]');
  this.createAppModal = $('[aria-labelledby="ui-id-1"]');
  this.viewModal = $('[aria-labelledby="ui-id-2"]');
  this.addCommentBtn = $('[aria-labelledby="ui-id-2"] [src="/tdstm/icons/comment_add.png"]');
  this.addTaskBtn = $('[aria-labelledby="ui-id-2"] [src="/tdstm/i/db_table_light.png"]');
  this.closeViewModalBtn = $('[aria-labelledby="ui-id-2"] .ui-dialog-titlebar-close');

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
  
  this.createServer = function(appName,closeView){
    //needs to be open the create server modal
    // var that = this;
    var field = this.nameField;
    field.sendKeys(appName);
    expect(field.getAttribute('value')).toEqual(appName);
    if(closeView ==='close'){
      this.saveCloseBtn.click();
    }else{
      this.saveShowBtn.click();
      expect(this.isViewModalOpened()).toBe(true);
    }
  };

};
module.exports = CreateServerModal;