'use strict';
//Generates a pdf file
var Reports = require('./reports.po.js');
var  Menu = require('../menu/menu.po.js');
describe('Cabling Conflict Report',function(){
  var menu = new Menu();
  
  it('should load Cabling Conflict Report page after click on Reports > Cabling Conflict', function(){
    menu.goToReports('cablingConflict');
    expect(menu.getCurrentUrl('/tdstm/reports/retrieveBundleListForReportDialog?reportId=CablingConflict'))
    .toEqual(process.env.BASE_URL+'/tdstm/reports/retrieveBundleListForReportDialog?reportId=CablingConflict');
  });

  describe('set Report Criteria and generate it', function(){

    it('should have Cabling Conflict as title', function(){
      var cablingConflictPage = new Reports();
      expect(cablingCoanflictPage.getPageTitle()).toEqual('Cabling Conflict Report');
    });

    describe('bundle dropdown',function(){
      var cablingConflictPage = new Reports();

      it('should have bundle label', function(){
        expect(cablingConflictPage.getBundleLabel()).toEqual('Bundles:*');
      });
      
      it('should have  All bundles as default option',function(){
        expect(cablingConflictPage.getBundleSelected()).toEqual('All Bundles');
      });
      
      it('should have x options',function(){
        expect(cablingConflictPage.getBundlesOptionsLength()).toEqual(11);
      });
      
      it('should have the following options',function(){
        cablingConflictPage.getBundlesOptions().then(function(list){
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
      var cablingConflictPage = new Reports();

      it('should have cable type as label',function(){
        expect(cablingConflictPage.getCableTypeLabel()).toEqual('Cable Type');
      });
    
      it('should have All Cables as default option',function(){
        expect(cablingConflictPage.getCableTypeSelected()).toEqual('All Cables');
      });
    
      it('should have x options',function(){
        expect(cablingConflictPage.getCableTypeOptionsLength()).toEqual(9);
      });
    
      it('should have the following options',function(){
        cablingConflictPage.getCableTypeOptions().then(function(list){
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

    xit('should click on generate button', function(){
      var cablingConflictPage = new Reports();
      cablingConflictPage.getGenerateCablingConflictBtn().click();
    });

  }); // set report Criteria and generate it

}); //Cabling Conflict Report