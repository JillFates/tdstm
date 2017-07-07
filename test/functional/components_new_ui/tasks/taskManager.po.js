'use strict';

var TaskManager = function(){

	//menu text to determine Event
	this.projEventUser = $('#nav-project-name');
	//this.eventUserTxt = projEventUser.getText().substr(projEventUser.getText().indexOf(":") + 1);
	//Layout elements
	this.headerTitle =  $('h1');
	this.controlRowElements = $$('#controlRowId');
	this.controlRow	= $('#controlRowId');
	this.eventLb = null; // TODO Edit after FE solve the label issue
	this.moveEventDD = $('#moveEventId');
  this.moveEventDDOptions = $$('#moveEventId option');
  this.moveEventDDSelected = $('#moveEventId option:checked');
	this.breadCrumb = $('[class="breadcrumb"]');
	this.justRemainingCB = $('#justRemainingCB');
	this.justRemainingLb = $('[for="justRemainingCB"]');
	this.justMyTasksCB = $('#justMyTasksCB');
	this.justMyTasksLb = $('[for="justMyTasksCB"]');
	this.viewUnpublishedCB = $('#viewUnpublishedCB');
	this.viewUnpublishedLb = $('[for="viewUnpublishedCB"]');
	this.viewTaskGraphBt = $('#viewtaskgraph_button_graph');
	this.viewTaskGraphBtLb = $('#viewtaskgraph_text_graph');
	this.viewTaskGraphBtImg = this.viewTaskGraphBt.$('[ng-show="icon != \'\'"]');
	this.viewTimeLineBt = $('#viewtimeline_button_timeline');
	this.viewTimeLineBtLb = $('#viewtimeline_text_timeline');
	this.viewTimeLineBtImg = this.viewTimeLineBt.$('[ng-show="icon != \'\'"]');
	this.timeRefreshBt = $('[class="col-xs-4 item-wrapper refresh-button"]');
	this.selectTimedBarDD = $('#selectTimedBarId');
  this.selectTimedBarDDOptions = $$('#selectTimedBarId option');
  this.selectTimedBarDDSelected = $('#selectTimedBarId option:checked');
	// Action Buttons
  this.tasksLb = null; // TODO Edit after FE solve the label issue
  this.createTaskBt = $('#createtask_button_createTask');
	this.createTaskBtLb = $('#createtask_text_createTask');
	this.bulkEditBt = $('#bulkedit_button_bulkEdit');
	this.bulkEditBtLb = $('#bulkedit_text_bulkEdit');
	this.clearFiltersBt = $('#clearfilters_button_clearFilters');
	this.clearFiltersBtLb = $('#clearfilters_text_clearFilters');
	this.tableToggleBt = $('[class="ui-jqgrid-titlebar-close HeaderButton"]');
	// Table Header
	this.actionTCol = $('#taskListIdGrid_act');
	this.actionTColLb = $('#jqgh_act');
	this.taskTCol = $('#taskListIdGrid_taskNumber');
	this.taskTColLb = $('#jqgh_taskNumber');
	this.taskTColFlt = $('#gs_taskNumber');
	this.descriptionTCol = $('#taskListIdGrid_comment');
	this.descriptionTColLb = $('#jqgh_comment');
	this.descriptionTColFlt = $('#gs_comment');
	this.updatedTCol = $('#taskListIdGrid_updated');
	this.updatedTColLb = $('#jqgh_updated');
	this.dueDateTCol = $('#taskListIdGrid_dueDate');
	this.dueDateTColLb = $('#jqgh_dueDate');
	this.dueDateTColFlt = $('#gs_dueDate');
	this.statusTCol = $('#taskListIdGrid_status');
	this.statusTColLb = $('#jqgh_status');
	this.statusTColFlt = $('#gs_status');
	this.sucTCol = $('#taskListIdGrid_suc');
	this.sucTColLb = $('#jqgh_suc');
	this.scoreTCol = $('#taskListIdGrid_score	');
	this.scoreTColLb = $('#jqgh_score');
	this.customColumnTCol = $('#taskListIdGrid_custom'); // TODO Check and obtain custom columns for validate them
	this.customColumnTColLb  = $('#jqgh_custom');// TODO Check and obtain custom columns for validate them
	this.customColumnTColFlt = $('#gs_custom'); // TODO Check and obtain custom columns for validate them
//Table Footer
	this.tableRefreshBt = $('#taskListId');

	
	TaskManager.prototype = {};
	
	TaskManager.prototype.existElement = function(elem) {
	  var that = this;
	  return browser.wait(function () {
	    return that.elem.isPresent().then(function (valor) {
	      return valor;
	    });
	  }).then(function () {
	    return true;
	  });
	};
	
	TaskManager.prototype.isDisplayedElement = function(elem) {
	  var that = this;
	  return browser.wait(function () {
	    return that.elem.isDisplayed().then(function (value) {
	      return value;
	    });
	  }).then(function () {
	    return true;
	  });
	};

	TaskManager.prototype.clickElement = function(elem) {
	  var that = this;
	  return browser.wait(function () {
	    return elem.click().then(function (value) {
	      return value;
	    });
	  }).then(function () {
	    return true;
	  });
	};
	
/* traidas de Cookbook	
  this.createRecipeBtn = $('#generateTask[ng-click="createRecipe()"]');
  this.createModal = $('[class="modal fade in"]');
  this.modalTitleCss = this.createModal.$('h3');
  this.modalTabs = this.createModal.$$('.nav-tabs li');
  this.modalButtons = this.createModal.$$('.modal-footer button');
  this.nameField = this.createModal.$('#inputName');
  this.nameLabel = this.createModal.$('[for="inputName"]');
  this.descriptionField = this.createModal.$('#textareaDescription');
  this.descriptionLabel = this.createModal.$('[for="textareaDescription"]');
  this.contextLabel = this.createModal.$('[for="contextSelector2"]');
  this.contextField = this.createModal.$('#contextSelector2');
  this.contextOptions = this.createModal.$$('#contextSelector2 option');
  this.contextOptionSelected = this.createModal.$('#contextSelector2 option:checked');

  this.pageTabs = $$('#mainTabset li');
  //EditorTab
  this.editEditorBtn = $('[ng-click="showEditPopup()"]');
  this.saveWipBtn = $('[ng-click="saveWIP()"]');
  this.undoBtn =$('[ng-click="cancelChanges()"]');

  this.releaseBtn = $('[ng-click="releaseVersion()"]');
  this.discardWipBtn = $('[ng-click="discardWIP()"]');
  
  this.diffBtn = $('[ng-click="diff()"]');

  this.checkSyntaxBtn = $('[ng-click="validateSyntax()"]');
  this.syntaxErrorTab = $('[class="syntaxErrors ng-scope"]');
  this.checkSyntaxErrorTitle = this.syntaxErrorTab.$('[ng-bind="error.reason"]');
  this.checkSyntaxErrorDetails = this.syntaxErrorTab.$('[ng-bind-html="secureHTML(error.detail)"]');
  this.loadingIndicator = $('loading-indicator [ng-show="isLoading"]');
  //Editor Modal
  this.editorModal = $('[class="modal fade code-editor-modal in"]');
  this.editorTextAreaX = this.editorModal.$('[ng-model="modal.sourceCode"]');
  this.editorTextArea = $$('[ng-model="modal.sourceCode"] .CodeMirror-code pre');
  this.modalCloseBtn = $('[ng-click="storeCode()"]');

  //Task Generation tab
  this.eventLabel = $('[for="eventSelect"]');
  this.eventOptions = $$('#eventSelect option');
  this.eventSelected = $('#eventSelect option:checked');

  this.setAsDefaultLink = $('#setDefaultContext');
  this.clearDefaultLink = $('#clearDefaultContext');

  this.autoPublishTaskLabel = $('[for="autoPublishTasks"]');
  this.autoPublishTaskCheck = $('#autoPublishTasks');

  this.generateUsingWipLabel = $('[for="generateUsingWIP"]');
  this.generateUsingWipCheckBox = $('#generateUsingWIP');

  this.generateTasksBtn = $('[ng-click="tasks.generateTask(this)"]');
  this.errorModal = $('#errorModal');
  this.errorModalText = this.errorModal.$('#errorModalText');
  this.errorModalCloseBtn = this.errorModal.$('[class="btn btn-default"]');
  
  this.taskGenerationTabs = $('#taskGenerationTabs');
  //task Generation Tabs
  this.sumaryList = $('[ui-view="taskBatchCompleted"] [class="summaryList ng-scope"]');
*/

};

	
module.exports = TaskManager;