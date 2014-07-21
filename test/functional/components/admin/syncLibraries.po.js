'use strict';
var SyncLibraries = function(){
  this.titles = browser.driver.findElements(by.css('h1'));
};
module.exports = SyncLibraries;