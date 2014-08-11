'use strict';
var  Menu = require('./menu.po.js');
describe('Planning Menu', function(){
  describe('Admin Role', function(){
    var menu = new Menu();

    it('should have Planning as label',function(){
      expect(menu.getPlanning().getText()).toEqual('Planning');
    });
 
  
    describe('submenu',function(){
      it('should displayed 8 links', function(){
        menu.getPlanningSubmenu().then(function(list){
          expect(list.length).toEqual(8);
        });
      });

      it('should open Planning Menu',function(){
        menu.getPlanning().click();
      });
      it('should have List Events in the submenu', function(){
        expect(menu.getListEvents().getText()).toEqual('List Events');
      });
      it('should have List Event News in the submenu', function(){
        expect(menu.getListEventNews().getText()).toEqual('List Event News');
      });
      it('should have Pre-event Checklist in the submenu', function(){
        expect(menu.getPreEventChecklist().getText()).toEqual('Pre-event Checklist');
      });
      it('should have Export Runbook in the submenu', function(){
        expect(menu.getExportRunbook().getText()).toEqual('Export Runbook');
      });
      it('should have Event help in the submenu', function(){
        expect(menu.getEventHelp().getText()).toEqual('help');
      });
      it('should have List Bundles in the submenu', function(){
        expect(menu.getListBundles().getText()).toEqual('List Bundles');
      });
      it('should have Assign Assets in the submenu', function(){
        expect(menu.getAssignAssets().getText()).toEqual('Assign Assets... (old)');
      });
      it('should have Bundles Help in the submenu', function(){
        expect(menu.getBundlesHelp().getText()).toEqual('help');
      });

      it('should close Planning Menu',function(){
        menu.getPlanning().click();
      });
    });//submenu
  }); // Admin Role
});//Planning Menu
