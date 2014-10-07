'use strict';
var  Menu = require('../menu/menu.po.js');
var Reports = require('./reports.po.js');
describe('Event Results Report', function(){
  var menu = new Menu();
  
  it('should load Event Reports page after click on Reports > Application Migration', function(){
    menu.goToReports('eventResults');
    expect(menu.getCurrentUrl()).toEqual(process.env.BASE_URL+'/tdstm/reports/getBundleListForReportDialog?reportId=MoveResults');
  });

  it('should have Move Results Report as title',function(){
    var eventResultsPage = new Reports();
    expect(eventResultsPage.getPageTitle()).toEqual('Move Results Report');
  });

  describe('Move event Dropdown',function(){
    var eventResultsPage = new Reports();
    it('should have "Move Event:*" as label',function(){
      expect(eventResultsPage.getMoveEventLabel()).toEqual('Move Event:*');
    });
    it('should have "Please Select" as default option',function(){
      expect(eventResultsPage.getMoveEventSelected()).toEqual('Please Select');
    });
    it('should have 5 options on the list',function(){
      expect(eventResultsPage.getMoveEventOptionsLength()).toEqual(5);
    });
    it('should have the following options',function(){
      eventResultsPage.getMoveEventOptions().then(function(list){
        expect(list[0].getText()).toEqual('Please Select');
        expect(list[1].getText()).toEqual('Buildout');
        expect(list[2].getText()).toEqual('M1');
        expect(list[3].getText()).toEqual('M2');
        expect(list[4].getText()).toEqual('M3');
      });
    });
    it('should select a Move Event', function(){
      eventResultsPage.getMoveEventOptions().then(function(list){
        list[1].click();
      });
      expect(eventResultsPage.getMoveEventSelected()).toEqual('Buildout');
    });
  }); // Move event Dropdown
  describe('Report type dropdown',function(){
    var eventResultsPage = new Reports();
    it('should have "Report Type:*" as label',function(){
      expect(eventResultsPage.getReportTypeLabel()).toEqual('Report Type:*');
    });
    it('should have "Please Select" as default option',function(){
      expect(eventResultsPage.getReportTypeSelected()).toEqual('Please Select');
    });
    it('should have 3 options',function(){
      expect(eventResultsPage.getReportTypeOptionsLength()).toEqual(3);
    });
    it('should have the following options listed',function(){
      eventResultsPage.getReportTypeOptions().then(function(list){
        expect(list[0].getText()).toEqual('Please Select');
        expect(list[1].getText()).toEqual('Summary Report');
        expect(list[2].getText()).toEqual('Detailed Report');
      });
    });
    it('should select a Report Type - Summary Report',function(){
      eventResultsPage.getReportTypeOptions().then(function(list){
        list[1].click();
      });
      expect(eventResultsPage.getMoveEventSelected()).toEqual('Buildout');
    });

  });//Report type Dropdown
  describe('Generate Buttons',function(){
    var eventResultsPage = new Reports();
    it('should have 3 buttons',function(){
      eventResultsPage.getButtons().length;
    });

    it('should have Generate XLS',function(){
      eventResultsPage.getButtons().then(function(list){
        expect(list[0].getAttribute('value')).toEqual('Generate XLS');
      });
    });
    it('should have Generate PDF',function(){
      eventResultsPage.getButtons().then(function(list){
        expect(list[1].getAttribute('value')).toEqual('Generate PDF');
      });
    });
    it('should have Generate Web',function(){
      eventResultsPage.getButtons().then(function(list){
        expect(list[2].getAttribute('value')).toEqual('Generate WEB');
      });
    });

  }); // Generate Buttons
  describe('generate WEB',function(){
    it('should generate the report after click on generate button', function(){
      var eventResultsPage = new Reports();
      eventResultsPage.generateTaskReport('web');
      expect(menu.getCurrentUrl()).toEqual(process.env.BASE_URL+'/tdstm/reports/getBundleListForReportDialog?reportId=MoveResults');
    });

    it('should get title from generated report',function(){
      var  eventResultsPage= new Reports();
      expect(eventResultsPage.getPageTitle()).toContain('Move Results Report');
    });
  });
});