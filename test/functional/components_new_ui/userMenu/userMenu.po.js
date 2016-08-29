'use strict';
var UserMenu = function() {
	this.userMenuLoc = by.className('user-menu'); // assign ID
	this.userNameLoc = by.className('user-name'); // assign ID
	this.listLoc = by.className('list-group'); // assign ID?
  this.signOutLoc = by.css('a[href="/tdstm/auth/signOut"]'); // assign ID
	this.userMenuListItemLoc = by.className('list-group-item');

	// assign IDs for both list items and aria modal widgets
  this.accountDetails = {
		itemLoc: by.partialLinkText('Account Details'),
		modalLoc: by.css('[aria-describedby="personDialog"]')
	};
  this.dateAndTimezone = {
		itemLoc: by.partialLinkText('Date and Timezone'),
		modalLoc: by.css('[aria-describedby="userTimezoneDivId"]')
	};
  this.editPreferences = {
		itemLoc: by.partialLinkText('Edit preferences'),
		modalLoc: by.css('[aria-describedby="userPrefDivId"]')
	};
  this.modelScore = {
		itemLoc: by.partialLinkText('Model Score')
	};

	this.modalTitleLoc = by.className('ui-dialog-title');
	this.modalCloseLoc = by.className('ui-dialog-titlebar-close');

  this.toggle = function() {
    browser.driver.findElement(this.userMenuLoc).click();
  };
  this.clickSignOut = function() {
    browser.driver.findElement(this.signOutLoc).click();
		// webdriver acts too fast and does not wait for next page unless sleep is implemented
		browser.sleep(1000);
  };
	this.getUserName = function() {
		return browser.driver.findElement(this.userNameLoc).getText();
	};
	this.getList = function() {
		return browser.driver.findElement(this.listLoc);
	};
	this.getListItems = function() {
		return browser.driver.findElements(this.userMenuListItemLoc);
	};
  this.getListItem = function(item) {
    return this.getList().findElement(this[item].itemLoc);
  };
	this.getModal = function(modal) {
		return browser.driver.findElement(this[modal].modalLoc);
	};
	this.getModalTitle = function(modal) {
		return this.getModal(modal).findElement(this.modalTitleLoc).getText();
	};
	this.closeModal = function(modal) {
		this.getModal(modal).findElement(this.modalCloseLoc).click();
	};
	this.isModalOpen = function(modal) {
		var that = this;
		return browser.driver.wait(function() {
    	return that.getModal(modal).isDisplayed();
		}, 1000).then(function() {
			return true; // waits at least 1 second for modal to appear
		}, function() {
			return false; // otherwise returns false
		});
	};
};

module.exports = UserMenu;
