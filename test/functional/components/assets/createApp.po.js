'use strict';
var CreateAppModal = function(){
  this.creatAppModalTitleId = 'ui-id-1';
  this.nameLabelCss = '[class="label C  highField"] [for="assetName"]';
  this.nameFieldId = 'assetName';
  this.saveCloseBtnCss = 'input[value="Save/Close"]';
  this.saveShowBtnCss = '[data-redirect][onclick="saveToShow($(this),\'AssetEntity\')"]';
  this.createAppModalCss = '[aria-labelledby="ui-id-1"]';
  this.viewModalCss = '[aria-labelledby="ui-id-2"]';

  this.getTitle = function(){
      return element(by.id(this.creatAppModalTitleId));
  };

  this.getNameLabel = function(){
    return $(this.nameLabelCss);
  };

  this.getNameField = function(){
    return element(by.id(this.nameFieldId));
  };

  this.clickSaveCloseBtn = function(){
    $(this.saveCloseBtnCss).click();
  };
  this.clickSaveShowBtn = function(){
    $(this.saveShowBtnCss).click();
  };
  this.getSaveShowBtn = function(){
    return $(this.saveShowBtnCss);
  };

  this.isCreateModalClosed = function(){
    var that = this;
    return browser.wait(function(){
      return $(that.createAppModalCss).getAttribute('style').then(function(style){
        return  style === 'position: absolute; height: auto; width: auto; top: 0px; left: 0px; display: none;';
      });
    }).then(function(){
       return true;
    });
  };

  this.isCreateModalOpened = function(){
    var that = this;
    return browser.wait(function(){
      return $(that.createAppModalCss).getAttribute('style').then(function(style){
        return  style === 'position: absolute; height: auto; width: auto; top: 0px; left: 0px; display: block;';
      });
    }).then(function(){
       return true;
    });
  };

  this.isViewModalOpened = function(){
    var that = this;
    return browser.wait(function(){
      return $(that.viewModalCss).getAttribute('style').then(function(style){
        return  style === 'position: absolute; height: auto; width: auto; top: 0px; left: 3px; display: block;';
      });
    }).then(function(){
       return true;
    });
  };
};
module.exports = CreateAppModal;