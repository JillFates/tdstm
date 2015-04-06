'use strict';
var Menu = require('../../menu/menu.po.js');
var ImportPage = require('./import.po.js');
var ManageBatches = require('./manageBatches.po.js');
var path = require('path');
var ExportPage = require('./export.po.js');
var File = require('./file.po.js');
var ConnectDatabase = require('./connectDatabase.po.js');

describe('Import Export Devices',function(){

  describe('Preconditions',function () {
    var connectDatabase = new ConnectDatabase();
    connectDatabase.connection.connect();

    xit('should delete some manufacturers',function () {
      var before ;
      var after=0;
      var sql = 'select count(*) as results from manufacturer where name in ("Dell-ToCreate2", "Dell-TEST290", "Dell-TOCREATE1");';
      var sqlDelete = 'delete  from manufacturer where name in ("Dell-ToCreate2", "Dell-TEST290", "Dell-TOCREATE1");';
      
      connectDatabase.connection.query(sql,function (err,rows) {
        if(!err){
          before = rows[0].results;
        }
      });
      
      connectDatabase.connection.query(sqlDelete,function (err,rows) {
        if(!err){
          if(rows.length > 0){
            after = rows[0].affectedRows;
            expect(after).toEqual(before);
          }else{
            expect(after).toEqual(0);
          }
        }
      });
    });
    
    xit('should delete some models', function () {
      var sql = 'select count(*) as results from model where name in ("PowerEdge TOCREATE2","Hola","TOCREATE3")' ;
      var sqlDelete = 'delete from model where name in ("PowerEdge TOCREATE2","Hola","TOCREATE3");';
      var before ;
      var after=0;

      connectDatabase.connection.query(sql,function (err,rows) {
        if(!err){
          before = rows[0].results;
        }
      });

      connectDatabase.connection.query(sqlDelete,function (err,rows) {
        if(!err){
          if(rows.length > 0){
            after = rows[0].affectedRows;
            expect(after).toEqual(before);
          }else{
            expect(after).toEqual(0);
          }
        }
      });
    });
    
  }); // Preconditions

  describe('Import',function () {
    
    it('should load Import Assets page after select Assets > Import Assets', function(){
      var menu = new Menu();
      menu.goToAssets('importAssets');
      expect(menu.getCurrentUrl()).toEqual(process.env.BASE_URL+'/tdstm/assetEntity/assetImport');
    });

    it('should have "Asset Import" as title', function () {
      var importPage =  new ImportPage();
      expect(importPage.getTitle().getText()).toEqual('Asset Import');
    });

    it('should have "TDS Master Spreadsheet" as import type',function () {
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
      var filename = 'TDS-To_import_Devices_working.xls';
      var filePath = path.resolve(__dirname, './files/'+filename);
      var importPage =  new ImportPage();
      var field = importPage.getFileField();
      field.sendKeys(filePath);
      expect(field.getAttribute('value')).toEqual('C:\\fakepath\\'+filename);
    });

    it('should click on Import Spreadsheet and validate Result Message',function () {
      var importPage =  new ImportPage();
      // var resultMessage = importPage.getExpectedSucessMessage(34,0,0,0,0,0,0); //change when all devices can be imported.
      var resultMessage = importPage.getExpectedSucessMessage(15,0,0,0,0,0,0);
      importPage.getImportBtn().click(); 
      expect(importPage.getResultMessage()).toEqual(resultMessage);
    });

    it('should have Manage Batches: 1',function () {
      var importPage =  new ImportPage();
      expect(importPage.getManageBatchesLink().getText()).toEqual('Manage Batches: 1');
    });

    it('should go to Manage Batches after click on Manage Batches link',function () {
      var importPage =  new ImportPage();
      importPage.getManageBatchesLink().click();
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
        /*Results with all devices
        var results = [
          'Results',
          'Assets in batch: 34',
          'Missing Mfg / Model references:',
          'Mfg: | Model: PowerEdge 2950 | 4 reference(s)',
          'Mfg: | Model: PowerEdge TEST | 3 reference(s)',
          'Mfg: Dell-TEST290 | Model: | 3 reference(s)',
          'Mfg: Dell-TEST290 | Model: PowerEdge 2950 | 3 reference(s)',
          'Mfg: Dell-TOCREATE1 | Model: PowerEdge 2950 | 1 reference',
          'Mfg: Dell-TEST290 | Model: PowerEdge TEST | 2 reference(s)',
          'Mfg: Dell-ToCREATE2 | Model: PowerEdge TOCREATE2 | 1 reference',
          'Mfg: IBM | Model: | 2 reference(s)',
          'Mfg: Dell | Model: | 1 reference',
          'Mfg: Dell | Model: hola | 2 reference(s)',
          'Mfg: Dell | Model: TOCREATE3 | 1 reference',
          'Invalid device types specified:',
          '(8)',
          'NoExiste (8)\n\n\n',
          'Review took \\d.\\d* seconds to complete'
        ].join('\n').replace(/[(|)]/g,'.');
        */
        var results = [
          'Results',
          'Assets in batch: 15',
          'Missing Mfg / Model references:',
          'Mfg: | Model: PowerEdge 2950 | 4 reference(s)',
          'Mfg: | Model: PowerEdge TEST | 3 reference(s)',
          'Mfg: Dell-TEST290 | Model: | 3 reference(s)',
          'Mfg: Dell-TEST290 | Model: PowerEdge 2950 | 2 reference(s)',
          'Invalid device types specified:',
          '(5)',
          'NoExiste (4)\n',
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
          var alertDialog = browser.driver.switchTo().alert();
          var message= 'Please confirm that you want to post the imported assets to inventory?';
          expect(alertDialog.getText()).toEqual(message);
          alertDialog.accept();
        }
      });
      
      it('should have "Posting assets to inventory for batch ..." as title when posting process ends',function () {
        expect(manageBatches.isPostingEnded()).toBe(true);
      });

      it('should have This Posting Results',function () {
        /* enable when all devices can be imported
          var results = [
          'Process Results for Batch \\d*:',
          'Assets in Batch: 3',
          'Records Inserted: 1',
          'Records Updated: 0',
          'Rooms Created: 0',
          'Racks Created: 0',
          'Asset Errors: 2',
          'Attribute Errors: 0',
          'AssetId Errors: 0',
          'Warnings:',
          'ERROR: TC 111 – BBB (row 2) - A Model Name plus Mfg Name or Device Type are required',
          'ERROR: TC 114 – BB!E (row 4) - An invalid device type (NoExiste) was specified\n',
          'Elapsed time to process batch: \\d.\\d* seconds'
        ].join('\n').replace(/[(|)]/g,'.');*/
        var results = [
          'Process Results for Batch \\d*:',      
          'Assets in Batch: 15',
          'Records Inserted: 4',
          'Records Updated: 0',
          'Rooms Created: 0',
          'Racks Created: 0',
          'Asset Errors: 11',
          'Attribute Errors: 0',
          'AssetId Errors: 0',
          'Warnings:',
          'ERROR: TC 111 BBB (row 2) - A Model Name plus Mfg Name or Device Type are required',
          'ERROR: TC 114 BB!E (row 4) - An invalid device type (NoExiste) was specified',
          'ERROR: TC 121 BMB (row 5) - Multiple models matched by name without a type specified',
          'WARNING: TC 122 BMM (row 6) - No Mfg specified therefore assuming you meant Mfg (Dell)',
          'ERROR: TC 123 BM!M (row 7) - Multiple models matched by name but none had the specified type',
          'ERROR: TC 124 BM!E (row 8) - Device Type (NoExiste) is invalid',
          'ERROR: TC 131 B!EB (row 9) - Device Type is needed but is blank',
          'ERROR: TC 132 B!EM (row 10) - Incomplete Mfg/Model/Type therefore did not create device',
          'ERROR: TC 134 B!E!E (row 11) - Device Type (NoExiste) is invalid',
          'ERROR: TC 411 !EBB (row 12) - Device Type is needed but is blank. Model name is needed but is blank',
          'ERROR: TC 414 !EB!E (row 14) - Device Type (NoExiste) is invalid',
          'ERROR: TC 421 !EMB (row 15) - Device Type is needed but is blank',
          'WARNING: TC 422 !EMM (row 16) - Specified Mfg (Dell-TEST290) does not match the existing Model Mfg (Dell)\n',
          'Elapsed time to process batch: \\d.\\d* seconds'          
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
        browser.driver.get(process.env.BASE_URL+'/tdstm/dataTransferBatch/list');
        manageBatches.getStatusList().then(function (list) {
          expect(list[0].getText()).toEqual('COMPLETED');
        });
      });

      it('should remove processed batch',function () {
        manageBatches.getRemoveLinkList().then(function (list) {
          list[0].click();
        }); 
        // browser.driver.get(process.env.BASE_URL+'/tdstm/dataTransferBatch/list');
        manageBatches.getBatchesList().then(function (list) {
          expect(list.length).toEqual(0);
        });
      });
    }); // Manage Bathes
  
  }); // Import

  describe('Export', function () {
    var file = new File();
    var date = new Date().toJSON().slice(0,10).replace(/-/g,'');
    //var filePath = process.env.DOWNLOAD_PATH+'TDS-To_Export_and_Import-All-SADFXRrcM-'+date+'.xls';
    var filePath = process.env.DOWNLOAD_PATH+'TDS-MarketingDemo-All-SADFXRrcM-'+date+'.xls';
  
    xit('should load Export Assets page after select Assets > Export Assets', function(){
      var menu = new Menu();
      menu.goToAssets('exportAssets');
      expect(menu.getCurrentUrl()).toEqual(process.env.BASE_URL+'/tdstm/assetEntity/exportAssets');
    });

    describe('generate Export',function () {

      xit('Should export all devices', function () {
        // var toExport = {'application':'true', 'devices':'true','database':'true','storage':'true','room':'true','rack':'true',
        // 'dependency':'true','cabling':'true','comment':'true'};
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

      xit('should click on Export Button',function(){
        var exportPage =  new ExportPage();
        exportPage.getExportBtn().click();
      }); 

      xit('should be opened export progress modal', function  () {
        var exportPage =  new ExportPage();
        expect(exportPage.isExportingModalOpened()).toBe(true);
      });

      xit('should be closed export progress modal', function () {
        var exportPage =  new ExportPage();
        expect(exportPage.isExportingModalClosed(filePath)).toBe(true);
      });

      xit('should check that file was generated', function () {
        expect(file.existsSync(filePath)).toBe(true);
      });

    });// generate Export
    
    describe('Validate Exported File',function () {
      var xlsFileObj;
      xit('should find downloaded file',function  () {
        xlsFileObj = file.getFileObject(filePath);        
        expect(xlsFileObj).not.toBe(null);
      });

      xit('should have x devices exported',function () {

        expect(file.getPageSize(xlsFileObj,'Devices')).toEqual(2);
      });

      xit('should validate each row',function () {
        var expResults = { 
          '1': [ 'Name', 'Manufacturer', 'Model', 'Type' ],
          '2': [ 'TC 112 BBM', 'unknown', 'Unknown - Server', 'Server' ],
          '3': [ 'TC 121 BMB', 'Dell', 'PowerEdge 2950', 'Server' ],
          '4': [ 'TC 122 BMM', 'Dell', 'PowerEdge 2950', 'Server' ],
          '5': [ 'TC 123 BM!M', 'Dell', 'PowerEdge 2950', 'Server' ],
          '6': [ 'TC 412 !EBM', 'unknown', 'Unknown - Server', 'Server' ],
          '7': [ 'TC 422 !EMM', 'Dell', 'PowerEdge 2950', 'Server' ],
          '8': [ 'TC 423 !EM!E', 'Dell-TOCREATE1', 'PowerEdge 2950', 'VM' ],
          '9': [ 'TC 432 !E!EM', 'Dell-ToCREATE2', 'PowerEdge TOCREATE2', 'VM' ],
          '10': [ 'TC 322 !MMM', 'Dell', 'PowerEdge 2950', 'Server' ],
          '11': [ 'TC 212 MBM', 'unknown', 'Unknown - Server', 'Server' ],
          '12': [ 'TC 221 MMB', 'Dell', 'PowerEdge 2950', 'Server' ],
          '13': [ 'TC 222 MMM', 'Dell', 'PowerEdge 2950', 'Server' ],
          '14': [ 'TC 224 MM!E', 'Dell', 'PowerEdge 2950', 'Server' ],
          '15': [ 'TC 223 MM!M', 'Dell', 'PowerEdge 2950', 'Server' ],
          '16': [ 'TC 232 M!EM', 'Dell', 'TOCREATE3', 'Server' ]
        };

        var results = file.getRows(xlsFileObj,'Devices',['B','F','G','H']);
        expect(Object.keys(expResults).length).toEqual(Object.keys(results).length);

        for(var i=0;i<=Object.keys(expResults).length; i++){
          expect(results[i]).toEqual(expResults[i]);
        }
      });
      
    }); // Validate Exported File

    xit('should delete exported file',function () {
      var file = new File();
      file.delete(filePath);
      expect(file.existsSync(filePath)).toBe(false);
    });
  }); // Export

}); // Import Export Devices