'use strict';
var CreateServerModal = function(){
  // this.modalTitleId = 'ui-id-1';
  this.createServerTitle = element(by.id('ui-id-1'));
  this.nameLabelCss = '[class="label C highField"] [for="assetName"]';
  this.nameFieldId = 'assetName';
  this.saveCloseBtnCss = 'input[value="Save/Close"]';
  this.saveShowBtnCss = '[data-redirect][onclick="saveToShow($(this),\'AssetEntity\')"]';
  this.createAppModalCss = '[aria-labelledby="ui-id-1"]';
  this.viewModalCss = '[aria-labelledby="ui-id-2"]';
  this.addCommentBtnCss = '[aria-labelledby="ui-id-2"] [src="/tdstm/icons/comment_add.png"]';
  this.addTaskBtnCss = '[aria-labelledby="ui-id-2"] [src="/tdstm/i/db_table_light.png"]';
  this.closeViewModalCss = '[aria-labelledby="ui-id-2"] .ui-dialog-titlebar-close';


  // this.getTitle = function(){
  //   return element(by.id(this.modalTitleId));
  // };

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
        return style.search('display: none;') !== -1;
      });
    }).then(function(){
       return true;
    });
  };

  this.isCreateModalOpened = function(){
    var that = this;
    return browser.wait(function(){
      return $(that.createAppModalCss).getAttribute('style').then(function(style){
        return style.search('display: block;') !== -1;
      });
    }).then(function(){
       return true;
    });
  };

  this.isViewModalOpened = function(){
    var that = this;
    return browser.wait(function(){
      return $(that.viewModalCss).getAttribute('style').then(function(style){
        return style.search('display: block;') !== -1;
      });
    }).then(function(){
       return true;
    });
  };

  this.isViewModalClosed = function(){
    var that = this;
    return browser.wait(function(){
      return $(that.viewModalCss).getAttribute('style').then(function(style){
        return style.search('display: none;') !== -1;
      });
    }).then(function(){
       return true;
    });
  };
  this.clickAddComment = function(){
    $(this.addCommentBtnCss).click();
  };
  this.clickAddTask = function(){
    $(this.addTaskBtnCss).click();
  };
  this.closeViewModal = function(){
    $(this.closeViewModalCss).click();
  };
  
  this.createServer = function(appName,closeView){
    //needs to be open the create server modal
    // var that = this;
    var field = this.getNameField();
    field.sendKeys(appName);
    expect(field.getAttribute('value')).toEqual(appName);
    if(closeView ==='close'){
      this.clickSaveCloseBtn();
    }else{
      this.clickSaveShowBtn();
      expect(this.isViewModalOpened()).toBe(true);
    }
  };
};
module.exports = CreateServerModal;