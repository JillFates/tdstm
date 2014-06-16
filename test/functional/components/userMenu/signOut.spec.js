'use strict';
var UserMenu = require('./userMenu.po.js');
var Login = require('../login/login.po.js');

describe('sign out', function(){
  var userMenu = new UserMenu();
  var loginPage = new Login();
  
  it('should sign out successfully', function(){
    userMenu.open();
    userMenu.selectSignOut();
    var loginPageTitle=loginPage.getTitle();
    expect(loginPageTitle.getText()).toEqual('TransitionManager™');
  });

});//signOut
