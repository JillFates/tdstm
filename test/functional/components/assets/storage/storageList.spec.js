'use strict';
var  Menu = require('../../menu/menu.po.js');
var  ListApps = require('../listApps.po.js');
describe('Logical Storage', function(){

  it('should load  Logical Storage List page after select Assets > Logical Storage', function(){
    var menu = new Menu();
    menu.goToAssets('logicalStorage');
    expect(menu.getCurrentUrl()).toEqual(process.env.BASE_URL+'/tdstm/files/list');
  });

  it('should have "Logical Storage List" as title', function(){
    var storageList = new ListApps();
    expect(storageList.titleh.getText()).toEqual('Logical Storage List');
  });

  describe('List Headers',function(){
    var storageList = new ListApps();
    var listin;

    it('should have 8 columns',function(){
      storageList.getColumnsHeaders().then(function(list){
        listin = list;
        expect(list.length).toEqual(8);
      });
    });
    it('should have only 5 columns with select header option',function(){
      storageList.getColumnsWithHeaderSelector().then(
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
    it('should have "Format" column',function(){
      expect(listin[3].findElement(by.css('div')).getText()).toEqual('Format');
    });
    it('should have Format Column with header selector',function(){
      expect(listin[3].findElement(by.css('img')).getAttribute('onClick')).toEqual('showSelect(\'fileFormat\',\'storage\',\'1\')');
    });
    it('should have "Size" column',function(){
      expect(listin[4].findElement(by.css('div')).getText()).toEqual('Size');
    });
    it('should have Size Column with header selector',function(){
      expect(listin[4].findElement(by.css('img')).getAttribute('onClick')).toEqual('showSelect(\'size\',\'storage\',\'2\')');
    });
    it('should have "Validation" column',function(){
      expect(listin[5].findElement(by.css('div')).getText()).toEqual('Validation');
    });
    it('should have Validation Column with header selector',function(){
      expect(listin[5].findElement(by.css('img')).getAttribute('onClick')).toEqual('showSelect(\'validation\',\'storage\',\'3\')');
    });
    it('should have "PlanStatus" column',function(){
      expect(listin[6].findElement(by.css('div')).getText()).toEqual('PlanStatus');
    });
    it('should have PlanStatus Column with header selector',function(){
      expect(listin[6].findElement(by.css('img')).getAttribute('onClick')).toEqual('showSelect(\'planStatus\',\'storage\',\'4\')');
    });
    it('should have "MoveBundle" column',function(){
      expect(listin[7].findElement(by.css('div')).getText()).toEqual('MoveBundle');
    });
    it('should have MoveBundle Column with header selector',function(){
      expect(listin[7].findElement(by.css('img')).getAttribute('onClick')).toEqual('showSelect(\'moveBundle\',\'storage\',\'5\')');
    });

  }); // List Headers

});// storage List