'use strict';
var  Menu = require('./menu.po.js');
describe('Planning Menu', function(){

  var menu = new Menu();

  it('should have \'Planning\' as the parent label', function() {
    expect(menu.planning.getParent().getText()).toEqual('Planning');
  });

	it('should expand the Planning Menu',function(){
		menu.planning.toggle();
		expect(menu.planning.isExpanded()).toBe(true);
	});

  describe('Child Items', function() {

		it('should have the text \'List Events\' as its link text', function() {
			expect(menu.planning.getListEvents().findElement(by.tagName('a')).getText())
				.toMatch(/List Events/);
		});

		it('should have the target URL \'/tdstm/moveEvent/list\'', function() {
			expect(menu.planning.getListEvents().findElement(by.tagName('a')).getAttribute('href'))
				.toContain('/tdstm/moveEvent/list');
		});

		it('should have the text \'[Event Name] Event Details\' as its link text', function() {
			expect(menu.planning.getEventDetails().findElement(by.tagName('a')).getText())
				.toMatch(/.+ Event Details/);
		});

		it('should have the target URL \'/tdstm/moveEvent/show/[Event Number]\'', function() {
			expect(menu.planning.getEventDetails().findElement(by.tagName('a')).getAttribute('href'))
				.toContain('/tdstm/moveEvent/show/');
		});

		it('should have the text \'List Event News\' as its link text', function() {
			expect(menu.planning.getListEventNews().findElement(by.tagName('a')).getText())
				.toMatch(/List Event News/);
		});

		it('should have the target URL \'/tdstm/newsEditor/index\'', function() {
			expect(menu.planning.getListEventNews().findElement(by.tagName('a')).getAttribute('href'))
				.toContain('/tdstm/newsEditor/index');
		});

		it('should have the text \'Pre-event Checklist\' as its link text', function() {
			expect(menu.planning.getPreEventChecklist().findElement(by.tagName('a')).getText())
				.toMatch(/Pre-event Checklist/);
		});

		it('should have the target URL \'/tdstm/reports/preMoveCheckList\'', function() {
			expect(menu.planning.getPreEventChecklist().findElement(by.tagName('a')).getAttribute('href'))
				.toContain('/tdstm/reports/preMoveCheckList');
		});

		it('should have the text \'Export Runbook\' as its link text', function() {
			expect(menu.planning.getExportRunbook().findElement(by.tagName('a')).getText())
				.toMatch(/Export Runbook/);
		});

		it('should have the target URL \'/tdstm/moveEvent/exportRunbook\'', function() {
			expect(menu.planning.getExportRunbook().findElement(by.tagName('a')).getAttribute('href'))
				.toContain('/tdstm/moveEvent/exportRunbook');
		});

		it('should have the text \'List Bundles\' as its link text', function() {
			expect(menu.planning.getListBundles().findElement(by.tagName('a')).getText())
				.toMatch(/List Bundles/);
		});

		it('should have the target URL \'/tdstm/moveBundle/list\'', function() {
			expect(menu.planning.getListBundles().findElement(by.tagName('a')).getAttribute('href'))
				.toContain('/tdstm/moveBundle/list');
		});

		it('should have the text \'[Bundle Name] Details\' as its link text', function() {
			expect(menu.planning.getBundleDetails().findElement(by.tagName('a')).getText())
				.toMatch(/.+ Bundle Details/);
		});

		it('should have the target URL \'/tdstm/moveBundle/show\'', function() {
			expect(menu.planning.getBundleDetails().findElement(by.tagName('a')).getAttribute('href'))
				.toContain('/tdstm/moveBundle/show');
		});

  }); // end Child Items

	it('should close the Planning Menu',function(){
		menu.planning.toggle();
		expect(menu.planning.isExpanded()).toBe(false);
	});

}); // end Planning Menu
