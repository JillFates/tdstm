'use strict';
var  Menu = require('../menu/menu.po.js');
var ListProjects = require('./listProjects.po.js');
var Project = require('./project.po.js');
describe('Create Project Page Validations', function() {
  var menu = new Menu();
  var listProjectPage =  new ListProjects();
  var projectPage = new Project();
  // var projId;

  it('should load Active list projects page after select Projects > List Projects', function(){
    menu.goToProjects('listProjects');
    expect(menu.getCurrentUrl('/tdstm/project/list?active=active')).toEqual(process.env.BASE_URL+'/tdstm/project/list?active=active');
  });

  it('should have Project List - Active Projects as title', function () {
    expect(listProjectPage.getTitle().getText()).toEqual('Project List - Active Projects');
  });

  it('should load create project page after hitting create project button',function(){
    listProjectPage.clickOnCreateProjectBtn();
    expect(menu.getCurrentUrl('/tdstm/project/create')).toEqual(process.env.BASE_URL+'/tdstm/project/create');

  });

  it('should have "Create Project" as title', function() {
    expect(projectPage.getTitle().getText()).toEqual('Create Project');
  });

  it('should have project/create url', function(){
    expect(menu.getCurrentUrl()).toEqual(process.env.BASE_URL+'/tdstm/project/create');
  });

  describe('Client dropdown',function () {
    it('should have "Client: *" as label for client', function(){
      expect(projectPage.getClientFieldLabel().getText()).toEqual('Client: *');
    });  
  }); // Client dropdown


  describe('Require field validations', function(){

    xit('should attempt to create and submit a project with no workflow code',function(){
        browser.driver.findElement(by.css('input[class="create"]')).click();
      browser.driver.findElement(by.id('name')).sendKeys('TEST_PROJECT');   
      browser.driver.findElement(by.id('projectCode')).sendKeys('TESTY');
      browser.driver.findElement(by.css('input[class="save"')).click();
      expect(listProjectPage.getTitle().getText()).toEqual('Create Project');
      browser.sleep(500); 
    });
    
    xit('should attempt to submit a project with no project name',function(){
      browser.driver.findElement(by.id('name')).clear();  
      browser.driver.findElement(by.id('workflowCode')).click;
      browser.driver.findElement(by.css('option[value="STD_PROCESS"')).click();
      browser.driver.findElement(by.css('input[class="save"')).click();
      expect(listProjectPage.getTitle().getText()).toEqual('Create Project');
      browser.sleep(500);
    });
    
    xit('should attempt to submit a project with no project code',function(){
      browser.driver.findElement(by.id('name')).sendKeys('TEST_PROJECT');
      browser.driver.findElement(by.id('projectCode')).clear();
      browser.driver.findElement(by.css('input[class="save"')).click();
      expect(listProjectPage.getTitle().getText()).toEqual('Create Project');
      browser.sleep(500);
    });
    
    xit('should attempt to submit a project with no creation date',function(){
      browser.driver.findElement(by.id('completionDateId')).clear();
      browser.driver.findElement(by.id('projectCode')).sendKeys('TESTY');
      browser.driver.findElement(by.css('input[class="save"')).click();
      expect(listProjectPage.getTitle().getText()).toEqual('Create Project');
      browser.sleep(500); 
    });
    
    xit('should press the cancel button',function(){
    browser.driver.findElement(by.css('input[class="delete"')).click();
      expect(listProjectPage.getTitle().getText()).toEqual('Project List - Active Projects');
    });
    
  }); // require field validation
    
}); // Create Project Page Validations