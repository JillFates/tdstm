'use strict';
var  Menu = require('../menu/menu.po.js');
var AssetOptions = require('./assetOptions.po.js');
describe('Asset Options page after select Admin - Asset Options', function(){
  it('should go to Asset Option Page',function(){
    var menu = new Menu();
    menu.goToAdmin('assetOptions');
    expect(menu.getCurrentUrl()).toEqual(process.env.BASE_URL+'/tdstm/assetEntity/assetOptions')
  });

  it('should load Asset Options page and have "Administrative Setting" as title', function(){
    var assetOptionsPage = new AssetOptions();
    expect(assetOptionsPage.titleh.getText()).toEqual('Administrative Setting');
  });
});