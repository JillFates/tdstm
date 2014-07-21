'use strict';
var  Menu = require('../menu/menu.po.js');
var ListUsers = require('./listUsers.po.js');
describe('List Users page', function(){

  it('should load Users List page after select Admin - list Users', function(){
    var menu = new Menu();
    menu.goToAdmin('listUsers');
    var listUsersPage = new ListUsers();
    expect(listUsersPage.titleh.getText()).toEqual('UserLogin List - Active Users');
  });
});