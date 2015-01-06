'use strict';
var CompareMergeModal = function () {
  this.compareMergeModal = browser.driver.findElement(by.css('[aria-describedby="showOrMergeId"]'));
  this.mergeBtnId = 'mergeModelId';
};

CompareMergeModal.prototype = {};

CompareMergeModal.prototype.isOpened = function() {
  //aria-describedby="showOrMergeId"
  var that = this;
  return browser.driver.wait(function(){
    return that.compareMergeModal.getAttribute('style').then(function(text){
      return text.indexOf('display: block') !== -1;
    });
  }).then(function(){
     return true;
  });
};
CompareMergeModal.prototype.clickMergeBtn = function() {
  var that = this;
  browser.driver.executeScript('return document.getElementById("'+that.mergeBtnId+'").scrollIntoView(true);');
  var mergeBtn = browser.driver.findElement(by.id(that.mergeBtnId));
  mergeBtn.click();
};
module.exports = CompareMergeModal;