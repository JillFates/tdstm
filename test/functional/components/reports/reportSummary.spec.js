'use strict';
var  Menu = require('../menu/menu.po.js');
var Reports = require('./reports.po.js');
describe('Report Summary', function(){
  var menu = new Menu();
  it('should load Report Summary page after click on Reports >  Report Summary', function(){
    menu.goToReports('reportSummary');
    expect(menu.getCurrentUrl()).toMatch(process.env.BASE_URL+'/tdstm/reports/index.projectId=\\d{4,6}');
  });
  it('should have 5 titles',function(){
    var reprotSummaryPage = new Reports();
    expect(reprotSummaryPage.getPageTitlesLength()).toEqual(5);
  });
  it('should have the these titles',function(){
    var reprotSummaryPage = new Reports();
    reprotSummaryPage.getPageTitlesList().then(function(list){
      expect(list[0].getText()).toEqual('Discovery');
      expect(list[1].getText()).toEqual('Move Prep');
      expect(list[2].getText()).toEqual('Move Day');
      expect(list[3].getText()).toEqual('Application');
      expect(list[4].getText()).toEqual('Infrastructure');
    });
  });
  describe('Report Links', function(){
    var reprotSummaryPage = new Reports();
    it('should have 12 links', function(){
      expect(reprotSummaryPage.getReportsListLength()).toEqual(12);

    });
    it('should have expected label and href defined', function(){
      reprotSummaryPage.getReportsList().then(function(list){
        expect(list[0].getText()).toEqual('admin report1');
        expect(list[0].getAttribute('href')).toMatch(process.env.BASE_URL+'/tdstm/reports/index.projectId=\\d{4,6}#');
        expect(list[1].getText()).toEqual('Pre-move Checklist');
        expect(list[1].getAttribute('href')).toEqual(process.env.BASE_URL+'/tdstm/reports/preMoveCheckList');
        expect(list[2].getText()).toEqual('Login Badges');
        expect(list[2].getAttribute('href')).toEqual(process.env.BASE_URL+'/tdstm/reports/getBundleListForReportDialog?reportId=Login+Badges');
        expect(list[3].getText()).toEqual('Asset Tags');
        expect(list[3].getAttribute('href')).toEqual(process.env.BASE_URL+'/tdstm/reports/getBundleListForReportDialog?reportId=Asset+Tag');
        expect(list[4].getText()).toEqual('Transport Worksheets');
        expect(list[4].getAttribute('href')).toEqual(process.env.BASE_URL+'/tdstm/reports/getBundleListForReportDialog?reportId=Transportation+Asset+List');
        expect(list[5].getText()).toEqual('Issue Report');
        expect(list[5].getAttribute('href')).toEqual(process.env.BASE_URL+'/tdstm/reports/getBundleListForReportDialog?reportId=Issue+Report');
        expect(list[6].getText()).toEqual('Move Results');
        expect(list[6].getAttribute('href')).toEqual(process.env.BASE_URL+'/tdstm/reports/getBundleListForReportDialog?reportId=MoveResults');
        expect(list[7].getText()).toEqual('Cabling QA');
        expect(list[7].getAttribute('href')).toEqual(process.env.BASE_URL+'/tdstm/reports/getBundleListForReportDialog?reportId=CablingQA');
        expect(list[8].getText()).toEqual('app report1');
        expect(list[8].getAttribute('href')).toMatch(process.env.BASE_URL+'/tdstm/reports/index.projectId=\\d{4,6}#');
        expect(list[9].getText()).toEqual('Cabling Conflict');
        expect(list[9].getAttribute('href')).toEqual(process.env.BASE_URL+'/tdstm/reports/getBundleListForReportDialog?reportId=CablingConflict');
        expect(list[10].getText()).toEqual('Cabling Data');
        expect(list[10].getAttribute('href')).toEqual(process.env.BASE_URL+'/tdstm/reports/getBundleListForReportDialog?reportId=CablingData');
        expect(list[11].getText()).toEqual('Power');
        expect(list[11].getAttribute('href')).toEqual(process.env.BASE_URL+'/tdstm/reports/powerReport');
      });
    });
  });
});//Report Summary