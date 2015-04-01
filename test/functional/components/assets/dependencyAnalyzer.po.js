'use strict';
var DependencyAnalizer = function(){
  this.titleh = $('h1');
  this.controlCompactCheckbx = $('#compactControl');
  this.compactClassElements = $$('.compactClass');
  this.regenerateBtn = $('[onclick="showDependencyControlDiv()"]');

  //Dependency Group Control Modal Elements  === Start
  this.depGroupControlModal = $('[aria-describedby="checkBoxDiv"]');
  this.modalTitle = this.depGroupControlModal.$('.ui-dialog-title');
  this.generateBtn = $('[onclick="submitCheckBox()"');

  //Progress Bar
  this.progressBarModal = $('#globalProgressBar');
  this.progressBarTitle = this.progressBarModal.$('#progressTitle');
  this.progressBarCloseBtn = this.progressBarModal.$('#progressClose');
};

DependencyAnalizer.prototype = {};

DependencyAnalizer.prototype.isProgressBarOpened = function() {
  return browser.wait(function(){
    return browser.wait(function(){
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

DependencyAnalizer.prototype.isProgressBarClosed = function() {
  return browser.wait(function waitForModalHides () {
    return browser.driver.findElement(by.id('globalProgressBar')).getAttribute('aria-hidden').then(function (text) {
      return text === 'true';
    });
  }).then(function () {
    return true;
  });
};


DependencyAnalizer.prototype.isGenerationEnded = function() {
 return browser.driver.wait(function () {
    return browser.driver.findElement(by.css('#progressStatus')).getText().then(function (text) {
      return text.indexOf('Finished') === -1;
      });
  }).then(function () {
    return true;
  });
};

DependencyAnalizer.prototype.isCloseButtonDisplayed = function() {
  var that = this;
  return browser.wait(function () {
    return that.progressBarCloseBtn.getAttribute('style').then(function (valor) {
      return valor === '';
    });
  }).then(function () {
    return true;
  });
};

module.exports = DependencyAnalizer;