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
var DatabaseModal = require('./databaseModal.po.js');
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
        }
      };

  it('should load database List page after select Assets > Databases', function(){
    var menu = new Menu();
    menu.goToAssets('databases');
    expect(menu.getCurrentUrl()).toEqual(process.env.BASE_URL+'/tdstm/database/list');
  });

  it('should have Database List as title', function(){
    var databaseList = new DatabaseList();
    expect(databaseList.titleh.getText()).toEqual('Database List');
  });
    
  it('should open create Database modal after click on create DB button', function(){
    var databaseList = new DatabaseList();
    databaseList.createDBBtn.click();
    var createDBModal = new DatabaseModal();
    expect(createDBModal.isCreateModalOpened()).toBe(true);
  });

  describe('create database',function(){

    it('should have Database Detail as title', function(){
      var createDBModal = new DatabaseModal();
      expect(createDBModal.createTitle.getText()).toEqual('Database Detail');
    });
    
    describe('Buttons',function(){

      it('should have only 2 buttons', function(){
        var createDBModal = new DatabaseModal();
        expect(createDBModal.createModalButtons.count()).toEqual(2);

      });
      it('should have save button',function(){
        var createDBModal = new DatabaseModal();
        expect(createDBModal.createModalButtons.get(0).getAttribute('value')).toEqual('Save');
      });
      
      it('should have cancel button',function(){
        var createDBModal = new DatabaseModal();
        expect(createDBModal.createModalButtons.get(1).getAttribute('value')).toEqual('Cancel');
      });
    });//Buttons
    
    describe('Name',function(){

      it('should have name* label', function(){
        var createDBModal = new DatabaseModal();
        expect(createDBModal.nameLabel.getText()).toEqual('Name*');
      });
      
      it('should be empty by default', function(){
        var createDBModal = new DatabaseModal();
        expect(createDBModal.nameField.getAttribute('value')).toEqual('');
      });

      it('should not allow to save if name is empty', function(){
        var createDBModal = new DatabaseModal();
        if(process.env.BROWSER_NAME === 'phantomjs'){
          // browser.executeScript("window.alert = function(){}");
          createDBModal.saveBtn.click();
        }else{
          createDBModal.saveBtn.click();
          var alertDialog = browser.driver.switchTo().alert();
          expect(alertDialog.getText()).toEqual('Please provide a name for the asset');
          alertDialog.accept();
        }
        expect(createDBModal.isCreateModalOpened()).toBe(true);
      });

      it('should set a name', function(){
        var createDBModal = new DatabaseModal();
        createDBModal.setName(dbInfo['name']);
        expect(createDBModal.nameField.getAttribute('value')).toEqual(dbInfo['name']);
      });

    }); //name

    describe('size/scale',function(){

      it('should have as label size/scale*',function(){
        var createDBModal = new DatabaseModal();
        expect(createDBModal.sizeScaleLabel.getText()).toEqual('Size/Scale*');
      });
     
      describe('size',function(){

        it('should not allow to save if size is empty',function(){
          var createDBModal = new DatabaseModal();
          expect(createDBModal.sizeField.getAttribute('value')).toEqual('');
          if(process.env.BROWSER_NAME === 'phantomjs'){
            // browser.executeScript("window.alert = function(){}");
            createDBModal.saveBtn.click();
          }else{
          createDBModal.saveBtn.click();
          var alertDialog = browser.driver.switchTo().alert();
          expect(alertDialog.getText()).toEqual('Please enter numeric value for DB Size');
          alertDialog.accept();
          }
          expect(createDBModal.isCreateModalOpened()).toBe(true);
        });
        
        it('should not allow to save if Size is non numeric', function(){
          var createDBModal = new DatabaseModal();
          createDBModal.sizeField.sendKeys('hola');
          expect(createDBModal.sizeField.getAttribute('value')).toEqual('hola');
          if(process.env.BROWSER_NAME === 'phantomjs'){
            // browser.executeScript("window.alert = function(){}");
            createDBModal.saveBtn.click();
          }else{
            createDBModal.saveBtn.click();
            var alertDialog = browser.driver.switchTo().alert();
            expect(alertDialog.getText()).toEqual('Please enter numeric value for DB Size');
            alertDialog.accept();        
          }
          expect(createDBModal.isCreateModalOpened()).toBe(true);
        });

        it('should set a numeric value for size',function(){
          var createDBModal = new DatabaseModal();
          createDBModal.sizeField.clear();
          createDBModal.sizeField.sendKeys(dbInfo['size']);
          expect(createDBModal.sizeField.getAttribute('value')).toEqual(dbInfo['size']);
        });

      });// size

      describe('scale',function(){

        it('should have Please Select as default value',function(){
          var createDBModal = new DatabaseModal();
          if(process.env.BROWSER_NAME=== 'phantomjs'){
            createDBModal.getScaleSelected().then(function(op){
              expect(op).toEqual(' Please Select');
            });
          }else{
            expect(createDBModal.scaleSelected.getText()).toEqual(' Please Select');
          }
        });

        it('should have the following 6 options',function(){
          var createDBModal = new DatabaseModal();
          expect(createDBModal.scaleOptions.count()).toEqual(6);
        });

        it('should have the following options',function(){
          var createDBModal = new DatabaseModal();
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
          // var createDBModal = new DatabaseModal();

        });
        
        it('should select Megabyte as scale', function(){
          var createDBModal = new DatabaseModal();
          createDBModal.scaleOptions.get(2).click();
          if(process.env.BROWSER_NAME=== 'phantomjs'){
            createDBModal.getScaleSelected().then(function(op){
              expect(op).toEqual(dbInfo['scale']);
            });
          }else{
            expect(createDBModal.scaleSelected.getText()).toEqual(dbInfo['scale']);
          }
        });

      });//scale
    }); // size/scale

    describe('Format',function(){

      it('should have Format* as label', function(){
        var createDBModal = new DatabaseModal();
        expect(createDBModal.formatLabel.getText()).toEqual('Format*');
      });

      it('should be empty by default',function(){
        var createDBModal = new DatabaseModal();
        expect(createDBModal.formatField.getAttribute('value')).toEqual('');
      });

      it('should not allow to save if Format is empty', function(){
        var createDBModal = new DatabaseModal();
        if(process.env.BROWSER_NAME === 'phantomjs'){
          createDBModal.saveBtn.click();
        }else{
          createDBModal.saveBtn.click();
          var alertDialog = browser.driver.switchTo().alert();
          expect(alertDialog.getText()).toEqual('Please enter value for DB Format');
          alertDialog.accept();
        }
        expect(createDBModal.isCreateModalOpened()).toBe(true);
      });

      it('should set a value for Format', function(){
        var createDBModal = new DatabaseModal();
        createDBModal.formatField.sendKeys(dbInfo['format']);
        expect(createDBModal.formatField.getAttribute('value')).toEqual(dbInfo['format']);
      });
    
    }); // Format


    describe('type',function(){
    
      it('should have Type as label', function(){
        var createDBModal = new DatabaseModal();
        expect(createDBModal.typeLabel.getText()).toEqual('Type');
      });
    
      it('should have Database as default value', function(){
        var createDBModal = new DatabaseModal();
        expect(createDBModal.typeField.getAttribute('value')).toEqual(dbInfo['type']);
      });

      it('should be readonly', function(){
        var createDBModal = new DatabaseModal();
        expect(createDBModal.typeField.getAttribute('readonly')).toEqual('true');
      });

    }); //Type

    describe('Rate of Change (%)', function(){

      it('should have Rate of Change as label', function(){
        var createDBModal = new DatabaseModal();
        expect(createDBModal.rateOfChangeLabel.getText()).toEqual('Rate of Change (%)');

      });
      
      it('should be empty by default', function(){
        var createDBModal = new DatabaseModal();
        expect(createDBModal.rateOfChangeField.getAttribute('value')).toEqual('');

      });
      xit('TM-3180 - should not allow non numeric values', function(){
        //currently getting white page
      });

      it('should allow numeric values',function(){
        var createDBModal = new DatabaseModal();
        createDBModal.rateOfChangeField.sendKeys(dbInfo['rateOfChange']);      
        expect(createDBModal.rateOfChangeField.getAttribute('value')).toEqual(dbInfo['rateOfChange']);

      });
    });//Rate of Change


    describe('Description',function(){

      it('should have description as label',function(){
        var createDBModal = new DatabaseModal();
        expect(createDBModal.descriptionLabel.getText()).toEqual('Description');
      });

      it('should be empty by default',function(){
        var createDBModal = new DatabaseModal();
        expect(createDBModal.descriptionField.getAttribute('value')).toEqual('');
      });
   
      it('should add a value',function(){
        var createDBModal = new DatabaseModal();
        createDBModal.descriptionField.sendKeys(dbInfo['description']);
        expect(createDBModal.descriptionField.getAttribute('value')).toEqual(dbInfo['description']);
      });

    }); //Description

    describe('Support',function(){

      it('should have support as label',function(){
        var createDBModal = new DatabaseModal();
        expect(createDBModal.supportLabel.getText()).toEqual('Support');
      });
      
      it('should be empty by default',function(){
        var createDBModal = new DatabaseModal();
        expect(createDBModal.supportField.getAttribute('value')).toEqual('');
      });
      
      it('should add a value',function(){
        var createDBModal = new DatabaseModal();
        createDBModal.supportField.sendKeys(dbInfo['support']);
        expect(createDBModal.supportField.getAttribute('value')).toEqual(dbInfo['support']);
      });

    }); // Support
    describe('Retire Date - Datepicker',function(){
      
      it('should have Retire Date as value',function(){
        var createDBModal = new DatabaseModal();
        expect(createDBModal.retireDateLabel.getText()).toEqual('Retire Date:');
      });
      
      it('should  be empty by default',function(){
        var createDBModal = new DatabaseModal();
        expect(createDBModal.retireDateField.getAttribute('value')).toEqual('');
      });

      it('should allow only dates',function(){
        var createDBModal = new DatabaseModal();
        createDBModal.retireDateField.sendKeys('hola');
        expect(createDBModal.retireDateField.getAttribute('value')).toEqual('');
      });
      
      it('should add a value',function(){
        var createDBModal = new DatabaseModal();
        createDBModal.retireDateField.sendKeys('03/08/2015');
        expect(createDBModal.retireDateField.getAttribute('value')).toEqual('03/08/2015');
      });
      
      xit('should add a value from daypicker',function(){
        // var createDBModal = new DatabaseModal();

      });

    });//Retire Date
    
    describe('Maint Exp. - Datepicker',function(){
      it('should have Maint Exp. as label',function(){
        var createDBModal = new DatabaseModal();
        expect(createDBModal.maintExpLabel.getText()).toEqual('Maint Exp.');
      });
      
      it('should be empty by default',function(){
        var createDBModal = new DatabaseModal();
        expect(createDBModal.maintExpField.getAttribute('value')).toEqual('');
      });
      
      it('should allow only dates',function(){
        var createDBModal = new DatabaseModal();
        createDBModal.maintExpField.sendKeys('hola');
        expect(createDBModal.maintExpField.getAttribute('value')).toEqual('');
      });

      it('should add a value',function(){
        var createDBModal = new DatabaseModal();
        createDBModal.maintExpField.sendKeys('05/05/2015');
        expect(createDBModal.maintExpField.getAttribute('value')).toEqual('05/05/2015');
      });

      xit('should add a value from daypicker',function(){
        // var createDBModal = new DatabaseModal();

      });

    });//Maint Exp.

    describe('External Ref Id',function(){

      it('should have as label External REf Id',function(){
        var createDBModal = new DatabaseModal();
        expect(createDBModal.externalRefIdLabel.getText()).toEqual('External Ref Id');
      });

      it('should be empty by default',function(){
        var createDBModal = new DatabaseModal();
        expect(createDBModal.externalRefIdField.getAttribute('value')).toEqual('');
      });

      it('should add a value',function(){
        var createDBModal = new DatabaseModal();
        createDBModal.externalRefIdField.sendKeys('5543');
        expect(createDBModal.externalRefIdField.getAttribute('value')).toEqual('5543');
      });

    });//External Ref Id

    describe('Environment',function(){

      it('should have Environment as label',function(){
        var createDBModal = new DatabaseModal();
        expect(createDBModal.environmentLabel.getText()).toEqual('Environment');
      });
        
      it('shoud have Please Select option selected by default',function(){
        var createDBModal = new DatabaseModal();
        if(process.env.BROWSER_NAME=== 'phantomjs'){
          createDBModal.getEnvironmentSelected().then(function(env){
            expect(env).toEqual(' Please Select');
          });
        }else{
          expect(createDBModal.environmentSelected.getText()).toEqual(' Please Select');
        }
      });
      
      it('should have 7 elements on the list',function(){
        var createDBModal = new DatabaseModal();
        expect(createDBModal.environmentOptions.count()).toEqual(7);
      });

      it('should have these options',function(){
        // this is configure on admin > assets options - using Marketing demo setup - DB dependent
        var createDBModal = new DatabaseModal();
        createDBModal.environmentOptions.then(function(list){
          expect(list[0].getText()).toEqual(' Please Select');
          expect(list[1].getText()).toEqual('Production');
          expect(list[2].getText()).toEqual('DR');
          expect(list[3].getText()).toEqual('Development');
          expect(list[4].getText()).toEqual('QA');
          expect(list[5].getText()).toEqual('Staging');
          expect(list[6].getText()).toEqual('UAT');
        });

      });
        
      it('should select a value from the dropdown',function(){
        var createDBModal = new DatabaseModal();
        createDBModal.environmentOptions.get(dbInfo['environment']['posList']).click();
        if(process.env.BROWSER_NAME ==='phantomjs'){
          createDBModal.getEnvironmentSelected().then(function(env){
            expect(env).toEqual(dbInfo['environment']['text']);
          });
        }else{
          expect(createDBModal.environmentSelected.getText()).toEqual(dbInfo['environment']['text']);
        }

      });
    });//Environment

    describe('Bundle',function(){
      it('should have Bundle as label',function(){
        var createDBModal = new DatabaseModal();
        expect(createDBModal.bundleLabel.getText()).toEqual('Bundle');

      });
      it('should have TBD as default',function(){
        var createDBModal = new DatabaseModal();
        if(process.env.BROWSER_NAME === 'phantomjs'){
          createDBModal.getBundleSelected().then(function(op){
            expect(op).toEqual('TBD');
          });
        }else{
          expect(createDBModal.bundleSelected.getText()).toEqual('TBD');
        }
      });

      it('should have 2 in the list', function(){
        var createDBModal = new DatabaseModal();
        expect(createDBModal.bundleOptions.count()).toEqual(2);
      });
      it('should have the following list',function(){
        var createDBModal = new DatabaseModal();
        createDBModal.bundleOptions.then(function(list){
          expect(list[0].getText()).toEqual('BundTest');
          expect(list[1].getText()).toEqual('TBD');
        });
      });

      it('should select Bund Test',function(){
        var createDBModal = new DatabaseModal();
        createDBModal.bundleOptions.get(0).click();
        if(process.env.BROWSER_NAME === 'phantomjs'){
          createDBModal.getBundleSelected().then(function(op){
            expect(op).toEqual('BundTest');
          });
        }else{
          expect(createDBModal.bundleSelected.getText()).toEqual('BundTest');
        }
      });

    });//Bundle
    describe('Plan Status',function(){

      it('should have Plan Status as label',function(){
        var createDBModal = new DatabaseModal();
        expect(createDBModal.planStatusLabel.getText()).toEqual('Plan Status');
      });
      
      it('should have Unassigned as default',function(){
        var createDBModal = new DatabaseModal();
        if(process.env.BROWSER_NAME === 'phantomjs'){
          createDBModal.getPlanStatusSelected().then(function(op){
            expect(op).toEqual('Unassigned');
          });
        }else{
          expect(createDBModal.planStatusSelected.getText()).toEqual('Unassigned');
        }
      });

      it('should have 5 in the list', function(){
        var createDBModal = new DatabaseModal();
        expect(createDBModal.planStatusOptions.count()).toEqual(5);
      });

      it('should have the following list',function(){
        var createDBModal = new DatabaseModal();
        createDBModal.planStatusOptions.then(function(list){
          expect(list[0].getText()).toEqual('Unassigned');
          expect(list[1].getText()).toEqual('Assigned');
          expect(list[2].getText()).toEqual('Confirmed');
          expect(list[3].getText()).toEqual('Locked');
          expect(list[4].getText()).toEqual('Moved');
         }); 
      });

      it('should select Confirmed',function(){
        var createDBModal = new DatabaseModal();
        createDBModal.planStatusOptions.get(2).click();
        if(process.env.BROWSER_NAME === 'phantomjs'){
          createDBModal.getPlanStatusSelected().then(function(op){
            expect(op).toEqual('Confirmed');
          });
        }else{
          expect(createDBModal.planStatusSelected.getText()).toEqual('Confirmed');
        }
      });

    }); //Plan Status

    describe('Validation',function(){

      it('should have Validation as label',function(){
        var createDBModal = new DatabaseModal();
        expect(createDBModal.validationLabel.getText()).toEqual('Validation');
      });

      it('should have Discovery as default',function(){
        var createDBModal = new DatabaseModal();
        if(process.env.BROWSER_NAME === 'phantomjs'){
          createDBModal.getValidationSelected().then(function(op){
            expect(op).toEqual('Discovery');
          });
        }else{
          expect(createDBModal.validationSelected.getText()).toEqual('Discovery');
        }
      });

      it('should have 5 in the list', function(){
        var createDBModal = new DatabaseModal();
        expect(createDBModal.validationOptions.count()).toEqual(5);
      });

      it('should have the following list',function(){
        var createDBModal = new DatabaseModal();
        createDBModal.validationOptions.then(function(list){
          expect(list[0].getText()).toEqual('Discovery');
          expect(list[1].getText()).toEqual('Validated');
          expect(list[2].getText()).toEqual('DependencyReview');
          expect(list[3].getText()).toEqual('DependencyScan');
          expect(list[4].getText()).toEqual('BundleReady');
        });
      });

      it('should select Validated',function(){
        var createDBModal = new DatabaseModal();
        createDBModal.validationOptions.get(1).click();
         if(process.env.BROWSER_NAME === 'phantomjs'){
          createDBModal.getValidationSelected().then(function(op){
            expect(op).toEqual('Validated');
          });
        }else{
          expect(createDBModal.validationSelected.getText()).toEqual('Validated');
        }
      });

    });//Validation
    xdescribe('Custom fields',function(){
      
      describe('Network Interfaces - Custom 1',function(){
        it('should have as label Network Interface',function(){

        });
        it('should be empty by default',function(){

        });
        it('should add a value', function(){

        });

      });//Network Interface
      
      describe('CMDB ID',function(){
        it('should have CMDB ID as label',function(){

        });
        it('should be empty by default',function(){

        });
        it('should add a value',function(){

        });

      }); //CMDB ID

    });//Custom fields

    it('should save database and displayed view details',function(){
      var createDBModal = new DatabaseModal();
      createDBModal.saveBtn.click();
      expect(createDBModal.isCreateModalClosed()).toBe(true);
      expect(createDBModal.isViewModalOpened()).toBe(true);
    });

  }); // create database

  describe('view created database', function(){

    describe('edit Database from view database', function(){

    }); // edit Database from view database

  }); // view created database

});//Database