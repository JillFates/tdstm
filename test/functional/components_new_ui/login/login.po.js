'use strict';
var Login = function() {

	this.buildInfoLoc = by.className('buildInfo'); // assign ID
  this.usernameLoc = by.id('usernameid');
  this.passwordLoc = by.name('password');
	this.messageLoc = by.css('#loginForm div.message'); // assign ID
	this.signInButtonLoc = by.id('submitButton');
	this.forgotPasswordLoc = by.css('a[href="/tdstm/auth/forgotMyPassword"]'); // assign ID?

	this.forceSignOut = function() {
		browser.driver.get(process.env.BASE_URL+'/tdstm/auth/signOut');
	};
	this.getDashboard = function() {
		browser.driver.get(process.env.BASE_URL+'/tdstm/dashboard/userPortal');
	};
	this.getMessage = function() {
		return browser.driver.findElement(this.messageLoc).getText();
	};
	this.getBuildInfo = function() {
		return browser.driver.findElement(this.buildInfoLoc).getText();
	};
	this.getForgotPasswordText = function() {
		return browser.driver.findElement(this.forgotPasswordLoc).getText();
	};
	this.clearUsername = function() {
		browser.driver.findElement(this.usernameLoc).clear();
	};
  this.setUsername = function(name) {
    browser.driver.findElement(this.usernameLoc).sendKeys(name);
  };
	this.clearPassword = function() {
		browser.driver.findElement(this.passwordLoc).clear();
	};
  this.setPassword = function(password) {
    browser.driver.findElement(this.passwordLoc).sendKeys(password);
  };
	this.clickSignInBtn = function() {
		browser.driver.findElement(this.signInButtonLoc).click();
	};
};
module.exports = Login;
/*
'use strict';
var Login = function() {

	this.buildInfoClass = 'buildInfo';
  this.usernameId = 'usernameid';
  this.passwordName = 'password';
	this.messageCss = '#loginForm div.message';
	this.signInbtnId = 'submitButton';
	this.forgotPasswordCss = 'a[href="/tdstm/auth/forgotMyPassword"]';

	this.forceSignOut = function() {
		browser.driver.get(process.env.BASE_URL+'/tdstm/auth/signOut');
	};
	this.getDashboard = function() {
		browser.driver.get(process.env.BASE_URL+'/tdstm/dashboard/userPortal');
	};
	this.getMessage = function() {
		return browser.driver.findElement(by.css(this.messageCss)).getText();
	};
	this.getBuildInfo = function() {
		return browser.driver.findElement(by.className(this.buildInfoClass)).getText();
	};
	this.getForgotPasswordText = function() {
		return browser.driver.findElement(by.css(this.forgotPasswordCss)).getText();
	};
	this.clearUsername = function() {
		browser.driver.findElement(by.id(this.usernameId)).clear();
	};
  this.setUsername = function(name) {
    browser.driver.findElement(by.id(this.usernameId)).sendKeys(name);
  };
	this.clearPassword = function() {
		browser.driver.findElement(by.name(this.passwordName)).clear();
	};
  this.setPassword = function(password) {
    browser.driver.findElement(by.name(this.passwordName)).sendKeys(password);
  };
	this.clickSignInBtn = function() {
		browser.driver.findElement(by.id(this.signInbtnId)).click();
	};
};
module.exports = Login;
*/
