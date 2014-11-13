'use strict';
var  Menu = require('../../menu/menu.po.js');
var  ListApps = require('../listApps.po.js');

describe('All Devices', function(){

  it('should load All Devices List page after select Assets > All Devicess', function(){
    var menu = new Menu();
    menu.goToAssets('allDevices');
    expect(menu.getCurrentUrl()).toEqual(process.env.BASE_URL+'/tdstm/assetEntity/list?filter=all');
  });

  it('should have "All Devices List" as title', function(){
    var appsListPage =  new ListApps();
    expect(appsListPage.titleh.getText()).toEqual('All Devices List');
  });

  describe('List Headers',function(){
    var dbList = new ListApps();
    var listin;

    it('should have 8 columns',function(){
      dbList.getColumnsHeaders().then(function(list){
        listin = list;
        expect(list.length).toEqual(14);
      });
    });
    
    it('should have only 5 columns with select header option',function(){
      dbList.getColumnsWithHeaderSelector().then(
      function(list){
        expect(list.length).toEqual(5);
      });
    });

    it('should have column 0 with empty header',function(){
      expect(listin[0].getText()).toEqual('');
    });
    
    it('should have "Actions" column',function(){
      expect(listin[1].findElement(by.css('div')).getText()).toEqual('Actions');
    });
    
    it('should have "Name" column',function(){
      expect(listin[2].findElement(by.css('div')).getText()).toEqual('Name');
    });
    
    it('should have "Device Type" column',function(){
      expect(listin[3].findElement(by.css('div')).getText()).toEqual('Device Type');
    });

    it('should have "Manufacturer" column',function(){
      expect(listin[4].findElement(by.css('div')).getText()).toEqual('Manufacturer');
    });

    it('should have "Model" column',function(){
      expect(listin[5].findElement(by.css('div')).getText()).toEqual('Model');
    });

    it('should have "Location" column',function(){
      expect(listin[6].findElement(by.css('div')).getText()).toEqual('Location');
    });

    it('should have "Source Rack/Cab" column',function(){
      expect(listin[7].findElement(by.css('div')).getText()).toEqual('Source Rack/Cab');
    });

    it('should have Source Rack/Cab Column with header selector',function(){
      expect(listin[7].findElement(by.css('img')).getAttribute('onClick')).toEqual('showSelect(\'sourceRack\',\'assetList\',\'1\')');
    });

    it('should have "Environment" column',function(){
      expect(listin[8].findElement(by.css('div')).getText()).toEqual('Environment');
    });

    it('should have Environment Column with header selector',function(){
      expect(listin[8].findElement(by.css('img')).getAttribute('onClick')).toEqual('showSelect(\'environment\',\'assetList\',\'2\')');
    });

    it('should have "Asset Tag" column',function(){
      expect(listin[9].findElement(by.css('div')).getText()).toEqual('Asset Tag');
    });

    it('should have Asset Tag Column with header selector',function(){
      expect(listin[9].findElement(by.css('img')).getAttribute('onClick')).toEqual('showSelect(\'assetTag\',\'assetList\',\'3\')');
    });

    it('should have "Serial #" column',function(){
      expect(listin[10].findElement(by.css('div')).getText()).toEqual('Serial #');
    });

    it('should have Serial # Column with header selector',function(){
      expect(listin[10].findElement(by.css('img')).getAttribute('onClick')).toEqual('showSelect(\'serialNumber\',\'assetList\',\'4\')');
    });

    it('should have "Validation" column',function(){
      expect(listin[11].findElement(by.css('div')).getText()).toEqual('Validation');
    });

    it('should have Validation Column with header selector',function(){
      expect(listin[11].findElement(by.css('img')).getAttribute('onClick')).toEqual('showSelect(\'validation\',\'assetList\',\'5\')');
    });

    it('should have "Plan Status" column',function(){
      expect(listin[12].findElement(by.css('div')).getText()).toEqual('Plan Status');
    });

    it('should have "Bundle" column',function(){
      expect(listin[13].findElement(by.css('div')).getText()).toEqual('Bundle');
    });

  }); // List Headers

}); // All Devices