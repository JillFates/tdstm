'use strict';
var ListEventNews = function  () {
  this.createNewsButtonCss = '[onclick="openCreateNewsDialog()"]';

};

ListEventNews.prototype = {};

ListEventNews.prototype.getCreateNewsButton = function() {
  return browser.driver.findElement(by.css(this.createNewsButtonCss));
};
//Modal
ListEventNews.prototype.getCreateNewsModal = function() {
  return browser.driver.findElement(by.css('[aria-describedby="createNewsDialog"]'));
};
ListEventNews.prototype.getModalTitle = function() {
  return browser.driver.findElement(by.css('[aria-describedby="createNewsDialog"] .ui-dialog-title'));
};

ListEventNews.prototype.isCreateOpened = function() {
  var that = this;
  return browser.driver.wait(function () {
    return that.getCreateNewsModal().getAttribute('style').then(function (style) {
      console.log('style',style);
      return style.indexOf('display: block;') !==-1;
    });
  }).then(function () {
    return true;
  })
};

ListEventNews.prototype.isCreateModalClosed = function() {
  var that = this;
  return browser.driver.wait(function () {
    return that.getCreateNewsModal().getAttribute('style').then(function (style) {
      console.log('style',style);
      return style.indexOf('display: none;') !==-1;
    });
  }).then(function () {
    return true;
  })
};

ListEventNews.prototype.getModalCancelBtn = function() {
  return browser.driver.findElement(by.css(('[onclick="$(\'#createNewsDialog\').dialog(\'close\');"]')));
};

module.exports = ListEventNews;