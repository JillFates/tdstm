'use strict';
var ListApps = function(){
  this.titleh1Css = 'h1';
  this.createAppbtnCss = 'input[value="Create App"]';
  this.createServerBtnCss = '[onclick="createAssetDetails(\'assetEntity\')"]';
  this.searchNamefieldId = 'gs_assetName';
  this.appsOnListCss =  '[role="grid"] tbody tr.ui-widget-content';
  this.messageId = 'messageId';

  this.getTitle = function(){
    return $(this.titleh1Css);
  };
  this.clickOnCreateAppBtn = function(){
    $(this.createAppbtnCss).click();
  };
  this.clickOnCreateTaskBtn = function(){
    $(this.createServerBtnCss).click();
  };
  this.getCreateServerBtn = function(){
    return $(this.createServerBtnCss);
  };
  this.getConfirmMsg = function(){
    return element(by.id(this.messageId));
  };
  this.getSearchAppName = function(){
    return element(by.id(this.searchNamefieldId));
  };
  this.verifySearchResults = function(count){
    var that=this;
    var webdriver = require('selenium-webdriver');
    var d = webdriver.promise.defer();
    browser.wait(function(){
      return  $$(that.appsOnListCss).then(function(list){
        return list.length === count;
      });
    }).then(function(){
      $$(that.appsOnListCss).then(function(list){
        if(count>0){
          d.fulfill(list);
        }else{
          d.fulfill('No results found');
        }
      });
    });
    return d.promise;
  };
  this.clickOnCommentTaskIcon = function(appId){
    var addtaskCommentIcon =  $('#icon_'+appId+' a');
    addtaskCommentIcon.click();
  };
};
module.exports = ListApps;