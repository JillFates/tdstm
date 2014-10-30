'use strict';
var  Menu = require('./menu.po.js');
describe('Data Centers Menu', function(){
  describe('Admin Role', function(){
    var menu = new Menu();

    it('should have Data Centers as label',function(){
      expect(menu.getDataCenters().getText()).toEqual('Data Centers');
    });
  
    describe('submenu',function(){

      it('should displayed 3 links', function(){
        menu.getDataCentersSubmenu().then(function(list){
          expect(list.length).toEqual(3);
        });
      });

      it('should open admin Menu',function(){
        menu.getDataCenters().click();
      });
      it('should have List Rooms in the submenu', function(){
        expect(menu.getListRooms().getText()).toEqual('List Rooms');
      });
      it('should have Racks in the submenu', function(){
        expect(menu.getRackElevations().getText()).toEqual('Rack Elevations');
      });
      it('should have Data Centers Help in the submenu', function(){
        expect(menu.getDataCentersHelp().getText()).toEqual('help');
      });
      it('should close admin Menu',function(){
        menu.getDataCenters().click();
      });
    });//submenu
  }); // Admin Role
});//Data Centers Menu
