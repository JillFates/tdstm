'use strict';
var Menu = require('../menu/menu.po.js');
var Racks = require('./racks.po.js');
describe('Racks page', function(){
  it('should go to Racks View after select Data Centers - Racks',function(){
    var menu = new Menu();
    menu.goToDataCentars('rackElevations');
    expect(menu.getCurrentUrl()).toEqual(process.env.BASE_URL+'/tdstm/rackLayouts/create');
  });

  it('should have "Rack View" Title', function(){
    var racksPage = new Racks();
    expect(racksPage.titleh.getText()).toEqual('Rack View');
  });
});//Racks page