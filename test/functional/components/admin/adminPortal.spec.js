'use strict';
var  Menu = require('../menu/menu.po.js');
var AdminPortal = require('./adminPortal.po.js');
describe('Admin Portal page', function(){
  it('should go to Admin Portal page after select Admin - Admin Portal',function(){
    var menu = new Menu();
    menu.goToAdmin('adminPortal');
    expect(menu.getCurrentUrl()).toEqual(process.env.BASE_URL+'/tdstm/auth/home');
  });

  it('should have "Recent Users" Title', function(){
    var adminPortalPage = new AdminPortal();
    adminPortalPage.titles.then(function(list){
      expect(list[0].getText()).toEqual('Recent Users');
    });
  });
  it('should have "Current and Recent Events" Title', function(){
    var adminPortalPage = new AdminPortal();
    adminPortalPage.titles.then(function(list){
      expect(list[1].getText()).toEqual('Current and Recent Events');
    });
  });
  it('should have "Upcoming Events" Title', function(){
    var adminPortalPage = new AdminPortal();
    adminPortalPage.titles.then(function(list){
      expect(list[2].getText()).toEqual('Upcoming Events');
    });
  });
});