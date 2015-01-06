'use strict';
var List = require('../helpers/list.po.js');
var ListStaff = function(){
  this.titleh = browser.driver.findElement(by.css('.body h1'));
  this.compareMergeBtn = browser.driver.findElement(by.id('compareMergeId'));
};
ListStaff.prototype = new List();
ListStaff.prototype.isCompareMergeDisabled = function () {
  var that = this;
  return browser.driver.wait(function () {
    return that.compareMergeBtn.getAttribute('disabled').then(function (valor) {
      console.log('no valor', valor);
      return valor;
    });
  }).then(function () {
    return true;
  });
};

ListStaff.prototype.isCompareMergeEnabled = function() {
  var that = this;
  return browser.driver.wait(function () {
    return that.compareMergeBtn.getAttribute('disabled').then(function (valor) {
      return !valor;
    });
  }).then(function () {
    return true;
  });
};
module.exports = ListStaff;