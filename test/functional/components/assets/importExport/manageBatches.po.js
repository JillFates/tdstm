'use strict';
var ManageBatches = function () {
  this.get
  
};
ManageBatches.prototype = { };

ManageBatches.prototype.getTitle = function() {
  return browser.driver.findElement(by.css('h1'));
};

ManageBatches.prototype.getReviewLinkList = function() {
  return browser.driver.findElements(by.css('.list tbody [onclick^="kickoffProcess"]'));
};

ManageBatches.prototype.isReviewModalOpened = function() {
  return browser.wait(function(){
    return browser.wait(function(){
      // return browser.driver.findElement(by.id('globalProgressBar')).isDisplayed().then(function (valor) {
      return browser.executeScript('return $("#globalProgressBar").text()').then(function (valor) {
        return valor;
      });
     }).then(function(){
      return browser.driver.findElement(by.id('globalProgressBar')).getAttribute('aria-hidden').then(function(text){
        return text==='false';
      });
     }); 
  }).then(function(){
     return true;
  });
};

ManageBatches.prototype.isReviewEnded = function() {
  var that = this;
  return browser.driver.wait(function(){
      return browser.driver.wait(function() {
      return that.isReviewModalOpened();
    }).then(function () {
      return browser.driver.findElement(by.css('#globalProgressBar #progressTitle h1')).getText().then(function(text){
        return text.indexOf('Reviewing assets in batch') !== -1;
      });
    });
  }).then(function(){
     return true;
  });
};

ManageBatches.prototype.getPostingTitle = function() {
  var that = this;
  return browser.driver.wait(function(){
    return browser.driver.wait(function() {
      return that.isReviewModalOpened();
    }).then(function () {
      return browser.driver.findElement(by.css('#globalProgressBar #progressTitle h1')).getText().then(function(text){
        return text.indexOf('Posting assets to inventory for batch') !== -1;
      });
    });
  }).then(function(){
    return browser.driver.findElement(by.css('#globalProgressBar #progressTitle h1')).getText();
  });
};

ManageBatches.prototype.getReviewResults = function() {
  return browser.driver.wait(function () {
    return browser.driver.findElement(by.css('#progressStatus')).getText().then(function (text) {
      return text.indexOf('Initializing...','In Progress','In progress') === -1;
      });
  }).then(function () {
    return browser.driver.findElement(by.id('progressStatus')).getText().then(function (text) {
      return text;
    });
  });
};

ManageBatches.prototype.getCloseModalBtn = function() {
  return browser.driver.findElement(by.css('button[type="button"]#progressClose'));
};

ManageBatches.prototype.isReviewModalClosed = function() {
  return browser.wait(function waitForModalHides () {
    return browser.driver.findElement(by.id('globalProgressBar')).getAttribute('aria-hidden').then(function (text) {
      return text === 'true';
    });
  }).then(function () {
    return true;
  });
};

ManageBatches.prototype.getProcessLinkList = function() {
  return browser.driver.wait(function () {
    return browser.driver.findElements(by.css('.list tbody [onclick^="return kickoffProcess"]')).then(function (list) {
      return list.length>0;
    });
  }).then(function () {
    return browser.driver.findElements(by.css('.list tbody [onclick^="return kickoffProcess"]'));
  });
};

ManageBatches.prototype.getRemoveLinkList = function() {
  return browser.driver.wait(function () {
    return browser.driver.findElements(by.css('.list tbody a[href^="/tdstm/dataTransferBatch/delete?batchId="]')).then(function (list) {
      return list.length>0;
    });
  }).then(function () {
    return browser.driver.findElements(by.css('.list tbody a[href^="/tdstm/dataTransferBatch/delete?batchId="]'));
  });
};

ManageBatches.prototype.getStatusList = function() {
  return browser.driver.findElements(by.css('[id^="statusCode"]'));
};

ManageBatches.prototype.getBatchesList = function() {
  return browser.driver.findElements(by.css('.list tbody tr'));
};
module.exports = ManageBatches;