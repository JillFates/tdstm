'use strict';
var UserMenu = require('./userMenu.po.js');
var Menu = require('../menu/menu.po.js');

describe('sign out', function(){
  var userMenu = new UserMenu();
  var menu = new Menu();  
  it('should sign out successfully', function(){
    userMenu.open();
    userMenu.selectSignOut();
    expect(menu.getCurrentUrl('/tdstm/auth/login')).toEqual(process.env.BASE_URL+'/tdstm/auth/login');
  });

});//signOut
