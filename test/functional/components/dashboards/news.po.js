'use strict';
var News = function () {
  this.createModalId = 'createNews'; 
  this.editModalId = 'showEditCommentDialog';
};

News.prototype.isCreateOpened = function() {
  var that = this;
  return browser.driver.wait(function(){
    return browser.driver.findElement(by.id(that.createModalId)).isDisplayed().
    then(function(valor){
      return valor;
    });
  }).then(function(){
     return true;
  });
};

News.prototype.isCreateClosed = function() {
  var that = this;
  return browser.driver.wait(function(){
    return browser.driver.findElement(by.id(that.createModalId)).isDisplayed().
    then(function(valor){
      return !valor;
    });
  }).then(function(){
     return true;
  });
};

News.prototype.isEditOpened = function(){
  var that = this;
  return browser.driver.wait(function(){
    return browser.driver.findElement(by.id(that.editModalId)).isDisplayed().
    then(function(valor){
      return valor;
    });
  }).then(function(){
     return true;
  });
};

News.prototype.isEditClosed = function(){
  var that = this;
  return browser.driver.wait(function(){
    return browser.driver.findElement(by.id(that.editModalId)).isDisplayed().
    then(function(valor){
      return !valor;
    });
  }).then(function(){
     return true;
  });
};
News.prototype.addANews = function(news) {
  this.getMessageField().sendKeys(news['message']);
  this.getResolutionField.endKeys(news['Resolution']);


};

News.prototype.getCreateTitle = function() {
  return browser.driver.findElement(by.css('[aria-describedby="createNews"] .ui-dialog-title'));
};

News.prototype.getCreateButtons = function() {
  return browser.driver.findElements(by.css('#createNews .button [type="button"]'));
};

News.prototype.getCreateBtn = function() {
  return browser.driver.findElement(by.css('[onclick="return submitCreateNewsForm()"]'));
};

News.prototype.getCancelBtn = function() {
  return browser.driver.findElement(by.css('[onclick="resetCreateNewsForm();"]'));
};

News.prototype.getTypeLabel = function() {
  return browser.driver.findElement(by.css('#createNewsForm label'));
};

News.prototype.getTypeSelected = function() {
  if(process.env.BROWSER_NAME === 'phantomjs'){
    return browser.driver.executeScript('return $("#createNewsForm select[disabled=\'disabled\'] option:checked").text()');
  }else{
    return browser.driver.findElement(by.css('#createNewsForm select[disabled="disabled"] option:checked')).getText();
  }
};

News.prototype.getMessageLabel = function() {
  return browser.driver.findElement(by.css('#createNewsForm [for="messageId"]'));
};
News.prototype.getMessageField = function() {
  return browser.driver.findElement(by.css('#createNewsForm #messageNews'));
};

News.prototype.getResolvedArchivedLabel = function() {
  return browser.driver.findElement(by.css('#createNewsForm [for="isArchiveId"]'));
};

News.prototype.getResolveArchivedCheck = function() {
  // return browser.driver.findElement(by.css('#createNewsForm #isArchivedId'));
  return browser.driver.findElement(by.css('#createNewsForm #isArchivedHiddenId'));
};

News.prototype.getResolutionLabel = function() {
  return browser.driver.findElement(by.css('#createNewsForm [for="resolutionNews"]'));
};

News.prototype.getResolutionField = function() {
  return browser.driver.findElement(by.css('#createNewsForm #resolutionNews'));
};
module.exports = News;