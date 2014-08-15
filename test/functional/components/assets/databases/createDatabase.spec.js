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
  
  var appName = 'DB Test 1';
  var appType = 'Database';
  var format = 'Oracle';
  var appId;

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
    expect(createDBModal.isCOpened).toBe(true);
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
    
    describe('size/scale',function(){

      it('should have as label size/scale*',function(){
        var createDBModal = new DatabaseModal();
        expect(createDBModal.sizeScaleLabel.getText()).toEqual('Size/Scale*');
      });
     
      describe('size',function(){

        it('should not allow to save if size is empty',function(){
          var createDBModal = new DatabaseModal();
          expect(createDBModal.sizeField.getAttribute('value')).toEqual('');
          createDBModal.saveBtn.click();
          var alertDialog = browser.driver.switchTo().alert();
          expect(alertDialog.getText()).toEqual('Please enter numeric value for DB Size');
          alertDialog.accept();
        });
        
        it('should not allow to save if Size is not numeric', function(){
          var createDBModal = new DatabaseModal();
          createDBModal.sizeField.sendKeys('hola');
          expect(createDBModal.sizeField.getAttribute('value')).toEqual('hola');
          createDBModal.saveBtn.click();
          var alertDialog = browser.driver.switchTo().alert();
          expect(alertDialog.getText()).toEqual('Please enter numeric value for DB Size');
          alertDialog.accept();        
        });

        it('should set a numeric value for size',function(){
          var createDBModal = new DatabaseModal();
          createDBModal.sizeField.clear();
          createDBModal.sizeField.sendKeys('2');
          expect(createDBModal.sizeField.getAttribute('value')).toEqual('2');
        });

      });// size

      describe('scale',function(){
        it('should have Please Select as default value',function(){
          var createDBModal = new DatabaseModal();
          expect(createDBModal.scaleSelected.getText()).toEqual(' Please Select');
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
          expect(createDBModal.scaleSelected.getText()).toEqual('Megabyte');
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
        //Broken reported on jira
        var createDBModal = new DatabaseModal();
        createDBModal.saveBtn.click();
        var alertDialog = browser.driver.switchTo().alert();
        expect(alertDialog.getText()).toEqual('Please enter value for DB Format');
        alertDialog.accept();
      });
      
      it('should set a value for Format', function(){
        var createDBModal = new DatabaseModal();
        createDBModal.formatField.sendKeys(format);
        expect(createDBModal.formatField.getAttribute('value')).toEqual(format);
      });
    
    }); // Format

    describe('Name',function(){

      it('should have name* label', function(){
        var createDBModal = new DatabaseModal();
        expect(createDBModal.nameLabel.getText()).toEqual('Name*');
      });
      
      it('should be empty by default', function(){
        var createDBModal = new DatabaseModal();
        expect(createDBModal.nameField.getAttribute('value')).toEqual('');
      });

      xit('should not allow to save if name is empty', function(){
        //Broken reported on jira
        var createDBModal = new DatabaseModal();
        createDBModal.saveBtn.click();
        var alertDialog = browser.driver.switchTo().alert();
        expect(alertDialog.getText()).toEqual('');
        alertDialog.accept();
      });

      it('should set a name', function(){
        var createDBModal = new DatabaseModal();
        createDBModal.setName(appName);
        expect(createDBModal.nameField.getAttribute('value')).toEqual(appName);
      });

    }); //name

    describe('type',function(){
    
      it('should have Type as label', function(){
        var createDBModal = new DatabaseModal();
        expect(createDBModal.typeLabel.getText()).toEqual('Type');
      });
    
      it('should have Database as default value', function(){
        var createDBModal = new DatabaseModal();
        expect(createDBModal.typeField.getAttribute('value')).toEqual(appType);
      });

      it('should be readonly', function(){
        var createDBModal = new DatabaseModal();
        expect(createDBModal.typeField.getAttribute('readonly')).toEqual('true');
      });
    }); //Type
    describe('Rate of Change (%)', function(){

    });//Rate of Change
    describe('Network Interfaces',function(){

    });//Network Interface
    
  }); // create database

  describe('view created database', function(){

    describe('edit Database from view database', function(){

    }); // edit Database from view database
  }); // view created database

});//Database