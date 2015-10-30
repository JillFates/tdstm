'use strict';
var  Menu = require('./menu.po.js');
var Links = 14
describe('Assets Menu', function(){
  
  describe('Admin Role - Menu and submenu items', function(){
    
    var menu = new Menu();

    it('should have Assets as label',function(){
        expect(menu.getAssets().getText()).toEqual('Assets');
    });  
  
    it('should displayed 14 links', function(){
      menu.getAssetsSubmenu().then(function(list){
        expect(list.length).toEqual(Links);
      });
    });

    it('should open Assets Menu',function(){
      menu.getAssets().click();
      expect(menu.getAssetsSubmenuContainer().getAttribute('class')).toEqual('megamenu rooms active');
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

    it('should have Architecture Viewer in the submenu', function() {
      expect(menu.getArchitectureViewer().getText()).toEqual('Architecture Graph');
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

    it('should close Assets Menu',function(){
      menu.getAssets().click();
      expect(menu.getAssetsSubmenuContainer().getAttribute('class')).toEqual('megamenu rooms inActive');
    });

  }); // Admin Role - Menu and submenu items
});//Assets Menu
