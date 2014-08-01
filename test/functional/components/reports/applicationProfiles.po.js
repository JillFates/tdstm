'use strict';
var ApplicationProfiles = function(){
  this.titleh = browser.driver.findElement(by.css('h1'));
  this.generateBtn = browser.driver.findElement(by.id('applicationProfilesButton'));
};
module.exports = ApplicationProfiles;