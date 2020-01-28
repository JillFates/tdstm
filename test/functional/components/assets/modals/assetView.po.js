'use strict';
var ViewAsset = function(){
  this.viewModal = $('[aria-describedby="showEntityView"]');
  this.viewAppTitle = this.viewModal.element(by.css('.ui-dialog-title'));
  this.nameLabel = this.viewModal.$('[for="assetName"]');
  this.typeLabel = this.viewModal.$('[for="assetType"]');
  this.editBtn = this.viewModal.$('[onclick^="EntityCrud.showAssetEditView"]');
  this.deleteBtn = this.viewModal.$('[onclick="return confirm(\'Are you sure?\');"]');
  this.addCommentBtn = this.viewModal.$('[src="/tdstm/static/icons/comment_add.png"]');
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
  },8000).then(function(){
     return true;
  });
};
ViewAsset.prototype.isViewModalOpened = function(){
  var that = this;
  return browser.wait(function(){
    return that.viewModal.$('div.dialog').isPresent().then(function(valor){
      return valor;
    });
  },8000).then(function(){
     return true;
  });
};
module.exports = ViewAsset;