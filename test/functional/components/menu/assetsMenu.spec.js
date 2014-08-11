'use strict';
var  Menu = require('./menu.po.js');
describe('Assets Menu', function(){
  describe('Admin Role', function(){
    var menu = new Menu();

    it('should have Assets as label',function(){
        expect(menu.getAssets().getText()).toEqual('Assets');
    });  
  
    describe('submenu',function(){
      it('should displayed 15 links', function(){
        menu.getAssetsSubmenu().then(function(list){
          expect(list.length).toEqual(15);
        });
      });

      it('should open Assets Menu',function(){
        menu.getAssets().click();
      });
      it('should have Summary Table in the submenu', function(){
        expect(menu.getSummaryTable().getText()).toEqual('Summary Table');
      });
      it('should have Applications in the submenu', function(){
        expect(menu.getApplications().getText()).toEqual('Applications');
      });
      it('should have Servers in the submenu', function(){
        expect(menu.getServers().getText()).toEqual('Servers');
      });
      it('should have All Devices in the submenu', function(){
        expect(menu.getAllDevices().getText()).toEqual('All Devices');
      });
      it('should have Databases in the submenu', function(){
        expect(menu.getDatabases().getText()).toEqual('Databases');
      });
      it('should have Storage Devices in the submenu', function(){
        expect(menu.getStorageDevices().getText()).toEqual('Storage Devices');
      });
      it('should have Logical Storage in the submenu', function(){
        expect(menu.getLogicalStorage().getText()).toEqual('Logical Storage');
      });
      it('should have Comments in the submenu', function(){
        expect(menu.getComments().getText()).toEqual('Comments');
      });
      it('should have Dependencies in the submenu', function(){
        expect(menu.getDependencies().getText()).toEqual('Dependencies');
      });
      it('should have Dependency Analyzer in the submenu', function(){
        expect(menu.getDependencyAnalizer().getText()).toEqual('Dependency Analyzer');
      });
      it('should have Assets Help in the submenu', function(){
        expect(menu.getAssetsHelp().getText()).toEqual('help');
      });
      it('should have Import Assets in the submenu', function(){
        expect(menu.getImportAssets().getText()).toEqual('Import Assets');
      });
      it('should have Manage Batches in the submenu', function(){
        expect(menu.getManageBatches().getText()).toEqual('Manage Batches');
      });
      it('should have Export Assets in the submenu', function(){
        expect(menu.getExportAssets().getText()).toEqual('Export Assets');
      });
      it('should have Manage Data Help in the submenu', function(){
        expect(menu.getManageDataHelp().getText()).toEqual('help');
      });

      it('should close Assets Menu',function(){
        menu.getAssets().click();
      });
    });//submenu
  }); // Admin Role
});//Assets Menu
