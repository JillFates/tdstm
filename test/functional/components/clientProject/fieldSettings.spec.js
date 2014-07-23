'use strict';
var Menu = require('../menu/menu.po.js');
var FieldSettings = require('./fieldSettings.po.js');

describe('Field Settings', function() {
	var menu = new Menu();
	var fieldSettingsPage = new FieldSettings();
	it('should load field settings page after clicking Client/Project Field Settings', function() {
		menu.goToClientProject('fieldSettings');
		expect(fieldSettingsPage.getTitle().getText()).toEqual('Project Field Settings');
	});
	
	it('should open the legend on the field settings page', function() {
		var legendButton = fieldSettingsPage.getLegendButton();
		expect(fieldSettingsPage.getLegend().getAttribute('class')).toEqual('legendTable ng-hide');
		legendButton.click();
		expect(fieldSettingsPage.getLegend().getAttribute('class')).toEqual('legendTable');
	});
	
	it('should click the asset tab', function() {
		var tab = fieldSettingsPage.getTab('AssetEntity');
		var tabParent = fieldSettingsPage.getTabParent('AssetEntity');
		tab.click();
		expect(tabParent.getAttribute('class')).toEqual('ng-scope ng-isolate-scope active');
	});
	
	it('should click the application tab', function() {
		var tab = fieldSettingsPage.getTab('Application');
		var tabParent = fieldSettingsPage.getTabParent('Application');
		tab.click();
		expect(tabParent.getAttribute('class')).toEqual('ng-scope ng-isolate-scope active');
	});
	
	it('should click the database tab', function() {
		var tab = fieldSettingsPage.getTab('Database');
		var tabParent = fieldSettingsPage.getTabParent('Database');
		tab.click();
		expect(tabParent.getAttribute('class')).toEqual('ng-scope ng-isolate-scope active');
	});
	
	it('should click the storage tab', function() {
		var tab = fieldSettingsPage.getTab('Storage');
		var tabParent = fieldSettingsPage.getTabParent('Storage');
		tab.click();
		expect(tabParent.getAttribute('class')).toEqual('ng-scope ng-isolate-scope active');
	});
	
	it('should click the edit button', function() {
		fieldSettingsPage.clickEdit();
	});
	
	/*
	it('should click the "I" option on the first row and column', function() {
		var picker = fieldSettingsPage.getImportancePicker();
		picker.click();
	});
	*/
});