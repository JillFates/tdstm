/* 
Scenario 
1. Create a server (with required info)
2. save and view
3. add a comments
4. add a task
5. close the server
6. search for the server
7. validate that on task/comment list are the comment/task created.
*/
/*To automate
CRUD 
title Device Details
*/
'use strict';
var  Menu = require('../menu/menu.po.js');
var  ListApps = require('./listApps.po.js');
var CreateServerModal = require('./createServer.po.js');
var  TaskModal = require('./task.po.js');
var CommentTaskList = require('./commentTask-list.po.js');

describe('Server list', function(){
  var appName= 'Server CTV';
  var appId;

  describe('Server',function(){

    it('should load server List page after select Assets > Servers', function(){
      var menu = new Menu();
      menu.goToAssets('servers');
      var appsListPage =  new ListApps();
      expect(appsListPage.titleh.getText()).toEqual('Server List');
    });

    it('should open Create server modal after click on create button', function(){
      var appsListPage =  new ListApps();
      appsListPage.createServerBtn.click();
      var serverModal = new CreateServerModal();
      expect(serverModal.isCreateModalOpened()).toBe(true);
    });
    describe('Create Server Modal', function(){
      var serverModal = new CreateServerModal();

      it('should has as title create server', function(){
        expect(serverModal.createServerTitle.getText()).toEqual('Device Detail');
      });

      it('should create a server and save and view',function(){
        var serverModal = new CreateServerModal();
        serverModal.createServer(appName);
        expect(serverModal.isViewModalOpened()).toBe(true);
      });
  
    }); // Create Server Modal

  });//add a server

  describe('add a comment',function(){
    it('should open create comment popup after click on addComment', function(){
      var serverModal = new CreateServerModal();
      serverModal.addCommentBtn.click();
      var taskCommenModal = new TaskModal();
      expect(taskCommenModal.editCommentModal.isPresent()).toBe(true);
    });

    it('should add a comment', function(){
      var taskCommenModal = new TaskModal();
      taskCommenModal.addComment('This is the comments',3,'verify', '','Server','',appName);
    });
    
    it('should save created comment',function(){ 
      var taskCommenModal = new TaskModal();
      taskCommenModal.saveTaskBtn.click();
      expect(taskCommenModal.editCommentModal.isPresent()).toBe(false );
  });

  });// add a comment

  xdescribe('add a task',function(){
    it('should open create task popup after click on add Task', function(){
      var serverModal = new CreateServerModal();
      var taskCommenModal = new TaskModal();
      serverModal.addTaskBtn.click();
      expect(taskCommenModal.editTaskModal.isPresent()).toBe(true);
    });
    it('should add a task', function(){
      var taskCommenModal = new TaskModal();
      taskCommenModal.addTask('This is task bla bla');
    });
    it('should save created task',function(){ 
      var taskCommenModal = new TaskModal();
      taskCommenModal.saveTaskBtn.click();
      expect(taskCommenModal.editTaskModal.isPresent()).toBe(false );
    });
  }); // add a Task

  it('should close server modal', function(){
    var serverModal = new CreateServerModal();
    serverModal.closeViewModalBtn.click();
    expect(serverModal.isViewModalClosed()).toBe(true);
  });

 describe('search for the server', function(){

    xit('should load server List page after select Assets List Servers', function(){
      var menu = new Menu();
      menu.goToAssets('server');
      var appsListPage =  new ListApps();
      expect(appsListPage.titleh.getText()).toEqual('Server List');
    });

    var appsListPage =  new ListApps();
    it('should set name to search', function(){
      var field = appsListPage.searchNamefield;
      field.clear();
      field.sendKeys(appName);
      expect(field.getAttribute('value')).toEqual(appName);
    });

    it('should validate search results', function(){
      // var appsListPage =  new ListApps();
      appsListPage.verifySearchResults(1).then(function(list){
        list[0].getAttribute('id').then(function(pid){
          // console.log('s',pid);
          appId = pid;
        });
      });
    });
  }); // Search for an app


  describe('Comments List Modal', function(){
    
    it('should have comments as icon', function(){
      var appsListPage =  new ListApps();
      // browser.sleep(300);
      // browser.wait(function(){
      //   return appsListPage.getCommentIcon(appId).getAttribute('src'))===process.env.BASE_URL+'/tdstm/icons/comments.png';
      // }).then(function(){
      //   expect(appsListPage.getCommentIcon(appId).getAttribute('src')).toEqual(process.env.BASE_URL+'/tdstm/icons/comments.png');
      // });
      expect(appsListPage.isCommentsIconDisplayed(appId)).toBe(true);
    });

    it('should open Comment List modal', function(){
      var appsListPage =  new ListApps();
      appsListPage.clickOnCommentIcon(appId);
    });



    it('should have as title Show Comments and task + App Name', function(){
      var commTasklist = new CommentTaskList();
      expect(commTasklist.titleh.getText()).toEqual('Show Comments: Server/'+appName);
    });

    it('should have only 1 items', function(){
      var commTasklist = new CommentTaskList();
      expect(commTasklist.list.count()).toEqual(1);
    });

    it('should get List',function(){
      var elems = ['This is the comments verify'];
      var commTasklist = new CommentTaskList();
      commTasklist.list.then(function(list){
        expect(list[0].getText()).toEqual(elems[0]);
      });
    });

    it('should close Comments List modal', function(){
      var commTasklist = new CommentTaskList();
      commTasklist.closeModalBtn.click();
      expect(commTasklist.commentTaskListModal.isPresent()).toBe(false);
    });
  
  }); //Comment List Modal

}); //Server List