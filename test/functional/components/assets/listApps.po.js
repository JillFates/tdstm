'use strict';
var ListApps = function(){
  this.titleh =  $('h1');
  this.createAppBtn = $('input[value="Create App"]');
  this.createServerBtn = $('[onclick="createAssetDetails(\'assetEntity\')"]');
  this.searchNamefield = element(by.id('gs_assetName'));
  this.appsOnList =  $$('[role="grid"] tbody tr.ui-widget-content');
  this.confirmMsg = $('div#messageId');
  
  this.verifySearchResults = function(count){
    var that=this;
    var webdriver = require('selenium-webdriver');
    var d = webdriver.promise.defer();
    browser.wait(function(){
      return  that.appsOnList.then(function(list){
        // console.log('bla',list.length === count);
        return list.length === count;
      });
    }).then(function(){
        that.appsOnList.then(function(list){
        if(count>0){
          d.fulfill(list);
        }else{
          d.fulfill('No results found');
        }
      });
    });
    return d.promise;
  };

  this.clickOnTaskIcon = function(appId){
    var addtaskIcon =  $('#icon_task_'+appId);
    addtaskIcon.click();
  };

  this.isTasksIconDisplayed = function(appId){
    var that = this;
    return browser.wait(function(){
      return that.getTaskIcon(appId).getAttribute('src').then(function(ico){
        return ico === process.env.BASE_URL+'/tdstm/icons/table_multiple.png';
      });
    }).then(function(){
      return true;
    });
  };
  this.isAddTaskIconDisplayed = function(appId){
    var that = this;
    return browser.wait(function(){
      return that.getTaskIcon(appId).getAttribute('src').then(function(ico){
        return ico === process.env.BASE_URL+'/tdstm/icons/table_add.png';
      });
    }).then(function(){
      return true;
    });
  };
  this.getTaskIcon = function(appId){
    return $('#icon_task_'+appId +' img');
  };
  
  this.clickOnCommentIcon = function(appId){
    var addCommentIcon =  $('#icon_comment_'+appId);
    addCommentIcon.click();
  };
  
  this.getCommentIcon = function(appId){
    return $('#icon_comment_'+appId +' img');
  };

  this.isCommentsIconDisplayed = function(appId){
    var that = this;
    return browser.wait(function(){
      return that.getCommentIcon(appId).getAttribute('src').then(function(ico){
        return ico === process.env.BASE_URL+'/tdstm/icons/comments.png';
      });
    }).then(function(){
      return true;
    });
  };

  this.isAddCommentIconDisplayed = function(appId){
    var that = this;
    return browser.wait(function(){
      return that.getCommentIcon(appId).getAttribute('src').then(function(ico){
        return ico === process.env.BASE_URL+'/tdstm/icons/comment_add.png';
      });
    }).then(function(){
      return true;
    });
  };
  
};
module.exports = ListApps;