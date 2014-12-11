'use strict';
var  Menu = require('../../menu/menu.po.js');
var  ListApps = require('../listApps.po.js');
describe('Application List', function(){

  it('should load Application List page after select Assets List Apps', function(){
    var menu = new Menu();
    menu.goToAssets('applications');
    expect(menu.getCurrentUrl()).toEqual(process.env.BASE_URL+'/tdstm/application/list');
  });

  it('should have as title Application List', function(){
    var appsListPage =  new ListApps();
    expect(appsListPage.titleh.getText()).toEqual('Application List');
  });

  describe('List Headers',function(){
    var appsListPage = new ListApps();
    var listin;

    it('should have 8 columns',function(){
      appsListPage.getColumnsHeaders().then(function(list){
        listin = list;
        expect(list.length).toEqual(8);
      });
    });

    it('should have only 5 columns with select header option',function(){
      appsListPage.getColumnsWithHeaderSelector().then(
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

    it('should have "SME" column',function(){
      expect(listin[3].findElement(by.css('div')).getText()).toEqual('SME');
    });

    it('should have SME Column with header selector',function(){
      expect(listin[3].findElement(by.css('img')).getAttribute('onClick')).toEqual('showSelect(\'sme\',\'application\',\'1\')');
    });

    it('should have "Environment" column',function(){
      expect(listin[4].findElement(by.css('div')).getText()).toEqual('Environment');
    });

    it('should have Environment Column with header selector',function(){
      expect(listin[4].findElement(by.css('img')).getAttribute('onClick')).toEqual('showSelect(\'environment\',\'application\',\'2\')');
    });

    it('should have "Validation" column',function(){
      expect(listin[5].findElement(by.css('div')).getText()).toEqual('Validation');
    });
    
    it('should have Validation Column with header selector',function(){
      expect(listin[5].findElement(by.css('img')).getAttribute('onClick')).toEqual('showSelect(\'validation\',\'application\',\'3\')');
    });
    
    it('should have "PlanStatus" column',function(){
      expect(listin[6].findElement(by.css('div')).getText()).toEqual('PlanStatus');
    });
    
    it('should have PlanStatus Column with header selector',function(){
      expect(listin[6].findElement(by.css('img')).getAttribute('onClick')).toEqual('showSelect(\'planStatus\',\'application\',\'4\')');
    });
    
    it('should have "MoveBundle" column',function(){
      expect(listin[7].findElement(by.css('div')).getText()).toEqual('MoveBundle');
    });
    
    it('should have MoveBundle Column with header selector',function(){
      expect(listin[7].findElement(by.css('img')).getAttribute('onClick')).toEqual('showSelect(\'moveBundle\',\'application\',\'5\')');
    });

  }); // List Headers

});// Application List