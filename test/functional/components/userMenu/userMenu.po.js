'use strict';
var UserMenu = function() {
  this.UserMenuId = 'loginUserId';
  this.signOutCss = 'a[href="/tdstm/auth/signOut"].mmlink';
  this.accountDetailsCss = 'a[href^="/tdstm/person/getPersonDetails/"]';
  this.editPreferencesCss = '#resetPreferenceId';
  this.useMobileSiteCss = 'a[href="/tdstm/clientTeams/listTasks?viewMode=mobile&tab="]';

  this.open = function() {
    browser.driver.findElement(by.id(this.UserMenuId)).click();
  };
  this.selectSignOut = function(){
    browser.driver.findElement(by.css(this.signOutCss)).click();
  };
  this.selectAccountDetails = function(){
    browser.driver.findElement(by.css(this.accountDetailsCss)).click();
  };
  this.selectEditPreferences = function(){
    browser.driver.findElement(by.css(this.editPreferencesCss)).click();
  };
  this.selectUseMobileSite = function(){
    browser.driver.findElement(by.css(this.useMobileSiteCss)).click();
  };
};
module.exports = UserMenu;