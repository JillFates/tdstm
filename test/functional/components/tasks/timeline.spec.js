'use strict';
var  Menu = require('../menu/menu.po.js');
var Timeline = require('./timeline.po.js');
describe('Timeline page', function(){

  it('should go to Tasks Timeline', function(){
    var menu = new Menu();
    menu.goToTasks('timeline');
    expect(menu.getCurrentUrl()).toEqual(process.env.BASE_URL+'/tdstm/task/taskTimeline');
  });
  it('should have "Task Timeline" as title',function(){
    var timelinePage = new Timeline();
    expect(timelinePage.titleh.getText()).toEqual('Task Timeline');
  });

});