'use strict';
var  Menu = require('../menu/menu.po.js');
var Reports = require('./reports.po.js');
describe('Pre-Event Checklist Reports', function(){
  var menu = new Menu();
  
  it('should load Pre-Event Checklist Report page after click on Reports > Pre-Event Checklist', function(){
    menu.goToReports('preEventCheckList');
    var url ='/tdstm/reports/preMoveCheckList';
    expect(menu.getCurrentUrl(url)).toEqual(process.env.BASE_URL+url);
  });
  
  it('should have Pre-Event Checklist as title',function(){
      var preEventChecklistPage = new Reports();
      expect(preEventChecklistPage.getPageTitle()).toEqual('Pre-Event Checklist');
  });
  
  describe('Move Event',function(){
    var preEventChecklistPage = new Reports();
  
    it('should have 4 options listed',function(){
      expect(preEventChecklistPage.getEventsOptionsLength()).toEqual(4);
    });
  
    it('should have the following options',function(){
      preEventChecklistPage.getEventsOptions().then(function(list){
        expect(list[0].getText()).toEqual('Buildout');
        expect(list[1].getText()).toEqual('M1');
        expect(list[2].getText()).toEqual('M2');
        expect(list[3].getText()).toEqual('M3');
      });
    });
  
  }); //Move Event
  
  it('should have web enabled',function(){
    var preEventChecklistPage = new Reports();
    expect(preEventChecklistPage.getOutputWeb().getAttribute('checked')).toBe('true');
  });
  
  describe('generated report web',function(){
  
    it('should generate the report after click on generate button', function(){
      var preEventChecklistPage = new Reports();
      preEventChecklistPage.generatePreEventChecklistBtn().click();
      var url = '/tdstm/reports/generateCheckList';
      expect(menu.getCurrentUrl(url)).toEqual(process.env.BASE_URL+url);
    });
  
    it('should get title from generated report',function(){
      var preEventChecklistPage = new Reports();
      expect(preEventChecklistPage.getPreEventCheckGeneratedReportHeader().getText()).toContain('Pre-Event Checklist - MarketingDemo : Buildout');
    });
  
  }); //generated report
 
 }); 