'use strict';
var  Menu = require('../menu/menu.po.js');
var SyncLibraries = require('./syncLibraries.po.js');
describe('Synch Libraries page', function(){

    it('should load Synch Libraries page after select Admin - Synch Libraries', function(){
    var menu = new Menu();
    menu.goToAdmin('syncLibraries');
    var syncLibrariesPage = new SyncLibraries();
    syncLibrariesPage.titles.then(function(list){
      expect(list[0].getText()).toEqual('Import');
      expect(list[1].getText()).toEqual('Import or export TDS model data. This feature only synchronizes those with the Source TDS flag when checked.');
      expect(list[2].getText()).toEqual('Export');
    });
  });
});