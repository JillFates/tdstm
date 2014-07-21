'use strict';
var  Menu = require('../menu/menu.po.js');
var AdminPortal = require('./adminPortal.po.js');
describe('Admin Portal page', function(){

  it('should load Admin Portal page after select Admin - Admin Portal', function(){
    var menu = new Menu();
    menu.goToAdmin('adminPortal');
    var adminPortalPage = new AdminPortal();
    adminPortalPage.titles.then(function(list){
      expect(list[0].getText()).toEqual('Recent Users');
      expect(list[1].getText()).toEqual('Current and Recent Events');
      expect(list[2].getText()).toEqual('Upcoming Events');
    });
  });
});