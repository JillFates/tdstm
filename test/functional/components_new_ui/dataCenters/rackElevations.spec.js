'use strict';
var Menu = require('../menu/menu.po.js');
var Racks = require('./rackElevations.po.js');
describe('Racks page', function(){
  it('should go to Racks View after select Data Centers - Racks',function(){
    var menu = new Menu();
    menu.goToDataCentars('rackElevations');
    expect(menu.getCurrentUrl('/tdstm/rackLayouts/create')).toEqual(process.env.BASE_URL+'/tdstm/rackLayouts/create');
  });

  it('should have "Rack View" Title', function(){
    var racksPage = new Racks();
    expect(racksPage.titleh.getText()).toEqual('Rack Elevations');
  });
});//Racks page