'use strict';
var  Menu = require('../menu/menu.po.js');
var Reports = require('./reports.po.js');

describe('Application Conflicts',function(){
  var menu = new Menu();
  
  it('should load Application Conflict Report page after click on Reports > Application Conflict', function(){
    menu.goToReports('applicationConflicts');
    expect(menu.getCurrentUrl()).toEqual(process.env.BASE_URL+'/tdstm/reports/applicationConflicts');
  });
  it('should have Application Conflicts as title',function(){
      var applicationConflictsPage = new Reports();
      expect(applicationConflictsPage.getPageTitle()).toEqual('Application Conflicts');
    });

	describe('bundle',function(){
     var applicationConflictsPage = new Reports();
      xit('should have bundle label', function(){
        expect(applicationConflictsPage.getBundleLabel()).toEqual('Bundles:*');
      });

      it('should have  All bundles as default option',function(){
        expect(applicationConflictsPage.getBundleSelected()).toEqual('Buildout');

      });
      it('should have x options',function(){
        expect(applicationConflictsPage.getBundlesOptionsLength()).toEqual(11);

      });
      it('should have the following options',function(){
        applicationConflictsPage.getBundlesOptions().then(function(list){
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
	xdescribe('App Owner',function(){

	});
	describe('checkboxes',function(){
    var applicationConflictsPage = new Reports();
		xit('should have "Missing" as label',function(){

		});
		it('should have Missing checked by default',function(){
      expect(applicationConflictsPage.getMissingInput().getAttribute('checked')).toBe('true');
		});
    xit('should have "Unresolved" as label',function(){

		});
		it('should have Unresolved checked by default',function(){
      expect(applicationConflictsPage.getUnresolvedInput().getAttribute('checked')).toBe('true');
		});
		xit('should have "Conflicts" as label',function(){

    });
    it('should have Conflicts checked by default',function(){
      expect(applicationConflictsPage.getConflictsInput().getAttribute('checked')).toBe('true');
    });
	});//checkboxes

  describe('generated report Web',function(){

    it('should generate the report after click on generate button', function(){
      var cablingConflictPage = new Reports();
      cablingConflictPage.generateApplicationConflictsBtn().click();
      expect(menu.getCurrentUrl()).toEqual(process.env.BASE_URL+'/tdstm/reports/generateApplicationConflicts');
    });
    
    it('should get title from generated report',function(){
      var appProfilesPage = new Reports();
      expect(appProfilesPage.getAppProfileGeneratedHeader().getText()).toContain('Application Conflicts - MarketingDemo');
    });

  }); //generated report
});