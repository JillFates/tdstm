'use strict';

var TaskManager = function(){

	//menu text to determine Event
	this.projEventUser = $('#nav-project-name');
	//this.eventUserTxt = projEventUser.getText().substr(projEventUser.getText().indexOf(":") + 1);
	//-START Layout elements
	this.headerTitle =  $('h1');
	this.controlRowElements = $$('#controlRowId');
	this.controlRow	= $('#controlRowId');
	this.eventLb = $('[for="lbl-task-event-title"]');
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
	//-END of Layout Elements
	
	//-START Functionality
  // Create Task
  this.createTaskModal = $('[class="modal fade modal-task in"]');
  this.createTaskTitle = this.createTaskModal.$('ui-id-5');
  this.taskNameLabel = this.createTaskModal.$('[for="taskName"]');
  
  // TODO For task modal window features

	//-END Functionality
	
	
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
	
};

	
module.exports = TaskManager;