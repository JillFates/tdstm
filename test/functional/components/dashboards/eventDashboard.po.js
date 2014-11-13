'use strict';
var EventDashboard = function(){};
EventDashboard.prototype.getEventLabel = function(){
  return browser.driver.findElement(by.css('[for="moveEvent"]'));
};
EventDashboard.prototype.getEventField = function(){
  return browser.driver.findElement(by.id('#moveEvent'));
};
EventDashboard.prototype.getEventOptions = function(){
  return browser.driver.findElements(by.css('#moveEvent option'));
};
EventDashboard.prototype.getEventSelected = function(){
  if(process.env.BROWSER_NAME ==='phantomjs'){
    return browser.executeScript('return $("#moveEvent option:checked").text()');
  }else{
    return browser.driver.findElement(by.css('#moveEvent option:checked')).getText();
  }
};
EventDashboard.prototype.getEventOptionsLength = function(){
  if(process.env.BROWSER_NAME==='phantomjs'){
    return browser.driver.executeScript('return $("#moveEvent option").length');
  }else{
    return browser.driver.findElements(by.css('#moveEvent option')).then(function(list){
      return list.length;
    });
  }
};
EventDashboard.prototype.getAddNewsBtn = function(){
  return browser.driver.findElement(by.css('[onclick="opencreateNews()"]'));
};
EventDashboard.prototype.getTaskSummaryTitle = function(){
  return browser.driver.findElement(by.css('#taskSummary h3'));
};

module.exports = EventDashboard;