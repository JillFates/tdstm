'use strict';
//Generates a pdf file
var CablingConflict = require('./cablingConflict.po.js');
var  Menu = require('../menu/menu.po.js');
describe('Cabling Conflict Report',function(){
  var menu = new Menu();
  
  it('should load Cabling Conflict Report page after click on Reports > Cabling Conflict', function(){
    menu.goToReports('cablingConflict');
    expect(menu.getCurrentUrl()).toEqual(process.env.BASE_URL+'/tdstm/reports/getBundleListForReportDialog?reportId=CablingConflict');
  });

  describe('set Report Criteria and generate it', function(){

    it('should have Cabling Conflict as title', function(){
      var cablingConglictPage = new CablingConflict();
      expect(cablingConglictPage.getPageTitle()).toEqual('Cabling Conflict Report');
    });

    xdescribe('bundle dropdown',function(){

      it('should have bundle label', function(){

      });
      it('should have  All bundles as default option',function(){

      });
      it('should have x options',function(){

      });
      it('should have the following options',function(){

      });
    });//bundle dropdown
    xdescribe('Cable type dropdown',function(){
      it('should have cable type as label',function(){

      });
      it('should have All Cables as default option',function(){

      });
      it('should have x options',function(){

      });
      it('should have the following options',function(){

      });
    }); // Cable type dropdown
    it('should generate the report after click on generate button', function(){
      var cablingConflictPage = new CablingConflict();
      cablingConflictPage.generateBtn.click();
      // expect(menu.getCurrentUrl()).toEqual(process.env.BASE_URL+'/tdstm/reports/generateApplicationProfiles');
    });

  }); // set report Criteria and generate it

}); //Cabling Conflict Report