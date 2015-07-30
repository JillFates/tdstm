'use strict';
var  Menu = require('../menu/menu.po.js');
var SyncLibraries = require('./syncLibraries.po.js');
describe('Synch Libraries page', function(){

  it('should load Synch Libraries page after select Admin - Synch Libraries', function(){
    var menu = new Menu();
    menu.goToAdmin('syncLibraries');
    expect(menu.getCurrentUrl('/tdstm/model/importExport')).toEqual(process.env.BASE_URL+'/tdstm/model/importExport');
  });

  it('should have "Export" as title',function () {
    var syncLibrariesPage = new SyncLibraries();
    expect(syncLibrariesPage.titles.getText()).toEqual('Export');
  });
});