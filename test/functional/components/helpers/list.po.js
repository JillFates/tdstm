'use strict';
var List = function () {

};

List.prototype = {};
List.prototype.getListItems = function(expListItems,byColumn) {
  // by default return the line, but can be specify a column
  // send expListItems = 0 if you don't know list lengh (to get default list not after a search)
  if(!byColumn){byColumn='';}
  return browser.driver.wait(function () {
    return browser.driver.findElements(by.css('[role="grid"] tbody tr.ui-widget-content')).then(function(list){
      if(expListItems===0){expListItems=list.length;}
      return list.length===expListItems;
    });
  }).then(function () {
    return browser.driver.findElements(by.css('[role="grid"] tbody tr.ui-widget-content '+byColumn)).then(function(list){
      return list;
    });
  });
};
List.prototype.isListItemSelected = function(listItem) {
  //get checkbox element from a listItem
  //by default if item is not selected returns null - if you check and uncheck it return false
  return listItem.getAttribute('aria-selected');
};
List.prototype.getMessage = function(text) {
  return browser.driver.wait(function () {
    return browser.driver.findElement(by.id('messageId')).getText().then(function (message) {
      return message.indexOf(text) !== -1;
    });
  }).then(function () {
    return browser.driver.findElement(by.id('messageId')).getText();
  });
};
module.exports = List;
