'use strict';
var  Menu = require('../menu/menu.po.js');
var ImportAccounts = require('./importAccounts.po.js');
describe('Import Accounts page', function(){
  
  it('should go to Import Accounts page after select Admin - Import Accounts', function(){
    var menu = new Menu();
    menu.goToAdmin('importAccounts');
    expect(menu.getCurrentUrl('/tdstm/admin/importAccounts')).toEqual(process.env.BASE_URL+'/tdstm/admin/importAccounts');
  });

  it('should have "Import Accounts" as title', function(){
    var importAccountsPage = new ImportAccounts();
    expect(importAccountsPage.titleh.getText()).toEqual('Import Accounts');
  });

  xit('should display an error message if the project is not confirmed', function() {
    //not to enable 'YES - I want to import into project MarketingDemo : MarketingDemo'
    expect().toEqual('You must confirm the project to import into before continuing');
  });

  xit('should display error message if no file is attached', function  () {
    expect().toEqual('Upload file appears to be empty');
  });


  
});