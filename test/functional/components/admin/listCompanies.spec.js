'use strict';
var  Menu = require('../menu/menu.po.js');
var ListCompanies = require('./listCompanies.po.js');
describe('List Companies page', function(){

  it('should load List Companies page after select Admin - List Companies', function(){
    var menu = new Menu();
    menu.goToAdmin('listCompanies');
    var listCompaniesPage = new ListCompanies();
    expect(listCompaniesPage.titleh.getText()).toEqual('Company List');
  });
});