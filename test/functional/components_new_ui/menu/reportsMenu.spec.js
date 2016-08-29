'use strict';
var  Menu = require('./menu.po.js');
describe('Reports Menu', function(){

  var menu = new Menu();

  it('should have \'Reports\' as the parent label', function() {
    expect(menu.reports.getParent().getText()).toEqual('Reports');
  });

	it('should expand the Reports Menu',function(){
		menu.reports.toggle();
		expect(menu.reports.isExpanded()).toBe(true);
	});

  describe('Child Items', function() {

		it('should have the text \'Cabling Conflict\' as its link text', function() {
			expect(menu.reports.getCablingConflict().findElement(by.tagName('a')).getText())
				.toMatch(/Cabling Conflict/);
		});

		it('should have the target URL \'/tdstm/reports/retrieveBundleListForReportDialog?reportId=CablingConflict\'', function() {
			expect(menu.reports.getCablingConflict().findElement(by.tagName('a')).getAttribute('href'))
				.toContain('/tdstm/reports/retrieveBundleListForReportDialog?reportId=CablingConflict');
		});

		it('should have the text \'Cabling Data\' as its link text', function() {
			expect(menu.reports.getCablingData().findElement(by.tagName('a')).getText())
				.toMatch(/Cabling Data/);
		});

		it('should have the target URL \'/tdstm/reports/retrieveBundleListForReportDialog?reportId=CablingData\'', function() {
			expect(menu.reports.getCablingData().findElement(by.tagName('a')).getAttribute('href'))
				.toContain('/tdstm/reports/retrieveBundleListForReportDialog?reportId=CablingData');
		});

		it('should have the text \'Power\' as its link text', function() {
			expect(menu.reports.getPower().findElement(by.tagName('a')).getText())
				.toMatch(/Power/);
		});

		it('should have the target URL \'/tdstm/reports/powerReport\'', function() {
			expect(menu.reports.getPower().findElement(by.tagName('a')).getAttribute('href'))
				.toContain('/tdstm/reports/powerReport');
		});

		it('should have the text \'Application Profiles\' as its link text', function() {
			expect(menu.reports.getApplicationProfiles().findElement(by.tagName('a')).getText())
				.toMatch(/Application Profiles/);
		});

		it('should have the target URL \'/tdstm/reports/applicationProfiles\'', function() {
			expect(menu.reports.getApplicationProfiles().findElement(by.tagName('a')).getAttribute('href'))
				.toContain('/tdstm/reports/applicationProfiles');
		});

		it('should have the text \'Application Conflicts\' as its link text', function() {
			expect(menu.reports.getApplicationConflicts().findElement(by.tagName('a')).getText())
				.toMatch(/Application Conflicts/);
		});

		it('should have the target URL \'/tdstm/reports/applicationConflicts\'', function() {
			expect(menu.reports.getApplicationConflicts().findElement(by.tagName('a')).getAttribute('href'))
				.toContain('/tdstm/reports/applicationConflicts');
		});

		it('should have the text \'Server Conflicts\' as its link text', function() {
			expect(menu.reports.getServerConflicts().findElement(by.tagName('a')).getText())
				.toMatch(/Server Conflicts/);
		});

		it('should have the target URL \'/tdstm/reports/serverConflicts\'', function() {
			expect(menu.reports.getServerConflicts().findElement(by.tagName('a')).getAttribute('href'))
				.toContain('/tdstm/reports/serverConflicts');
		});

		it('should have the text \'Database Conflicts\' as its link text', function() {
			expect(menu.reports.getDatabaseConflicts().findElement(by.tagName('a')).getText())
				.toMatch(/Database Conflicts/);
		});

		it('should have the target URL \'/tdstm/reports/databaseConflicts\'', function() {
			expect(menu.reports.getDatabaseConflicts().findElement(by.tagName('a')).getAttribute('href'))
				.toContain('/tdstm/reports/databaseConflicts');
		});

		it('should have the text \'Task Report\' as its link text', function() {
			expect(menu.reports.getTaskReport().findElement(by.tagName('a')).getText())
				.toMatch(/Task Report/);
		});

		it('should have the target URL \'/tdstm/reports/retrieveBundleListForReportDialog?reportId=Task+Report\'', function() {
			expect(menu.reports.getTaskReport().findElement(by.tagName('a')).getAttribute('href'))
				.toContain('/tdstm/reports/retrieveBundleListForReportDialog?reportId=Task+Report');
		});

		it('should have the text \'Report Summary\' as its link text', function() {
			expect(menu.reports.getReportSummary().findElement(by.tagName('a')).getText())
				.toMatch(/Report Summary/);
		});

		it('should have the target URL \'/tdstm/reports/index?projectId=2445\'', function() {
			expect(menu.reports.getReportSummary().findElement(by.tagName('a')).getAttribute('href'))
				.toContain('/tdstm/reports/index?projectId=2445');
		});

		it('should have the text \'Activity Metrics\' as its link text', function() {
			expect(menu.reports.getActivityMetrics().findElement(by.tagName('a')).getText())
				.toMatch(/Activity Metrics/);
		});

		it('should have the target URL \'/tdstm/reports/projectActivityMetrics\'', function() {
			expect(menu.reports.getActivityMetrics().findElement(by.tagName('a')).getAttribute('href'))
				.toContain('/tdstm/reports/projectActivityMetrics');
		});

		it('should have the text \'Pre-event Checklist\' as its link text', function() {
			expect(menu.reports.getPreeventChecklist().findElement(by.tagName('a')).getText())
				.toMatch(/Pre-event Checklist/);
		});

		it('should have the target URL \'/tdstm/reports/preMoveCheckList\'', function() {
			expect(menu.reports.getPreeventChecklist().findElement(by.tagName('a')).getAttribute('href'))
				.toContain('/tdstm/reports/preMoveCheckList');
		});

		it('should have the text \'Login Badges\' as its link text', function() {
			expect(menu.reports.getLoginBadges().findElement(by.tagName('a')).getText())
				.toMatch(/Login Badges/);
		});

		it('should have the target URL \'/tdstm/reports/retrieveBundleListForReportDialog?reportId=Login+Badges\'', function() {
			expect(menu.reports.getLoginBadges().findElement(by.tagName('a')).getAttribute('href'))
				.toContain('/tdstm/reports/retrieveBundleListForReportDialog?reportId=Login+Badges');
		});

		it('should have the text \'Asset Tags\' as its link text', function() {
			expect(menu.reports.getAssetTags().findElement(by.tagName('a')).getText())
				.toMatch(/Asset Tags/);
		});

		it('should have the target URL \'/tdstm/reports/retrieveBundleListForReportDialog?reportId=Asset+Tag\'', function() {
			expect(menu.reports.getAssetTags().findElement(by.tagName('a')).getAttribute('href'))
				.toContain('/tdstm/reports/retrieveBundleListForReportDialog?reportId=Asset+Tag');
		});

		it('should have the text \'Transport Worksheets\' as its link text', function() {
			expect(menu.reports.getTransportWorksheets().findElement(by.tagName('a')).getText())
				.toMatch(/Transport Worksheets/);
		});

		it('should have the target URL \'/tdstm/reports/retrieveBundleListForReportDialog?reportId=Transportation+Asset+List\'', function() {
			expect(menu.reports.getTransportWorksheets().findElement(by.tagName('a')).getAttribute('href'))
				.toContain('/tdstm/reports/retrieveBundleListForReportDialog?reportId=Transportation+Asset+List');
		});

		it('should have the text \'Application Migration Results\' as its link text', function() {
			expect(menu.reports.getApplicationMigrationResults().findElement(by.tagName('a')).getText())
				.toMatch(/Application Migration Results/);
		});

		it('should have the target URL \'/tdstm/reports/applicationMigrationReport\'', function() {
			expect(menu.reports.getApplicationMigrationResults().findElement(by.tagName('a')).getAttribute('href'))
				.toContain('/tdstm/reports/applicationMigrationReport');
		});

		it('should have the text \'Issue Report\' as its link text', function() {
			expect(menu.reports.getIssueReport().findElement(by.tagName('a')).getText())
				.toMatch(/Issue Report/);
		});

		it('should have the target URL \'/tdstm/reports/retrieveBundleListForReportDialog?reportId=Issue+Report\'', function() {
			expect(menu.reports.getIssueReport().findElement(by.tagName('a')).getAttribute('href'))
				.toContain('/tdstm/reports/retrieveBundleListForReportDialog?reportId=Issue+Report');
		});

		it('should have the text \'Cabling QA\' as its link text', function() {
			expect(menu.reports.getCablingQA().findElement(by.tagName('a')).getText())
				.toMatch(/Cabling QA/);
		});

		it('should have the target URL \'/tdstm/reports/retrieveBundleListForReportDialog?reportId=CablingQA\'', function() {
			expect(menu.reports.getCablingQA().findElement(by.tagName('a')).getAttribute('href'))
				.toContain('/tdstm/reports/retrieveBundleListForReportDialog?reportId=CablingQA');
		});
		
  }); // end Child Items

	it('should close the Reports Menu',function(){
		menu.reports.toggle();
		expect(menu.reports.isExpanded()).toBe(false);
	});

}); // end Reports Menu
