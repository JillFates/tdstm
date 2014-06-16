'use strict';
var Login = function() {

  this.signInbtnCss = '.buttonR input';
  this.validationMessageCss = 'form div.message';
  this.usernameId= 'usernameId';
  this.fieldLabelsCss = 'td tr td';
  this.passwordName= 'password';
  this.titleh1TagName = 'h1';
  this.afterLoginTitleCss =  '.title';

  this.get = function() {
    browser.driver.get(process.env.BASE_URL+'/tdstm/auth/login');
  };
  this.getUserNameLabel = function(){
     return browser.driver.findElements(by.css(this.fieldLabelsCss)).then(function(labels){
      return labels[0];
    });
  };
  this.setUsername = function(name) {
    browser.driver.findElement(by.id(this.usernameId)).sendKeys(name);
  };
  this.clearUsername = function(){
    browser.driver.findElement(by.id(this.usernameId)).clear();
  };
  this.getPasswordLabel = function(){
     return browser.driver.findElements(by.css(this.fieldLabelsCss)).then(function(labels){
      return labels[2];
    });
  };
  this.setPassword = function(password){
    browser.driver.findElement(by.name(this.passwordName)).sendKeys(password);
  };
  this.getTitle = function(){
    return browser.driver.findElement(by.tagName(this.titleh1TagName));
  };
  this.clickSingInBtn = function(){
    browser.driver.findElement(by.css(this.signInbtnCss)).click();
  };
  this.getValidationMessage = function(){
    return browser.driver.findElement(by.css(this.validationMessageCss));
  };
  this.getAfterLoginTitle = function(){
    return browser.driver.findElement(by.css(this.afterLoginTitleCss));
  };
};
module.exports = Login;