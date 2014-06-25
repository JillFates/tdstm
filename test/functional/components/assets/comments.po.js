'use strict';
var CommentModal = function(){
  this.editModalId = 'editCommentPopup';
  this.editTaskTitleCss = '#editCommentPopup #ui-id-5';

  this.getEditModal = function(){
    return element(by.id(this.editModalId));
  };
  this.getTitle = function(){
    return $(this.editTaskTitleCss);
  };

};
module.export = CommentModal;