'use strict';
var  Menu = require('../menu/menu.po.js');
var ListManufacturers = require('./listManufacturers.po.js');
describe('List Manufacturer page', function(){

  it('should load List Manufacturer page after select Admin - List Manufacturer', function(){
    var menu = new Menu();
    menu.goToAdmin('listManufacturer');
    var listManufacturerPage = new ListManufacturers();
    expect(listManufacturerPage.titleh.getText()).toEqual('Manufacturer List');
  });
});