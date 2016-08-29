'use strict';
var  Menu = require('../menu/menu.po.js');
var ListManufacturers = require('./listManufacturers.po.js');
describe('List Manufacturer page', function(){

  it('should load List Manufacturer page after select Admin - List Manufacturer', function(){
    var menu = new Menu();
    menu.goToAdmin('listManufacturer');
    expect(menu.getCurrentUrl('/tdstm/manufacturer/list')).toMatch(process.env.BASE_URL+'/tdstm/manufacturer/list(/\\d{4,6}|)');
    
  });

  it('should have "Manufacturer List" as title', function() {
    var listManufacturerPage = new ListManufacturers();
    expect(listManufacturerPage.titleh.getText()).toEqual('Manufacturer List');
  });
});