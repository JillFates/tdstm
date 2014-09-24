'use strict';
var  Menu = require('../menu/menu.po.js');
var Reports = require('./reports.po.js');
describe('Application Migration Reports', function(){
  var menu = new Menu();
  it('should load Application Migration Report page after click on Reports > Application Migration', function(){
    menu.goToReports('applicationMigrationResults');
    expect(menu.getCurrentUrl()).toEqual(process.env.BASE_URL+'/tdstm/reports/applicationMigrationReport');
  });
  it('should have Application Migration as title',function(){
    var applicationMigrationPage = new Reports();
    expect(applicationMigrationPage.getPageTitle()).toEqual('Application Migration Results');
  });

  describe('bundle dropdown',function(){
    var applicationMigrationPage = new Reports();
    it('should have bundle label', function(){
      expect(applicationMigrationPage.getMoveBundleLabel().getText()).toEqual('Bundle:');
    });
    it('should have  All bundles as default option',function(){
      expect(applicationMigrationPage.getBundleSelected()).toEqual('Buildout');
    });
    it('should have x options',function(){
      expect(applicationMigrationPage.getBundlesOptionsLength()).toEqual(11);
    });
    it('should have the following options',function(){
      applicationMigrationPage.getBundlesOptions().then(function(list){
        expect(list[0].getText()).toEqual('Buildout');
        expect(list[1].getText()).toEqual('M1');
        expect(list[2].getText()).toEqual('M2');
        expect(list[3].getText()).toEqual('M3');
        expect(list[4].getText()).toEqual('Master Bundle');
        expect(list[5].getText()).toEqual('Not Moving');
        expect(list[6].getText()).toEqual('Retired');
        expect(list[7].getText()).toEqual('Retiring');
        expect(list[8].getText()).toEqual('TBD');
        expect(list[9].getText()).toEqual('Wave1- Bundle1');
      });
    });
  });//bundle dropdown
  xdescribe('SME dropdown',function(){

  }); //SME dropdown
  xdescribe('start of with dropdown',function(){

  }); //start of with dropdown
  xdescribe('Testing dropdown',function(){

  }); //Testing dropdown
  xdescribe('end with dropdown',function(){

  }); //end with dropdown
  xdescribe('Outage window dropdown',function(){

  }); //Outage window dropdown
  describe('generate Web',function(){
    it('should click on generate report button',function(){
      var applicationMigrationPage = new Reports();
      applicationMigrationPage.generateApplicationMigrationBtn().click();
      expect(menu.getCurrentUrl()).toEqual(process.env.BASE_URL+'/tdstm/reports/generateApplicationMigration');

    });
    it('should get title from generated report',function(){
      var applicationMigrationPage = new Reports();
      expect(applicationMigrationPage.getPreEventCheckGeneratedReportHeader().getText()).toContain('Application Migration Results - MarketingDemo : Buildout');
    });
  });
}); // Transport Worksheet Report