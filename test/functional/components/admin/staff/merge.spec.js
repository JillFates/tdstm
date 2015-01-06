'use strict';
var  Menu = require('../../menu/menu.po.js');
var ListStaff = require('../listStaff.po.js');
var CompareMergeModal = require('./compareMergeModal.po.js');
var ConnectDatabase = require('../../assets/importExport/connectDatabase.po.js');

describe('Merge Staff', function(){
  var personTo = {
    'id':'',
    'loginId': 0,
    'sme1': [],
    'sme2': []
  };
  var person1 = {
    'id':'',
    'loginId':0,
    'sme1': [],
    'sme2': []
  };

  it('should go to List Staff page after select Admin - List Staff', function(){
    var menu = new Menu();  
    menu.goToAdmin('listStaff');
    expect(menu.getCurrentUrl()).toEqual(process.env.BASE_URL+'/tdstm/person/list');
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
    
    it('should get login id rows', function () {
      var sqlloginId = 'select count(*) as results from user_login where user_login.person_id='+personTo.id+';';
      connectDatabase.connection.query(sqlloginId,function (err,rows) {
        if(!err){
          personTo.loginId = rows[0].results;
        }
      });
      sqlloginId = 'select count(*) as results from user_login where user_login.person_id='+person1.id+';';
      connectDatabase.connection.query(sqlloginId,function (err,rows) {
        if(!err){
          person1.loginId = rows[0].results;
        }
      });
    });    
    
    it('should get sme1, sme2',function () {
      var persons = [personTo,person1];
      persons.forEach(function (person) {
        var sqlSME1 = 'select app_id as app from application where application.sme_id ='+person.id+';';  
        connectDatabase.connection.query(sqlSME1,function (err,rows) {
          if(!err){
            for (var i = 0; i < rows.length; i++) {
              person.sme1.push(rows[i].app);
            }
            expect(rows.length).toEqual(person.sme1.length);
          }else {
            console.log('error',err);
          }
        });

        var sqlSME2 = 'select app_id as app from application where application.sme2_id ='+person.id+';';  
         connectDatabase.connection.query(sqlSME2,function (err,rows) {
          if(!err){
            for (var i = 0; i < rows.length; i++) {
              person.sme2.push(rows[i].app);
            }
            expect(rows.length).toEqual(person.sme2.length);
          } else {
            console.log('error',err);
          }
        });
         // console.log('person end', person);
      });
    
    });
    
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
        expect(alertDialog.getText()).toEqual('This will merge the selected Person');
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
    it('should delete person1 user login', function () {
      var sqlloginId = 'select count(*) as results from user_login where user_login.person_id='+person1.id+';';
      connectDatabase.connection.query(sqlloginId,function (err,rows) {
        if(!err){
          expect(rows[0].results).toEqual(0);
        }
      });
    });    

    it('should have sme1 from both persons', function() {
      var sme1After =[];
      var sqlSME1 = 'select app_id as app from application where application.sme_id ='+personTo.id+';';  
      connectDatabase.connection.query(sqlSME1,function (err,rows) {
        if(!err){
          for (var i = 0; i < rows.length; i++) {
            sme1After.push(rows[i].app);
          }
          var expSme1 = personTo.sme1.concat(person1.sme1);
          expect(sme1After.length).toEqual(expSme1.length);
          expect(sme1After.sort()).toEqual(expSme1.sort());
        }else {
          console.log('error',err);
        }
      });
    });

    it('should have sme2 from both persons', function() {
      var sme2After =[];
      var sqlSME2 = 'select app_id as app from application where application.sme2_id ='+personTo.id+';';  
      connectDatabase.connection.query(sqlSME2,function (err,rows) {
        if(!err){
          for (var i = 0; i < rows.length; i++) {
            sme2After.push(rows[i].app);
          }
          var expSme2 = personTo.sme2.concat(person1.sme2);
          expect(sme2After.length).toEqual(expSme2.length);
          expect(sme2After.sort()).toEqual(expSme2.sort());
        }else {
          console.log('error',err);
        }
      });
    });

  }); // After Merge Data Validation
}); // Merge Staff