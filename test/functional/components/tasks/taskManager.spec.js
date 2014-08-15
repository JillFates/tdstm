'use strict';
var  Menu = require('../menu/menu.po.js');
var TaskManager = require('./taskManager.po.js');
describe('Task Manager page', function(){

  it('should load My tasks page after select Tasks - Task Manager', function(){
    var menu = new Menu();
    menu.goToTasks('taskManager');
    var taskManagerPage = new TaskManager();
    expect(taskManagerPage.titleh.getText()).toEqual('Task Manager');
  });

});