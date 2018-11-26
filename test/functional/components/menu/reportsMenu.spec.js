'use strict';
var  Menu = require('./menu.po.js');
describe('Reports Menu', function(){
  describe('Admin Role', function(){
    var menu = new Menu();
    var Links = 17;

    it('should have Reports as label',function(){
      expect(menu.getReports().getText()).toEqual('Reports');
    });

    describe('submenu',function(){
      it('should open Reports Menu',function(){
        menu.getReports().click();
        expect(menu.getReportsMegaMenu().getAttribute('class')).toEqual('megamenu reports active');
      });
      it('should displayed '+ Links +' links', function(){
        menu.getReportsSubmenu().then(function(list){
          expect(list.length).toEqual(Links);
        });
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
      it('should have Application Migration Results in the submenu', function(){
        expect(menu.getApplicationMigrationResults().getText()).toEqual('Application Migration Results');
      });
      it('should close Reports Menu',function(){
        menu.getReports().click();
      });
    });//submenu
  }); // Admin Role
});//Reports Menu
