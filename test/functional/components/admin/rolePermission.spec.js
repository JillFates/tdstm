'use strict';
var  Menu = require('../menu/menu.po.js');
var RolePermission = require('./rolePermission.po.js');
describe('Role Permissions page', function(){

  it('should load Role Permissions page after select Admin - Role Permissions', function(){
    var menu = new Menu();
    menu.goToAdmin('rolePermissions');
    var rolePermissionPage = new RolePermission();
    expect(rolePermissionPage.titleh.getText()).toEqual('Role Permissions');
  });
});