'use strict',
var Events = function(){
//Event List
  this.getTitle = function(){
    return browser.driver.findElement(by.css('h1'));
  };
// Create Event
  this.getCreateEventBtn = function(){
    browser.driver.findElement(by.css('[onclick="window.location.href=\'/tdstm/moveEvent/create\'"]'));
  }
};
module.exports = Events;