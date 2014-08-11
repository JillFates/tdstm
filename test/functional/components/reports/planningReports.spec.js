'use strict';
var  Menu = require('../menu/menu.po.js');
describe('Application Profiles', function(){
  var menu = new Menu();
	 it('should attempt to generate an application profiles report and check that it reports something',function(){
		browser.driver.findElement(by.css('a[href="javascript:showMegaMenu(\'#reportsMegaMenu\')"]')).click();
		browser.driver.findElement(by.css('a[href="/tdstm/reports/applicationProfiles"]')).click();
		browser.driver.findElement(by.id('applicationProfilesButton')).click();
		var ptr = protractor.getInstance();
		ptr.ignoreSynchronization = true;
		var element = by.css('a[class="inlineLink"]');
		expect(ptr.isElementPresent(element)).toBe(true);
		//expect(browser.driver.findElement(by.css('input[class="inlineLink"'))).not.toBeNull();
	 });
	 it('should attempt to generate an application conflicts report and check that it reports something',function(){
		browser.driver.findElement(by.css('a[href="javascript:showMegaMenu(\'#reportsMegaMenu\')"]')).click();
		browser.driver.findElement(by.css('a[href="/tdstm/reports/applicationConflicts"]')).click();
		browser.driver.findElement(by.id('applicationConflictsButton')).click();
		var ptr = protractor.getInstance();
		ptr.ignoreSynchronization = true;
		var element = by.css('a[class="inlineLink"]');
		expect(ptr.isElementPresent(element)).toBe(true);
		//expect(browser.driver.findElement(by.css('input[class="inlineLink"'))).not.toBeNull();
	 });
	 it('should attempt to generate a server conflicts report and check that it reports something',function(){
		browser.driver.findElement(by.css('a[href="javascript:showMegaMenu(\'#reportsMegaMenu\')"]')).click();
		browser.driver.findElement(by.css('a[href="/tdstm/reports/serverConflicts"]')).click();
		browser.driver.findElement(by.id('serverConflictsButton')).click();
		var ptr = protractor.getInstance();
		ptr.ignoreSynchronization = true;
		var element = by.css('a[class="inlineLink"]');
		expect(ptr.isElementPresent(element)).toBe(true);
		//expect(browser.driver.findElement(by.css('input[class="inlineLink"'))).not.toBeNull();
	 });
	 xit('should attempt to generate a database conflicts report and check that it reports something',function(){
		browser.driver.findElement(by.css('a[href="javascript:showMegaMenu(\'#reportsMegaMenu\')"]')).click();
		browser.driver.findElement(by.css('a[href="/tdstm/reports/databaseConflicts"]')).click();
		browser.driver.findElement(by.id('databaseConflictsButton')).click();
		var ptr = protractor.getInstance();
		ptr.ignoreSynchronization = true;
		var element = by.css('a[class="inlineLink"]');
		expect(ptr.isElementPresent(element)).toBe(true);
		//expect(browser.driver.findElement(by.css('input[class="inlineLink"'))).not.toBeNull();
	 });
	 	 it('should attempt to generate a task report and check that it reports something',function(){
		browser.driver.findElement(by.css('a[href="javascript:showMegaMenu(\'#reportsMegaMenu\')"]')).click();
		browser.driver.findElement(by.css('a[href="/tdstm/reports/getBundleListForReportDialog?reportId=Task+Report"]')).click();
		browser.driver.findElement(by.css('input[value="Generate Web"]')).click();
		var ptr = protractor.getInstance();
		ptr.ignoreSynchronization = true;
		var element = by.css('tr[class="even"]');
		expect(ptr.isElementPresent(element)).toBe(true);
		//expect(browser.driver.findElement(by.css('input[class="inlineLink"'))).not.toBeNull();
	 });
 });