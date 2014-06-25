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
'use strict';
var  Menu = require('../menu/menu.po.js');
var  ListApps = require('./listApps.po.js');
var CreateServerModal = require('./createServer.po.js');
var  TaskModal = require('./task.po.js');
var CommentTaskList = require('./commentTask-list.po.js');

describe('validate comments/task on server list', function(){
  var menu = new Menu();
  var appsListPage =  new ListApps();
  var serverModal = new CreateServerModal();
  var appName= 'Server CTV';
  var taskCommenModal = new TaskModal();
  var commTasklist = new CommentTaskList();
  var appId;

  describe('add a server',function(){

    it('should load server List page after select Assets List Servers', function(){
      menu.goToAssets('listServers');
      expect(appsListPage.getTitle().getText()).toEqual('Server List');
    });

    it('should open Create server modal after click on create button', function(){
      appsListPage.clickOnCreateTaskBtn();
      expect(serverModal.isCreateModalOpened()).toBe(true);
    });

    it('should create a server and save and view',function(){
      serverModal.createServer(appName,'view');
    });
  });//add a server

  describe('add a comment',function(){
    it('should open create comment popup after click on addComment', function(){
      serverModal.clickAddComment();
      expect(taskCommenModal.getEditCommentModal().isPresent()).toBe(true);
    });

    it('should add a comment', function(){
      taskCommenModal.addComment('This is the comments',3,'verify', '','Server','',appName);
    });
    
    it('should save created comment',function(){ 
      taskCommenModal.saveTask();
      expect(taskCommenModal.getEditCommentModal().isPresent()).toBe(false );
  });

  });// add a comment

  describe('add a task',function(){
    it('should open create task popup after click on add Task', function(){
      serverModal.clickAddTask();
      expect(taskCommenModal.getEditTaskModal().isPresent()).toBe(true);
    });
    it('should add a task', function(){
      taskCommenModal.addTask('This is task bla bla');
    });
    it('should save created task',function(){ 
      taskCommenModal.saveTask();
      expect(taskCommenModal.getEditTaskModal().isPresent()).toBe(false );
    });
  }); // add a Task

  it('should close server modal', function(){
    serverModal.closeViewModal();
    expect(serverModal.isViewModalClosed()).toBe(true);
  });

 describe('search for the server', function(){

    it('should set name to search', function(){
      var field = appsListPage.getSearchAppName();
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

  describe('open comments/task List Modal', function(){

    it('should open comment Task List modal', function(){
      console.log(appId);
      browser.sleep(300);
      appsListPage.clickOnCommentTaskIcon(appId);
      expect(commTasklist.getCommentTaskListModal().isPresent()).toBe(true);
    });

    it('should have as title Show Comments and task + App Name', function(){
      expect(commTasklist.getTitle().getText()).toEqual('Show Comments & Tasks: Server/'+appName);
    });

    it('should have only 2 items', function(){
      expect(commTasklist.getList().count()).toEqual(2);
    });
    it('should get List',function(){
      var elems = ['This is the comments comment verify', 'This is task bla bla issue Ready general'];
      commTasklist.getList().then(function(list){
        expect(list[0].getText()).toEqual(elems[0]);
        expect(list[1].getText()).toEqual(elems[1]);
      });
    });

    xit('should select a task', function(){
      commTasklist.selectFromList(0);
      expect(commTasklist.getTitle().getText()).toEqual('jiji');
    });

    it('should close Task List modal', function(){
      commTasklist.clickClose();
      expect(commTasklist.getCommentTaskListModal().isPresent()).toBe(false);
    });
  }); //open comments/task List Modal

}); //validate comments/task on server list