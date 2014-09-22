'use strict';
var  Menu = require('../menu/menu.po.js');
var Reports = require('./reports.po.js');
describe('Task Report',function(){
   var menu = new Menu();
  
  it('should load Task Report page after click on Reports > Task Report', function(){
    menu.goToReports('taskReport');
    expect(menu.getCurrentUrl()).toEqual(process.env.BASE_URL+'/tdstm/reports/getBundleListForReportDialog?reportId=Task+Report');
  });

  it('should have Task Report as title',function(){
    var taskReportPage = new Reports();
    expect(taskReportPage.getPageTitle()).toEqual('Task Report');
  });

  describe('events',function(){
    var taskReportPage = new Reports();
    it('should have events as label and marked as required',function(){
      expect(taskReportPage.getEventsLabel().getText()).toEqual('    Events:*');
    });
    it('should have All events as default option',function(){
      expect(taskReportPage.getEventsSelected()).toEqual('All Events');

    });
    it('should have the existing events listed', function(){
      expect(taskReportPage.getEventsOptionsLength()).toEqual(5);
    });
    it('should have the following options',function(){
      taskReportPage.getEventsOptions().then(function(list){
        expect(list[0].getText()).toEqual('All Events');
        expect(list[1].getText()).toEqual('Buildout');
        expect(list[2].getText()).toEqual('M1');
        expect(list[3].getText()).toEqual('M2');
        expect(list[4].getText()).toEqual('M3');
      });
    });
  }); // events multiselect
  describe('checkboxes',function(){
    var taskReportPage = new Reports();

    xit('should have "include comments in reports" as level',function(){

    });
    it('should have include comments enabled by default',function(){
      expect(taskReportPage.getIncludeCommentsCheckbox().getAttribute('checked')).toBe('true');
    });
    xit('should have "include only remaining tasks in report" as label',function(){

    });
    it('should have include only remaining tasks in report enabled by default',function(){
      expect(taskReportPage.getIncludeOnlyRemainingCheckbox().getAttribute('checked')).toBe('true');
    });
    xit('should have "include unpublished tasks" as label', function(){

    });
    it('should have include unpublished tasks disabled by default',function(){
      expect(taskReportPage.getIncludeUnpublishedCheckbox().getAttribute('checked')).toBe(null);
    });
  }); //checkboxes
  describe('Buttons',function(){
    var taskReportPage = new Reports();
    it('should have 3 buttons to generate reprots',function(){
      taskReportPage.getButtons().then(function(list){
        expect(list.length).toEqual(3);
      });  
    });
    it('should have "Generate Web, XLS and PDF" buttons',function(){
      taskReportPage.getButtons().then(function(list){
        expect(list[0].getAttribute('value')).toEqual('Generate Web');
        expect(list[1].getAttribute('value')).toEqual('Generate Xls');
        expect(list[2].getAttribute('value')).toEqual('Generate Pdf');
      });
    }); 
  }); // buttons
  describe('Generate Web',function(){
    it('should generate the report after click on generate button', function(){
      var taskReportPage = new Reports();
      taskReportPage.generateTaskReport('web');
      expect(menu.getCurrentUrl()).toEqual(process.env.BASE_URL+'/tdstm/reports/index');
    });
    it('should get title from generated report',function(){
      var  taskReportPage= new Reports();
      expect(taskReportPage.getPageTitle()).toContain('Task Report');
    });
  }); // Generate web
});//Task Report