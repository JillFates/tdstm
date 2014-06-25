'use strict';
var  ListApps = require('./listApps.po.js');
var TaskModal = require('./task.po.js');
var CommentTaskList = require('./commentTask-list.po.js');
var  Menu = require('../menu/menu.po.js');
describe('Add a Comment', function(){
  var menu = new Menu();
  var commentModal = new TaskModal();
  var appsListPage =  new ListApps();
  var commTasklist = new CommentTaskList();
  var appId;
  var appName= 'App Test 1';

 describe('search for app', function(){

    xit('should load Application List page after select Assets List Apps', function(){
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

  it('should open commentTask List', function(){
    browser.sleep(300);
    appsListPage.clickOnCommentTaskIcon(appId);
    expect(commTasklist.getCommentTaskListModal().isPresent()).toBe(true);
  });

  it('should open create comment popup when click on addComment btn', function(){
    commTasklist.clickAddComment();
    expect(commentModal.getEditCommentModal().isPresent()).toBe(true);
  });
  
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

  // We can validate thelist

  it('should have as title Show Comments and task + App Name', function(){
    expect(commTasklist.getTitle().getText()).toEqual('Show Comments & Tasks: Application/'+appName);
  });

  it('should have only 2 items', function(){
    expect(commTasklist.getList().count()).toEqual(2);
  });

  it('should get List',function(){
    var elems = ['This is task 01 and I am addi... issue 09/17/2014 Ready planning', 
    'This is comment 01 and I am a... comment general'];
    commTasklist.getList().then(function(list){
      expect(list[0].getText()).toEqual(elems[0]);
      expect(list[1].getText()).toEqual(elems[1]);
    });
  });

  it('should close Task List modal', function(){
    commTasklist.clickClose();
    expect(commTasklist.getCommentTaskListModal().isPresent()).toBe(false);
  });

});// Add a Comment