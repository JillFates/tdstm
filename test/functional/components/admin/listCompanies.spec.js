'use strict';
var  Menu = require('../menu/menu.po.js');
var ListCompanies = require('./listCompanies.po.js');
describe('List Companies page', function(){

  it('should go to List Companies after select Admin - List Companies',function(){
    var menu = new Menu();
    menu.goToAdmin('listCompanies');
    expect(menu.getCurrentUrl()).toEqual(process.env.BASE_URL+'/tdstm/partyGroup/list?active=active&tag_s_2_name=asc')
  });

  it('should load List Companies page after select Admin - List Companies', function(){
    var listCompaniesPage = new ListCompanies();
    expect(listCompaniesPage.titleh.getText()).toEqual('Company List');
  });
});