'use strict';
var  Menu = require('../menu/menu.po.js');
var ListProjects = require('./listProjects.po.js');
var Project = require('./project.po.js');
describe('Edit a project', function(){
  var menu = new Menu();
  var listProjectPage =  new ListProjects();
  var projectPage = new Project();
  var projId;
  
  it('loads the List Projects page', function(){
    menu.goToProjects('listProjects');
    expect(listProjectPage.getTitle().getText()).toEqual('Project List - Active Projects');
  });
  
  it('should find the project to edit', function(){
    listProjectPage.setSearchProjectCode('TP01');
    expect(listProjectPage.getSearchProjectCode().getAttribute('value')).toEqual('TP01');
  });

  it('should find TP01 project and no others', function(){
    listProjectPage.verifySearchResults(1).then(function(list){
      expect(list.length).toEqual(1);
      list[0].getAttribute('id').then(function(text){
        // expect(list[0].getText()).toEqual(details);
        projId = text;
      });
    });
  });

  it('should select the project found', function(){
    listProjectPage.selectProjectfromListByPos(1,'TP01');
  });

  it('should be at project/show/project+id', function(){
    expect(menu.getCurrentUrl()).toEqual(process.env.BASE_URL+'/tdstm/project/show/'+projId);
  });

  
  it('opens the edit page for the project', function(){
    browser.driver.findElement(by.css('input[class="edit"]')).click();
  });
  
  it('appends " Edit" to the name', function(){
    browser.driver.findElement(by.id('name')).sendKeys(' Edit');
  });
  
  it('changes the project code', function(){
    browser.driver.findElement(by.id('projectCode')).clear();
    browser.driver.findElement(by.id('projectCode')).sendKeys('TPEdit');
  });
  
  it('adds a description', function(){
    browser.driver.findElement(by.id('description')).clear();
    browser.driver.findElement(by.id('description')).sendKeys('This field can be edited');
  });
  
  it('adds a comment', function(){
    browser.driver.findElement(by.id('comment')).clear();
    browser.driver.findElement(by.id('comment')).sendKeys('And so can this');
  });
  
  it('changes the start and completion dates', function(){
    browser.driver.findElement(by.id('startDateId')).clear();
    browser.driver.findElement(by.id('startDateId')).sendKeys('01/01/2000');
	browser.driver.findElement(by.id('completionDateId')).clear();
	browser.driver.findElement(by.id('completionDateId')).sendKeys('12/31/2020');
  });
  
  it('selects a Project Manager', function(){
    browser.driver.findElement(by.css('#projectManagerId option[value="Macfarlane, Tucker"]')).click();
  });
  
  it('selects a Move Manager', function(){
    browser.driver.findElement(by.css('#moveManagerId option[value="Macfarlane, Tucker"]')).click();
  });
  
  it('updates the project', function(){
    browser.driver.findElement(by.css('input[class="save"]')).click();
  });
  
  it('changes the default move bundle', function(){ 
    browser.driver.findElement(by.css('#defaultBundle option[value="M1"]')).click();
  });
  
  it('set display transitions to "No"', function(){ 
    browser.driver.findElement(by.css('#trackChanges option[value="No"]')).click();
  });
  
  it('changes workflow code, closes popup dialog, changes workflow code back, closes popup dialog', function(){ 
    browser.driver.findElement(by.css('#workflowCode option[value="Please Select"]')).click();
	var alert = browser.driver.switchTo().alert().accept();
    browser.driver.findElement(by.css('#workflowCode option[value="STD_PROCESS"]')).click();
	var alert = browser.driver.switchTo().alert().accept();
  });
  
  it('clicks runbook driven checkbox', function(){
    browser.driver.findElement(by.id('runbookOn')).click();
  });
  
  it('verifies the project is updated', function(){
	expect(browser.driver.findElement(by.css("div#updateShow table tbody tr td b")).getText()).toEqual("Test Project Edit")
  });
  
  it('changes the name back (for deleteProject to succeed)', function(){
    browser.driver.findElement(by.css('input[class="edit"]')).click();
	browser.driver.findElement(by.id('name')).clear();
	browser.driver.findElement(by.id('name')).sendKeys('Test Project');
	browser.driver.findElement(by.id('projectCode')).clear();
	browser.driver.findElement(by.id('projectCode')).sendKeys('TP01');
	browser.driver.findElement(by.css('input[class="save"]')).click();
  });
  
  it('verifies the name is the same', function(){
    expect(browser.driver.findElement(by.css("div#updateShow table tbody tr td b")).getText()).toEqual("Test Project")
  });
});