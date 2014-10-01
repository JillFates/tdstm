'use strict';
var  Menu = require('../menu/menu.po.js');
var TaskManager = require('./taskManager.po.js');
describe('Task Manager page', function(){

  it('should load My tasks page after select Tasks - Task Manager', function(){
    var menu = new Menu();
    menu.goToTasks('taskManager');
    expect(menu.getCurrentUrl()).toEqual(process.env.BASE_URL+'/tdstm/assetEntity/listTasks?initSession=true');
  });
  it('should have "Task Manager" as title',function(){
    var taskManagerPage = new TaskManager();
    expect(taskManagerPage.titleh.getText()).toEqual('Task Manager');
  });
  xdescribe('Bulk edit button',function(){
  	//should only expand task with Ready and  Started
  });
});