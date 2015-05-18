'use strict';
var UserMenu = function() {
  this.userMenuId = 'loginUserId';
  this.signOutCss = 'a[href="/tdstm/auth/signOut"].mmlink';
  this.accountDetailsCss = 'a[href^="/tdstm/person/retrievePersonDetails/"]';
  this.editPreferencesCss = '#resetPreferenceId';
  this.useMobileSiteCss = 'a[href="/tdstm/clientTeams/listTasks?viewMode=mobile&tab="]';
  this.userMegaMenuId = 'userMegaMenu';
  

  this.open = function() {
    browser.driver.findElement(by.id(this.userMenuId)).click();
  };
  this.getUserMegaMenu = function () {
    return browser.driver.findElement(by.id(this.userMegaMenuId));
  };
  // this.getLinksList = function () {
    // return browser.driver.findElements(by.css(this.linkListCss));
  // }
  this.selectSignOut = function(){
    browser.driver.findElement(by.css(this.signOutCss)).click();
  };
  this.selectAccountDetails = function(){
    browser.driver.findElement(by.css(this.accountDetailsCss)).click();
  };
  // Account Details
  this.getAccountDetailsModal = function () {
    return browser.driver.findElement(by.css('[aria-describedby="personDialog"]'));
  };

  this.getAccountDetailsModalTitle = function () {
    return browser.driver.findElement(by.css('[aria-describedby="personDialog"] .ui-dialog-title'));
  };
  this.closeAccountDetailsModal = function () {
    browser.driver.findElement(by.css('[aria-describedby="personDialog"] [title="close"]')).click();
  }
  this.isAccountDetailsModalOpened = function () {
    var that = this;
    return browser.wait(function(){
      return that.getAccountDetailsModal().isDisplayed().then(function(valor){
        return valor;
      });
    }).then(function(){
       return true;
    });
  };
  this.isAccountDetailsModalClosed = function () {
    var that = this;
    return browser.wait(function(){
      return that.getAccountDetailsModal().isDisplayed().then(function(valor){
        return !valor;
      });
    }).then(function(){
       return true;
    });
    
  };
  // User Preference
  this.selectEditPreferences = function(){
    browser.driver.findElement(by.css(this.editPreferencesCss)).click();
  };
  this.getUserPreferencesModal = function () {
    return browser.driver.findElement(by.css('[aria-describedby="userPrefDivId"]'));
  };

  this.getUserPreferencesModalTitle = function () {
    return browser.driver.findElement(by.css('[aria-describedby="userPrefDivId"] .ui-dialog-title'));
  };

  this.closeUserPreferencesModal = function () {
    browser.driver.findElement(by.css('[aria-describedby="userPrefDivId"] [title="close"]')).click();
  };

  this.isUserPreferencesModalOpened =function () {
    var that = this;
    return browser.wait(function(){
      return that.getUserPreferencesModal().isDisplayed().then(function(valor){
        return valor;
      });
    }).then(function(){
       return true;
    });
  };

  this.isUserPreferencesModalClosed = function () {
    var that = this;
    return browser.wait(function(){
      return that.getUserPreferencesModal().isDisplayed().then(function(valor){
        return !valor;
      });
    }).then(function(){
       return true;
    });
  };

  this.selectUseMobileSite = function(){
    browser.driver.findElement(by.css(this.useMobileSiteCss)).click();
  };

  this.getUserMobileTitle = function () {
    return browser.driver.findElement(by.css('#mobtitle'));
  }
};
UserMenu.prototype = {};

UserMenu.prototype.getLinksList = function() {
  return browser.driver.findElements(by.css('#userMegaMenu li a'));
};
module.exports = UserMenu;