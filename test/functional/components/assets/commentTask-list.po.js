'use strict';
var CommentTaskList = function(){
  this.titleCss = '#listCommentsTasksPopup #ui-id-5';
  this.commentTaskListModId = 'listCommentsTasksPopup';
  this.addCommentCss = '#listCommentsTasksPopup [ng-click="createComment()"]';
  this.addCommentImg = '#listCommentsTasksPopup [ng-click="createComment()"] img';
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
  this.editFromList = function(pos){
    element(by.repeater('comment in commentsData').row(pos)).$('a').click();
  };
  this.viewCommentDetail = function(pos){
    element(by.repeater('comment in commentsData').row(pos).column('{{truncate(comment.commentInstance.comment)}}')).click();
  };
  this.clickAddComment = function(){
    $(this.addCommentCss).click();
  };
  this.getAddComment = function(){
     return $(this.addCommentCss);
  };
  this.getAddCommentIcon = function(){
    return  $(this.addCommentImg);
  };
  this.clickAddTask = function(){
    $(this.addTaskCss).click();
  };
  this.clickClose = function(){
    $(this.closeModalCss).click();
  };
};
module.exports = CommentTaskList;
