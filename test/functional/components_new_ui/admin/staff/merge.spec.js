'use strict';
var  Menu = require('../../menu/menu.po.js');
var ListStaff = require('../listStaff.po.js');
var CompareMergeModal = require('./compareMergeModal.po.js');
var ConnectDatabase = require('../../assets/importExport/connectDatabase.po.js');
var Person = require('./person.js');
describe('Merge Staff', function(){
  var personTo = new Person();
  var person1 = new Person();
  var assetComment = {'createdBy':'created_by','resolvedBy':'resolved_by','ownerId':'owner_id','assignedToId':'assigned_to_id'};


  it('should go to List Staff page after select Admin - List Staff', function(){
    var menu = new Menu();  
    menu.goToAdmin('listStaff');
    expect(menu.getCurrentUrl('/tdstm/person/list')).toEqual(process.env.BASE_URL+'/tdstm/person/list');
  });

  it('should have list Staff as title',function(){
    var listStaffPage = new ListStaff();
    expect(listStaffPage.titleh.getText()).toEqual('Staff List');
  });

  it('should select first 2 persons to merge', function() {
    var listStaffPage = new ListStaff();
    listStaffPage.getListItems(0,'[aria-describedby="personIdGrid_cb"]').then(function (list) {
      list[0].click();
      list[1].click();
    });

    listStaffPage.getListItems(0).then(function (list) {
      list[0].getAttribute('id').then(function (id) {
        personTo.id = id;
      });
      list[1].getAttribute('id').then(function (id) {
        person1.id = id;
      });
      expect(listStaffPage.isListItemSelected(list[0])).toEqual('true');
      expect(listStaffPage.isListItemSelected(list[1])).toEqual('true');
    });
  });


  describe('Before Merge Data', function() {
    var connectDatabase = new ConnectDatabase();
    connectDatabase.connection.connect();
    
    var persons = [personTo,person1];

    persons.forEach(function (person) {
    
      it('should get login id rows USER_LOGIN table', function () {
        var sqlloginId = 'select count(*) as results from user_login where user_login.person_id='+person.id+';';
        connectDatabase.connection.query(sqlloginId,function (err,rows) {
          if(!err){
            person.loginId = rows[0].results;
          }
        });
      });    
    
      it('should get applicationSme1, applicationSme2 APPLICATION table',function () {
        var sqlapplicationSme1 = 'select app_id as app from application where application.sme_id ='+person.id+';';  
        connectDatabase.connection.query(sqlapplicationSme1,function (err,rows) {
          if(!err){
            for (var i = 0; i < rows.length; i++) {
              person.application.bySme1.push(rows[i].app);
            }
            expect(rows.length).toEqual(person.application.bySme1.length);
          }else {
            console.log('error',err);
          }
        });

        var sqlapplicationSme2 = 'select app_id as app from application where application.sme2_id ='+person.id+';';  
         connectDatabase.connection.query(sqlapplicationSme2,function (err,rows) {
          if(!err){
            for (var i = 0; i < rows.length; i++) {
              person.application.bySme2.push(rows[i].app);
            }
            expect(rows.length).toEqual(person.application.bySme2.length);
          } else {
            console.log('error',err);
          }
        });
         // console.log('person end', person);
      });
      
      for ( var outerKey in assetComment){

        (function(key) {

          it('should get data from ASSET_COMMENT Table by '+key, function () {
            var sql = 'select asset_comment_id as '+key+' from asset_comment where '+assetComment[key]+'='+person.id+';';
            connectDatabase.connection.query(sql,function (err,rows) {
              if(!err){
                for (var i = 0; i < rows.length; i++) {
                  person.assetComment[key].push(rows[i][key]);
                }
                expect(rows.length).toEqual(person.assetComment[key].length);
              } else {
                console.log('error',err);
              }
            });
          });

        })(outerKey);
      }

    }); // foreach person
    
  }); // Before Merge Data
  
  describe('merge', function() {
    
    it('should have Compare/Merge button enabled',function () {
      var listStaffPage = new ListStaff();
      expect(listStaffPage.isCompareMergeEnabled()).toEqual(true);
    });
    
    it('should displayed merge modal after click on merge button', function() {
      var listStaffPage = new ListStaff();
      listStaffPage.compareMergeBtn.click();
      var compareMergeModal = new CompareMergeModal();
      expect(compareMergeModal.isOpened()).toEqual(true);
    });

    it('should merge persons', function() {
      var compareMergeModal = new CompareMergeModal();
      compareMergeModal.clickMergeBtn();
      if(process.env.BROWSER_NAME !== 'phantomjs'){
        var alertDialog = browser.driver.switchTo().alert();
        expect(alertDialog.getText()).toEqual('This will merge the selected Person(s)');
        alertDialog.accept();        
      }
    });

    it('should displayed merged to confirmation message', function() {
      var listStaffPage = new ListStaff();
      //add persons name to the expect
      expect(listStaffPage.getMessage('Merged to')).toContain('Merged to');
    });
  }); // merge

  describe('After Merge Data Validation', function() {
    var connectDatabase = new ConnectDatabase();
    connectDatabase.connection.connect();
    
    it('should keep personTo user_login',function () {
      var sqlloginId = 'select count(*) as results from user_login where user_login.person_id='+personTo.id+';';
      connectDatabase.connection.query(sqlloginId,function (err,rows) {
        if(!err){
          expect(rows[0].results).toEqual(personTo.loginId);
        }
      });
    });

    it('should delete person1 from person table', function () {
      var sql = 'select count(*) as results from person where person_id ='+person1.id+';';
        connectDatabase.connection.query(sql,function (err,rows) {
        if(!err){
          expect(rows[0].results).toEqual(0);
        }
      });
    });

    it('should delete person1 user login', function () {
      var sqlloginId = 'select count(*) as results from user_login where user_login.person_id='+person1.id+';';
      connectDatabase.connection.query(sqlloginId,function (err,rows) {
        if(!err){
          expect(rows[0].results).toEqual(0);
        }
      });
    });    

    it('should have applicationSme1 from both persons', function() {
      var applicationSme1After =[];
      var sqlapplicationSme1 = 'select app_id as app from application where application.sme_id ='+personTo.id+';';  
      connectDatabase.connection.query(sqlapplicationSme1,function (err,rows) {
        if(!err){
          for (var i = 0; i < rows.length; i++) {
            applicationSme1After.push(rows[i].app);
          }
          var expapplicationSme1 = personTo.application.bySme1.concat(person1.application.bySme1);
          expect(applicationSme1After.length).toEqual(expapplicationSme1.length);
          expect(applicationSme1After.sort()).toEqual(expapplicationSme1.sort());
        }else {
          console.log('error',err);
        }
      });
    });

    it('should have applicationSme2 from both persons', function() {
      var applicationSme2After =[];
      var sqlapplicationSme2 = 'select app_id as app from application where application.sme2_id ='+personTo.id+';';  
      connectDatabase.connection.query(sqlapplicationSme2,function (err,rows) {
        if(!err){
          for (var i = 0; i < rows.length; i++) {
            applicationSme2After.push(rows[i].app);
          }
          var expapplicationSme2 = personTo.application.bySme2.concat(person1.application.bySme2);
          expect(applicationSme2After.length).toEqual(expapplicationSme2.length);
          expect(applicationSme2After.sort()).toEqual(expapplicationSme2.sort());
        }else {
          console.log('error',err);
        }
      });
    });

    for (var outerKey in assetComment){
      (function(key){

        it('should have assets_comment '+key+' from both person', function () {
          var sql = 'select asset_comment_id as '+key+' from asset_comment where '+assetComment[key]+'='+personTo.id+';';
          var results = [];
          connectDatabase.connection.query(sql,function (err,rows) {
            if(!err){
              for (var i = 0; i < rows.length; i++) {
                results.push(rows[i][key]);
              }
              console.log('assetComment '+key, results);
              var expResults = personTo.assetComment[key].concat(person1.assetComment[key]);
              expect(results.length).toEqual(expResults.length);
              expect(results.sort()).toEqual(expResults.sort());
            } else {
              console.log('error',err);
            }
          });
        });

        it('should have no asset_comment '+key+' person1', function() {
          var sql = 'select count(*) as results from asset_comment where '+assetComment[key]+'='+person1.id+';';
          connectDatabase.connection.query(sql,function (err,rows) {
            if(!err){
              expect(rows[0].results).toEqual(0);
            } else {
              console.log('error',err);
            }
          });
        });

      })(outerKey);
    }

  }); // After Merge Data Validation
}); // Merge Staff