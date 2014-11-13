'use strict';
var Menu = require('../../menu/menu.po.js');
var ListApps = require('../listApps.po.js');
var CreateDeviceModal = require('./devicesCreateModal.po.js');
// var EditDeviceModal = require('./devicesEditModal.po.js');
var ViewDeviceModal = require('./devicesViewModal.po.js');
describe('All Devices', function(){
    var appName = 'Blade app1';
    it('should go to All Devices List page after select Assets > All Devices', function(){
      var menu = new Menu();
      menu.goToAssets('allDevices');
      expect(menu.getCurrentUrl()).toEqual(process.env.BASE_URL+'/tdstm/assetEntity/list?filter=all');
    });

    it('should have "All Devices List" as title', function(){
      var appsListPage =  new ListApps();
      expect(appsListPage.titleh.getText()).toEqual('All Devices List');
    });

    it('should open Create device modal after click on create button', function(){
      var appsListPage =  new ListApps();
      appsListPage.createServerBtn.click();
      var deviceModal = new CreateDeviceModal();
      expect(deviceModal.isCreateModalOpened()).toBe(true);
    });

    describe('Create Server Modal', function(){

      it('should has as title create server', function(){
        var deviceModal = new CreateDeviceModal();
        expect(deviceModal.getModalTitle().getText()).toEqual('Device Create');
      });

      it('should create a server and save and view',function(){
        var deviceModal = new CreateDeviceModal();
        var deviceType = 'Blade';
        deviceModal.createDevice(appName,deviceType);
        var viewDeviceModal = new ViewDeviceModal();
        expect(viewDeviceModal.isViewModalOpened()).toBe(true);
      });
  
    }); // Create Server Modal

  describe('Check View Device Blade',function(){
    
    it('should displayed BladeChassis Label',function(){
      var viewDeviceModal = new ViewDeviceModal();
      expect(viewDeviceModal.chassisBladeLabel.isDisplayed()).toEqual(true);
    });
  
    it('should be hidden Rack/Cabinet Label',function(){
      var viewDeviceModal = new ViewDeviceModal();
      expect(viewDeviceModal.rackCabinetLabel.isDisplayed()).toEqual(false);
    });
  
  });

  describe('Check Edit Device Blade Label',function(){
    it('should close view modal after click on edit button',function(){
      var viewDeviceModal = new ViewDeviceModal();
      viewDeviceModal.editBtn.click();
      expect(viewDeviceModal.isViewModalClosed()).toBe(true);
     });
     it('should be opened edit modal',function(){ 
      var deviceModal = new CreateDeviceModal();
      expect(deviceModal.isCreateModalOpened()).toBe(true);
    });
   
    it('should have Device Edit as title', function(){
      var deviceModal = new CreateDeviceModal();
      expect(deviceModal.getModalTitle('view').getText()).toEqual('Device Edit');
    });

    it('should displayed BladeChassis Label',function(){
      var deviceModal = new CreateDeviceModal();
      expect(deviceModal.chassisBladeLabel.isDisplayed()).toEqual(true);
    });
  
    it('should be hidden Rack/Cabinet Label',function(){
      var deviceModal = new CreateDeviceModal();
      expect(deviceModal.rackCabinetLabel.isDisplayed()).toEqual(false);
    });

  });

});//Server Modal