'use strict';
var  Menu = require('../menu/menu.po.js');
var ListModel = require('./listModel.po.js');
describe('List Model page', function(){

  it('should load list Model page after select Admin - List Model', function(){
    var menu = new Menu();
    menu.goToAdmin('listModel');
    var listModelPage = new ListModel();
    expect(listModelPage.titleh.getText()).toEqual('Model List');
  });
});