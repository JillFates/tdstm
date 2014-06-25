'use strict';
var TaskModal = function(){
// // Tasks
  this.EditTaskModalId = 'editTaskPopup';
  this.createTaskTitleCss= '#editTaskPopup #ui-id-5';
  this.taskLabelCss = '#commentEditTdId label';
  this.taskTextAreaModel = 'ac.comment';
  this.commentRequiredId = 'commentEditTdId';
  this.personTeamLabelCss = '#assignedToTrEditId label[for="assignedTo"]';
  this.personDropdownModel = 'ac.assignedTo';
  this.fixedAssignedLabelCss = '[for="hardAssignedEdit"]';
  this.fixedAssignModel = 'ac.hardAssigned';
  this.teamDropdownModel = 'ac.role';
  this.eventLabelCss = '[for="moveEvent"]';
  this.eventModel = 'ac.moveEvent';
  this.categoryLabelCss = '[for="category"]';
  this.categoryModel =   'ac.category';
  this.assetsLabelCss =  '[for="asset"]';
  this.assetTypeModel = 'ac.assetType';
  this.assetEntityModel = 'ac.assetEntity';
  this.durationLabelCss = '[for="durationEdit "]';
  this.durationFieldModel = 'duration';
  this.durationScaleModel = 'scale';
  this.priorityLabelCss = '[for="priority"]';
  this.priorityModel = 'ac.priority';
  this.dueDateLabelCss= '[for="dueDateEditId"]';
  this.dueDateModel = 'ac.dueDate';
  this.estimatedStFinLabel = '[for="estStartTrId"]';
  this.estimatedStFinFieldModel = 'acData.estRange';
  this.statusLabelCss = '[for="status"]';
  this.statusModel = 'ac.status';
  this.saveTaskBtn = '[ng-click="saveComment(true)"]';
  // comments
  this.editCommentModalId = 'editCommentPopup';
  this.editCommentTitleCss = '#editCommentPopup #ui-id-5';
  this.commentLabelCss = '#commentEditTdId label';
  this.commentLabelReq = '#commentEditTdId span';
  this.commentlabReqId = '#commentEditTdId';

  this.getEditCommentModal = function(){
    return element(by.id(this.editCommentModalId));
  };
  this.getEditCommentTitle = function(){
    return $(this.editCommentTitleCss);
  };
  this.getCommentLabel = function (){
    return $(this.commentLabelCss);
  };
  this.getCommentSpanReq = function(){
    return $(this.commentLabelReq);
  };  
  this.getCommentLabReq = function(){
    return $(this.commentlabReqId);
  };
  this.getEditTaskModal = function(){
    return element(by.id(this.EditTaskModalId));
  };

  this.getTitle = function(){
    return $(this.createTaskTitleCss);
  };
  this.getTaskLabel = function(){
    return $(this.taskLabelCss);
  };
  this.getTaskTextArea = function(){
    return element(by.model(this.taskTextAreaModel));
  };
  this.getTaskLabelReq = function (){
    return element(by.id(this.commentRequiredId));
  };
  this.getTeamPersonLabel = function(){
    return $(this.personTeamLabelCss);
  };
  this.getPersonDropdown = function(){
    return element(by.model(this.personDropdownModel)).element(by.model('ngModel')).$$('option');
  };
  this.getPersonSelected = function(){
    return   element(by.model(this.personDropdownModel)).element(by.model('ngModel')).$('option:checked');
  };
  this.getFixedAssignedCheckboxLabel = function(){
    return  $(this.fixedAssignedLabelCss);
  };
  this.getFixedAssignmentCheck = function(){
    return element(by.model(this.fixedAssignModel));
  };
  this.getTeamDropdownoptions = function(){
    return  element(by.model(this.teamDropdownModel)).element(by.model('ngModel')).$$('option');
  };
  this.getTeamsReapeat = function(){
    return element.all(by.repeater('role in roleTypes').column('{role.description'));
  };
  this.getTeamSelected = function(){
    return element(by.model(this.teamDropdownModel)).element(by.model('ngModel')).$('option:checked');
  };
  this.getEventLabel = function(){
    return $(this.eventLabelCss);
  };
  this.getEventDropdownOptions = function(){
    return element(by.model(this.eventModel)).$$('option');
  };
  this.getEventSelected = function(){
    return element(by.model(this.eventModel)).$('option:checked');
  };
  this.getCategoryLabel = function(){
    return $(this.categoryLabelCss);
  };
  this.getCategoryDropdownOptions = function(){
    return element(by.model(this.categoryModel)).$$('option');
  };
  this.getCategorySelected = function(){
    return element(by.model(this.categoryModel)).$('option:checked');
  };
  this.getAssetsLabel = function(){
    return $(this.assetsLabelCss);
  };
  this.getAssetTypeOptions = function(){
    return element(by.model(this.assetTypeModel)).$$('option');
  };
  this.getAssetTypeSelected = function(){
    return element(by.model(this.assetTypeModel)).$('option:checked');
  };
  this.getAssetEntityOptions = function(){
    return element(by.model(this.assetEntityModel)).$$('option');
  };
  this.getAssetEntitySelected = function(){
    return element(by.model(this.assetEntityModel)).$('option:checked');
  };
  this.getDurationLabel = function(){
    return $(this.durationLabelCss);
  };
  this.getDurationfield= function(){
    return element(by.model(this.durationFieldModel));
  };
  this.getDurationScaleOptions = function(){
    return element(by.model(this.durationScaleModel)).$$('option');
  };

  this.getDurationScaleSelected = function(){
    return element(by.model(this.durationScaleModel)).$('option:checked');
  };
  this.getPriorityLabel = function(){
    return $(this.priorityLabelCss);
  };
  this.getPrioiryOptions = function(){
    return element(by.model(this.priorityModel)).$$('option');
  };
  this.getPrioritySelected = function(){
    return element(by.model(this.priorityModel)).$('option:checked');
  };
  this.getDueDateLabel = function(){
    return $(this.dueDateLabelCss);
  };
  this.getDueDateField = function(){
    return element(by.model(this.dueDateModel));
  };
  this.getEstimatedLabel=function(){
    return $(this.estimatedStFinLabel);
  };
  this.getEstimatedField =function(){
    return element(by.model(this.estimatedStFinFieldModel));
  };
  this.getStatusLabel = function(){
    return $(this.statusLabelCss);
  };
  this.getStatusOptions = function(){
    return element(by.model(this.statusModel)).$$('option');
  };
  this.getStatusSelected = function(){
    return element(by.model(this.statusModel)).$('option:checked');
  };
  this.saveTask= function(){
    $(this.saveTaskBtn).click();
  };

  this.addComment = function(comment,categoryPos,category,assetTypePos,assetType,assetPos,asset){
    //add comment
    //if pos is null, will leave default information
    var comm = this.getTaskTextArea();
    comm.sendKeys(comment);
    //select Category
    expect(this.getCommentSpanReq().getAttribute('class')).toEqual('error-msg ng-hide');
    if(categoryPos!==''){
      this.getCategoryDropdownOptions().then(function(list){
        list[categoryPos].click();
      });
    }
    expect(this.getCategorySelected().getText()).toEqual(category);
    //select assetType
    if(assetTypePos!==''){
      this.getAssetTypeOptions().then(function(list){
        list[assetTypePos].click();
      });
    }
    expect(this.getAssetTypeSelected().getText()).toEqual(assetType);
    //select asset
    if(assetPos!==''){
      this.getAssetEntityOptions().then(function(list){
        list[assetPos].click();
      });
    }
    expect(this.getAssetEntitySelected().getText()).toEqual(asset);
  };
  this.addTask = function(task){
// Task Text Area
    var field = this.getTaskTextArea();
    field.sendKeys(task);
// Person Team
// Event Dropdown
// Category
// Base on Category Workflow      
//Asset Dropdowns
// Duration  
//Priority
// Due Date    
//Estimated Start Finish
// Status
//Predecessor
//Successor
  };
};
module.exports = TaskModal;