'use strict';
var  Menu = require('../menu/menu.po.js');
var ImportAccounts = require('./importAccounts.po.js');
describe('Import Accounts page', function(){

  it('should load Import Accounts page after select Admin - Import Accounts', function(){
    var menu = new Menu();
    menu.goToAdmin('importAccounts');
    var importAccountsPage = new ImportAccounts();
    expect(importAccountsPage.titleh.getText()).toEqual('Import Accounts');
  });
});