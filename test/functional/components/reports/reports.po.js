'use strict';
var CablingConflict = function(){
  this.getPageTitle = function(){
    return browser.driver.findElement(by.css('h1')).getText();
  };
  this.getGenerateCablingConflictBtn = function(){
    return browser.driver.findElement(by.css('[onclick="return submit_CablingQAReport(this)"]'));
  };
  this.generateApplicationConflictsBtn = function(){
    return browser.driver.findElement(by.id('applicationConflictsButton'));
  };
  this.generateCablingDataBtn = function(){
    return browser.driver.findElement(by.css('[onclick="return submit_CablingDataReport(this)"]'));
  };
  this.generateApplicationProfilesBtn = function(){
    return browser.driver.findElement(by.id('applicationProfilesButton'));
  };
  this.getMessage = function(){
    return browser.driver.findElement(by.css('.main_bottom .body .message'));
  }
  this.getBundleLabel = function(){
    return browser.driver.findElement(by.css('#bundleRow label')).getText();
  };
  this.getBundlesDropdown = function(){
    return browser.driver.findElement(by.css('#moveBundleId'));
  };
  this.getBundleSelected = function(){
    if(process.env.BROWSER_NAME==='phantomjs'){
     return browser.driver.executeScript('return $("#moveBundleId option:checked").text()');
    }else{
     return browser.driver.findElement(by.css('#moveBundleId option:checked')).getText();
    }
  };
  this.getBundlesOptionsLength = function(){
    if(process.env.BROWSER_NAME==='phantomjs'){
      return browser.driver.executeScript('return $("#moveBundleId option").length');
    }else{
      return browser.driver.findElements(by.css('#moveBundleId option')).then(function(list){
        return list.length;
      });
    }
  };
  this.getBundlesOptions = function(){
    if(process.env.BROWSER_NAME==='phantomjs'){
      return browser.driver.executeScript('return $("#moveBundleId option")');
    }else{
      return browser.driver.findElements(by.css('#moveBundleId option'));
    }
  };
  this.getCableTypeLabel = function(){
    return browser.driver.findElements(by.css('label')).then(function(list){
      return list[1].getText();
    });
  };
  this.getCableTypeSelected = function(){
    if(process.env.BROWSER_NAME==='phantomjs'){
     return browser.driver.executeScript('return $("#cableTypeId option:checked").text()');
    }else{
     return browser.driver.findElement(by.css('#cableTypeId option:checked')).getText();
    }
  };
  this.getCableTypeOptionsLength = function(){
    if(process.env.BROWSER_NAME==='phantomjs'){
      return browser.driver.executeScript('return $("#cableTypeId option").length');
    }else{
      return browser.driver.findElements(by.css('#cableTypeId option')).then(function(list){
        return list.length;
      });
    }
  };
  this.getCableTypeOptions = function(){
    if(process.env.BROWSER_NAME==='phantomjs'){
      return browser.driver.executeScript('return $("#cableTypeId option")');
    }else{
      return browser.driver.findElements(by.css('#cableTypeId option'));
    }
  };
};
module.exports = CablingConflict;