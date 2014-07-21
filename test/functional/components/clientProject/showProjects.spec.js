'use strict';
var  Menu = require('../menu/menu.po.js');
var ListProjects = require('./listProjects.po.js');
describe('List Projects', function(){
  var menu = new Menu();
  var listProjectPage =  new ListProjects();
	 it('Goes to list projcts and opens a create project window',function(){
		menu.goToClientProject('listProjects');
		browser.driver.findElement(by.css('input[onclick="window.location.href=\'/tdstm/project/create\'"]')).click();
		browser.sleep(2000);
	 });
	 it('Create a new project',function(){
		browser.driver.findElement(by.id('name')).sendKeys('TEST_PROJECT');
		browser.driver.findElement(by.id('projectCode')).sendKeys('TESTY');
		browser.driver.findElement(by.id('workflowCode')).click;
		browser.driver.findElement(by.css('option[value="STD_PROCESS"')).click();
		browser.driver.findElement(by.css('input[class="save"')).click();
		browser.sleep(1000);
	 });
	 it('Goes to test project just created',function(){
		menu.goToClientProject('projDetails');
	 });
	 it('Opens the edit menu and immediately saves',function(){
		browser.driver.findElement(by.css('input[class="edit"')).click();
		browser.driver.findElement(by.css('input[class="save"')).click();
		browser.sleep(1000);
	 });
	 	 it('Deletes Project',function(){
			browser.driver.findElement(by.css('input[class="delete"')).click();
			browser.sleep(5000);
			var alert = browser.driver.switchTo().alert();
			//update is executed
			alert.accept();
			browser.sleep(1000);
	 });
 });