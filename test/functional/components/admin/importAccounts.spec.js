'use strict';
var  Menu = require('../menu/menu.po.js');
var ImportAccounts = require('./importAccounts.po.js');
describe('Import Accounts page', function(){
  it('should go to Import Accounts page after select Admin - Import Accounts', function(){
    var menu = new Menu();
    menu.goToAdmin('importAccounts');
    expect(menu.getCurrentUrl()).toEqual(process.env.BASE_URL+'/tdstm/admin/importAccounts');
  });

  it('should have "Import Accounts" as title', function(){
    var importAccountsPage = new ImportAccounts();
    expect(importAccountsPage.titleh.getText()).toEqual('Import Accounts');
  });
});