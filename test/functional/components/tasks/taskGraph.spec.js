'use strict';
var  Menu = require('../menu/menu.po.js');
var TaskGraph = require('./taskGraph.po.js');
describe('Task Graph page', function(){

  it('should load My tasks page after select Tasks - Task Graph', function(){
    var menu = new Menu();
    menu.goToTasks('taskGraph');
    var taskGraphPage = new TaskGraph();
    expect(taskGraphPage.titleh.getText()).toEqual('Task Graph');
  });

});