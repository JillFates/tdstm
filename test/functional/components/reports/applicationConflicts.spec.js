'use strict';
var  Menu = require('../menu/menu.po.js');
var CablingConflict = require('./reports.po.js');

describe('Application Conflicts',function(){
  var menu = new Menu();
  
  it('should load Cabling Conflict Report page after click on Reports > Cabling Conflict', function(){
    menu.goToReports('applicationConflicts');
    expect(menu.getCurrentUrl()).toEqual(process.env.BASE_URL+'/tdstm/reports/applicationConflicts');
  });
  it('should have Application Conflicts as title',function(){
      var cablingConflictPage = new CablingConflict();
      expect(cablingConflictPage.getPageTitle()).toEqual('Application Conflicts');
    });

	describe('bundle',function(){
     var cablingConflictPage = new CablingConflict();
      xit('should have bundle label', function(){
        expect(cablingConflictPage.getBundleLabel()).toEqual('Bundles:*');
      });

      it('should have  All bundles as default option',function(){
        expect(cablingConflictPage.getBundleSelected()).toEqual('Buildout');

      });
      it('should have x options',function(){
        expect(cablingConflictPage.getBundlesOptionsLength()).toEqual(11);

      });
      it('should have the following options',function(){
        cablingConflictPage.getBundlesOptions().then(function(list){
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
	xdescribe('checkboxes',function(){

		it('should have missing as label',function(){

		});
		it('should have Missing checked by default',function(){

		});
		it('should have Unresolved as label',function(){

		});
		it('should have Unresolved checked by default',function(){

		});
		it('should have Conflicts as label',function(){

    });
    it('should have Conflicts checked by default',function(){

    });
    it('should click on generate report',function(){

    });

	});//checkboxes
  it('should generate the report after click on generate button', function(){
    var cablingConflictPage = new CablingConflict();
    cablingConflictPage.generateApplicationConflictsBtn().click();
    expect(menu.getCurrentUrl()).toEqual(process.env.BASE_URL+'/tdstm/reports/generateApplicationConflicts');
  });

});