'use strict';
var  Menu = require('./menu.po.js');
describe('Admin Menu', function(){
  describe('Admin Role', function(){
    var menu = new Menu();
    var Links = 12;
      
    it('should have Admin as label',function(){
      expect(menu.getAdmin().getText()).toEqual('Admin');
    });

    describe('submenu',function(){

      it('should displayed '+ Links +' links', function(){
          menu.getAdminSubmenu().then(function(list){
            expect(list.length).toEqual(Links);
          });
      });
      
      it('should open admin Menu',function(){
        menu.getAdmin().click();
      });

      it('should have Admin Portal in the submenu', function(){
        expect(menu.getAdminPortal().getText()).toEqual('Admin Portal');
      });
      
      it('should have Role Permissions in the submenu', function(){
        expect(menu.getRolePermissions().getText()).toEqual('Role Permissions');
      });
      
      it('should have Asset Options in the submenu', function(){
        expect(menu.getAssetOptions().getText()).toEqual('Asset Options');
      });
      
      it('should have List Companies in the submenu', function(){
        expect(menu.getListCompanies().getText()).toEqual('List Companies');
      });
      
      it('should have List Staff in the submenu', function(){
        expect(menu.getListStaff().getText()).toEqual('List Staff');
      });
      
      it('should have List Users in the submenu', function(){
        expect(menu.getListUsers().getText()).toEqual('List Users');
      });
      
      it('should have Import Accounts in the submenu', function(){
        expect(menu.getImportAccounts().getText()).toEqual('Import Accounts');
      });
      
      it('should have List Workflows in the submenu', function(){
        expect(menu.getListWorkflows().getText()).toEqual('List Workflows');
      });
      
      it('should have List Manufacturers in the submenu', function(){
        expect(menu.getListManufacturers().getText()).toEqual('List Manufacturers');
      });
      
      it('should have List Models in the submenu', function(){
        expect(menu.getListModels().getText()).toEqual('List Models');
      });
      
      it('should have Sync Libraries in the submenu', function(){
        expect(menu.getSyncLibraries().getText()).toEqual('Sync Libraries');
      });

      it('should close admin Menu',function(){
        menu.getAdmin().click();
      });
    });//submenu

  }); // Admin Role
});//Menu