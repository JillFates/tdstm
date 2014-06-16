'use strict';
var MenuHelper=require('./menu-helper.js').MenuHelper;

function AssetsHelper(){
  //app list page
  this.createAppbtnCss = 'input[value="Create App"]';
  this.titleh1Css = 'h1';
  this.searchNamefieldCss = 'input[name="assetName"]';
  this.appsOnListCss =  '#applicationIdGrid tbody tr.ui-widget-content';
  // this.addtaskCommentIconCss = '#'+appId+' #icon_'+appId+' a';

  this.creatAppModalTitleCss = '#ui-id-1';
  this.nameLabelCss = '[aria-labelledby="ui-id-1"] [for="assetName"]';
  this.nameFieldCss = '#assetName';
  this.saveCloseBtnCss = 'input[value="Save/Close"]';
  this.messageCss = '.message[style=""]';
  this.createAppModalCss = '[aria-labelledby="ui-id-1"]';
// comments
  this.createTaskPopupCss = '#createTaskPopup';
  this.createTaskTitleCss= this.createTaskPopupCss +' #ui-id-5';
  this.editTaskPopupCss = '#editTaskPopup';
  this.showTaskPopupCss ='#showTaskPopup';
  this.createCommentPopupCss ='#createCommentPopup';
  this.editCommentPopupCss = '#editCommentPopup';
  this.showCommentPopup = '#showCommentPopup';
  this.listCommentsTasksPopupCss ='#listCommentsTasksPopup';
  this.listcommentTaskTitleCss = this.listCommentsTasksPopupCss+' #ui-id-5';
// Tasks
  this.commentLabelCss = '#issueItemId label';
  this.commentRequiredCss= '#issueItemId span' ;
  this.commentTextAreaCss = '#comment';
  this.personTeamLabelCss ='#assignedToId label[for="assignedTo"]';
  this.fixedAssignedLabelCss = '#assignedToId label[for="hardAssigned"]';
  this.fixedAssignedCheckCss= '#assignedToId #hardAssigned';
  this.personDropdownCss = '#assignedToId #assignedTo';
  this.personOptionsCss = '#assignedToId #assignedTo option';
  this.personOptionSelectedCss = this.personOptionsCss+'[selected]';
  this.teamDropdownCss =  '#assignedToId #roleType';
  this.teamOptionsCss =  '#assignedToId #roleType option';
  this.teamOptionsSelectedCss= this.teamOptionsCss+'[selected]';






}

AssetsHelper.prototype={};

AssetsHelper.prototype.goToApplicationList = function goToApplicationList(){
  var menu = new MenuHelper();
  menu.selectopt('assets','listApps');
  expect($(this.titleh1Css).getText()).toEqual('Application List');
};

AssetsHelper.prototype.goToCreateApplicationModal = function goToCreateApplicationModal(){
  var assets = new AssetsHelper();
  $(this.createAppbtnCss).click();
  browser.wait(function(){
      return $(assets.createAppModalCss).getAttribute('style').then(function(style){
        return  style === 'position: absolute; height: auto; width: auto; top: 0px; left: 0px; display: block;';
      });
  }).then(function(){
    expect($(assets.creatAppModalTitleCss).getText()).toEqual('Create Application');
  });
};

AssetsHelper.prototype.saveCloseCreateApp = function saveCloseCreateApp(){
  var assets = new AssetsHelper();
  $(assets.saveCloseBtnCss).click();
  browser.wait(function(){
      return $(assets.createAppModalCss).getAttribute('style').then(function(style){
        return  style === 'position: absolute; height: auto; width: auto; top: 0px; left: 0px; display: none;';
      });
  }).then(function(){
    expect($(assets.createAppModalCss).getAttribute('style')).toEqual('position: absolute; height: auto; width: auto; top: 0px; left: 0px; display: none;');  
  });
};


AssetsHelper.prototype.searchAppByName = function searchAppByName(name){
  var field = $(this.searchNamefieldCss);
  field.sendKeys(name);
  expect(field.getAttribute('value')).toEqual(name);
};


AssetsHelper.prototype.verifySearchResultsReturnId = function verifySearchResultsReturnId(exists,details){
  var webdriver = require('selenium-webdriver');
  var d = webdriver.promise.defer();
  var assets = new AssetsHelper();
  var option =0;
  if(exists){
    option=1;
  }
  browser.driver.wait(function() {
    return  $$(assets.appsOnListCss).then(function(list){
            return list.length ===option;
          });
  }).then(function(){
    $$(assets.appsOnListCss).then(function(list){
      expect(list.length).toEqual(option);
      if(exists){
        // expect(list[0].getText()).toEqual(details);
        list[0].getAttribute('id').then(function(text){
          d.fulfill(text);
        });
      }else{
        d.fulfill();
      }
    });
  });
  return d.promise;
};


AssetsHelper.prototype.clickOnCommentTaskIcon = function clickOnCommentTaskIcon(firstTime,type,name,appId){
  // 2 op 0 no task.comment created -> opene created task modal
  // else open list and then pick to add a comment or a task 
  var assets = new AssetsHelper();
  var addtaskCommentIcon =  $('#icon_'+appId+' a');
  addtaskCommentIcon.click();
  if(firstTime){
    expect($(assets.createTaskTitleCss).getText()).toEqual('Create Task');
  }else{
    expect($(assets.listcommentTaskTitleCss).getText()).toEqual('Show Comments & Tasks: '+type+'\/'+name);

  }
};

exports.AssetsHelper = AssetsHelper;