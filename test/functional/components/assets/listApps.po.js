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

  this.clickOnCommentIcon = function(appId){
    var addCommentIcon =  $('#icon_comment_'+appId);
    addCommentIcon.click();
  };
  
  this.getCommentIcon = function(appId){
    return $('#icon_comment_'+appId +' img');
  };

};
module.exports = ListApps;