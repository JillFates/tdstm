'use strict';
var CommentTaskList = function(){
  this.titleCss = '#listCommentsTasksPopup #ui-id-5';
  this.commentTaskListModId = 'listCommentsTasksPopup';
  this.addCommentCss = '#listCommentsTasksPopup [ng-click="createComment()"]';
  this.addTaskCss = '#listCommentsTasksPopup [ng-click="createTask()"]';
  // this.closeModalCss = '#listCommentsTasksPopup .ui-button-text';
  this.closeModalCss = '#listCommentsTasksPopup [ng-click="close()"]';

  this.getTitle = function(){
    return $(this.titleCss);
  };
  this.getCommentTaskListModal = function(){
    return element(by.id(this.commentTaskListModId));
  };
  this.getList = function(){
    return element.all(by.repeater('comment in commentsData'));
  };
  this.selectFromList = function(pos){
    element(by.repeater('comment in commentsData').row(pos)).click();
  };
  this.clickAddComment = function(){
    $(this.addCommentCss).click();
  };
  this.clickAddTask = function(){
    $(this.addTaskCss).click();
  };
  this.clickClose = function(){
    $(this.closeModalCss).click();
  };
};
module.exports = CommentTaskList;
