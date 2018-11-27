'use strict';
var ListAssets = function(){
  this.titleLoc = by.tagName('h1'); // assign ID
  this.filterAssetNameLoc = by.id('gs_assetName');
  this.messageLoc = by.id('messageId');
  this.storageLoadingLoc = by.id('load_storageIdGrid');
  this.dependencyLoadingLoc = by.id('load_dependencyGridIdGrid');
	this.labelRowLoc = by.className('ui-jqgrid-labels');
	this.labelHeaderLoc = by.css('.ui-th-column:visible'); // assign class in BEM so 'visible' is a --modifier
	this.customizeSelectLoc = by.className('customizeSelect');

	this.assetRowLoc = by.className('ui-row-ltr');

	this.getMessage = function() {
		return element(this.messageLoc).getText();
	};

	this.getColumnHeaders = function() {
	  return element(this.labelRowLoc).all(this.labelHeaderLoc);
	};

	this.getCustomizeSelects = function() {
	  return this.getColumnHeaders().all(this.customizeSelectLoc).then(function(list) {
      return list.length;
    });
	};

};

ListAssets.prototype = {};

ListAssets.prototype

ListAssets.prototype.clickOnTaskIcon = function(appId){
  var addtaskIcon =  $('#icon_task_'+appId);
  addtaskIcon.click();
};

ListAssets.prototype.isTasksIconDisplayed = function(appId){
  var that = this;
  return browser.wait(function(){
    return that.getTaskIcon(appId).getAttribute('src').then(function(ico){
      return ico === process.env.BASE_URL+'/tdstm/icons/table_multiple.png';
    });
  }).then(function(){
    return true;
  });
};

ListAssets.prototype.isAddTaskIconDisplayed = function(appId){
  var that = this;
  return browser.wait(function(){
    return that.getTaskIcon(appId).getAttribute('src').then(function(ico){
      return ico === process.env.BASE_URL+'/tdstm/assets/icons/table_add.png';
    });
  }).then(function(){
    return true;
  });
};

ListAssets.prototype.getTaskIcon = function(appId){
  return $('#icon_task_'+appId +' img');
};

ListAssets.prototype.clickOnCommentIcon = function(appId){
  var addCommentIcon =  $('#icon_comment_'+appId);
  addCommentIcon.click();
};

ListAssets.prototype.getCommentIcon = function(appId){
  return $('#icon_comment_'+appId +' img');
};

ListAssets.prototype.isCommentsIconDisplayed = function(appId){
  var that = this;
  return browser.wait(function(){
    return that.getCommentIcon(appId).getAttribute('src').then(function(ico){
      return ico === process.env.BASE_URL+'/tdstm/images/comment_exists.png';
      // return ico === process.env.BASE_URL+'/tdstm/icons/comments.png';
    });
  }).then(function(){
    return true;
  });
};

ListAssets.prototype.isAddCommentIconDisplayed = function(appId){
  var that = this;
  return browser.wait(function(){
    return that.getCommentIcon(appId).getAttribute('src').then(function(ico){
      return ico === process.env.BASE_URL+'/tdstm/icons/comment_add.png';
    });
  }).then(function(){
    return true;
  });
};

module.exports = ListAssets;
