'use strict';
// Application profile 
// : It is basically a screen shot of each application for a filter as it is shown from the Application List detail.
// [14h'24:00] John Martin: So we can generate reports and distribute to the App Owner or SME so that they can review without needing to login and use TM.
var  Menu = require('../menu/menu.po.js');
var Reports = require('./reports.po.js');
describe('Application Profiles Report', function(){
  var menu = new Menu();
  
  it('should load application Profiles page after click on Reports > Application Profiles', function(){
    menu.goToReports('applicationProfiles');
    expect(menu.getCurrentUrl()).toEqual(process.env.BASE_URL+'/tdstm/reports/applicationProfiles');
  });


  describe('set Report Criteria and generate it', function(){

    it('should have Application Profiles as title', function(){
      var appProfilesPage = new Reports();
      expect(appProfilesPage.getPageTitle()).toEqual('Application Profiles');
    });
    describe('Criteria',function(){
      describe('Bundles dropdown',function(){
        var appProfilesPage = new Reports();
        xit('should have bundle label', function(){
          expect(appProfilesPage.getBundleLabel()).toEqual('Bundles:*');
        });
        it('should have  All bundles as default option',function(){
          expect(appProfilesPage.getBundleSelected()).toEqual('Buildout');
        });
        it('should have x options',function(){
          expect(appProfilesPage.getBundlesOptionsLength()).toEqual(11);
        });
        it('should have the following options',function(){
          appProfilesPage.getBundlesOptions().then(function(list){
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
      }); // Bundles Dropdown

      describe('SME dropdown',function(){

      }); // SME dropdown
      describe('App Owner dropdown', function(){

      });// app Owner dropdown
    }); //Criteria
    it('should generate the report after click on generate button', function(){
      var appProfilesPage = new Reports();
      appProfilesPage.generateApplicationProfilesBtn().click();
      expect(menu.getCurrentUrl()).toEqual(process.env.BASE_URL+'/tdstm/reports/generateApplicationProfiles');
    });
    describe('generated report',function(){
      it('should get title from generated report',function(){
        var appProfilesPage = new Reports();
        expect(appProfilesPage.getAppProfileGeneratedHeader().getText()).toContain('Application Profiles - MarketingDemo');
      });
    }); //generated report

  }); // set report Criteria and generate it

});