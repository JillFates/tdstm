'use strict';
var  Menu = require('../menu/menu.po.js');
var AssetOptions = require('./assetOptions.po.js');
describe('Asset Options page', function(){

  it('should load Asset Options page after select Admin - Asset Options', function(){
    var menu = new Menu();
    menu.goToAdmin('assetOptions');
    var assetOptionsPage = new AssetOptions();
    expect(assetOptionsPage.titleh.getText()).toEqual('Administrative Setting');
  });
});