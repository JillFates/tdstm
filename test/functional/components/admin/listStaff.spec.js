'use strict';
var  Menu = require('../menu/menu.po.js');
var ListStaff = require('./listStaff.po.js');
describe('List Staff page', function(){

  it('should go to List Staff page after select Admin - List Staff', function(){
    var menu = new Menu();
    menu.goToAdmin('listStaff');
    expect(menu.getCurrentUrl()).toMatch(process.env.BASE_URL+'/tdstm/person/list(/\\d{4,6}|)');
  });

  it('should have list Staff as title',function(){
    var listStaffPage = new ListStaff();
    expect(listStaffPage.titleh.getText()).toEqual('Staff List');
  });
});