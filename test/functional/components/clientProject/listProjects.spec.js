'use strict';
var  Menu = require('../menu/menu.po.js');
var ListProjects = require('./listProjects.po.js');
describe('List Projects', function(){
  var menu = new Menu();
  var listProjectPage =  new ListProjects();
  it('should load list projects page after select Client/Project ListProjects', function(){
    menu.goToClientProject('listProjects');
    expect(listProjectPage.getTitle().getText()).toEqual('Project List - Active Projects');
  });

}); // List Projects