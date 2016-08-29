'use strict';
var ListProjects = function(){
  this.projetsOnListCss2 = '#projectGridIdGrid tbody tr.ui-widget-content';
  this.projetsOnListCss = '#projectGridIdGrid tbody tr';
  // this.refreshbtnCss = '#projectGridId span';
  this.projectsLinkListCss = this.projetsOnListCss+' a';

};

ListProjects.prototype = {};

ListProjects.prototype.getTitle = function() {

  return browser.driver.findElement(by.css('h1'));

};

// Buttons starts
ListProjects.prototype.clickOnCreateProjectBtn = function() {

  browser.driver.findElement(by.css('input[value="Create Project"]')).click();

};
// Buttons ends

ListProjects.prototype.getSearchProjectCode = function() {
  return browser.driver.findElement(by.id('gs_projectCode'));

};
ListProjects.prototype.setSearchProjectCode = function(projCode) {
  var field = this.getSearchProjectCode();
  field.clear();
  field.sendKeys(projCode);
};

ListProjects.prototype.verifySearchResults = function(count) {

  var that=this;
  var webdriver = require('selenium-webdriver');
  var d = webdriver.promise.defer();
  browser.driver.wait(function(){
    return  browser.driver.findElements(by.css(that.projectsLinkListCss)).then(function(list){
      return list.length >=0 && list.length <=count;
    });
  },8000).then(function(){
    browser.driver.findElements(by.css(that.projetsOnListCss)).then(function(list){
      d.fulfill(list);
    });
  });
  return d.promise;

};

ListProjects.prototype.selectProjectfromListByPos= function(pos,projName) {

  pos--;

  browser.driver.findElements(by.css(this.projectsLinkListCss)).then(function(list){

    expect(list[pos].getText()).toEqual(projName);
    list[pos].click();

  });

};
ListProjects.prototype.selectProjectIfExists = function(projCode) {

  var webdriver = require('selenium-webdriver');
  var d = webdriver.promise.defer();

  this.setSearchProjectCode(projCode);
  this.verifySearchResults(2).then(function(list){
    if(list.length ===2){
      list[1].getAttribute('id').then(function(projId){
        var selector = '#projectGridIdGrid a[href="/tdstm/project/addUserPreference/'+projId+'"]';
        browser.driver.findElement(by.css(selector)).click();
        d.fulfill(projId);
      });
    }else{
      d.fulfill(-1);
    }
  });

  return d.promise;

};
module.exports = ListProjects;