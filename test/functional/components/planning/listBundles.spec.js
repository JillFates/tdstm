'use strict';
var  Menu = require('../menu/menu.po.js');
var ListBundles = require('./bundles.po.js');
describe('List Bundles', function() {
  var menu = new Menu();
  var bundlePage = new ListBundles();
  var bundleId;
  var bundleName = 'TBD';

  it('should load list Bundles page after select Events/Bundles > List Bundles', function(){
    menu.goToPlanning('listBundles');
    expect(menu.getCurrentUrl()).toEqual(process.env.BASE_URL+'/tdstm/moveBundle/list');
  });

  it('should have "Bundle List" as Title',function () {
    expect(bundlePage.getTitle().getText()).toEqual('Bundle List');
  });

  describe('Table Headers', function() {
    var listin;
    
    it('should have 6 columns', function() {
      bundlePage.getColumnsHeaders().then(function(list){
        listin = list;
        expect(list.length).toEqual(6);
      });
    });

    it('should have column 1 with "Name" as header', function() {
      expect(listin[0].getText()).toEqual(' Name');
    });

    it('should have column 2 with "Description" as header', function() {
      expect(listin[1].getText()).toEqual(' Description');
    });

    it('should have column 3 with "Planning" as header', function() {
      expect(listin[2].getText()).toEqual(' Planning');
    });

    it('should have column 4 with "Asset Qty" as header', function() {
      expect(listin[3].getText()).toEqual(' Asset Qty');
    });

    it('should have column 5 with "Start" as header', function() {
      expect(listin[4].getText()).toEqual(' Start');
    });

    it('should have column 6 with "Completion" as header', function() {
      expect(listin[5].getText()).toEqual(' Completion');
    });

  }); // Table Headers

  describe('Default bundle', function() {
    
    it('should set name TBD to search Bundle', function() {
      var field = bundlePage.getSearchNameField();
      field.clear();
      field.sendKeys(bundleName);
      expect(field.getAttribute('value')).toEqual(bundleName);
    });
    it('should validate search results', function() {
      bundlePage.verifySearchResults(1,bundleName).then(function(list){
        list[0].getAttribute('id').then(function(pid){
          bundleId = pid;
        });
        expect(list.length).toEqual(1);
      });
    });

    it('should select TBD bundle', function() {
      bundlePage.selectBundle(bundleId);
      expect(menu.getCurrentUrl()).toEqual(process.env.BASE_URL+'/tdstm/moveBundle/show/'+bundleId);
        
    });
    xit('should not be able to delete it using "Delete" button', function() {
      
    });
    xit('should not be able to delete it using "Delete Bundle and Assets" button', function() {
      
    });

    it('should go to Bundle list after click on "Bundle List" button', function() {
      bundlePage.clickBundleListBtn();
      expect(menu.getCurrentUrl()).toEqual(process.env.BASE_URL+'/tdstm/moveBundle/list');
    });
  
  });//Default bundle
  
  describe('not allow to create a Bundle with existing Bundle Name', function() {

    it('should load create Bundle page after hitting create Bundle button',function(){
      bundlePage.clickOnCreateBundleBtn();
      expect(bundlePage.getTitle().getText()).toEqual('Create Bundle');
    });
    
    it('should have moveBundle/create url', function(){
      expect(menu.getCurrentUrl())
        .toEqual(process.env.BASE_URL+'/tdstm/moveBundle/create');
    });

    it('should set bundle Name', function(){
      bundlePage.setName(bundleName);
      expect(bundlePage.getName()
        .getAttribute('value')).toEqual(bundleName);
    });

    it('should save the bundle and go to moveBundle/show/', function(){
      bundlePage.clickSaveBundle();
      bundlePage.getErrors().then(function (list) {
        expect(list.length).toEqual(1);
        expect(list[0].getText()).toEqual('Property name of class MoveBundle with value [TBD] must be unique');
      });
    });
    
  });

}); // List Bundles