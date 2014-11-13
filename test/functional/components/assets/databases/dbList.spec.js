'use strict';
var  Menu = require('../../menu/menu.po.js');
var  ListApps = require('../listApps.po.js');
describe('Database List', function(){

  it('should load database List page after select Assets > Databases', function(){
    var menu = new Menu();
    menu.goToAssets('databases');
    expect(menu.getCurrentUrl()).toEqual(process.env.BASE_URL+'/tdstm/database/list');
  });

  it('should have Database List as title', function(){
    var dbList = new ListApps();
    expect(dbList.titleh.getText()).toEqual('Database List');
  });

  describe('List Headers',function(){
    var dbList = new ListApps();
    var listin;

    it('should have 8 columns',function(){
      dbList.getColumnsHeaders().then(function(list){
        listin = list;
        expect(list.length).toEqual(8);
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
    it('should have "Format" column',function(){
      expect(listin[3].findElement(by.css('div')).getText()).toEqual('Format');
    });
    it('should have Format Column with header selector',function(){
      expect(listin[3].findElement(by.css('img')).getAttribute('onClick')).toEqual('showSelect(\'dbFormat\',\'database\',\'1\')');
    });
    it('should have "Size" column',function(){
      expect(listin[4].findElement(by.css('div')).getText()).toEqual('Size');
    });
    it('should have Size Column with header selector',function(){
      expect(listin[4].findElement(by.css('img')).getAttribute('onClick')).toEqual('showSelect(\'size\',\'database\',\'2\')');
    });
    it('should have "Validation" column',function(){
      expect(listin[5].findElement(by.css('div')).getText()).toEqual('Validation');
    });
    it('should have Validation Column with header selector',function(){
      expect(listin[5].findElement(by.css('img')).getAttribute('onClick')).toEqual('showSelect(\'validation\',\'database\',\'3\')');
    });
    it('should have "PlanStatus" column',function(){
      expect(listin[6].findElement(by.css('div')).getText()).toEqual('PlanStatus');
    });
    it('should have PlanStatus Column with header selector',function(){
      expect(listin[6].findElement(by.css('img')).getAttribute('onClick')).toEqual('showSelect(\'planStatus\',\'database\',\'4\')');
    });
    it('should have "MoveBundle" column',function(){
      expect(listin[7].findElement(by.css('div')).getText()).toEqual('MoveBundle');
    });
    it('should have MoveBundle Column with header selector',function(){
      expect(listin[7].findElement(by.css('img')).getAttribute('onClick')).toEqual('showSelect(\'moveBundle\',\'database\',\'5\')');
    });

  }); // List Headers

});// Database List