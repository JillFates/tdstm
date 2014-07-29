'use strict';
var TaskModal = function(){
// // Tasks
  this.editTaskModal = element(by.id('editTaskPopup'));
  this.detailsTaskModal = element(by.id('showCommentPopup'));
  this.closeDetailsTaskModalBtn = $('#showCommentPopup [ng-click="close()"]');
  this.deleteCommentBtn = $('#showCommentPopup [ng-click="deleteComment()"]');
  this.commentDetailTitle =  $('#showCommentPopup #ui-id-5');
  this.createTaskTitle = $('#editTaskPopup #ui-id-5');
  this.taskLabel = $('#commentEditTdId label');
  this.taskTextArea = element(by.model('ac.comment'));
  this.personTeamLabel = $('#assignedToTrEditId label[for="assignedTo"]');
  this.personField = element(by.model('ac.assignedTo'));
  this.personOptions = this.personField.$$('option');
  this.personSelected = this.personField.$('option:checked');
  this.fixedAssignedLabel = $('[for="hardAssignedEdit"]');
  this.fixedAssigned = element(by.model('ac.hardAssigned'));
  this.teamField = element(by.model('ac.role'));
  this.teamOptions = this.teamField.element(by.model('ngModel')).$$('option');
  this.teamSelected = this.teamField.element(by.model('ngModel')).$('option:checked');
  this.eventLabel = $('[for="moveEvent"]');
  this.eventField = element(by.model('ac.moveEvent'));
  this.eventOptions = this.eventField.$$('option');
  this.eventSelected = this.eventField.$('option:checked');
  this.categoryLabel = $('[for="category"]');
  this.categoryField =  element(by.model('ac.category'));
  this.categoryOptions = this.categoryField.$$('option');
  this.categorySelected = this.categoryField.$('option:checked');
  this.assetsLabel =  $('[for="asset"]');
  this.assetType = element(by.model('ac.assetType'));
  this.assetTypeOptions = this.assetType.$$('option');
  this.assetTypeSelected = this.assetType.$('option:checked');
  this.assetEntity = element(by.model('ac.assetEntity'));
  this.assetEntityOptions = this.assetEntity.$$('option');
  this.assetEntitySelected = this.assetEntity.$('option:checked');
  this.durationLabel = $('[for="durationEdit "]');
  this.durationField = element(by.model('duration'));
  this.durationScale = element(by.model('scale'));
  this.durationScaleOptions = this.durationScale.$$('option');
  this.durationScaleSelected = this.durationScale.$('option:checked');
  this.priorityLabel = $('[for="priority"]');
  this.priority = element(by.model('ac.priority'));
  this.priorityOptions = this.priority.$$('option');
  this.prioritySelected = this.priority.$('option:checked');
  this.dueDateLabel= $('[for="dueDateEditId"]');
  this.dueDate = element(by.model('ac.dueDate'));
  this.estimatedStFinLabel = $('[for="estStartTrId"]');
  this.estimatedStFinField = element(by.model('acData.estRange'));
  this.statusLabel = $('[for="status"]');
  this.statusField = element(by.model('ac.status'));
  this.statusOptions = this.statusField.$$('option');
  this.statusSelected = this.statusField.$('option:checked');
  this.saveTaskBtn = $('[ng-click="saveComment(true)"]');
  // comments
  this.editCommentModal = element(by.id('editCommentPopup'));
  this.editCommentTitle = $('#editCommentPopup #ui-id-5');
  this.commentLabelReq = $('#commentEditTdId span');
  this.commentLabel = element(by.id('commentEditTdId'));

  this.isEditCommentModalPresent = function(){
    var that = this;
    return browser.wait(function(){
      return that.editCommentModal.isPresent();

    }).then(function(){
      return true;
    });
  };
  this.setCategoryByPos = function(pos){
    this.categoryOptions.then(function(list){
        list[pos].click();
      });
  };

  this.addComment = function(comment,categoryPos,category,assetTypePos,assetType,assetPos,asset){
    //add comment
    //if pos is null, will leave default information
    var comm = this.taskTextArea;
    comm.sendKeys(comment);
    //select Category
    // expect(this.commentSpanReq.getAttribute('class')).toEqual('error-msg ng-hide');
    if(categoryPos!==''){
      this.categoryOptions.then(function(list){
        list[categoryPos].click();
      });
    }
    expect(this.categorySelected.getText()).toEqual(category);
    //select assetType
    if(assetTypePos!==''){
      this.assetTypeOptions.then(function(list){
        list[assetTypePos].click();
      });
    }
    expect(this.assetTypeSelected.getText()).toEqual(assetType);
    //select asset
    if(assetPos!==''){
      this.asetEntityOptions.then(function(list){
        list[assetPos].click();
      });
    }
    expect(this.assetEntitySelected.getText()).toEqual(asset);
  };
  this.addTask = function(task){
// Task Text Area
    var field = this.taskTextArea;
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