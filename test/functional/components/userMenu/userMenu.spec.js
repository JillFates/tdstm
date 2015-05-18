'use strict';
var UserMenu = require('./userMenu.po.js');
var Menu = require('../menu/menu.po.js');

describe('User Menu', function() {
  var userMenu = new UserMenu();
  var menu = new Menu();
  xit('should have as title user name', function() {
    
  });

  it('should have 4 links', function() {
    userMenu.open();
    userMenu.getLinksList().then(function (list) {
      expect(list.length).toEqual(4);
    });
  });

  it('should have "Account Details" as link', function() {
    userMenu.getLinksList().then(function (list) {
      expect(list[0].getText()).toEqual('Account Details...');
    });
  });

  it('should have "Edit Preferences" as link', function() {
    userMenu.getLinksList().then(function (list) {
      expect(list[1].getText()).toEqual('Edit preferences');
    });
  });

  it('should have "Use Mobile Site" as link', function() {
    userMenu.getLinksList().then(function (list) {
      expect(list[2].getText()).toEqual('Use Mobile Site');
    });  
  });

  it('should have "Sign out" as link', function() {
    userMenu.getLinksList().then(function (list) {
      expect(list[3].getText()).toEqual('Sign out');
    });
  });

  describe('Account Details', function() {
    
    it('should displayed Account Details modal after click on the link', function() {
      userMenu.selectAccountDetails();
      expect(userMenu.isAccountDetailsModalOpened()).toEqual(true);
    });

    it('should close Account Details modal',function () {
      userMenu.closeAccountDetailsModal();
      expect(userMenu.isAccountDetailsModalClosed()).toEqual(true);
    });
  
  }); // Account Details

  describe('User preferences', function() {
    
    it('should displayed User Preferences modal after click on the link', function() {
      userMenu.open();
      userMenu.selectEditPreferences();
      expect(userMenu.isUserPreferencesModalOpened()).toEqual(true);
    });

    it('should close User Preferences modal', function() {
      userMenu.closeUserPreferencesModal();
      expect(userMenu.isUserPreferencesModalClosed()).toEqual(true);
    });

  }); //User preferences
  
  describe('Use Mobile Site', function() {

    xit('should load Mobile site after click on Use Mobile Site Link', function() {
      userMenu.selectUseMobileSite();
      expect(menu.getCurrentUrl()).toEqual(process.env.BASE_URL+'/tdstm/clientTeams/listTasks?viewMode=mobile&tab=');
    });

  }); //Use Mobile site
  
  describe('sign out', function(){
    var userMenu = new UserMenu();
    xit('should sign out successfully', function(){
      userMenu.open();
      userMenu.selectSignOut();
      expect(menu.getCurrentUrl()).toEqual(process.env.BASE_URL+'/tdstm/auth/login');
    });

  });//signOut

}); // user userMenu