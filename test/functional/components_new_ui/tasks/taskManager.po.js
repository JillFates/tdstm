'use strict';

var TaskManager = function(){

	// Locators
	
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
  this.tasksLb = $('[for="lbl-task-list-title"]');
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

	// Error Modal window
  this.errorModal = $('#errorModal');
  this.errorModalText = this.errorModal.$('#errorModalText');
  this.errorModalCloseBtn = this.errorModal.$('[class="btn btn-default"]');
	// End of error modal window
  
	// Create Task Modal Window
  this.ctModal = $('[class="modal fade modal-task in"]');
  this.ctModalTitle = this.ctModal.$('#ui-id-5');
  this.ctModalNameLb = this.ctModal.$('#commentEditTdId');
  this.ctModalNameErrMsg = this.ctModal.$('[class="error-msg"]');
  this.ctModalNameTA = this.ctModal.$('#commentEditId');
  this.ctModalPersTeamLb = this.ctModal.$('[for="assignedToEditTdId"]');
  this.ctModalPersDD = this.ctModal.$('#assignedTo');
  this.ctModalPersDDOptions = this.ctModal.$$('#assignedTo option');
  this.ctModalPersDDSelected = this.ctModal.$('#assignedTo option:checked');
  this.ctModalTeamDD = this.ctModal.$('#roleType');
  this.ctModalTeamDDOptions = this.ctModal.$$('#roleType option');
  this.ctModalTeamDDSelected = this.ctModal.$('#roleType option:checked');
	this.ctModalFixAssgCB = this.ctModal.$('#hardAssignedEdit');
	this.ctModalFixAssgLb = this.ctModal.$('[for="hardAssignedEdit"]');
	this.ctModalSendNotifCB = this.ctModal.$('#sendNotificationEdit');
	this.ctModalSendNotifLb = this.ctModal.$('[for="sendNotificationEdit"]');
  this.ctModalEventLb = this.ctModal.$('[for="moveEvent"]');
  this.ctModalEventDD = this.ctModal.$('#moveEvent');
  this.ctModalEventDDOptions = this.ctModal.$$('#moveEvent option');
  this.ctModalEventDDSelected = this.ctModal.$('#moveEvent option:checked');
  this.ctModalCategoryLb = this.ctModal.$('[for="category"]');
  this.ctModalCategoryDD = this.ctModal.$('#category');
  this.ctModalCategoryDDOptions = this.ctModal.$$('#category option');
  this.ctModalCategoryDDSelected = this.ctModal.$('#category option:checked');
  this.ctModalAssetLb = this.ctModal.$('[for="asset"]');
  this.ctModalAssetTypeDD = this.ctModal.$('[ng-model="commentInfo.currentAssetClass"]');
  this.ctModalAssetTypeDDOptions = this.ctModal.$$('[ng-model="commentInfo.currentAssetClass"] option');
  this.ctModalAssetTypeDDSelected = this.ctModal.$('[ng-model="commentInfo.currentAssetClass"] option:checked');
  this.ctModalAssetNameDD = this.ctModal.$('#s2id_currentAsset');
  this.ctModalAssetNameDDOptions = this.ctModal.$$('#s2id_currentAsset option');
  this.ctModalAssetNameDDSelected = this.ctModal.$('#s2id_currentAsset option:checked');
  this.ctModalInstLinkLb = this.ctModal.$('[for="instructionsLink"]');
  this.ctModalInstLinkTF = this.ctModal.$('#instructionsLinkId');
  this.ctModalPriorityLb = this.ctModal.$('[for="priority"]');
  this.ctModalPriorityDD = this.ctModal.$('#priority');
  this.ctModalPriorityDDOptions = this.ctModal.$$('#priority option');
  this.ctModalPriorityDDSelected = this.ctModal.$('#priority option:checked');
  this.ctModalDueDateLb = this.ctModal.$('[for="dueDateEditSpanId"]');
  this.ctModalDueDateTF = this.ctModal.$('#dueDate');
  this.ctModalDueDateCal = this.ctModal.$('[class="k-icon k-i-calendar"]');
  this.ctModalEstDurLb = this.ctModal.$('[for="durationEditId"]');
  this.ctModalEstDurDaysTF = this.ctModal.$('[ng-model="durationpicker.day"]');
  this.ctModalEstDurDaysLb = this.ctModal.$('[class="duration_days duration_label"]');
  this.ctModalEstDurHourTF = this.ctModal.$('[ng-model="durationpicker.hour"]');
  this.ctModalEstDurHourLb = this.ctModal.$('[class="duration_hours duration_label"]');
  this.ctModalEstDurMinTF = this.ctModal.$('[ng-model="durationpicker.minutes"]');
  this.ctModalEstDurMinLb = this.ctModal.$('[class="duration_minutes duration_label"]');
  this.ctModalEstDurLock = this.ctModal.$('[ng-if="!ac.durationLocked"]');
  this.ctModalEstStFinLb = this.ctModal.$('[for="estStartEditTrId"]');
  this.ctModalEstStFinTF = this.ctModal.$('#estRange');
  this.ctModalStatusLb = this.ctModal.$('[for="status"]');
  this.ctModalStatusDD = this.ctModal.$('#status');
  this.ctModalDepLb = this.ctModal.$('[for="predecessorHeadTrId"]');
  //this.ctModalDepPredLb = this.ctModal.$('[for="predecessors"]');
  //this.ctModalDepPredBt = this.ctModal.$('[ng-click="$broadcast('addDependency','predecessor')"]');
  //this.ctModalDepPredType = this.ctModal.$('');
  //this.ctModalDepPredTsk = this.ctModal.$('');
  //this.ctModalDepPredDel = this.ctModal.$('[ng-click="deleteRow($index)"]');
  //this.ctModalDepSuccLb = this.ctModal.$('[for="successors"]');
  //this.ctModalDepSuccBt = this.ctModal.$('[ng-click="$broadcast('addDependency','successor')"]');
  //this.ctModalDepSuccType = this.ctModal.$('');
  //this.ctModalDepSuccTsk = this.ctModal.$(''); 
  //this.ctModalDepSuccTsk = this.ctModal.$('[ng-click="deleteRow($index)"]');
  this.ctModalButtons = $$('.buttons button');
  this.ctModalSaveBt = $('#saveAndCloseBId');
  this.ctModalCancelBt = $('[class="btn btn-default tablesave cancel"]');
  
  // End of Create Task Modal Window


	//-END Functionality

}; //end locators
	
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
TaskManager.prototype.isErrorModalDisplayed = function() {
  var that = this;
  return browser.wait(function () {
    return that.errorModal.isDisplayed().then(function (valor) {
      return valor;
    });
  }).then(function () {
    return true;
  });
};
TaskManager.prototype.isErrorModalNotDisplayed = function() {
  var that = this;
  return browser.wait(function () {
    return that.errorModal.isDisplayed().then(function (valor) {
      return !valor;
    });
  }).then(function () {
    return true;
  });
};
TaskManager.prototype.isErrorMsgDisplayed = function() {
  var that = this;
  return browser.wait(function () {
    return that.errorModalText.getText().then(function (text) {
      return text !== '';
    });
  }).then(function () {
    return true;
  });
};
TaskManager.prototype.isCreateTaskModalOpened = function() {
  return browser.wait(function () {
    return $('body').getAttribute('class').then(function (val) {
      return val==='skin-blue layout-top-nav modal-open';
    });
  }).then(function () {
    return true;
  });
};
TaskManager.prototype.isModalClosed = function() {
  return browser.wait(function () {
    return $('body').getAttribute('class').then(function (val) {
      return val==='skin-blue layout-top-nav';
    });
  }).then(function () {
    return true;
  });
};	

	
module.exports = TaskManager;