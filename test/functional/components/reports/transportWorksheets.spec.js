'use strict';
var  Menu = require('../menu/menu.po.js');
var Reports = require('./reports.po.js');
describe('Transport Worksheets Reports', function(){
  var menu = new Menu();
  it('should load Transport Worksheets Report page after click on Reports > Transport Worksheets', function(){
    menu.goToReports('transportWorksheets');
    expect(menu.getCurrentUrl()).toEqual(process.env.BASE_URL+'/tdstm/reports/getBundleListForReportDialog?reportId=Transportation+Asset+List');
  });
  it('should have Transport Worksheets as title',function(){
    var transportWorksheetsPage = new Reports();
    expect(transportWorksheetsPage.getPageTitle()).toEqual('Transport Worksheets');
  });

  describe('bundle dropdown',function(){
    var transportWorksheetsPage = new Reports();
    it('should have bundle label', function(){
      expect(transportWorksheetsPage.getBundleLabel()).toEqual('Bundles:*');
    });
    it('should have  All bundles as default option',function(){
      expect(transportWorksheetsPage.getBundleSelected()).toEqual('Please Select');
    });
    it('should have x options',function(){
      expect(transportWorksheetsPage.getBundlesOptionsLength()).toEqual(12);
    });
    it('should have the following options',function(){
      transportWorksheetsPage.getBundlesOptions().then(function(list){
        expect(list[0].getText()).toEqual('Please Select');
        expect(list[1].getText()).toEqual('All Bundles');
        expect(list[2].getText()).toEqual('Buildout');
        expect(list[3].getText()).toEqual('M1');
        expect(list[4].getText()).toEqual('M2');
        expect(list[5].getText()).toEqual('M3');
        expect(list[6].getText()).toEqual('Master Bundle');
        expect(list[7].getText()).toEqual('Not Moving');
        expect(list[8].getText()).toEqual('Retired');
        expect(list[9].getText()).toEqual('Retiring');
        expect(list[10].getText()).toEqual('TBD');
        expect(list[11].getText()).toEqual('Wave1- Bundle1');
      });
    });
  });//bundle dropdown
  xdescribe('generate pdf',function(){
    it('should click on generate report button',function(){
      var transportWorksheetsPage = new Reports();
      transportWorksheetsPage.generateTransportWorksheetBtn().click();
    });
  });
}); // Transport Worksheet Report