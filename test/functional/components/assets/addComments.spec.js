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
  var appId;
  var appName= 'App Test 3 comments';

  describe('Preconditions', function(){
    
    it('should load Application List page after select Assets List Apps', function(){
      var menu = new Menu();
      menu.goToAssets('applications');
      var appsListPage =  new ListApps();
      expect(appsListPage.titleh.getText()).toEqual('Application List');
    });
    
    it('should open Create app modal after click on create app button', function(){
      var appsListPage =  new ListApps();
      appsListPage.createAppBtn.click();
      var appModal = new CreateAppModal();
      expect(appModal.isCreateModalOpened()).toBe(true);
    });

    it('should create an app and save', function(){
      var appModal = new CreateAppModal();
      appModal.createApp(appName);
      expect(appModal.isCreateModalClosed()).toBe(true);
    });

    it('should displayed view Modal', function(){
      var appModal = new CreateAppModal();
      expect(appModal.isViewModalOpened()).toBe(true);
    });
    
    it('should close view modal', function(){
      var appModal = new CreateAppModal();
      appModal.closeBtn.click();
      expect(appModal.isViewModalClosed()).toBe(true);
    });
  }); //preconditions

  describe('search for app', function(){
    var appsListPage =  new ListApps();

    it('should set name to search an app', function(){
      var field = appsListPage.searchNamefield;
      field.clear();
      field.sendKeys(appName);
      expect(field.getAttribute('value')).toEqual(appName);
    });

    it('should validate search results', function(){
      appsListPage.verifySearchResults(1,'application').then(function(list){
        list[0].getAttribute('id').then(function(pid){
          appId = pid;
        });
        expect(list.length).toEqual(1);
      });
    });
    
  }); // Search for an app

  it('should have add comment as icon', function(){
    var appsListPage =  new ListApps();
    expect(appsListPage.isAddCommentIconDisplayed(appId)).toBe(true);
  });

  it('should open create comment modal', function(){
    var appsListPage =  new ListApps();    
    appsListPage.clickOnCommentIcon(appId);
    var commentModal = new TaskModal();
    expect(commentModal.isEditCommentModalPresent()).toBe(true);
  });

  describe('Create Comment Modal', function(){
    var commentModal = new TaskModal();
    
    it('should have Create Comment as title', function(){
      expect(commentModal.editCommentTitle.getText()).toEqual('Create Comment');
    });

    describe('comment text area', function(){

      it('should have "Comment: " as label', function(){
        expect(commentModal.commentLabel.$('label').getText()).toEqual('Comment:');
      });

      it('should be empty by default', function(){
        expect(commentModal.taskTextArea.getAttribute('value')).toEqual('');
      });

      it('should have textArea as required',function(){
        expect(commentModal.taskTextArea.getAttribute('required')).toBe('true');
      });

      it('should be mark as required', function(){
        var commentModal = new TaskModal();
        expect(commentModal.commentLabelReq.getAttribute('class')).toEqual('error-msg');
        expect(commentModal.commentLabel.getText()).toEqual('Comment: *');
      });

      xit('should have save button disabled', function(){

      });

      it('should add a value', function(){
        var message= 'This is comment 01  and I am adding some text';
        var field = commentModal.taskTextArea;
        field.sendKeys(message);
        expect(field.getAttribute('value')).toEqual(message);
      });
        
      it('should not longer have Task marked as required',function(){
        expect(commentModal.commentLabelReq.getAttribute('class')).toEqual('error-msg ng-hide');
        expect(commentModal.commentLabel.getText()).toEqual('Comment: ');
      }); 

    }); // comment text area

    describe('Category dropdown',function(){

      it('should have Category as label', function(){
        expect(commentModal.categoryLabel.getText()).toEqual('Category:');
      });

      it('should be general by default', function(){
        if(process.env.BROWSER_NAME==='phantomjs'){
          commentModal.getCategorySelected().then(function(op){
            expect(op).toEqual('general');
          });
        }else{
          expect(commentModal.categorySelected.getText()).toEqual('general');
        }

      });

      it('should have this options', function(){
        commentModal.categoryOptions.then(function(options){
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
        // commentModal.setCategoryByPos(10);
        commentModal.categoryOptions.get(10).click();
        if(process.env.BROWSER_NAME==='phantomjs'){
          commentModal.getCategorySelected().then(function(op){
            expect(op).toEqual('moveday');
          });
        }else{
          expect(commentModal.categorySelected.getText()).toEqual('moveday');
        }
      });

    }); // category dropdown
    
    describe('Assets dropdowns', function(){
      
      it('should have Assets as label', function(){
        expect(commentModal.assetsLabel.getText()).toEqual('Asset:');
      });

      describe('Asset Type dropdown', function(){
        
        it('should have Application selected by default', function(){
          if(process.env.BROWSER_NAME==='phantomjs'){
            commentModal.getAssetTypeSelected().then(function(op){
              expect(op).toEqual('Application');
            });
          }else{
            expect(commentModal.assetTypeSelected.getText()).toEqual('Application');
          }
        });

        it('should have this options', function(){
          commentModal.assetTypeOptions.then(function(options){
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
        if(process.env.BROWSER_NAME==='phantomjs'){
          commentModal.getAssetEntitySelected().then(function(op){
            expect(op).toEqual(appName);
          });
        }else{
          expect(commentModal.assetEntitySelected.getText()).toEqual(appName);
        }
      });

      xit('should have the existing applications listed',function(){
        commentModal.assetEntityOptions.then(function(options){
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
      commentModal.saveTaskBtn.click();
      expect(commentModal.editCommentModal.isPresent()).toBe(false);
    });
  }); //Create Comment Modal

  describe('Comment List from App List', function(){

    describe('search for app - this is added due to TM-2996', function(){

      it('should load Application List page after select Assets List Apps', function(){
        var menu = new Menu();
        menu.goToAssets('applications');
        var appsListPage =  new ListApps();
        expect(appsListPage.titleh.getText()).toEqual('Application List');
      });

      it('should set name to search an app', function(){
        var appsListPage =  new ListApps();
        var field = appsListPage.searchNamefield;
        field.clear();
        field.sendKeys(appName);
        expect(field.getAttribute('value')).toEqual(appName);
      });

      it('should validate search results', function(){
        var appsListPage =  new ListApps();
        appsListPage.verifySearchResults(1,'application').then(function(list){
          list[0].getAttribute('id').then(function(pid){
            appId = pid;
          });
          expect(list.length).toEqual(1);
        });
      });
    }); // Search for an app
    
    it('should have comments as icon', function(){
      var appsListPage =  new ListApps();
      expect(appsListPage.isCommentsIconDisplayed(appId)).toBe(true);
    });

    it('should open comment list modal', function(){
      var appsListPage =  new ListApps();
      browser.waitForAngular();
      appsListPage.clickOnCommentIcon(appId);
      var commTasklist = new CommentTaskList(); 
      expect(commTasklist.isListModalPresent()).toBe(true);
    });

    it('should have as title Show Comments and task + App Name', function(){
      var commTasklist = new CommentTaskList();
      expect(commTasklist.titleh.getText()).toEqual('Show Comments: Application/'+appName);
    });

    it('should have only 1 item', function(){
      var commTasklist = new CommentTaskList();
      expect(commTasklist.list.count()).toEqual(1);
    });
  
    it('should get List',function(){
      var elems = ['This is comment 01 and I am a... moveday'];
      var commTasklist = new CommentTaskList();
      commTasklist.list.then(function(list){
        expect(list[0].getText()).toEqual(elems[0]);
      });
    });

    describe('add new comment', function(){

      describe('add comment button', function(){
        
        var commTasklist = new CommentTaskList();

        it('should have add comment icon', function(){
          expect(commTasklist.addCommentIcon.getAttribute('src')).toEqual(process.env.BASE_URL+'/tdstm/icons/comment_add.png');
        });

        it('should have "Add Comment" label', function(){
          expect(commTasklist.addCommentBtn.getText()).toEqual('  Add Comment');
        });

      }); // add comment button
      
      it('should open create comment modal', function(){
        var commTasklist = new CommentTaskList();
        commTasklist.addCommentBtn.click();
        expect(commTasklist.isListModalPresent()).toBe(true);
        var commentModal = new TaskModal();
        expect(commentModal.isEditCommentModalPresent()).toBe(true);
      });

      it('should have Create comment as title', function(){
        var commentModal = new TaskModal();
        expect(commentModal.editCommentTitle.getText()).toEqual('Create Comment');
      });

      it('should add a comment', function(){
        var commentModal = new TaskModal();
        commentModal.addComment('Comment 2 added from list comments',5,'planning','','Application','',appName);
      });
      
      it('should save created comment',function(){ 
        var commentModal = new TaskModal();
        commentModal.saveTaskBtn.click();
        expect(commentModal.editCommentModal.isPresent()).toBe(false);
      });

    });
    describe('validate list after adding comment', function(){
      
      var commTasklist = new CommentTaskList();
      
      it('should remain opened comment list',function(){
        expect(commTasklist.isListModalPresent()).toBe(true);
      });

      it('should have only 2 item', function(){
        expect(commTasklist.list.count()).toEqual(2);
      });
  
      it('should get List',function(){
        var elems = ['This is comment 01 and I am a... moveday','Comment 2 added from list comm... planning'];
        commTasklist.list.then(function(list){
          expect(list[0].getText()).toEqual(elems[0]);
          expect(list[1].getText()).toEqual(elems[1]);
        });
      });
    }); //validate list after adding comment

    describe('edit comment from list', function(){
    
      var commTasklist = new CommentTaskList();

      describe('edit button', function(){
    
        it('should have edit comment icon', function(){
          commTasklist.list.then(function(list){
            expect(list[0].$('a img').getAttribute('src')).toEqual(process.env.BASE_URL+'/tdstm/icons/comment_edit.png');
          });
        });
        
      }); // edit button
    
      it('should open Edit Comment Modal after click on edit btn', function(){
        commTasklist.editFromList(0);
        expect(commTasklist.isListModalPresent()).toBe(true);
        var commentModal = new TaskModal();
        expect(commentModal.isEditCommentModalPresent()).toBe(true);
      });

      it('should have edit comment as title', function(){
        var commentModal = new TaskModal();
        expect(commentModal.editCommentTitle.getText()).toEqual('Edit Comment');
      });

      it('should change the category to general', function(){
        var commentModal = new TaskModal();
        commentModal.categoryOptions.get(1).click();
        if(process.env.BROWSER_NAME==='phantomjs'){
          commentModal.getCategorySelected().then(function(op){
            expect(op).toEqual('general');
          });
        }else{
          expect(commentModal.categorySelected.getText()).toEqual('general');
        }
      });

      it('should save the changes and display comment Details modal', function(){
        var commentModal = new TaskModal();
        commentModal.saveTaskBtn.click();
        expect(commentModal.editCommentModal.isPresent()).toBe(false);
        expect(commentModal.detailsTaskModal.isPresent()).toBe(true);
      });
      
      describe('Comments Details Modal', function(){

        var commentModal = new TaskModal();
        
        it('should have Comments Details as title', function(){
          expect(commentModal.commentDetailTitle.getText()).toEqual('Comment Details');
        });

        it('should close comments details', function(){
          commentModal.closeDetailsTaskModalBtn.click();
          expect(commentModal.detailsTaskModal.isPresent()).toBe(false);
        });

        describe('validate edited comment on the list', function(){
        
          it('should remain opened comment list',function(){
            expect(commTasklist.isListModalPresent()).toBe(true);
          });

          it('should have only 2 item', function(){
            expect(commTasklist.list.count()).toEqual(2);
          });
  
          it('should get List',function(){
            var elems = ['This is comment 01 and I am a... general','Comment 2 added from list comm... planning'];
            commTasklist.list.then(function(list){
              expect(list[0].getText()).toEqual(elems[0]);
              expect(list[1].getText()).toEqual(elems[1]);
            });
          });
        }); //validate edition on the list
      }); // Comments Details Modal

    }); //edit comment from list

    describe('view and delete a comment created', function(){
      
      var commTasklist = new CommentTaskList();
      var commentModal = new TaskModal();
      
      it('should open Details Comment modal after click on comment.comment',function(){
        commTasklist.viewCommentDetail(1);
        expect(commentModal.detailsTaskModal.isPresent()).toBe(true);
      });

      it('should close Comment Detail after delete comment button is hit', function(){
        if(process.env.BROWSER_NAME === 'phantomjs'){
          browser.driver.executeScript('confirm = function(){return true}');
          commentModal.deleteCommentBtn.click();
        }else{
          commentModal.deleteCommentBtn.click();
          var alertDialog = browser.switchTo().alert();
          var message= 'Are you sure?';
          expect(alertDialog.getText()).toEqual(message);
          alertDialog.accept();
        }
        expect(commentModal.detailsTaskModal.isPresent()).toBe(false);
      });
      
      it('should remain opened comment list',function(){
        expect(commTasklist.isListModalPresent()).toBe(true);
      });

      xit('should have only 1 item', function(){
        expect(commTasklist.list.count()).toEqual(1);
      });

      it('should get List',function(){
        var elems = ['This is comment 01 and I am a... general','Comment 2 added from list comm... planning'];
        commTasklist.list.then(function(list){
          expect(list[0].getText()).toEqual(elems[0]);
        });
      });
    }); //view and delete first comment created
  }); //Comment List from App List

  it('should close Task List modal', function(){
    var commTasklist = new CommentTaskList();
    commTasklist.closeModalBtn.click();
    expect(commTasklist.commentTaskListModal.isPresent()).toBe(false);
  });

});// Comments - Application