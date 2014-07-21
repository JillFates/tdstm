'use strict';
// Run test to create first comment for an assert from asset list
// Run test to create 2nd comment for the same asset from asset list
// validate create comment modal
// validate edit comment comal
// validate comment list
// validate 
var  ListApps = require('./listApps.po.js');
var TaskModal = require('./task.po.js');
var CommentTaskList = require('./commentTask-list.po.js');
var  Menu = require('../menu/menu.po.js');
var CreateAppModal = require('./createApp.po.js');
describe('Comments - Application', function(){
  var menu = new Menu();
  var commentModal = new TaskModal();
  var appsListPage =  new ListApps();
  var commTasklist = new CommentTaskList();
  var appModal = new CreateAppModal();
  var appId;
  var appName= 'App Test 2 comments';

  describe('Preconditions', function(){
    
    it('should load Application List page after select Assets List Apps', function(){
      menu.goToAssets('listApps');
      expect(appsListPage.getTitle().getText()).toEqual('Application List');
    });
    
    it('should open Create app modal after click on create app button', function(){
      appsListPage.clickOnCreateAppBtn();
      expect(appModal.isCreateModalOpened()).toBe(true);
    });

    it('should add a Name', function(){
      var field = appModal.getNameField();
      field.sendKeys(appName);
      expect(field.getAttribute('value')).toEqual(appName);
    });

    it('should save and close', function(){
      appModal.clickSaveCloseBtn();
      expect(appModal.isCreateModalClosed()).toBe(true);
    });

  }); //preconditions

  describe('search for app', function(){

    it('should set name to search an app', function(){
      var field = appsListPage.getSearchAppName();
      field.clear();
      field.sendKeys(appName);
      expect(field.getAttribute('value')).toEqual(appName);
    });

    it('should validate search results', function(){
      appsListPage.verifySearchResults(1).then(function(list){
        list[0].getAttribute('id').then(function(pid){
          appId = pid;
        });
      });
    });
    
  }); // Search for an app

  it('should have add comment as icon', function(){
    expect(appsListPage.getCommentIcon(appId).getAttribute('src')).toEqual(process.env.BASE_URL+'/tdstm/icons/comment_add.png');
  });

  it('should open create comment modal', function(){
    appsListPage.clickOnCommentIcon(appId);
    expect(commentModal.getEditCommentModal().isPresent()).toBe(true);
  });

  describe('Create Comment Modal', function(){
  
    it('should have Create Comment as title', function(){
      expect(commentModal.getEditCommentTitle().getText()).toEqual('Create Comment');
    });

    describe('comment text area', function(){

      it('should have "Comment: " as label', function(){
        expect(commentModal.getCommentLabel().getText()).toEqual('Comment:');
      });

      it('should be empty by default', function(){
        expect(commentModal.getTaskTextArea().getAttribute('value')).toEqual('');
      });

      it('should have textArea as required',function(){
        expect(commentModal.getTaskTextArea().getAttribute('required')).toBe('true');
      });

      it('should be mark as required', function(){
        expect(commentModal.getCommentSpanReq().getAttribute('class')).toEqual('error-msg');
        expect(commentModal.getCommentLabReq().getText()).toEqual('Comment: *');
      });

      xit('should have save button disabled', function(){

      });

      it('should add a value', function(){
        var message= 'This is comment 01  and I am adding some text';
        var field = commentModal.getTaskTextArea();
        field.sendKeys(message);
        expect(field.getAttribute('value')).toEqual(message);
      });
        
      it('should not longer have Task marked as required',function(){
        expect(commentModal.getCommentSpanReq().getAttribute('class')).toEqual('error-msg ng-hide');
        expect(commentModal.getCommentLabReq().getText()).toEqual('Comment: ');
      }); 

    }); // comment text area

    describe('Category dropdown',function(){

      it('should have Category as label', function(){
        expect(commentModal.getCategoryLabel().getText()).toEqual('Category:');
      });

      it('should be general by default', function(){
        expect(commentModal.getCategorySelected().getText()).toEqual('general');
      });

      it('should have this options', function(){
        commentModal.getCategoryDropdownOptions().then(function(options){
          expect(options[0].getText()).toEqual('please select');
          expect(options[1].getText()).toEqual('general');
          expect(options[2].getText()).toEqual('discovery');
          expect(options[3].getText()).toEqual('verify');
          expect(options[4].getText()).toEqual('analysis');
          expect(options[5].getText()).toEqual('planning');
          expect(options[6].getText()).toEqual('design');
          expect(options[7].getText()).toEqual('buildout');
          expect(options[8].getText()).toEqual('walkthru');
          expect(options[9].getText()).toEqual('premove');
          expect(options[10].getText()).toEqual('moveday');
          expect(options[11].getText()).toEqual('shutdown');
          expect(options[12].getText()).toEqual('physical');
          expect(options[13].getText()).toEqual('transport');
          expect(options[14].getText()).toEqual('startup');
          expect(options[15].getText()).toEqual('postmove');
        });
      });
      
      it('should select moveday as category', function(){
        commentModal.setCategoryByPos(10);
        expect(commentModal.getCategorySelected().getText()).toEqual('moveday');
      });

    }); // category dropdown
    
    describe('Assets dropdowns', function(){
      
      it('should have Assets as label', function(){
        expect(commentModal.getAssetsLabel().getText()).toEqual('Asset:');
      });

      describe('Asset Type dropdown', function(){
        
        it('should have Application selected by default', function(){
            expect(commentModal.getAssetTypeSelected().getText()).toEqual('Application');
        });

        it('should have this options', function(){
          commentModal.getAssetTypeOptions().then(function(options){
            expect(options.length).toEqual(5);
            expect(options[0].getText()).toEqual('Application');
            expect(options[1].getText()).toEqual('Server');
            expect(options[2].getText()).toEqual('Database');
            expect(options[3].getText()).toEqual('Storage');
            expect(options[4].getText()).toEqual('Other');
          });
        });

      });//AssetType dropdown

    describe('Asset Entity dropdown', function(){
      
      it('should have the current application selected',function(){
        expect(commentModal.getAssetEntitySelected().getText()).toEqual(appName);
      });

      xit('should have the existing applications listed',function(){
        commentModal.getAssetEntityOptions().then(function(options){
          expect(options.length).toEqual(2);
          expect(options[0].getText()).toEqual('Please select');
          expect(options[1].getText()).toEqual(appName);
        });
      });

      xdescribe('application Entity filter by application type', function(){

        xit('should displayed only applications', function(){

        });
        xit('should displayed only', function(){

        });
        
        xit('should displayed only', function(){

        });
        
        xit('should displayed only', function(){

        });
        
        xit('should displayed only', function(){

        });

      }); //application Entity filter by application type

    });//Asset dropdown
  }); //Assets dropdowns
  
    it('should save created comment',function(){ 
      commentModal.saveTask();
      expect(commentModal.getEditCommentModal().isPresent()).toBe(false);
    });
  }); //Create Comment Modal

  describe('Comment List from App List', function(){

    describe('search for app - this is added due to TM-2996', function(){

      it('should load Application List page after select Assets List Apps', function(){
        menu.goToAssets('listApps');
        expect(appsListPage.getTitle().getText()).toEqual('Application List');
      });

      it('should set name to search an app', function(){
        var field = appsListPage.getSearchAppName();
        field.clear();
        field.sendKeys(appName);
        expect(field.getAttribute('value')).toEqual(appName);
      });

      it('should validate search results', function(){
        appsListPage.verifySearchResults(1).then(function(list){
          list[0].getAttribute('id').then(function(pid){
            appId = pid;
          });
        });
      });
    }); // Search for an app
    
    it('should have comments as icon', function(){
      expect(appsListPage.getCommentIcon(appId).getAttribute('src')).toEqual(process.env.BASE_URL+'/tdstm/icons/comments.png');
    });

    it('should open create comment modal', function(){
      appsListPage.clickOnCommentIcon(appId);
      expect(commTasklist.getCommentTaskListModal().isPresent()).toBe(true);
    });

    it('should have as title Show Comments and task + App Name', function(){
      expect(commTasklist.getTitle().getText()).toEqual('Show Comments: Application/'+appName);
    });

    it('should have only 1 item', function(){
      expect(commTasklist.getList().count()).toEqual(1);
    });
  
    it('should get List',function(){
      var elems = ['This is comment 01 and I am a... moveday'];
      commTasklist.getList().then(function(list){
        expect(list[0].getText()).toEqual(elems[0]);
      });
    });

    describe('add new comment', function(){

      describe('add comment button', function(){
        it('should have add comment icon', function(){
          expect(commTasklist.getAddCommentIcon().getAttribute('src')).toEqual(process.env.BASE_URL+'/tdstm/icons/comment_add.png');
        });

        it('should have "Add Comment" label', function(){
          expect(commTasklist.getAddComment().getText()).toEqual('  Add Comment');
        });

      }); // add comment button
      
      it('should open create comment modal', function(){
        commTasklist.clickAddComment();
        expect(commTasklist.getCommentTaskListModal().isPresent()).toBe(true);
        expect(commentModal.getEditCommentModal().isPresent()).toBe(true);
      });

      it('should have Create comment as title', function(){
        expect(commentModal.getEditCommentTitle().getText()).toEqual('Create Comment');
      });

      it('should add a comment', function(){
        commentModal.addComment('Comment 2 added from list comments',5,'planning','','Application','',appName);
      });
      
      it('should save created comment',function(){ 
        commentModal.saveTask();
        expect(commentModal.getEditCommentModal().isPresent()).toBe(false);
      });

    });
    describe('validate list after adding comment', function(){
      
      it('should remain opened comment list',function(){
        expect(commTasklist.getCommentTaskListModal().isPresent()).toBe(true);
      });

      it('should have only 2 item', function(){
        expect(commTasklist.getList().count()).toEqual(2);
      });
  
      it('should get List',function(){
        var elems = ['This is comment 01 and I am a... moveday','Comment 2 added from list comm... planning'];
        commTasklist.getList().then(function(list){
          expect(list[0].getText()).toEqual(elems[0]);
          expect(list[1].getText()).toEqual(elems[1]);
        });
      });
    }); //validate list after adding comment

    describe('edit comment from list', function(){

      describe('edit button', function(){
        it('should have edit comment icon', function(){
          commTasklist.getList().then(function(list){
            expect(list[0].$('a img').getAttribute('src')).toEqual(process.env.BASE_URL+'/tdstm/icons/comment_edit.png');
          });
        });
        
      }); // edit button
      it('should open Edit Comment Modal after click on edit btn', function(){
        commTasklist.editFromList(0);
        expect(commTasklist.getCommentTaskListModal().isPresent()).toBe(true);
        expect(commentModal.getEditCommentModal().isPresent()).toBe(true);
      });

      it('should have edit comment as title', function(){
        expect(commentModal.getEditCommentTitle().getText()).toEqual('Edit Comment');
      });

      it('should change the category to general', function(){
        commentModal.setCategoryByPos(1);
        expect(commentModal.getCategorySelected().getText()).toEqual('general');
      });

      it('should save the changes and display comment Details modal', function(){
        commentModal.saveTask();
        expect(commentModal.getEditCommentModal().isPresent()).toBe(false);
        expect(commentModal.getDetailsTaskModal().isPresent()).toBe(true);
      });
      
      describe('Comments Details Modal', function(){

        it('should have Comments Details as title', function(){
          expect(commentModal.getCommentDetailTitle().getText()).toEqual('Comment Details');
        });

        it('should close comments details', function(){
          commentModal.closeDetailsTaskModal();
          expect(commentModal.getDetailsTaskModal().isPresent()).toBe(false);
        });

        describe('validate edited comment on the list', function(){
        
          it('should remain opened comment list',function(){
            expect(commTasklist.getCommentTaskListModal().isPresent()).toBe(true);
          });

          it('should have only 2 item', function(){
            expect(commTasklist.getList().count()).toEqual(2);
          });
  
          it('should get List',function(){
            var elems = ['This is comment 01 and I am a... general','Comment 2 added from list comm... planning'];
            commTasklist.getList().then(function(list){
              expect(list[0].getText()).toEqual(elems[0]);
              expect(list[1].getText()).toEqual(elems[1]);
            });
          });
        }); //validate edition on the list
      }); // Comments Details Modal

    }); //edit comment from list

    describe('view and delete a comment created', function(){
      
      it('should open Details Comment modal after click on comment.comment',function(){
        commTasklist.viewCommentDetail(1);
        expect(commentModal.getDetailsTaskModal().isPresent()).toBe(true);
      });

      it('should close Comment Detail after delete comment button is hit', function(){
        commentModal.deleteComment();
        var alertDialog = browser.switchTo().alert();
        var message= 'Are you sure?';
        expect(alertDialog.getText()).toEqual(message);
        alertDialog.accept();
        expect(commentModal.getDetailsTaskModal().isPresent()).toBe(false);
      });
      
      it('should remain opened comment list',function(){
        expect(commTasklist.getCommentTaskListModal().isPresent()).toBe(true);
      });

      it('should have only 2 item', function(){
        expect(commTasklist.getList().count()).toEqual(1);
      });

      it('should get List',function(){
        var elems = ['This is comment 01 and I am a... general','Comment 2 added from list comm... planning'];
        commTasklist.getList().then(function(list){
          expect(list[0].getText()).toEqual(elems[0]);
        });
      });
    }); //view and delete first comment created
  }); //Comment List from App List

  it('should close Task List modal', function(){
    commTasklist.clickClose();
    expect(commTasklist.getCommentTaskListModal().isPresent()).toBe(false);
  });

});// Comments - Application