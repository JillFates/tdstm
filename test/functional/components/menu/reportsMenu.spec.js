'use strict';
var  Menu = require('./menu.po.js');
describe('Reports Menu', function(){
  describe('Admin Role', function(){
    var menu = new Menu();

    it('should have Reports as label',function(){
      expect(menu.getReports().getText()).toEqual('Reports');
    });

    describe('submenu',function(){
      it('should open Reports Menu',function(){
        menu.getReports().click();
        expect(menu.getReportsMegaMenu().getAttribute('class')).toEqual('megamenu reports active');
      });
      it('should displayed 21 links', function(){
        menu.getReportsSubmenu().then(function(list){
          expect(list.length).toEqual(20);
        });
      });
      it('should have Cabling Conflict in the submenu', function(){
        expect(menu.getCablingConflict().getText()).toEqual('Cabling Conflict');
      });
      it('should have Cabling Data in the submenu', function(){
        expect(menu.getCablingData().getText()).toEqual('Cabling Data');
      });
      it('should have Power in the submenu', function(){
        expect(menu.getPower().getText()).toEqual('Power');
      });
      it('should have Discovery Help in the submenu', function(){
        expect(menu.getDiscoveryHelp().getText()).toEqual('help');
      });
      it('should have Application Profiles in the submenu', function(){
        expect(menu.getApplicationProfiles().getText()).toEqual('Application Profiles');
      });
      it('should have Application Conflicts in the submenu', function(){
        expect(menu.getApplicationConflicts().getText()).toEqual('Application Conflicts');
      });
      it('should have Server Conflicts in the submenu', function(){
        expect(menu.getServerConflicts().getText()).toEqual('Server Conflicts');
      });
      it('should have Database Conflicts in the submenu', function(){
        expect(menu.getDatabaseConflicts().getText()).toEqual('Database Conflicts');
      });
      it('should have Task Report in the submenu', function(){
        expect(menu.getTaskReport().getText()).toEqual('Task Report');
      });
      it('should have Report Summary in the submenu', function(){
        expect(menu.getReportSummary().getText()).toEqual('Report Summary');
      });
      it('should have Pre-event Checklist in the submenu', function(){
        expect(menu.getReportPreEventChecklist().getText()).toEqual('Pre-event Checklist');
      });
      it('should have Login Badges in the submenu', function(){
        expect(menu.getLoginBadges().getText()).toEqual('Login Badges');
      });
      it('should have Asset Tags in the submenu', function(){
        expect(menu.getAssetTags().getText()).toEqual('Asset Tags');
      });
      it('should have Transport Worksheets in the submenu', function(){
        expect(menu.getTransportWorksheets().getText()).toEqual('Transport Worksheets');
      });
      it('should have EventPrep Help in the submenu', function(){
        expect(menu.getEventPrepHelp().getText()).toEqual('help');
      });
      it('should have Application Migration Results in the submenu', function(){
        expect(menu.getApplicationMigrationResults().getText()).toEqual('Application Migration Results');
      });
      it('should have Issue Report in the submenu', function(){
        expect(menu.getIssueReport().getText()).toEqual('Issue Report');
      });
      it('should have Event Results in the submenu', function(){
        expect(menu.getEventResults().getText()).toEqual('Event Results');
      });
      it('should have Cabling QA in the submenu', function(){
        expect(menu.getCablingQA().getText()).toEqual('Cabling QA');
      });
      it('should have Event Day Help in the submenu', function(){
        expect(menu.getEventDayHelp().getText()).toEqual('help');
      });
      it('should close Reports Menu',function(){
        menu.getReports().click();
      });
    });//submenu
  }); // Admin Role
});//Reports Menu
