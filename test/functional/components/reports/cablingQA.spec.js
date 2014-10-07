'use strict';
//Generates a pdf file
var Reports = require('./reports.po.js');
var  Menu = require('../menu/menu.po.js');
describe('Cabling QA Report',function(){
  var menu = new Menu();
  
  it('should load Cabling QA Report page after click on Reports > Cabling QA', function(){
    menu.goToReports('cablingQA');
    expect(menu.getCurrentUrl()).toEqual(process.env.BASE_URL+'/tdstm/reports/getBundleListForReportDialog?reportId=CablingQA');
  });

  it('should have Cabling QA as title', function(){
      var cablingQAPage = new Reports();
      expect(cablingQAPage.getPageTitle()).toEqual('Cabling QA Report');
  });

  describe('bundle dropdown',function(){
    var cablingQAPage = new Reports();

    it('should have bundle label', function(){
      expect(cablingQAPage.getBundleLabel()).toEqual('Bundles:*');
    });

    it('should have  All bundles as default option',function(){
      expect(cablingQAPage.getBundleSelected()).toEqual('All Bundles');

    });
    it('should have x options',function(){
      expect(cablingQAPage.getBundlesOptionsLength()).toEqual(11);

    });
    it('should have the following options',function(){
      cablingQAPage.getBundlesOptions().then(function(list){
        expect(list[0].getText()).toEqual('All Bundles');
        expect(list[1].getText()).toEqual('Buildout');
        expect(list[2].getText()).toEqual('M1');
        expect(list[3].getText()).toEqual('M2');
        expect(list[4].getText()).toEqual('M3');
        expect(list[5].getText()).toEqual('Master Bundle');
        expect(list[6].getText()).toEqual('Not Moving');
        expect(list[7].getText()).toEqual('Retired');
        expect(list[8].getText()).toEqual('Retiring');
        expect(list[9].getText()).toEqual('TBD');
        expect(list[10].getText()).toEqual('Wave1- Bundle1');
      });
    });
  });//bundle dropdown
  describe('Cable type dropdown',function(){
    var cablingQAPage = new Reports();
    it('should have cable type as label',function(){
      expect(cablingQAPage.getCableTypeLabel()).toEqual('Cable Type');
    });
    it('should have All Cables as default option',function(){
      expect(cablingQAPage.getCableTypeSelected()).toEqual('All Cables');
    });
    it('should have x options',function(){
      expect(cablingQAPage.getCableTypeOptionsLength()).toEqual(9);
    });
    it('should have the following options',function(){
      cablingQAPage.getCableTypeOptions().then(function(list){
        expect(list[0].getText()).toEqual('All Cables');
        expect(list[1].getText()).toEqual('Ether');
        expect(list[2].getText()).toEqual('Serial');
        expect(list[3].getText()).toEqual('Power');
        expect(list[4].getText()).toEqual('Fiber');
        expect(list[5].getText()).toEqual('SCSI');
        expect(list[6].getText()).toEqual('USB');
        expect(list[7].getText()).toEqual('KVM');
        expect(list[8].getText()).toEqual('Others');
      });
    });
  }); // Cable type dropdown
  describe('Buttons',function(){
    var cablingQAPage = new Reports();
    it('should have 1 button',function(){
      cablingQAPage.getIssueReportButtons().then(function(list){
        expect(list.length).toEqual(1);
      });
    });
    it('should have pdf img',function(){
      cablingQAPage.getIssueReportBtnImg().then(function(list){
        expect(list[0].getAttribute('src')).toEqual(process.env.BASE_URL+'/tdstm/plugins/jasper-1.0.0/images/icons/PDF.gif');
      });
    });    
  }); //Buttons
}); //Cabling QA Report
