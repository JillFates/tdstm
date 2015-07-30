'use strict';
var ListAssets = require('../listAssets.po.js');

var ListApps = function () {
  this.createAppBtn = $('input[value="Create App"]');
  this.applicationLoadingId = 'load_applicationIdGrid';
};

ListApps.prototype = new ListAssets();

ListApps.prototype.isLoadingHidden = function () {
  var that = this;
  return browser.wait(function () {
    return element(by.id(that.applicationLoadingId)).isDisplayed().then(function (valor) {
      return !valor;
    });
  },8000).then(function () {
    return true;
  });
};

ListApps.prototype.getLoadingStyle = function() {
  return element(by.id(this.applicationLoadingId)).getAttribute('style').then(function(attClass){
    return (attClass.search('display: none;') !== -1);
  });      
};

ListApps.prototype.getListItems = function(expListItems,appName){
  return browser.wait(function () {
    return $$('[role="grid"] tbody tr.ui-widget-content').then(function(list){
      if(list.length===expListItems){
        if(expListItems === 1){
          return list[0].getText().then(function (text) {
            return text.search(appName) !== -1;
          });
        }else {
          return true;
        }
      }else{
      	return false;
      }
    });
  },8000).then(function () {
    return true;
  });
};

ListApps.prototype.verifySearchResults = function(count,appName){
  var that = this;
  return browser.wait(function(){
    return that.getLoadingStyle()&&that.getListItems(count,appName);
  },8000).then(function(){
      return $$('[role="grid"] tbody tr.ui-widget-content').then(function(list){
        return list;
    });
  },function(){
    return 'No results found';}
  );
};

module.exports = ListApps;