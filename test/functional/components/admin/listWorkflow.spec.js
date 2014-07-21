'use strict';
var  Menu = require('../menu/menu.po.js');
var ListWorkflow = require('./listWorkflow.po.js');
describe('listWorkflow page', function(){

  it('should load list workflow page after select Admin - list Workflow', function(){
    var menu = new Menu();
    menu.goToAdmin('listWorkflow');
    var listWorkflowPage = new ListWorkflow();
    expect(listWorkflowPage.header.getText()).toEqual('Workflows');
  });
});