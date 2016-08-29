'use strict';
var  Menu = require('../menu/menu.po.js');
var ListModel = require('./listModel.po.js');
describe('List Model page', function(){

  it('should load list Model page after select Admin - List Model', function(){
    var menu = new Menu();
    menu.goToAdmin('listModel');
    expect(menu.getCurrentUrl('/tdstm/model/list')).toMatch(process.env.BASE_URL+'/tdstm/model/list(/\\d{4,6}|)');
  });

  it('should have "Model List" as title', function() {
    var listModelPage = new ListModel();
    expect(listModelPage.titleh.getText()).toEqual('Model List');
  });
});