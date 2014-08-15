'use strict';
var  Menu = require('../menu/menu.po.js');
var Timeline = require('./timeline.po.js');
describe('Timeline page', function(){

  it('should load My tasks page after select Tasks - Timeline', function(){
    var menu = new Menu();
    menu.goToTasks('timeline');
    var timelinePage = new Timeline();
    expect(timelinePage.titleh.getText()).toEqual('Task Timeline');
  });

});