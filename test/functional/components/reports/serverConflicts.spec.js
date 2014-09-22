'use strict';
var  Menu = require('../menu/menu.po.js');
var Reports = require('./reports.po.js');

describe('Servers Conflicts',function(){
  var menu = new Menu();
  
  it('should load Server Conflict Report page after click on Reports > Server Conflict', function(){
    menu.goToReports('serverConflicts');
    expect(menu.getCurrentUrl()).toEqual(process.env.BASE_URL+'/tdstm/reports/serverConflicts');
  });
  it('should have Server Conflicts as title',function(){
      var serverConflictsPage = new Reports();
      expect(serverConflictsPage.getPageTitle()).toEqual('Server Conflicts');
    });

  describe('bundle dropdown',function(){
     var serverConflictsPage = new Reports();
      xit('should have bundle label', function(){
        //Missing label tag and id to add this validation
        expect(serverConflictsPage.getBundleLabel()).toEqual('Bundles:');
      });

      it('should have All bundles as default option',function(){
        expect(serverConflictsPage.getBundleSelected()).toEqual('Buildout');

      });
      it('should have 11 options',function(){
        expect(serverConflictsPage.getBundlesOptionsLength()).toEqual(11);

      });
      it('should have the following options',function(){
        serverConflictsPage.getBundlesOptions().then(function(list){
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
          expect(list[10].getText()).toEqual('Planning Bundles');
        });
      });
  });
  describe('checkboxes',function(){
     var serverConflictsPage = new Reports();
    xit('should have Bundle conflicts as label',function(){

    });
    it('should have Bundle conflicts checked by default',function(){
      expect(serverConflictsPage.getBundleConflictInput().getAttribute('checked')).toBe('true');
    });
    xit('should have Unresolved dependencies as label',function(){

    });
    it('should have Unresolved dependencies checked by default',function(){
      expect(serverConflictsPage.getUnresolvedDependenciesInput().getAttribute('checked')).toBe('true');
    });
    xit('should have No Runs On as label',function(){

    });
    it('should have No Runs On checked by default',function(){
      expect(serverConflictsPage.getNoRunsOnInput().getAttribute('checked')).toBe('true');
    });
    xit('should have VM with no support as label',function(){

    });
      it('should have VM with no support checked by default',function(){
      expect(serverConflictsPage.getVMWithNoSupportInput().getAttribute('checked')).toBe('true');
    });

  });//checkboxes

  it('should generate the report after click on generate button', function(){
    var serverConflictsPage = new Reports();
    serverConflictsPage.generateServerConflictsBtn().click();
    expect(menu.getCurrentUrl()).toEqual(process.env.BASE_URL+'/tdstm/reports/generateServerConflicts');
  });
  describe('generated report Web',function(){
    it('should get title from generated report',function(){
      var serverConflictsPage = new Reports();
      expect(serverConflictsPage.getAppProfileGeneratedHeader().getText()).toContain('Server Conflicts - MarketingDemo');
    });
  }); //generated report

});