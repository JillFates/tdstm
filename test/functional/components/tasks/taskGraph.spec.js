'use strict';
var  Menu = require('../menu/menu.po.js');
var TaskGraph = require('./taskGraph.po.js');
describe('Task Graph page', function(){

  it('should go to Task Graph', function(){
    var menu = new Menu();
    menu.goToTasks('taskGraph');
    expect(menu.getCurrentUrl()).toEqual(process.env.BASE_URL+'/tdstm/task/taskGraph?initSession=true');
  });

  it('should have "Task Graph" as title',function(){
    var taskGraphPage = new TaskGraph();
    expect(taskGraphPage.titleh.getText()).toEqual('Task Graph');
  });

});