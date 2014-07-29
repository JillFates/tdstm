//Text as link on url field and custom fields

'use strict';
var  Menu = require('../menu/menu.po.js');
var  ListApps = require('./listApps.po.js');
var CreateAppModal = require('./createApp.po.js');
describe('List App', function(){
  var appName= 'App Test 1';
  var appId;

  it('should load Application List page after select Assets List Apps', function(){
    var menu = new Menu();
    menu.goToAssets('listApps');
    var appsListPage =  new ListApps();
    expect(appsListPage.titleh.getText()).toEqual('Application List');
  });

  it('should open Create app modal after click on create app button', function(){
    var appsListPage =  new ListApps();
    appsListPage.createAppBtn.click();
    var appModal = new CreateAppModal();
    expect(appModal.isCreateModalOpened()).toBe(true);
  });
    
  it('should validate Create Application modal title', function(){
    var appModal = new CreateAppModal();
    expect(appModal.createAppTitle.getText()).toEqual('Create Application');
  });

  describe('Create Application Modal', function(){

    var appModal = new CreateAppModal();

    describe('name field', function(){

      it('Should have Name as label for Application Name', function(){
        expect(appModal.nameLabel.getText()).toEqual('Name*');
      });

      it('should add a Name', function(){
        appModal.setName(appName);
        expect(appModal.nameField.getAttribute('value')).toEqual(appName);
      });

      xit('should be required', function(){
          // this is not requested as required. 
      });

    }); // name field

    describe('type field',function(){

      it('should have type as label', function(){
        expect(appModal.typeLabel.getText()).toEqual('Type');
      });

      it('should be application by default', function(){
        expect(appModal.typeField.getAttribute('value')).toEqual('Application');
      });

    }); // type field

    it('should save and close', function(){
      appModal.saveCloseBtn.click();
      expect(appModal.isCreateModalClosed()).toBe(true);
    });

  }); // Create Application Modal

  xit('should displayed confirmation message', function(){
    var appsListPage =  new ListApps();
    expect(appsListPage.confirmMsg.getText()).toEqual('Application App Test 1 created');
  });
  
  xdescribe('search for app', function(){

    var appsListPage =  new ListApps();

    it('should set name to search an app', function(){
      var field = appsListPage.searchNamefield;
      field.clear();
      field.sendKeys(appName);
      expect(field.getAttribute('value')).toEqual(appName);
    });

    it('should validate search results', function(){
      appsListPage.verifySearchResults(1).then(function(list){
        list[0].getAttribute('id').then(function(pid){
          appId = pid;
        });
      });
    });
  }); // Search for an app

});// Create App