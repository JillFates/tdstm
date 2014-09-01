'use strict';
// Application profile 
// : It is basically a screen shot of each application for a filter as it is shown from the Application List detail.
// [14h'24:00] John Martin: So we can generate reports and distribute to the App Owner or SME so that they can review without needing to login and use TM.
var  Menu = require('../menu/menu.po.js');
var ApplicationProfiles = require('./reports.po.js');
describe('Application Profiles Report', function(){
  var menu = new Menu();
  
  it('should load application Profiles page after click on Reports > Application Profiles', function(){
    menu.goToReports('applicationProfiles');
    expect(menu.getCurrentUrl()).toEqual(process.env.BASE_URL+'/tdstm/reports/applicationProfiles');
  });


  describe('set Report Criteria and generate it', function(){

    it('should have Application Profiles as title', function(){
      var appProfilesPage = new ApplicationProfiles();
      expect(appProfilesPage.getPageTitle()).toEqual('Application Profiles');
    });

    it('should generate the report after click on generate button', function(){
      var appProfilesPage = new ApplicationProfiles();
      appProfilesPage.generateApplicationProfilesBtn().click();
      expect(menu.getCurrentUrl()).toEqual(process.env.BASE_URL+'/tdstm/reports/generateApplicationProfiles');
    });

  }); // set report Criteria and generate it

});