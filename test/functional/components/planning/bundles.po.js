'use strict';
var ListAssets = require ('../assets/listAssets.po.js');

var ListBundles = function(){
  this.createBundleBtn = '[onclick="window.location.href=\'/tdstm/moveBundle/create\'"]';
  // Create Bundle
  this.nameId = 'name';
  this.nameLabel =  '';
  this.saveBtnCss = '[onclick="return validateDates()"]';
// Show bundle
  this.messageCss = '.steps_table .message';

  this.getTitle = function(){
    return browser.driver.findElement(by.css('h1'));
  };

// Create Bundle
  this.clickOnCreateBundleBtn = function(){
    browser.driver.findElement(by.css(this.createBundleBtn)).click();
  };

  this.setName = function(name){
    browser.driver.findElement(by.id(this.nameId)).sendKeys(name);
  };  
  this.getName =  function(){
    return browser.driver.findElement(by.id(this.nameId));
  };

  this.clickSaveBundle = function(){
    browser.driver.findElement(by.css(this.saveBtnCss)).click();
  };
  //Show bundle
  this.getConfirmMsg = function(){
    return browser.driver.findElement(by.css(this.messageCss));
  };

};

ListBundles.prototype = new ListAssets();

ListBundles.prototype.getLoadingStyle = function() {
  return browser.driver.findElement(by.id('load_bundleGridIdGrid')).getAttribute('style').then(function(attClass){
    return (attClass.search('display: none;') !== -1);
  });      
};

ListBundles.prototype.getListItems = function(expListItems,appName){
  return browser.driver.wait(function () {
    return browser.driver.findElements(by.css('[role="grid"] tbody tr.ui-widget-content')).then(function(list){
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
  },1000).then(function () {
    return true;
  });
};

ListBundles.prototype.verifySearchResults = function(count,appName){
  var that = this;
  return browser.driver.wait(function(){
    return that.getLoadingStyle()&&that.getListItems(count,appName);
  },1000).then(function(){
      return browser.driver.findElements(by.css('[role="grid"] tbody tr.ui-widget-content')).then(function(list){
        return list;
    });
  },function(){
    return 'No results found';
  });
};

ListBundles.prototype.getSearchNameField = function() {
  return browser.driver.findElement(by.id('gs_name'));
  
};
ListBundles.prototype.selectBundle = function(bundleId) {
  browser.driver.findElement(by.css('a[href="/tdstm/moveBundle/show/'+bundleId+'"]')).click();
};

ListBundles.prototype.clickBundleListBtn = function() {
  browser.driver.findElement(by.css('a[href="/tdstm/moveBundle/list"].list')).click();
};

ListBundles.prototype.getErrors = function() {
  return browser.driver.wait(function () {
    return browser.driver.findElements(by.css('.errors li')).then(function (list) {
      return list.length >0;
    });
    
  },8000).then(function () {
    return browser.driver.findElements(by.css('.errors li'));
  });
};
module.exports = ListBundles;
