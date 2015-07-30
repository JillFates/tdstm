'use strict';
var  Menu = require('../menu/menu.po.js');
var ListWorkflow = require('./listWorkflow.po.js');
describe('listWorkflow page', function(){

  it('should load list workflow page after select Admin - list Workflow', function(){
    var menu = new Menu();
    menu.goToAdmin('listWorkflow');
    expect(menu.getCurrentUrl('/tdstm/workflow/home')).toEqual(process.env.BASE_URL+'/tdstm/workflow/home');

  });

  it('should have "Workflows" as title', function() {
    var listWorkflowPage = new ListWorkflow();
    expect(listWorkflowPage.header.getText()).toEqual('Workflows');
  });
});