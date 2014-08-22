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
  this.getPersonSelected = function(){
    return browser.executeScript('return $("[ng-model="ac.assignedTo"] option:checked").text()');
  };
  this.fixedAssignedLabel = $('[for="hardAssignedEdit"]');
  this.fixedAssigned = element(by.model('ac.hardAssigned'));
  this.teamField = element(by.model('ac.role'));
  this.teamOptions = this.teamField.element(by.model('ngModel')).$$('option');
  this.teamSelected = this.teamField.element(by.model('ngModel')).$('option:checked');
  this.getTeamSelected = function(){
    return browser.executeScript('return $("[ng-model=\'ac.role\'] [ng-model=\'ngModel\'] option:checked").text()');
  };
  this.eventLabel = $('[for="moveEvent"]');
  this.eventField = element(by.model('ac.moveEvent'));
  this.eventOptions = this.eventField.$$('option');
  this.eventSelected = this.eventField.$('option:checked');
  this.getEventSelected = function(){
    return browser.executeScript('return $("[ng-model=\'ac.moveEvent\'] option:checked").text()');
  };
  this.categoryLabel = $('[for="category"]');
  this.categoryField =  element(by.model('ac.category'));
  this.categoryOptions = this.categoryField.$$('option');
  this.categorySelected = this.categoryField.$('option:checked');
  this.getCategorySelected = function(){
    return browser.executeScript('return $("[ng-model=\'ac.category\'] option:checked").text()');
  };
  this.assetsLabel =  $('[for="asset"]');
  this.assetType = element(by.model('ac.assetType'));
  this.assetTypeOptions = this.assetType.$$('option');
  this.assetTypeSelected = this.assetType.$('option:checked');
  this.getAssetTypeSelected = function(){
    return browser.executeScript('return $("[ng-model=\'ac.assetType\'] option:checked").text()');
  };
  this.assetEntity = element(by.model('ac.assetEntity'));
  this.assetEntityOptions = this.assetEntity.$$('option');
  this.assetEntitySelected = this.assetEntity.$('option:checked');
  this.getAssetEntitySelected = function(){
    return browser.executeScript('return $("[ng-model=\'ac.assetEntity\'] option:checked").text()');
  };
  this.durationLabel = $('[for="durationEdit "]');
  this.durationField = element(by.model('duration'));
  this.durationScale = element(by.model('scale'));
  this.durationScaleOptions = this.durationScale.$$('option');
  this.durationScaleSelected = this.durationScale.$('option:checked');
  this.getDurationSelected = function(){
    return browser.executeScript('return $("[ng-model=\'scale\'] option:checked").text()');
  };
  this.priorityLabel = $('[for="priority"]');
  this.priority = element(by.model('ac.priority'));
  this.priorityOptions = this.priority.$$('option');
  this.prioritySelected = this.priority.$('option:checked');
  this.getPrioritySelected = function(){
    return browser.executeScript('return $("[ng-model=\'ac.priority\'] option:checked").text()');
  };
  this.dueDateLabel= $('[for="dueDateEditId"]');
  this.dueDate = element(by.model('ac.dueDate'));
  this.estimatedStFinLabel = $('[for="estStartTrId"]');
  this.estimatedStFinField = element(by.model('acData.estRange'));
  this.statusLabel = $('[for="status"]');
  this.statusField = element(by.model('ac.status'));
  this.statusOptions = this.statusField.$$('option');
  this.statusSelected = this.statusField.$('option:checked');
  this.getStatusSelected = function(){
    return browser.executeScript('return $("[ng-model=\'ac.status\'] option:checked").text()');
  };
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

    if(process.env.BROWSER_NAME ==='phantomjs'){
      this.getCategorySelected().then(function(op){
        expect(op).toEqual(category);
      });
    }else{
      expect(this.categorySelected.getText()).toEqual(category);
    }

    //select assetType
    if(assetTypePos!==''){
      this.assetTypeOptions.then(function(list){
        list[assetTypePos].click();
      });
    }
    
    if(process.env.BROWSER_NAME ==='phantomjs'){
      this.getAssetTypeSelected().then(function(op){
        expect(op).toEqual(assetType);
      });
    }else{
      expect(this.assetTypeSelected.getText()).toEqual(assetType);
    }
    //select asset
    if(assetPos!==''){
      this.asetEntityOptions.then(function(list){
        list[assetPos].click();
      });
    }
    if(process.env.BROWSER_NAME ==='phantomjs'){
      this.getAssetEntitySelected().then(function(op){
        expect(op).toEqual(asset);
      });
    }else{
      expect(this.assetEntitySelected.getText()).toEqual(asset);
    }
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