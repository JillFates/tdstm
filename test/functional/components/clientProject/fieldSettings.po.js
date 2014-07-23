'use strict';
var FieldSettings = function () {
	this.titleh1Css = 'div.main_bottom h1';
	this.legendButtonCss = 'h1.assetImage';
	this.legendCss = 'table.legendTable';
	this.tabsCss = 'ul.nav-tabs li';
	this.editButtonCss = 'div.tab-pane.ng-scope.active table tbody tr td div span input.edit[ng-click="toggleEditMode(type.name)"]';
	this.updateButtonsDivCss = 'div[src="\'editImportance.gsp\'"] div.active';
	this.importancePickerCss = 'td div[class="pickbox ng-scope ng-binding"]';
	
	this.createProjecth1Css = 'h1';
	this.projetsOnListCss = '#projectGridIdGrid tbody tr.ui-widget-content';
	this.searchProjectCodeId = 'gs_projectCode';
	this.refreshbtnCss = '#projectGridId span';
	this.projectsLinkListCss = this.projetsOnListCss + ' a';
	
	this.getTitle = function () {
		return browser.driver.findElement(by.css(this.titleh1Css));
	};
	this.getLegend = function () {
		return browser.driver.findElement(by.css(this.legendCss));
	};
	this.getLegendButton = function () {
		return browser.driver.findElement(by.css(this.legendButtonCss));
	};
	this.getTabs = function () {
		return browser.driver.findElement(by.css(this.tabsCss));
	};
	this.getTab = function (tab) {
		var selector = this.tabsCss + '[heading="' + tab + '"] a';
		return browser.driver.findElement(by.css(selector));
	};
	this.getTabParent = function (tab) {
		var selector = this.tabsCss + '[heading="' + tab + '"]';
		return browser.driver.findElement(by.css(selector));
	};
	this.clickEdit = function () {
		var buttons = browser.driver.findElements(by.css(this.editButtonCss));
	};
	this.getUpdateButtons = function () {
		return browser.driver.findElement(by.css(this.updateButtonsDivCss));
	};
	this.getImportancePicker = function () {
		return browser.driver.findElement(by.css(this.importancePickerCss));
	};
};
module.exports = FieldSettings;