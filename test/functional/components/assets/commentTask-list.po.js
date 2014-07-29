'use strict';
var CommentTaskList = function(){
  this.titleh = $('#listCommentsTasksPopup #ui-id-5');
  this.commentTaskListModal = element(by.id('listCommentsTasksPopup'));
  this.addCommentBtn = $('#listCommentsTasksPopup [ng-click="createComment()"]');
  this.addCommentIcon = $('#listCommentsTasksPopup [ng-click="createComment()"] img');
  this.addTaskBtn = $('#listCommentsTasksPopup [ng-click="createTask()"]');
  this.closeModalBtn = $('#listCommentsTasksPopup [ng-click="close()"]');
  this.list = element.all(by.repeater('comment in commentsData'));

  this.selectFromList = function(pos){
    element(by.repeater('comment in commentsData').row(pos)).click();
  };

  this.editFromList = function(pos){
    element(by.repeater('comment in commentsData').row(pos)).$('a').click();
  };

  this.viewCommentDetail = function(pos){
    element(by.repeater('comment in commentsData').row(pos).column('{{truncate(comment.commentInstance.comment)}}')).click();
  };

  this.isListModalPresent = function(){
    var that = this;
    return browser.wait(function(){
        return that.commentTaskListModal.isPresent();
      }).then(function(){
        return true;
      });

  };

};
module.exports = CommentTaskList;
