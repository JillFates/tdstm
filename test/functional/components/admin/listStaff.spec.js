'use strict';
var  Menu = require('../menu/menu.po.js');
var ListStaff = require('./listStaff.po.js');
describe('List Staff page', function(){

  it('should load List Staff page after select Admin - List Staff', function(){
    var menu = new Menu();
    menu.goToAdmin('listStaff');
    var listStaffPage = new ListStaff();
    expect(listStaffPage.titleh.getText()).toEqual('Staff List');
  });
});