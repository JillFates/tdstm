'use strict';
var  Menu = require('../menu/menu.po.js');
var Reports = require('./reports.po.js');
describe('Issue Report', function(){
  var menu = new Menu();
  it('should load Application Migration Report page after click on Reports > Application Migration', function(){
    menu.goToReports('issueReport');
    expect(menu.getCurrentUrl()).toEqual(process.env.BASE_URL+'/tdstm/reports/getBundleListForReportDialog?reportId=Issue+Report');
  });
  it('should have Issue Report as title',function(){
    var issueReportPage = new Reports();
    expect(issueReportPage.getPageTitle()).toEqual('Issue Report');
  });

  describe('bundle dropdown',function(){
    var issueReportPage = new Reports();
    it('should have bundle label', function(){
      expect(issueReportPage.getBundleLabel()).toEqual('    Bundles:*');
    });
    it('should have x options',function(){
      expect(issueReportPage.getBundlesOptionsLength()).toEqual(11);
    });
    it('should have the following options',function(){
      issueReportPage.getBundlesOptions().then(function(list){
        expect(list[0].getText()).toEqual('All Bundles');
        expect(list[1].getText()).toEqual('Buildout');
        expect(list[2].getText()).toEqual('M1');
        expect(list[3].getText()).toEqual('M2');
        expect(list[4].getText()).toEqual('M3');
        expect(list[5].getText()).toEqual('Master Bundle');
        expect(list[6].getText()).toEqual('Not Moving');
        expect(list[7].getText()).toEqual('Retired');
        expect(list[8].getText()).toEqual('Retiring');
        expect(list[9].getText()).toEqual('TBD');
        expect(list[10].getText()).toEqual('Wave1- Bundle1');
      });
    });
  });//bundle dropdown
  describe('Sort report by dropdown', function(){
    var issueReportPage = new Reports();
    it('should have "Sort report by" as label', function(){
      expect(issueReportPage.getSortReportByLabel().getText()).toEqual('Sort report by:');
    });
    it('should have "Asset id" as default option',function(){
      expect(issueReportPage.getSortReportBySelected()).toEqual('Asset Id ');
    });
    it('should have 4 option',function(){
      expect(issueReportPage.getSortReportByOptionsLength()).toEqual(4);
    });
    it('should have the following options',function(){
      issueReportPage.getSortReportByOptions().then(function(list){
        expect(list[0].getText()).toEqual('Asset Id ');
        expect(list[1].getText()).toEqual('Asset Name');
        expect(list[2].getText()).toEqual('Source Location');
        expect(list[3].getText()).toEqual('Target Location');
      });
    });
  });
  describe('checkboxes',function(){
    var issueReportPage = new Reports();
    xit('should have "Include comments in report" as label',function(){
    });
    it('should have Include comments in reports checked by default',function(){
      expect(issueReportPage.getIncludeCommentInReportCheck().getAttribute('checked')).toBe('true');
    });
    xit('should have "Include resolved issues in report" as label',function(){
    });
    it('should have Include resolved isses in report checked by default',function(){
      expect(issueReportPage.getIncludeResolvedIssuesCheck().getAttribute('checked')).toBe('true');
    });
    xit('should have "Include news in report" as label',function(){
    });
    it('should have Include news in report checked by default',function(){
      expect(issueReportPage.getIncludeNewsInReportCheck().getAttribute('checked')).toBe('true');
    });
  });//Checkboxes
  describe('Generate Buttons',function(){
    var issueReportPage = new Reports();
    it('should have 2 buttons',function(){
      issueReportPage.getIssueReportButtons().then(function(list){
        expect(list.length).toEqual(2);
      });
    });
    it('should have generate PDF icon',function(){
      issueReportPage.getIssueReportButtons().then(function(list){
        expect(list[0].getAttribute('title')).toEqual('PDF');
      });
    });
    it('should have PDF icon img',function(){
      issueReportPage.getIssueReportBtnImg().then(function(list){
        expect(list[0].getAttribute('src')).toEqual(process.env.BASE_URL+'/tdstm/plugins/jasper-1.0.0/images/icons/PDF.gif');
      });
    });
    it('should have generate XLS icon',function(){
      issueReportPage.getIssueReportButtons().then(function(list){
        expect(list[1].getAttribute('title')).toEqual('XLS');
      });
    });
    it('should have generate XLS iconimg',function(){
     issueReportPage.getIssueReportBtnImg().then(function(list){
        expect(list[1].getAttribute('src')).toEqual(process.env.BASE_URL+'/tdstm/plugins/jasper-1.0.0/images/icons/XLS.gif');
      });
    });
  });//Generate Buttons
  xdescribe('Generate Report PDF',function(){

  });
  xdescribe('Generate Report XLS',function(){

  });
}); // Issue Report