'use strict';
// go to Database List
// click on create database
// add validate create form
// save a database
// search for that database on the list
// view database form and validate information added
// edit database from the view modal 
// check the change on the view form and close it

var  Menu = require('../../menu/menu.po.js');
var DatabaseList = require('./databaseList.po.js');
var DBCreateModal = require('./dBCreateModal.po.js');
var DBViewModal = require('./dBViewModal.po.js');
describe('Database', function(){
  // var appId;
  var dbInfo = {
        'name':'DB Test 1',
        'type': 'Database',
        'format':'Oracle',
        'size': '2',
        'scale': 'Megabyte',
        'rateOfChange': '3',
        'description':'This is DB Test 1 Description',
        'support':'support info',
        'environment': {
          'text':'Development',
          'posList': 3
        },
        'retireDate':'03/08/2015',
        'mainExp':'05/05/2015',
        'externalRefId':'5543',
        'bundle':'BundTest',
        'planStatus':'Confirmed',
        'validation':'Validated'
      };

  it('should load database List page after select Assets > Databases', function(){
    var menu = new Menu();
    menu.goToAssets('databases');
    expect(menu.getCurrentUrl('/tdstm/database/list')).toEqual(process.env.BASE_URL+'/tdstm/database/list');
  });

  it('should have Database List as title', function(){
    var databaseList = new DatabaseList();
    expect(databaseList.titleh.getText()).toEqual('Database List');
  });
    
  it('should open create Database modal after click on create DB button', function(){
    var databaseList = new DatabaseList();
    databaseList.createDBBtn.click();
    var createDBModal = new DBCreateModal();
    expect(createDBModal.isCreateModalOpened()).toBe(true);
  });

  describe('create database Modal',function(){

    it('should have Database Detail as title', function(){
      var createDBModal = new DBCreateModal();
      expect(createDBModal.getModalTitle().getText()).toEqual('Database Create');
    });
    
    describe('Buttons',function(){
      
      it('should have only 2 buttons', function(){
        var createDBModal = new DBCreateModal();
        expect(createDBModal.createModalButtons.count()).toEqual(2);
      });
      
      it('should have save button',function(){
        var createDBModal = new DBCreateModal();
        expect(createDBModal.createModalButtons.get(0).getAttribute('value')).toEqual('Save');
      });
      
      it('should have cancel button',function(){
        var createDBModal = new DBCreateModal();
        expect(createDBModal.createModalButtons.get(1).getAttribute('value')).toEqual('Cancel');
      });
    
    });//Buttons

    describe('Canceling',function(){
    
      it('should close create modal after hit on cancel button',function(){
        var createDBModal = new DBCreateModal();
        createDBModal.createModalButtons.get(1).click();
        expect(createDBModal.isCreateModalClosed()).toBe(true);
      });
    
    });//Canceling
    
    it('should open create Database modal after click on create DB button', function(){
      var databaseList = new DatabaseList();
      databaseList.createDBBtn.click();
      var createDBModal = new DBCreateModal();
      expect(createDBModal.isCreateModalOpened()).toBe(true);
    });
    
    describe('Name',function(){

      it('should have name* label', function(){
        var createDBModal = new DBCreateModal();
        expect(createDBModal.nameLabel.getText()).toEqual('Name*');
      });
      
      it('should be empty by default', function(){
        var createDBModal = new DBCreateModal();
        expect(createDBModal.nameField.getAttribute('value')).toEqual('');
      });

      it('should not allow to save if name is empty', function(){
        var createDBModal = new DBCreateModal();
        if(process.env.BROWSER_NAME === 'phantomjs'){
          createDBModal.saveBtn.click();
        }else{
          createDBModal.saveBtn.click();
          browser.driver.sleep(1000);
          var alertDialog = browser.driver.switchTo().alert();
          expect(alertDialog.getText()).toEqual('Please provide a name for the asset');
          alertDialog.accept();
        }
        expect(createDBModal.isCreateModalOpened()).toBe(true);
      });

      it('should set a name', function(){
        var createDBModal = new DBCreateModal();
        createDBModal.setName(dbInfo['name']);
        expect(createDBModal.nameField.getAttribute('value')).toEqual(dbInfo['name']);
      });

    }); //name

    describe('size/scale',function(){

      it('should have as label size/scale*',function(){
        var createDBModal = new DBCreateModal();
        expect(createDBModal.sizeScaleLabel.getText()).toEqual('Size/Scale*');
      });
     
      describe('size',function(){

        it('should not allow to save if size is empty',function(){
          var createDBModal = new DBCreateModal();
          expect(createDBModal.sizeField.getAttribute('value')).toEqual('');
          if(process.env.BROWSER_NAME === 'phantomjs'){
            createDBModal.saveBtn.click();
          }else{
          createDBModal.saveBtn.click();
          browser.driver.sleep(1000);
          var alertDialog = browser.driver.switchTo().alert();
          expect(alertDialog.getText()).toEqual('Please enter numeric value for DB Size');
          alertDialog.accept();
          }
          expect(createDBModal.isCreateModalOpened()).toBe(true);
        });
        
        it('should not allow to save if Size is non numeric', function(){
          var createDBModal = new DBCreateModal();
          createDBModal.sizeField.sendKeys('hola');
          expect(createDBModal.sizeField.getAttribute('value')).toEqual('hola');
          if(process.env.BROWSER_NAME === 'phantomjs'){
            createDBModal.saveBtn.click();
          }else{
            createDBModal.saveBtn.click();
            browser.driver.sleep(1000);
            var alertDialog = browser.driver.switchTo().alert();
            expect(alertDialog.getText()).toEqual('Please enter numeric value for DB Size');
            alertDialog.accept();        
          }
          expect(createDBModal.isCreateModalOpened()).toBe(true);
        });

        it('should set a numeric value for size',function(){
          var createDBModal = new DBCreateModal();
          createDBModal.sizeField.clear();
          createDBModal.sizeField.sendKeys(dbInfo['size']);
          expect(createDBModal.sizeField.getAttribute('value')).toEqual(dbInfo['size']);
        });

      });// size

      describe('scale',function(){

        it('should have Please Select as default value',function(){
          var createDBModal = new DBCreateModal();
          expect(createDBModal.getScaleSelected()).toEqual(' Please Select');
        });

        it('should have the following 6 options',function(){
          var createDBModal = new DBCreateModal();
          expect(createDBModal.scaleOptions.count()).toEqual(6);
        });

        it('should have the following options',function(){
          var createDBModal = new DBCreateModal();
          createDBModal.scaleOptions.then(function(list){
            expect(list[0].getText()).toEqual(' Please Select');
            expect(list[1].getText()).toEqual('Kilobyte');
            expect(list[2].getText()).toEqual('Megabyte');
            expect(list[3].getText()).toEqual('Gigabyte');
            expect(list[4].getText()).toEqual('Terabyte');
            expect(list[5].getText()).toEqual('Petabyte');
          });
        });

        xit('should not allow to save is Scale is empty', function(){
          //it's not being validated
          // var createDBModal = new DBCreateModal();
        });
        
        it('should select Megabyte as scale', function(){
          var createDBModal = new DBCreateModal();
          createDBModal.scaleOptions.get(2).click();
          expect(createDBModal.getScaleSelected()).toEqual(dbInfo['scale']);
        });

      });//scale
    }); // size/scale

    describe('Format',function(){
    
      it('should have Format* as label', function(){
        var createDBModal = new DBCreateModal();
        expect(createDBModal.formatLabel.getText()).toEqual('Format*');
      });

      it('should be empty by default',function(){
        var createDBModal = new DBCreateModal();
        expect(createDBModal.formatField.getAttribute('value')).toEqual('');
      });

      it('should not allow to save if Format is empty', function(){
        var createDBModal = new DBCreateModal();
        if(process.env.BROWSER_NAME === 'phantomjs'){
          createDBModal.saveBtn.click();
        }else{
          createDBModal.saveBtn.click();
          browser.driver.sleep(1000);
          var alertDialog = browser.driver.switchTo().alert();
          expect(alertDialog.getText()).toEqual('Please enter value for DB Format');
          alertDialog.accept();
        }
        expect(createDBModal.isCreateModalOpened()).toBe(true);
      });

      it('should set a value for Format', function(){
        var createDBModal = new DBCreateModal();
        createDBModal.formatField.sendKeys(dbInfo['format']);
        expect(createDBModal.formatField.getAttribute('value')).toEqual(dbInfo['format']);
      });

    }); // Format

    describe('type',function(){
    
      it('should have Type as label', function(){
        var createDBModal = new DBCreateModal();
        expect(createDBModal.typeLabel.getText()).toEqual('Type');
      });
    
      it('should have Database as default value', function(){
        var createDBModal = new DBCreateModal();
        expect(createDBModal.typeField.getAttribute('value')).toEqual(dbInfo['type']);
      });

      it('should be readonly', function(){
        var createDBModal = new DBCreateModal();
        expect(createDBModal.typeField.getAttribute('readonly')).toEqual('true');
      });

    }); //Type

    describe('Rate of Change (%)', function(){

      it('should have Rate of Change as label', function(){
        var createDBModal = new DBCreateModal();
        expect(createDBModal.rateOfChangeLabel.getText()).toEqual('Rate of Change (%)');
      });
      
      it('should be empty by default', function(){
        var createDBModal = new DBCreateModal();
        expect(createDBModal.rateOfChangeField.getAttribute('value')).toEqual('');

      });

      xit('should not allow non numeric values', function(){
        var createDBModal = new DBCreateModal();
        createDBModal.rateOfChangeField.sendKeys('chau');
        expect(createDBModal.rateOfChangeField.getAttribute('value')).toEqual('chau');
        if(process.env.BROWSER_NAME === 'phantomjs'){
          createDBModal.saveBtn.click();
        }else{
          createDBModal.saveBtn.click();
          browser.driver.sleep(1000);
          var alertDialog = browser.driver.switchTo().alert();
          expect(alertDialog.getText()).toEqual('Unable to update asset  :  Property rateOfChange must be a valid number');
          alertDialog.accept();        
        }
        expect(createDBModal.isCreateModalOpened()).toBe(true);
      });

      it('should allow numeric values',function(){
        var createDBModal = new DBCreateModal();
        var field = createDBModal.rateOfChangeField;
        field.clear();
        field.sendKeys(dbInfo['rateOfChange']);      
        expect(createDBModal.rateOfChangeField.getAttribute('value')).toEqual(dbInfo['rateOfChange']);
      });
    });//Rate of Change

    describe('Description',function(){

      it('should have description as label',function(){
        var createDBModal = new DBCreateModal();
        expect(createDBModal.descriptionLabel.getText()).toEqual('Description');
      });

      it('should be empty by default',function(){
        var createDBModal = new DBCreateModal();
        expect(createDBModal.descriptionField.getAttribute('value')).toEqual('');
      });
   
      it('should add a value',function(){
        var createDBModal = new DBCreateModal();
        createDBModal.descriptionField.sendKeys(dbInfo['description']);
        expect(createDBModal.descriptionField.getAttribute('value')).toEqual(dbInfo['description']);
      });

    }); //Description

    describe('Support',function(){

      it('should have support as label',function(){
        var createDBModal = new DBCreateModal();
        expect(createDBModal.supportLabel.getText()).toEqual('Support');
      });
      
      it('should be empty by default',function(){
        var createDBModal = new DBCreateModal();
        expect(createDBModal.supportField.getAttribute('value')).toEqual('');
      });
      
      it('should add a value',function(){
        var createDBModal = new DBCreateModal();
        createDBModal.supportField.sendKeys(dbInfo['support']);
        expect(createDBModal.supportField.getAttribute('value')).toEqual(dbInfo['support']);
      });

    }); // Support
    //https://jira5.tdsops.com/browse/TM-3439
    describe('Retire Date - Datepicker',function(){
      
      it('should have Retire Date as value',function(){
        var createDBModal = new DBCreateModal();
        expect(createDBModal.retireDateLabel.getText()).toEqual('Retire Date:');
      });
      
      it('should  be empty by default',function(){
        var createDBModal = new DBCreateModal();
        expect(createDBModal.retireDateField.getAttribute('value')).toEqual('');
      });

      it('should allow only dates',function(){
        var createDBModal = new DBCreateModal();
        createDBModal.retireDateField.sendKeys('hola');
        expect(createDBModal.retireDateField.getAttribute('value')).toEqual('');
      });
      
      it('should add a value',function(){
        var createDBModal = new DBCreateModal();
        createDBModal.retireDateField.sendKeys(dbInfo['retireDate']);
        expect(createDBModal.retireDateField.getAttribute('value')).toEqual(dbInfo['retireDate']);
      });
      
      xit('should add a value from daypicker',function(){
        // var createDBModal = new DBCreateModal();
      });

    });//Retire Date
    //https://jira5.tdsops.com/browse/TM-3439
    
    describe('Maint Exp. - Datepicker',function(){
      
      it('should have Maint Exp. as label',function(){
        var createDBModal = new DBCreateModal();
        expect(createDBModal.maintExpLabel.getText()).toEqual('Maint Exp.');
      });
      
      it('should be empty by default',function(){
        var createDBModal = new DBCreateModal();
        expect(createDBModal.maintExpField.getAttribute('value')).toEqual('');
      });
      
      it('should allow only dates',function(){
        var createDBModal = new DBCreateModal();
        createDBModal.maintExpField.sendKeys('hola');
        expect(createDBModal.maintExpField.getAttribute('value')).toEqual('');
      });

      it('should add a value',function(){
        var createDBModal = new DBCreateModal();
        createDBModal.maintExpField.sendKeys(dbInfo['mainExp']);
        expect(createDBModal.maintExpField.getAttribute('value')).toEqual(dbInfo['mainExp']);
      });

      xit('should add a value from daypicker',function(){
        // var createDBModal = new DBCreateModal();
      });

    });//Maint Exp.

    describe('External Ref Id',function(){

      it('should have as label External REf Id',function(){
        var createDBModal = new DBCreateModal();
        expect(createDBModal.externalRefIdLabel.getText()).toEqual('External Ref Id');
      });

      it('should be empty by default',function(){
        var createDBModal = new DBCreateModal();
        expect(createDBModal.externalRefIdField.getAttribute('value')).toEqual('');
      });

      it('should add a value',function(){
        var createDBModal = new DBCreateModal();
        createDBModal.externalRefIdField.sendKeys(dbInfo['externalRefId']);
        expect(createDBModal.externalRefIdField.getAttribute('value')).toEqual(dbInfo['externalRefId']);
      });

    });//External Ref Id

    describe('Environment',function(){

      it('should have Environment as label',function(){
        var createDBModal = new DBCreateModal();
        expect(createDBModal.environmentLabel.getText()).toEqual('Environment');
      });
        
      it('shoud have Please Select option selected by default',function(){
        var createDBModal = new DBCreateModal();
        expect(createDBModal.getEnvironmentSelected()).toEqual(' Please Select');
      });
      
      it('should have 13 elements on the list',function(){
        var createDBModal = new DBCreateModal();
        expect(createDBModal.environmentOptions.count()).toEqual(13);
      });

      it('should have these options',function(){
        // this is configure on admin > assets options - using Marketing demo setup - DB dependent
        var createDBModal = new DBCreateModal();
        createDBModal.environmentOptions.then(function(list){
          expect(list[0].getText()).toEqual(' Please Select');
          expect(list[1].getText()).toEqual('Production');
          expect(list[2].getText()).toEqual('DR');
          expect(list[3].getText()).toEqual('Development');
          expect(list[4].getText()).toEqual('QA');
          expect(list[5].getText()).toEqual('Staging');
          expect(list[6].getText()).toEqual('UAT');
          expect(list[7].getText()).toEqual('Test');
          expect(list[8].getText()).toEqual('Integrated Test');
          expect(list[9].getText()).toEqual('To be retired');
          expect(list[10].getText()).toEqual('Unit Test');
          expect(list[11].getText()).toEqual('Archive');
          expect(list[12].getText()).toEqual('Retire');
        });

      });
        
      it('should select a value from the dropdown',function(){
        var createDBModal = new DBCreateModal();
        createDBModal.environmentOptions.get(dbInfo['environment']['posList']).click();
        expect(createDBModal.getEnvironmentSelected()).toEqual(dbInfo['environment']['text']);
      });
    });//Environment

    describe('Bundle',function(){

      it('should have Bundle as label',function(){
        var createDBModal = new DBCreateModal();
        expect(createDBModal.bundleLabel.getText()).toEqual('Bundle');

      });

      it('should have TBD as default',function(){
        var createDBModal = new DBCreateModal();
        expect(createDBModal.getBundleSelected()).toEqual('TBD');
      });

      it('should have 2 in the list', function(){
        var createDBModal = new DBCreateModal();
        expect(createDBModal.bundleOptions.count()).toEqual(2);
      });
      
      it('should have the following list',function(){
        var createDBModal = new DBCreateModal();
        createDBModal.bundleOptions.then(function(list){
          expect(list[0].getText()).toEqual('BundTest');
          expect(list[1].getText()).toEqual('TBD');
        });
      });

      it('should select Bund Test',function(){
        var createDBModal = new DBCreateModal();
        createDBModal.bundleOptions.get(0).click();
        expect(createDBModal.getBundleSelected()).toEqual(dbInfo['bundle']);
      });

    });//Bundle
    
    describe('Plan Status',function(){

      it('should have Plan Status as label',function(){
        var createDBModal = new DBCreateModal();
        expect(createDBModal.planStatusLabel.getText()).toEqual('Plan Status');
      });
      
      it('should have Unassigned as default',function(){
        var createDBModal = new DBCreateModal();
        expect(createDBModal.getPlanStatusSelected()).toEqual('Unassigned');
      });

      it('should have 5 in the list', function(){
        var createDBModal = new DBCreateModal();
        expect(createDBModal.planStatusOptions.count()).toEqual(6);
      });

      it('should have the following list',function(){
        var createDBModal = new DBCreateModal();
        createDBModal.planStatusOptions.then(function(list){
          expect(list[0].getText()).toEqual('Unassigned');
          expect(list[1].getText()).toEqual('Assigned');
          expect(list[2].getText()).toEqual('Confirmed');
          expect(list[3].getText()).toEqual('Locked');
          expect(list[4].getText()).toEqual('Moved');
          expect(list[5].getText()).toEqual('Future');
         }); 
      });

      it('should select Confirmed',function(){
        var createDBModal = new DBCreateModal();
        createDBModal.planStatusOptions.get(2).click();
        expect(createDBModal.getPlanStatusSelected()).toEqual(dbInfo['planStatus']);
      });

    }); //Plan Status

    describe('Validation',function(){

      it('should have Validation as label',function(){
        var createDBModal = new DBCreateModal();
        expect(createDBModal.validationLabel.getText()).toEqual('Validation');
      });

      it('should have Discovery as default',function(){
        var createDBModal = new DBCreateModal();
        expect(createDBModal.getValidationSelected()).toEqual('Discovery');
      });

      it('should have 5 in the list', function(){
        var createDBModal = new DBCreateModal();
        expect(createDBModal.validationOptions.count()).toEqual(5);
      });

      it('should have the following list',function(){
        var createDBModal = new DBCreateModal();
        createDBModal.validationOptions.then(function(list){
          expect(list[0].getText()).toEqual('Unknown');
          expect(list[1].getText()).toEqual('Validated');
          expect(list[4].getText()).toEqual('PlanReady');
        });
      });

      it('should select Validated',function(){
        var createDBModal = new DBCreateModal();
        createDBModal.validationOptions.get(1).click();
        expect(createDBModal.getValidationSelected()).toEqual(dbInfo['validation']);
      });

    });//Validation
    
    describe('Custom fields',function(){
      
      describe('Network Interfaces - Custom 1',function(){
        xit('should have as label Network Interface',function(){

        });
        xit('should be empty by default',function(){

        });
        xit('should add a value', function(){

        });

      });//Network Interface
      
      describe('CMDB ID',function(){
    
        xit('should have CMDB ID as label',function(){

        });
    
        xit('should be empty by default',function(){

        });
    
        xit('should add a value',function(){

        });

      }); //CMDB ID

    });//Custom fields

    it('should save database and displayed view details',function(){
      var createDBModal = new DBCreateModal();
      var viewDBModal = new DBViewModal();
      createDBModal.saveBtn.click();
      expect(createDBModal.isCreateModalClosed()).toBe(true);
      expect(viewDBModal.isViewModalOpened()).toBe(true);
    });

  }); // create database

  describe('view created database', function(){
    
    describe('buttons',function(){
    
      it('should have 4 buttons',function(){

      });
    
      it('should have edit button',function(){

      });
    
      it('should have cancel button',function(){

      });
    
      it('should have Add Task button',function(){

      });
    
      it('should have Add Comment Button',function(){

      });
    
    });
    describe('Validate Database created Information',function(){

    });

    describe('edit Database from view database', function(){

    }); // edit Database from view database

    it('should close View modal',function(){
      var viewDBModal = new DBViewModal();
      viewDBModal.closeViewModalBtn.click();
      expect(viewDBModal.isViewModalClosed()).toBe(true);
    });
  }); // view created database

});//Database