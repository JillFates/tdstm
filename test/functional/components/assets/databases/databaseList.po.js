'use strict';
var ListAssets = require('../listAssets.po.js');
var DatabaseList = function(){
  this.databaseLoadingId = 'load_databaseIdGrid';
  this.createDBBtn = $('input[onclick="EntityCrud.showAssetCreateView(\'DATABASE\');"]');
};

DatabaseList.prototype = new ListAssets();

DatabaseList.prototype.isLoadingHidden = function () {
  var that = this;
  return browser.wait(function () {
    return element(by.id(that.databaseLoadingId)).isDisplayed().then(function (valor) {
      return !valor;
    });
  },8000).then(function () {
    return true;
  });
};

DatabaseList.prototype.getLoadingStyle = function() {
  return element(by.id(this.databaseLoadingId)).getAttribute('style').then(function(attClass){
    return (attClass.search('display: none;') !== -1);
  });      
};

DatabaseList.prototype.getListItems = function(expListItems,appName){
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

DatabaseList.prototype.verifySearchResults = function(count,appName){
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

module.exports = DatabaseList;