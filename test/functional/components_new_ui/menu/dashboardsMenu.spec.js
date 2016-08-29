'use strict';
var  Menu = require('./menu.po.js');
describe('Dashboards Menu', function(){

  var menu = new Menu();

  it('should have \'Dashboards\' as the parent label', function() {
    expect(menu.dashboards.getParent().getText()).toEqual('Dashboards');
  });

	it('should expand the Dashboards Menu',function(){
		menu.dashboards.toggle();
		expect(menu.dashboards.isExpanded()).toBe(true);
	});

  describe('Child Items', function() {

		it('should have the text \'User Dashboard\' as its link text', function() {
			expect(menu.dashboards.getUserDashboard().findElement(by.tagName('a')).getText())
				.toMatch(/User Dashboard/);
		});

		it('should have the target URL \'/tdstm/dashboard/userPortal\'', function() {
			expect(menu.dashboards.getUserDashboard().findElement(by.tagName('a')).getAttribute('href'))
				.toContain('/tdstm/dashboard/userPortal');
		});

		it('should have the text \'Planning Dashboard\' as its link text', function() {
			expect(menu.dashboards.getPlanningDashboard().findElement(by.tagName('a')).getText())
				.toMatch(/Planning Dashboard/);
		});

		it('should have the target URL \'/tdstm/moveBundle/planningStats\'', function() {
			expect(menu.dashboards.getPlanningDashboard().findElement(by.tagName('a')).getAttribute('href'))
				.toContain('/tdstm/moveBundle/planningStats');
		});

		it('should have the text \'Event Dashboard\' as its link text', function() {
			expect(menu.dashboards.getEventDashboard().findElement(by.tagName('a')).getText())
				.toMatch(/Event Dashboard/);
		});

		it('should have the target URL \'/tdstm/dashboard/index\'', function() {
			expect(menu.dashboards.getEventDashboard().findElement(by.tagName('a')).getAttribute('href'))
				.toContain('/tdstm/dashboard/index');
		});
		
  }); // end Child Items

	it('should close the Dashboards Menu',function(){
		menu.dashboards.toggle();
		expect(menu.dashboards.isExpanded()).toBe(false);
	});

}); // end Dashboards Menu
