'use strict';
var  Menu = require('../menu/menu.po.js');
var MyTasks = require('./myTasks.po.js');
describe('My Tasks page', function(){

  it('should load My tasks page after select Tasks - My Tasks', function(){
    var menu = new Menu();
    menu.goToTasks('myTasks');
    // var myTasksPage = new MyTasks();
    // expect(myTasksPage.titleh.getText()).toEqual('My Tasks');
  });

});