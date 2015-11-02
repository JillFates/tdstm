'use strict';
var  Menu = require('./menu.po.js');
describe('Dashboards Menu', function(){

  describe('Admin Role', function(){
    var menu = new Menu();
    var Links = 4;

    it('should have Dashboards as label',function(){
      expect(menu.getDashboards().getText()).toEqual('Dashboards');
    });

    describe('submenu',function(){

      it('should displayed '+ Links +' links', function(){
        menu.getDashboardsSubmenu().then(function(list){
          expect(list.length).toEqual(Links);
        });
      });

      it('should open Dashboards Menu',function(){
        menu.getDashboards().click();
      });

      it('should have User Dashboard in the submenu', function(){
        expect(menu.getUserDashboard().getText()).toEqual('User Dashboard');
      });

      it('should have Planning Dashboard in the submenu', function(){
        expect(menu.getPlanningDashboard().getText()).toEqual('Planning Dashboard');
      });

      it('should have Event Dashboard in the submenu', function(){
        expect(menu.getEventDashboard().getText()).toEqual('Event Dashboard');
      });

      it('should have Cart Tracker in the submenu', function(){
        expect(menu.getDashboardCartTracker().getText()).toEqual('Cart Tracker');
      });

      it('should close Dashboards Menu',function(){
        menu.getDashboards().click();
      });

    });//submenu

  }); // Admin Role

});//Dashboards Menu
