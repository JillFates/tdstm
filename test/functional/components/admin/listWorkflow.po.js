'use strict';
var ListWorkFlow = function(){
  this.header = browser.driver.findElement(by.css('span b'));
};
module.exports = ListWorkFlow;