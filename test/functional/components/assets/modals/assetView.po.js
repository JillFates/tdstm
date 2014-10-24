'use strict';
var ViewAsset = function(){
  this.viewModal = $('[aria-labelledby="ui-id-2"]');
  this.viewModalTitle = this.viewModal.element(by.id('ui-id-2'));
  this.nameLabel = this.viewModal.$('[for="assetName"]');
  this.typeLabel = this.viewModal.$('[for="assetType"]');
  this.viewAppTitle = this.viewModal.element(by.id('ui-id-2'));
  this.editBtn = this.viewModal.$('[onclick^="EntityCrud.showAssetEditView"]');
  this.deleteBtn = this.viewModal.$('[onclick="return confirm(\'Are you sure?\');"]');
  this.addCommentBtn = this.viewModal.$('[src="/tdstm/icons/comment_add.png"]');
  this.addTaskBtn = this.viewModal.$('[src="/tdstm/i/db_table_light.png"]');
  this.addTaskCommentBtnList = this.viewModal.$$('a[href^="javascript:createIssue"]');
  this.closeBtn = this.viewModal.$('.ui-dialog-titlebar-close');
  this.closeViewModalBtn = this.viewModal.$('.ui-dialog-titlebar-close');
};
ViewAsset.prototype ={};
ViewAsset.prototype.isViewModalClosed = function(){
  var that = this;
  return browser.wait(function(){
    return that.viewModal.getAttribute('style').then(function(style){
      return style.search('display: none;') !== -1;
    });
  }).then(function(){
     return true;
  });
};
ViewAsset.prototype.isViewModalOpened = function(){
  var that = this;
  return browser.wait(function(){
    return that.viewModal.$('div.dialog').isPresent().then(function(valor){
      return valor;
    });
  }).then(function(){
     return true;
  });
};
module.exports = ViewAsset;