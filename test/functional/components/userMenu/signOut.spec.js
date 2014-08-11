'use strict';
var UserMenu = require('./userMenu.po.js');

describe('sign out', function(){
  var userMenu = new UserMenu();
  
  it('should sign out successfully', function(){
    userMenu.open();
    userMenu.selectSignOut();
    expect(browser.driver.getCurrentUrl()).toEqual(process.env.BASE_URL+'/tdstm/auth/login');
  });

});//signOut
