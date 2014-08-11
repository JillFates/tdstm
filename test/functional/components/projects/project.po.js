'use strict';
var Project = function(){
  this.titleh1Css = 'h1';
  this.clientLabelCss = '[for="client"] b';
  this.clientsId = 'clientId';
  this.clientsOptionsCss = '#clientId option';
  this.clientSelectedCss = '#clientId :selected';
  this.projectNameLabelCss = '[for="name"] b';
  this.projectNameFieldId = 'name';
  this.workflowCodeLabelCss = '[for="workflowCode"]';
  this.workflowCodeListId = 'workflowCode';
  this.workflowCodeListOptionCss = '#workflowCode option';
  this.projectCodeLabelCss = '[for="projectCode"] b';
  this.projectCodeFieldId = 'projectCode';
  this.projectTypeLabelCss = '[for="projectType"] b';
  this.projectTypeSelectId = 'projectType';
  this.projectTypesOptionCss = '#projectType option';
  this.completionDateLabelCss = '[for="completionDate"] b';
  this.completionDateId = 'completionDateId';
  this.commentLabelCss = '[for="comment"]';
  this.commentCss = 'textarea[name="comment"]';
  this.saveBtnCss = 'input[type="submit"][onclick="return validateDates();"]';
  this.messageCss = '.message';

  //project/show
  this.deleteProjectbtnCss = 'input[value="Delete"]';


  this.getTitle = function(){
    return browser.driver.findElement(by.css(this.titleh1Css));
  };
  this.getClientFieldLabel = function(){
    return browser.driver.findElement(by.css(this.clientLabelCss));
  };
  this.selectAClient = function(name){
    var selector = this.clientsOptionsCss + '[value="' + name + '"]';
    browser.driver.findElement(by.css(selector)).click();
  };
  this.getclientSelect = function(){
    return browser.driver.findElement(by.id(this.clientsId));
  };
  this.getClientSelected = function(){
    return browser.driver.executeScript('return $("'+this.clientSelectedCss+'").text()');
  };

  this.getNameFieldLabel = function(){
    return browser.driver.findElement(by.css(this.projectNameLabelCss));
  };
  this.setProjectName = function(name){
   browser.driver.findElement(by.id(this.projectNameFieldId)).sendKeys(name);
  };
  this.getProjectName = function(){
    return browser.driver.findElement(by.id(this.projectNameFieldId));
  };
  this.getWorkflowCodeLabel = function(){
    return browser.driver.findElement(by.css(this.workflowCodeLabelCss));
  };
  this.selectWorkflowCodeByName = function(name){
    var selector = this.workflowCodeListOptionCss + '[value="' + name + '"]';
    return browser.driver.findElement(by.css(selector));
  };
  this.getWorkflowSelected = function(){
    return browser.driver.findElement(by.id(this.workflowCodeListId));
  };
  this.getProjectCodeLabel = function(){
    return browser.driver.findElement(by.css(this.projectCodeLabelCss));
  };
  this.setProjectCode = function(pCode){
    browser.driver.findElement(by.id(this.projectCodeFieldId)).sendKeys(pCode);
  };
  this.getProjectCode = function(){
    return browser.driver.findElement(by.id(this.projectCodeFieldId));
  };
  this.getProjectTypeLabel = function(){
    return browser.driver.findElement(by.css(this.projectTypeLabelCss));
  };
  this.getProjectTypeOption = function(){
    return browser.driver.findElements(by.css(this.projectTypesOptionCss));
  };
  this.selectProjectType = function(pos){
    this.getProjectTypeOption().then(function(options){
      options[pos].click();
    });
  };
  this.getProjectCodeSelected = function(){
    return browser.driver.findElement(by.id(this.projectTypeSelectId));
  };
  this.getComplationDateLabel = function(){
    return browser.driver.findElement(by.css(this.completionDateLabelCss));
  };
  this.setCompletionDate = function(d){
    var cd = browser.driver.findElement(by.id(this.completionDateId));
    cd.clear();
    cd.sendKeys(d);
  };
  this.getCompletionDate = function(){
    return browser.driver.findElement(by.id(this.completionDateId));
  };
  this.getCommentLabel = function(){
    return browser.driver.findElement(by.css(this.commentLabelCss));
  };
  this.setComment = function(comment){
    browser.driver.findElement(by.css(this.commentCss)).sendKeys(comment);
  };
  this.getComment =  function(){
    return browser.driver.findElement(by.css(this.commentCss));
  };
  this.save = function(){
    browser.driver.findElement(by.css(this.saveBtnCss)).click();
  };
  this.getConfirmMsg =function(){
    return browser.driver.executeScript('return $("'+this.messageCss+'").text()');
  };

  this.deleteCurrentProject = function(){
    browser.driver.findElement(by.css(this.deleteProjectbtnCss)).click();
  };

};
module.exports = Project;