'use strict';
var  Menu = require('../menu/menu.po.js');
var RolePermission = require('./rolePermission.po.js');
describe('Role Permissions page', function(){

  it('should load Role Permissions page after select Admin - Role Permissions', function(){
    var menu = new Menu();
    menu.goToAdmin('rolePermissions');
    expect(menu.getCurrentUrl()).toEqual(process.env.BASE_URL+'/tdstm/permissions/show');
  });

  it('should have "Role Permissions" as title', function() {
    var rolePermissionPage = new RolePermission();
    expect(rolePermissionPage.titleh.getText()).toEqual('Role Permissions');
  });

  describe('Table', function() {
    xit('should have "Group" as column head', function () {
      
    });
    
    xit('should have "Permission item" as column head', function() {
      
    });

    xit('should have "ADMIN" as column head', function() {
      
    });

    xit('should have "CLIENT_ADMIN" as column head', function() {
      
    });

    xit('should have "CLIENT_MGR" as column head', function() {
      
    });

    xit('should have "SUPERVISOR" as column head', function() {
      
    });

    xit('should have "EDITOR" as column head', function() {
      
    });

    xit('should have "USER" as column head', function() {
      
    });

    xit('should have "Description" as column head', function() {
      
    });
    describe('Roles', function() {

      describe('User Group', function() {
        
        describe('Create User Login', function() {
          
          xit('should have "USER" as group', function() {
                      
          });
          
          xit('should have "CreateUserLogin" as Permission item', function() {
            
          });

          xit('should be enabled only for admin role', function() {
            
          });

          xit('should have "" as description', function() {
            
          });

        }); // Create User Login

        /*
        "group": "User",
        "permission Item" : "UserLoginDelete",
        "roles": ["Admin"],
        "description": "Ability to delete User accounts"}

        "group": "User",
        "permission Item" : "UserLoginView",
        "roles": ["Admin"],
        "description": "Ability to view User accounts list and details"}
        
        */ 
      }); // User Group
      
    }); //Roles
  }); // Table
});