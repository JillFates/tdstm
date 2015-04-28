'use strict';
var SyncLibraries = function(){
  this.titles = browser.driver.findElement(by.css('h1'));
};
module.exports = SyncLibraries;