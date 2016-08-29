'use strict';
var  Menu = require('../menu/menu.po.js');
var Reports = require('./reports.po.js');

describe('Database Conflicts Reports', function(){

  var menu = new Menu();

  it('should load Database Conflict Report page after click on Reports > Database Conflict', function(){
    menu.goToReports('databaseConflicts');
    var url = '/tdstm/reports/databaseConflicts';
    expect(menu.getCurrentUrl(url)).toEqual(process.env.BASE_URL+url);
  });
  
  it('should have Application Conflicts as title',function(){
      var databaseConflictsPage = new Reports();
      expect(databaseConflictsPage.getPageTitle()).toEqual('Database Conflicts');
  });

  describe('bundle',function(){
    var databaseConflictsPage = new Reports();

    xit('should have bundle label', function(){
      expect(databaseConflictsPage.getBundleLabel()).toEqual('Bundles:*');
    });

    it('should have  All bundles as default option',function(){
      expect(databaseConflictsPage.getBundleSelected()).toEqual('Buildout');
    });

    it('should have 12 options',function(){
        expect(databaseConflictsPage.getBundlesOptionsLength()).toEqual(12);
    });

    it('should have the following options',function(){
      databaseConflictsPage.getBundlesOptions().then(function(list){
        expect(list[0].getText()).toEqual('Planning Bundles');
        expect(list[1].getText()).toEqual('──────────');
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
  });

  describe('checkboxs',function(){

    var databaseConflictsPage = new Reports();

    xit('should have Bundle Conflicts checkbox',function(){

    });

    it('should have Bundle Conflicts checkbox enabled by default', function(){
      expect(databaseConflictsPage.getBundleConflictInput().getAttribute('checked')).toBe('true');
    });

    xit('should have Unresolved dependencies checkbox',function(){

    });

    it('should have Unresolved dependencies checkbox enabled by default', function(){
      expect(databaseConflictsPage.getUnresolvedDependenciesInput().getAttribute('checked')).toBe('true');
    });
    
    xit('should have No Application checkbox',function(){

    });
    
    it('should have No Application checkbox enabled by default', function(){
      expect(databaseConflictsPage.getNoApplicationInput().getAttribute('checked')).toBe('true');
    });
    
    xit('should have DB with no support checkbox',function(){

    });
    
    it('should have DB with no support checkbox enabled by default', function(){
      expect(databaseConflictsPage.getDbWithNoSupport().getAttribute('checked')).toBe('true');
    });
  
  }); //checkboxs

  it('should generate the report after click on generate button', function(){
    var databaseConflictsPage = new Reports();
    databaseConflictsPage.generateDatabaseConflictsBtn().click();
    var url ='/tdstm/reports/generateDatabaseConflicts';
    expect(menu.getCurrentUrl(url)).toEqual(process.env.BASE_URL+url);
  });
  
  describe('generated report web',function(){
  
    xit('should get title from generated report',function(){
      var databaseConflictsPage = new Reports();
      expect(databaseConflictsPage.getAppProfileGeneratedHeader().getText()).toContain('Database Conflicts - MarketingDemo');
    });
  
  }); //generated report

});