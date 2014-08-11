'use strict';
var ListBundles = function(){
  this.createBundleBtn = '[onclick="window.location.href=\'/tdstm/moveBundle/create\'"]';
  this.titleCss = 'h1';
  // Create Bundle
  this.nameId = 'name';
  this.nameLabel =  '';
  this.saveBtnCss = '[onclick="return validateDates()"]';
// Show bundle
  this.messageCss = '.steps_table .message';

  this.getTitle = function(){
    return browser.driver.findElement(by.css(this.titleCss));
  };

// Create Bundle
  this.clickOnCreateBundleBtn = function(){
    browser.driver.findElement(by.css(this.createBundleBtn)).click();
  };

  this.setName = function(name){
    browser.driver.findElement(by.id(this.nameId)).sendKeys(name);
  };  
  this.getName =  function(){
    return browser.driver.findElement(by.id(this.nameId));
  };

  this.clickSaveBundle = function(){
    browser.driver.findElement(by.css(this.saveBtnCss)).click();
  };
  //Show bundle
  this.getConfirmMsg = function(){
    return browser.driver.findElement(by.css(this.messageCss));
  };
};
module.exports = ListBundles;
