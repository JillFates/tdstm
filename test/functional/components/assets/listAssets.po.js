'use strict';
var ListAssets = function(){
  this.titleh =  $('h1');
  this.searchNamefield = element(by.id('gs_assetName'));
  this.confirmMsg = $('div#messageId');
  this.storageLoadingId = 'load_storageIdGrid';
  this.dependencyLoadingId ='load_dependencyGridIdGrid';
};

ListAssets.prototype = {};

ListAssets.prototype.getColumnsHeaders = function() {
  return browser.executeScript('return $("[role=\'grid\'] [class=\'ui-jqgrid-labels ui-sortable\'] th:visible")');
};

ListAssets.prototype.getColumnsWithHeaderSelector = function() {
  return browser.executeScript('return $("[role=\'grid\'] [class=\'ui-jqgrid-labels ui-sortable\'] th:visible img")');
};

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