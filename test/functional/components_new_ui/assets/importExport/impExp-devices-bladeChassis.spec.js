'use strict';
var Menu = require('../../menu/menu.po.js');
var ImportPage = require('./import.po.js');
var ManageBatches = require('./manageBatches.po.js');
var path = require('path');
var ListProjects = require('../../projects/listProjects.po.js');
// var ExportPage = require('./export.po.js');
// var File = require('./file.po.js');
// var ConnectDatabase = require('./connectDatabase.po.js');

describe('Import Export Devices - Blade Chassis', function() {
  var menu = new Menu();

  describe('Preconditions', function() {
    // check that project selected is marketing demo if not select it.

    describe('search by code and select it', function(){
      
      var projId;
      var listProjectPage =  new ListProjects();
      
      it('should load list projects page after select Client/Project ListProjects', function(){
        menu.goToProjects('listProjects');
        expect(menu.getCurrentUrl('/tdstm/project/list?active=active')).toEqual(process.env.BASE_URL+'/tdstm/project/list?active=active');
      });

      it('should have "Project List - Active Projects" as title',function () {
        expect(listProjectPage.getTitle().getText()).toEqual('Project List - Active Projects');
      });

      it('should find the project to select', function(){
        listProjectPage.setSearchProjectCode('MarketingDemo');
        expect(listProjectPage.getSearchProjectCode().getAttribute('value')).toEqual('MarketingDemo');
      });

      it('should only be found MarketingDemo project', function(){
        var results= 2;
        browser.sleep(1000);
        listProjectPage.verifySearchResults(results).then(function(list){
          expect(list.length).toEqual(results);
          list[1].getAttribute('id').then(function(text){
            projId = text;
          });
        });
      });

      it('should select the project found', function(){
        listProjectPage.selectProjectfromListByPos(1,'MarketingDemo');
      });

      it('should be redirect to project/show/project+id', function(){
        expect(menu.getCurrentUrl('/tdstm/project/show/')).toEqual(process.env.BASE_URL+'/tdstm/project/show/'+projId);
      });

    }); // search by code and select it

  }); // Preconditions

  describe('Import', function() {
    
    it('should load Import Assets page after select Assets > Import Assets', function(){
      var menu = new Menu();
      menu.goToAssets('importAssets');
      expect(menu.getCurrentUrl('/tdstm/assetEntity/assetImport')).toEqual(process.env.BASE_URL+'/tdstm/assetEntity/assetImport');
    });

    it('should have "Asset Import" as title', function () {
      var importPage =  new ImportPage();
      expect(importPage.getTitle().getText()).toEqual('Asset Import');
    });

    xit('should have "TDS Master Spreadsheet" as import type',function () {
      var importPage =  new ImportPage();
      expect(importPage.getImportTypeSelected()).toEqual('TDS Master Spreadsheet');
    });

    it('should select only devices',function () {
      var toImport = {'Application':'false', 'Devices':'true','Database':'false','Storage':'false',
        'Dependency':'false','Cabling':'false','Comment':'false'};
      var importPage =  new ImportPage();
      importPage.selectCheckboxToImport(toImport);
    });
    
    it('should have Application unchecked',function () {
      var importPage =  new ImportPage();
      expect(importPage.getApplicationCheckbox().getAttribute('checked')).toBe(null);
    });

    it('should have Devices checked',function () {
      var importPage =  new ImportPage();
      expect(importPage.getDevicesCheckbox().getAttribute('checked')).toEqual('true');
    });

    it('should have Databases unchecked',function () {
      var importPage =  new ImportPage();
      expect(importPage.getDatabaseCheckbox().getAttribute('checked')).toBe(null);
    });

    it('should have Storage unchecked',function () {
      var importPage =  new ImportPage();
      expect(importPage.getStorageCheckbox().getAttribute('checked')).toBe(null);
    });

    it('should have Dependency unchecked',function () {
      var importPage =  new ImportPage();
      expect(importPage.getDependencyCheckbox().getAttribute('checked')).toBe(null);
    });

    it('should have Cabling unchecked',function () {
      var importPage =  new ImportPage();
      expect(importPage.getCablingCheckbox().getAttribute('checked')).toBe(null);
    });

    it('should have Comment unchecked',function () {
      var importPage =  new ImportPage();
      expect(importPage.getCommentCheckbox().getAttribute('checked')).toBe(null);
    });

    it('should set file to upload',function(){
      var filename = 'TDS-MarketingDemo-Blades.xls';
      var filePath = path.resolve(__dirname, './files/'+filename);
      var importPage =  new ImportPage();
      var field = importPage.getFileField();
      field.sendKeys(filePath);
      expect(field.getAttribute('value')).toEqual('C:\\fakepath\\'+filename);
    });

    it('should click on Import Spreadsheet and validate Result Message',function () {
      var importPage =  new ImportPage();
      var resultMessage = importPage.getExpectedSucessMessage(7,0,0,0,0,0,0);
      importPage.getImportBtn().click(); 
      // browser.driver.sleep(8000);
      expect(importPage.getResultMessage()).toEqual(resultMessage);
    });

    xit('should have Manage Batches: 1',function () {
      var importPage =  new ImportPage();
      expect(importPage.getManageBatchesLink().getText()).toEqual('Manage Batches: 1');
    });

    it('should go to Manage Batches after click on Manage Batches link',function () {
      var importPage =  new ImportPage();
      importPage.getManageBatchesLink().click();
      expect(menu.getCurrentUrl('/tdstm/dataTransferBatch/list')).toEqual(process.env.BASE_URL+'/tdstm/dataTransferBatch/list');
    });

    it('should have "Manage Asset Import Batches" as title', function() {
      var manageBatches =  new ManageBatches();
      expect(manageBatches.getTitle().getText()).toEqual('Manage Asset Import Batches');
    });

    describe('Manage Batches',function () {
     
      var manageBatches =  new ManageBatches();
      
      it('should start review after click on review link',function () {
        manageBatches.getReviewLinkList().then(function (list) {
          list[0].click();
        });
        expect(manageBatches.isReviewModalOpened()).toBe(true);
      });

      it('should have "Reviewing Assets in Batch .." as title when review process ends',function () {
        expect(manageBatches.isReviewEnded()).toBe(true);
      });

      it('should have This Review Results',function () {
        var results = [
          'Results',
          'Assets in batch: 7\n',
          'Review took \\d.\\d* seconds to complete'
        ].join('\n').replace(/[(|)]/g,'.');
        expect(manageBatches.getReviewResults()).toMatch(results);
      });

      it('should close review modal',function () {
        manageBatches.getCloseModalBtn().click();
        expect(manageBatches.isReviewModalClosed()).toBe(true);
      });

      it('should be displayed process link',function (){
        manageBatches.getProcessLinkList().then(function (list) {
          expect(list[0].getText()).toEqual('Process');
        });
      });

      it('should display browser alert after click on process link',function () {
        if(process.env.BROWSER_NAME === 'phantomjs'){
          browser.driver.executeScript('$(\'input[value="Delete"]\').attr("onclick","").click("true")');
          manageBatches.getProcessLinkList().then(function (list) {
            list[0].click();
          }); 
        }else{
          manageBatches.getProcessLinkList().then(function (list) {
            list[0].click();
          }); 
          browser.driver.sleep(1000);
          var alertDialog = browser.driver.switchTo().alert();
          var message= 'Please confirm that you want to post the imported assets to inventory?';
          expect(alertDialog.getText()).toEqual(message);
          alertDialog.accept();
        }
      });
      
      it('should have "Posting assets to inventory for batch ..." as title when posting process ends',function () {
        expect(manageBatches.getPostingTitle()).toContain('Posting assets to inventory for batch');
      });

      it('should have This Posting Results',function () {
        var results = [
          'Process Results for Batch \\d*:',
          'Assets in Batch: 7',
          'Records Inserted: 7',
          'Records Updated: 0',
          'Rooms Created: 0',
          'Racks Created: 0',
          'Asset Errors: 0',
          'Attribute Errors: 0',
          'AssetId Errors: 0',
          'Warnings:',
          'WARNING: Blade Desktops Side by Side position (0) specified is invalid (row 2)',
          'WARNING: Blade Desktops Side by Side position (100) exceeds chassis capacity (2) (row 3)',
          'WARNING: Blade Desktops Side by Side position (lila) specified is invalid (row 4)',
          'ERROR: No source chassis found with name (Id:113922 Desktops Side by Side) (row 6)',
          'WARNING: Chassis (id:113921 Blade Chassis 2) for source does not match referenced name (Desktops Side by Side) (row 7)',
          'WARNING: Blade Desktops Side by Side position (0) specified is invalid (row 8)',
          '\nElapsed time to process batch: \\d.\\d* seconds'
        ].join('\n').replace(/[(|)]/g,'.');
        expect(manageBatches.getReviewResults()).toMatch(results);
      });

      it('should close Posting modal',function () {
        manageBatches.getCloseModalBtn().click();
        expect(manageBatches.isReviewModalClosed()).toBe(true);
      });

      it('should closed process modal when process is compelted',function () {
        expect(manageBatches.isReviewModalClosed()).toEqual(true);
      });

      it('should have status complete',function () {
        expect(menu.getCurrentUrl('/tdstm/dataTransferBatch/list')).toEqual(process.env.BASE_URL+'/tdstm/dataTransferBatch/list');
        manageBatches.getStatusList().then(function (list) {
          browser.driver.wait(function () {
            return list[0].getText().then(function (text) {
              return text ==='COMPLETED';
            });
          },8000).then(function () {
            expect(list[0].getText()).toEqual('COMPLETED');
          });
        });
      });

      it('should remove processed batch',function () {
        manageBatches.getRemoveLinkList().then(function (list) {
          list[0].click();
        }); 

        browser.driver.wait(function () {
          return manageBatches.getBatchesList().then(function (list) {
            return list.length === 0;
          });
        },8000).then(function () {
          manageBatches.getBatchesList().then(function (list) {
            expect(list.length).toEqual(0);
          });
        });
      });
    }); // Manage Bathes




  });// Import

  describe('Export', function() {
    
  });// Export

}); //Import Export Devices - Blade Chassis