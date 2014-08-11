'use strict';
var  Menu = require('../menu/menu.po.js');
var ListProjects = require('./listProjects.po.js');
describe('List Projects', function(){
  var menu = new Menu();
  var listProjectPage =  new ListProjects();
  
  describe('search by code and select it', function(){
    var projId;
    it('should load list projects page after select Client/Project ListProjects', function(){
      menu.goToProjects('listProjects');
      expect(listProjectPage.getTitle().getText()).toEqual('Project List - Active Projects');
    });

    it('should find the project to select', function(){
      listProjectPage.setSearchProjectCode('MarketingDemo');
      expect(listProjectPage.getSearchProjectCode().getAttribute('value')).toEqual('MarketingDemo');
    });

    it('should only be found MarketingDemo project', function(){
      var results= 1;
      listProjectPage.verifySearchResults(results).then(function(list){
        expect(list.length).toEqual(results);
        list[0].getAttribute('id').then(function(text){
          projId = text;
        });
      });
    });

    it('should select the project found', function(){
      listProjectPage.selectProjectfromListByPos(1,'MarketingDemo');
    });

    it('should be redirect to project/show/project+id', function(){
      expect(menu.getCurrentUrl()).toEqual(process.env.BASE_URL+'/tdstm/project/show/'+projId);
    });

    }); // search by code and select it

  
  it('should load list projects page after select Client/Project ListProjects', function(){
    menu.goToProjects('listProjects');
    expect(listProjectPage.getTitle().getText()).toEqual('Project List - Active Projects');
  });

  it('should sort the projects by project code in both orders', function(){
	browser.sleep(1000);
	browser.driver.findElement(by.id('jqgh_projectGridIdGrid_projectCode')).click();
	browser.sleep(1000);
	browser.driver.findElement(by.id('jqgh_projectGridIdGrid_projectCode')).click();
    expect(listProjectPage.getTitle().getText()).toEqual('Project List - Active Projects');
  });

  it('should sort the projects by name in both orders', function(){
	browser.sleep(1000);
	browser.driver.findElement(by.id('jqgh_projectGridIdGrid_name')).click();
	browser.sleep(1000);
	browser.driver.findElement(by.id('jqgh_projectGridIdGrid_name')).click();
    expect(listProjectPage.getTitle().getText()).toEqual('Project List - Active Projects');
  });
   it('should sort the projects by start date in both orders', function(){
	browser.sleep(1000);
	browser.driver.findElement(by.id('jqgh_projectGridIdGrid_startDate')).click();
	browser.sleep(1000);
	browser.driver.findElement(by.id('jqgh_projectGridIdGrid_startDate')).click();
    expect(listProjectPage.getTitle().getText()).toEqual('Project List - Active Projects');
  });
   it('should sort the projects by completion date in both orders', function(){
	browser.sleep(1000);
	browser.driver.findElement(by.id('jqgh_projectGridIdGrid_completionDate')).click();
	browser.sleep(1000);
	browser.driver.findElement(by.id('jqgh_projectGridIdGrid_completionDate')).click();
    expect(listProjectPage.getTitle().getText()).toEqual('Project List - Active Projects');
  });
   it('should sort the projects by comments in both orders', function(){
	browser.sleep(1000);
	browser.driver.findElement(by.id('jqgh_projectGridIdGrid_comment')).click();
	browser.sleep(1000);
	browser.driver.findElement(by.id('jqgh_projectGridIdGrid_comment')).click();
    expect(listProjectPage.getTitle().getText()).toEqual('Project List - Active Projects');
  });
   it('should search for "no" under Project Code, then delete it.', function(){
	browser.sleep(1000);
	browser.driver.findElement(by.id('gs_projectCode')).sendKeys('no');
	browser.sleep(1000);
	browser.driver.findElement(by.id('gs_projectCode')).clear();
    expect(listProjectPage.getTitle().getText()).toEqual('Project List - Active Projects');
	browser.sleep(1000);
  });
     it('should search for "Only Project" under Name, then delete it.', function(){
	browser.sleep(1000);
	browser.driver.findElement(by.id('gs_name')).sendKeys('Only Project');
	browser.sleep(1000);
	browser.driver.findElement(by.id('gs_name')).clear();
    expect(listProjectPage.getTitle().getText()).toEqual('Project List - Active Projects');
	browser.sleep(1000);
  });
     it('should search for "06/17/2014" under Start Date, then delete it.', function(){
	browser.sleep(1000);
	browser.driver.findElement(by.id('gs_startDate')).sendKeys('06/17/2014');
	browser.sleep(1000);
	browser.driver.findElement(by.id('gs_startDate')).clear();
    expect(listProjectPage.getTitle().getText()).toEqual('Project List - Active Projects');
	browser.sleep(1000);
  });
     it('should search for "09/15/2014" under Completion Date, then delete it.', function(){
	browser.sleep(1000);
	browser.driver.findElement(by.id('gs_completionDate')).sendKeys('09/15/2014');
	browser.sleep(1000);
	browser.driver.findElement(by.id('gs_completionDate')).clear();
    expect(listProjectPage.getTitle().getText()).toEqual('Project List - Active Projects');
	browser.sleep(1000);
  });
     it('should search for "test comment" under Project Code, then delete it.', function(){
	browser.sleep(1000);
	browser.driver.findElement(by.id('gs_comment')).sendKeys('test comment');
	browser.sleep(1000);
	browser.driver.findElement(by.id('gs_comment')).clear();
    expect(listProjectPage.getTitle().getText()).toEqual('Project List - Active Projects');
	browser.sleep(1000);
  });
    it('should go to the completed projects section', function(){
	browser.sleep(1000);
	browser.driver.findElement(by.css('a[href="/tdstm/project/list?active=completed"]')).click();
    expect(listProjectPage.getTitle().getText()).toEqual('Project List - Completed Projects');
  });
}); // List Projects