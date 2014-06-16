'use strict';
var MenuHelper=require('./menu-helper.js').MenuHelper;
function ClientProjectHelper(){
// menu
  this.clientProjectMenu = '';
  this.titleh1Css = 'h1';
  this.createProjectbtnCss = 'input[value="Create Project"]';
  this.createProjecth1Css = 'h1';
  this.cpClientLabelCss = '[for="client"] b';
  this.cpClientsCss = '#clientId';
  this.cpClientsOptionsCss = '#clientId option';
  this.cpClientSelectedCss = '#clientId :selected';
  this.cpProjectNameLabelCss = '[for="name"] b';
  this.cpProjectNameFieldCss = '#name';
  this.cpWorkflowCodeLabelCss = '[for="workflowCode"]';
  this.cpWorkflowCodeListCss = '#workflowCode';
  this.cpWorkflowCodeListOptionCss = '#workflowCode option';
  this.cpProjectcodeLabelCss = '[for="projectCode"] b';
  this.cpProjectcodefieldCss = '#projectCode';
  this.cpprojectTypeLabelCss = '[for="projectType"] b';
  this.cpProjectTypeListCss = '#projectType';
  this.cpProjecttypesListOptionCss = '#projectType option';
  this.cpCompletionDateLabelCss = '[for="completionDate"] b';
  this.cpcompletionDatefieldCss = '#completionDateId';
  this.cpCommentlabelCss = '[for="comment"]';
  this.saveBtnCss = 'input[type="submit"][onclick="return validateDates();"]';
  this.messageCss = 'div.message';
}

ClientProjectHelper.prototype = {};                       

ClientProjectHelper.prototype.createProject = function createProject(){
  var menu = new MenuHelper();
  menu.selectopt('clientProject','listProjects');
  var title = browser.driver.findElement(by.css(this.titleh1Css));
  expect(title.getText()).toEqual('Project List - Active Projects');
  var createProjectbtn = browser.driver.findElement(by.css(this.createProjectbtnCss));
  createProjectbtn.click();

};

exports.ClientProjectHelper = ClientProjectHelper;