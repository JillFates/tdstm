'use strict';
var  Menu = require('./menu.po.js');
describe('Projects Menu', function(){

  var menu = new Menu();

  it('should have \'Projects\' as the parent label', function() {
    expect(menu.projects.getParent().getText()).toEqual('Projects');
  });

	it('should expand the Projects Menu',function(){
		menu.projects.toggle();
		expect(menu.projects.isExpanded()).toBe(true);
	});

  describe('Child Items', function() {

		it('should have the text \'Active Projects\' as its link text', function() {
			expect(menu.projects.getActiveProjects().findElement(by.tagName('a')).getText())
				.toMatch(/Active Projects/);
		});

		it('should have the target URL \'/tdstm/project/list?active=active\'', function() {
			expect(menu.projects.getActiveProjects().findElement(by.tagName('a')).getAttribute('href'))
				.toContain('/tdstm/project/list?active=active');
		});

		it('should have the text \'[Project Name] Details\' as its link text', function() {
			expect(menu.projects.getProjectDetails().findElement(by.tagName('a')).getText())
				.toMatch(/.+ Details/);
		});

		it('should have the target URL \'/tdstm/projectUtil/index\'', function() {
			expect(menu.projects.getProjectDetails().findElement(by.tagName('a')).getAttribute('href'))
				.toContain('/tdstm/projectUtil/index');
		});

		it('should have the text \'Project Staff\' as its link text', function() {
			expect(menu.projects.getProjectStaff().findElement(by.tagName('a')).getText())
				.toMatch(/Project Staff/);
		});

		it('should have the target URL \'/tdstm/person/manageProjectStaff\'', function() {
			expect(menu.projects.getProjectStaff().findElement(by.tagName('a')).getAttribute('href'))
				.toContain('/tdstm/person/manageProjectStaff');
		});

		it('should have the text \'User Activation Emails\' as its link text', function() {
			expect(menu.projects.getUserActivationEmails().findElement(by.tagName('a')).getText())
				.toMatch(/User Activation Emails/);
		});

		it('should have the target URL \'/tdstm/project/userActivationEmailsForm\'', function() {
			expect(menu.projects.getUserActivationEmails().findElement(by.tagName('a')).getAttribute('href'))
				.toContain('/tdstm/project/userActivationEmailsForm');
		});

		it('should have the text \'Field Settings\' as its link text', function() {
			expect(menu.projects.getFieldSettings().findElement(by.tagName('a')).getText())
				.toMatch(/Field Settings/);
		});

		it('should have the target URL \'/tdstm/project/fieldImportance\'', function() {
			expect(menu.projects.getFieldSettings().findElement(by.tagName('a')).getAttribute('href'))
				.toContain('/tdstm/project/fieldImportance');
		});

  }); // end Child Items

	it('should close the Projects Menu',function(){
		menu.projects.toggle();
		expect(menu.projects.isExpanded()).toBe(false);
	});
	
}); // Projects Menu
