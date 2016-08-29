'use strict';
var ProjectStaff = function(){
  this.getTitle = $('h1');
  this.teamLabel = $('#teamLabel');
  this.teamField = $('#role');
  this.teamOptions = this.teamField.$$('option');
  this.teamSelected = this.teamField.$('option:checked');
  this.projectLabel = $('#projectLabel');
  this.projectField = $('#project');
  this.projectOptions = this.projectField.$$('option');
  this.projectSelected = this.projectField.$('option:checked');
  this.onlyClientStaffLabel = $('#onlyClientStaffLabel');
  this.onlyClientStaffCheck = $('#clientStaffId');
  this.onlyAssignedLabel = $('#onlyAssignedLabel');
  this.onlyAssignedCheck = $('#assignedId');
  this.table = $('#projectStaffTableId table');
  this.tableHeads = this.table.$$('thead th');
  this.tableNameList = $$('.js-staffFullName span');
  this.tableCompanyList = $$('.js-staffCompany span');
  this.tableTeamList = $$('.js-staffRole span');
  this.tableProjectList = $$('.js-staffProject');
};

ProjectStaff.prototype = {};

ProjectStaff.prototype.getTableNameList = function() {
   return browser.executeScript(function () {
    var list = [];
    $('.js-staffFullName span').text(function( index,text ) {
      list[index] =text.trim();
    });
    return list;
  });
};

ProjectStaff.prototype.getTableTeamList = function() {
  return browser.executeScript(function () {
    var list = [];
    $('.js-staffRole span').text(function( index,text ) {
      list[index] =text.trim();
    });
    return list;
  });
};

ProjectStaff.prototype.getTableCompanyList = function() {
  return browser.executeScript(function () {
    var list = [];
    $('.js-staffCompany span').text(function( index,text ) {
      list[index] =text.trim();
    });
    return list;
  });
};
ProjectStaff.prototype.getTeamSelected = function() {
  if(process.env.BROWSER_NAME ==='phantomjs'){
    return  browser.executeScript('return $("#role option:checked").text()');
  }else{
    return this.teamSelected.getText();
  }
};

ProjectStaff.prototype.getProjectSelected = function() {
  if(process.env.BROWSER_NAME ==='phantomjs'){
    return  browser.executeScript('return $("#project option:checked").text()');
  }else{
    return this.projectSelected.getText();
  }
};

module.exports = ProjectStaff;
