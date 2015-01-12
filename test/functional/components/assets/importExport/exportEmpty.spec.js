'use strict';
var Menu = require('../../menu/menu.po.js');
var ListProjects = require('../../projects/listProjects.po.js');
var Project = require('../../projects/project.po.js');
var ExportPage = require('./export.po.js');
var File = require('./file.po.js');
/*
Scenario:  
Create a new project - Export file - Should be empty
Validate New Project Template (sheets - col headers)
Note: Phantonjs does not support currently file downloads - 
*/
describe('Export', function(){

  var project ={
    'code': 'TOEXPIMP',
    'name': 'To Export and Import',
    'clientCode':'2444', 
    'clientName': 'Marketing Demo',
    'workflow': 'Demo',
    'type':'Demo'
  };

  describe('Preconditions', function  () {

    it('should load list projects page after select Client/Project ListProjects', function(){
      var menu = new Menu();
      menu.goToProjects('listProjects');
      var listProjectPage = new ListProjects();
      expect(listProjectPage.getTitle().getText()).toEqual('Project List - Active Projects');
    });
    
    describe('Cleanup',function(){

      it('should delete project if exists and return to project list', function () {
        var listProjectPage =  new ListProjects();
        var menu = new Menu();
        var projectPage = new Project();

        listProjectPage.selectProjectIfExists(project['code']).then(function(pid){
          if(pid>0){
            expect(menu.getCurrentUrl()).toEqual(process.env.BASE_URL+'/tdstm/project/show/'+pid);
            projectPage.deleteProject();
            browser.driver.wait(function() {
              return menu.getCurrentUrl().then(function(url){
                return url === process.env.BASE_URL+'/tdstm/project/list';
              });
            }).then(function(){
              expect(menu.getCurrentUrl()).toEqual(process.env.BASE_URL+'/tdstm/project/list');
            });
          }
        });
      });

    });//cleanup

    describe('create a project',function () {
      var projectPage = new Project();
      it('should load create project page after hitting create project button',function(){
        var listProjectPage = new ListProjects();
        listProjectPage.clickOnCreateProjectBtn();
        expect(projectPage.getTitle().getText()).toEqual('Create Project');
      });

      it('should have project/create url', function(){
        var menu = new Menu();
        expect(menu.getCurrentUrl())
        .toEqual(process.env.BASE_URL+'/tdstm/project/create');
      });

      it('should create a project', function () {
        var menu = new Menu();
        projectPage.createProject(project);
        expect(menu.getCurrentUrl())
        .toEqual(process.env.BASE_URL+'/tdstm/project/show');
      });

      it('should be displayed created project confirmation message', function(){
        expect(projectPage.getConfirmMsg()).toEqual('Project '+project['code']+' : '+project['name']+' created');
      });
      
      it('should updated Page title with the created project name', function(){
        var menu = new Menu();
        expect(menu.getHeaderTitle().getText()).toEqual(' TransitionManager™ - '+project['name']);
      });

    }); // create a project

    xit('should delete list of manufacturers if those exists', function () {

    });

  }); // Preconditions

  describe('Check Export All on empty project', function () {

    var file = new File();
    var date = new Date().toJSON().slice(0,10).replace(/-/g,'');
    var filePath = process.env.DOWNLOAD_PATH+'TDS-To_Export_and_Import-All-SADFXRrcM-'+date+'.xls';

    it('should load Export Assets page after select Assets > Export Assets', function(){
      var menu = new Menu();
      menu.goToAssets('exportAssets');
      expect(menu.getCurrentUrl()).toEqual(process.env.BASE_URL+'/tdstm/assetEntity/exportAssets');
    });
    
    describe('generate Export',function () {

      it('Should export all devices', function () {
        var toExport = {'Application':'true', 'Devices':'true','Database':'true','Storage':'true','Room':'true','Rack':'true',
        'Dependency':'true','Cabling':'true','Comment':'true'};
        var exportPage =  new ExportPage();
        exportPage.selectCheckboxToExport(toExport);
        expect(exportPage.getApplicationCheckbox().getAttribute('checked')).toEqual('true');
        expect(exportPage.getDevicesCheckbox().getAttribute('checked')).toEqual('true');
        expect(exportPage.getDatabaseCheckbox().getAttribute('checked')).toEqual('true');
        expect(exportPage.getStorageCheckbox().getAttribute('checked')).toEqual('true');
        expect(exportPage.getRoomCheckbox().getAttribute('checked')).toEqual('true');
        expect(exportPage.getRackCheckbox().getAttribute('checked')).toEqual('true');
        expect(exportPage.getDependencyCheckbox().getAttribute('checked')).toEqual('true');
        expect(exportPage.getCablingCheckbox().getAttribute('checked')).toEqual('true');
        expect(exportPage.getCommentCheckbox().getAttribute('checked')).toEqual('true');
      });

      it('should click on Export Button',function(){
        var exportPage =  new ExportPage();
        exportPage.getExportBtn().click();
      }); 

      it('should be opened export progress modal', function  () {
        var exportPage =  new ExportPage();
        expect(exportPage.isExportingModalOpened()).toBe(true);
      });

      it('should be closed export progress modal', function () {
        var exportPage =  new ExportPage();
        expect(exportPage.isExportingModalClosed(filePath)).toBe(true);
      });

      it('should check that file was generated', function () {
        expect(file.existsSync(filePath)).toBe(true);
      });

    });// generate Export

    describe('Validate Exported file',function () {
      var xlsFileObj;

      it('should find downloaded file',function  () {
        xlsFileObj = file.getFileObject(filePath);        
        expect(xlsFileObj).not.toBe(null);
      });

      var sheets;
      it('should have 10 sheets', function () {
        sheets = file.getSheetsName(xlsFileObj);
        expect(sheets.length).toEqual(10);
      });

      var i=0;
      file.sheetsNames.forEach(function(sheet){
        it('should have the '+sheet+' as Sheet Name',function () {
          expect(sheet).toEqual(sheets[i]);
          i++;
        });
      });

      xdescribe('Title',function  () {

      }); // Title

      // remove title from that list for this testing
      var expSheets = file.sheetsNames;
      expSheets.splice(0,1);
      var colHead = [];
    
      expSheets.forEach(function(sheet){
      
        describe(sheet, function(){
          
          it('should have '+file.getExpectedColHeaders(sheet).length+' cols', function(){
            colHead = file.getColumnsHead(xlsFileObj,sheet);
            expect(colHead.length).toEqual(file.getExpectedColHeaders(sheet).length);
          });

          var i=0;
          file.getExpectedColHeaders(sheet).forEach(function (chead) {
            it('should have '+chead+' as col header', function(){
              expect(chead).toEqual(colHead[i]);
              i++;
            });
          });

          it('should have only header row',function () {
            if(sheet ==='Cabling'){
              expect(file.getPageSize(xlsFileObj,sheet)).toEqual(2);
            }else{
              expect(file.getPageSize(xlsFileObj,sheet)).toEqual(1);
            }
          });

        }); // Sheet

      }); // end of foreach

    }); // Validate Exported file
    
    it('should delete exported file',function () {
      var file = new File();
      file.delete(filePath);
      expect(file.existsSync(filePath)).toBe(false);
    });

  }); // Check Export All on empty project
 
}); // Export