'use strict';
var ListProjects = function(){
  this.titleh1Css = 'h1';
  this.createProjectbtnCss = 'input[value="Create Project"]';
  this.createProjecth1Css = 'h1';
  this.projetsOnListCss2 = '#projectGridIdGrid tbody tr.ui-widget-content';
  this.projetsOnListCss = '#projectGridIdGrid tbody tr';
  this.searchProjectCodeId = 'gs_projectCode';
  this.refreshbtnCss = '#projectGridId span';
  this.projectsLinkListCss = this.projetsOnListCss+' a';


  this.getTitle = function(){
    return browser.driver.findElement(by.css(this.titleh1Css));
  };
  this.clickOnCreateProjectBtn = function(){
    browser.driver.findElement(by.css(this.createProjectbtnCss)).click();
  };
  this.setSearchProjectCode = function(projCode){
    browser.driver.findElement(by.id(this.searchProjectCodeId)).sendKeys(projCode);
  };
  this.getSearchProjectCode = function(){
    return browser.driver.findElement(by.id(this.searchProjectCodeId));
  };

  this.verifySearchResults = function(count){
    var that=this;
    var webdriver = require('selenium-webdriver');
    var d = webdriver.promise.defer();
    browser.driver.wait(function(){
      return  browser.driver.findElements(by.css(that.projectsLinkListCss)).then(function(list){
        return list.length >=0 && list.length <=count;
      });
    }).then(function(){
      browser.driver.findElements(by.css(that.projetsOnListCss)).then(function(list){
        d.fulfill(list);
      });
    });
    return d.promise;
  };

  this.selectProjectfromListByPos = function(pos, projName){
    pos--;
    browser.driver.findElements(by.css(this.projectsLinkListCss)).then(function(list){
      expect(list[pos].getText()).toEqual(projName);
      list[pos].click();
    });
  };
};
module.exports = ListProjects;