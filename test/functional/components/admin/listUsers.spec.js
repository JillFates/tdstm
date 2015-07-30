'use strict';
var  Menu = require('../menu/menu.po.js');
var ListUsers = require('./listUsers.po.js');
describe('List Users page', function(){

  it('should load Users List page after select Admin - list Users', function(){
    var menu = new Menu();
    menu.goToAdmin('listUsers');
    expect(menu.getCurrentUrl('/tdstm/userLogin/list'))
    .toMatch(process.env.BASE_URL+'/tdstm/userLogin/list(/\\d{4,6}|)');

  });

  it('should have "UserLogin List - Active Users" as title', function() {
    var listUsersPage = new ListUsers();
    expect(listUsersPage.titleh.getText()).toEqual('UserLogin List - Active Users');
  });

});