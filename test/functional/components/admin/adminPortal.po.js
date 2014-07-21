'use strict';
var AdminPortal = function(){
  this.titles =  browser.driver.findElements(by.css('h1'));
};
module.exports = AdminPortal;