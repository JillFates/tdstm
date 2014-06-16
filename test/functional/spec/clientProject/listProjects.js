'use Strict';
var ClientProjectHelper = require('../../support/helpers/clientProject-helper.js').ClientProjectHelper;
var MenuHelper = require('../../support/helpers/menu-helper.js').MenuHelper;
describe('List Projects',function(){
  var project = new ClientProjectHelper();
  var menu = new MenuHelper();  
  
  it('go to  Project list', function(){
    browser.driver.sleep(2222);
    menu.selectopt('clientProject','listProjects');
    var title = browser.driver.findElement(by.css(project.titleh1Css));
    expect(title.getText()).toEqual('Project List - Active Projects');
  });

  describe('create a project', function(){

    it('go to create project page', function(){
      var createProjectbtn = browser.driver.findElement(by.css(project.createProjectbtnCss));
      createProjectbtn.click();
      var title = browser.driver.findElement(by.css(project.createProjecth1Css))
      expect(title.getText()).toEqual('Create Project');
    });

    it('validate client label', function(){
      var label = browser.driver.findElement(by.css(project.cpClientLabelCss));
      expect(label.getText()).toEqual('Client: *');
    });

    it('select a client', function(){
      browser.driver.findElements(by.css(project.cpClientsOptionsCss)).then(function(options){
        options[68].click();
      });
      var selectop = browser.driver.findElement(by.css(project.cpClientsCss));
      expect(selectop.getAttribute('value')).toEqual('2444');
    });

    it('validate selected option', function(){
      browser.driver.executeScript('return $("#clientId :selected").text()').then(function(text){
        expect(text).toEqual('Marketing Demo');
      });
    });

    it('validat project name field label', function(){
      var label = browser.driver.findElement(by.css(project.cpProjectNameLabelCss));
      expect(label.getText()).toEqual('Project Name: *');
    });
    
    it('add project name', function(){
      var field = browser.driver.findElement(by.css(project.cpProjectNameFieldCss));
      field.sendKeys('Test Project');
      expect(field.getAttribute('value')).toEqual('Test Project');
    });

    it('validate workflow label', function(){
      var label = browser.driver.findElement(by.css(project.cpWorkflowCodeLabelCss));
      expect(label.getText()).toEqual('Workflow Code:');
    });

    it('select workflow option', function(){
      browser.driver.findElements(by.css(project.cpWorkflowCodeListOptionCss)).then(function(options){
        options[12].click();
      });
      var selectop = browser.driver.findElement(by.css(project.cpWorkflowCodeListCss));
      expect(selectop.getAttribute('value')).toEqual('Demo');
    });

    it('validate project code field label', function(){
      var label = browser.driver.findElement(by.css(project.cpProjectcodeLabelCss));
      expect(label.getText()).toEqual('Project Code: *');
    });

    it('add project code', function(){
      var field = browser.driver.findElement(by.css(project.cpProjectcodefieldCss));
      field.sendKeys('TP01');
      expect(field.getAttribute('value')).toEqual('TP01');
    });

    it('validate project type label', function(){
      var label = browser.driver.findElement(by.css(project.cpprojectTypeLabelCss));
      expect(label.getText()).toEqual('Project Type: *');
    });

    it('validate project type options', function(){
      browser.driver.findElements(by.css(project.cpProjecttypesListOptionCss)).then(function(options){
        expect(options[0].getText()).toEqual('Standard');
        expect(options[1].getText()).toEqual('Template');
        expect(options[2].getText()).toEqual('Demo');
      });
    });

    it('select as project Type Demo', function(){
        browser.driver.findElements(by.css(project.cpProjecttypesListOptionCss)).then(function(options){
        options[2].click();
      });
      var selectop = browser.driver.findElement(by.css(project.cpProjectTypeListCss));
      expect(selectop.getAttribute('value')).toEqual('Demo');
    });

    it('validate completion date label', function(){
      var label = browser.driver.findElement(by.css(project.cpCompletionDateLabelCss));
      expect(label.getText()).toEqual('Completion Date: *');
    });

    xit('validate default completion date', function(){
  // which is the logic of this field?
     var selectop = browser.driver.findElement(by.css(project.cpcompletionDatefieldCss));
      expect(selectop.getAttribute('value')).toEqual('08/24/2014');
    });

    it('add Completion Date ', function(){
      var compleTionDate = browser.driver.findElement(by.css(project.cpcompletionDatefieldCss));
      compleTionDate.clear();
      compleTionDate.sendKeys('12/30/2014');
      expect(compleTionDate.getAttribute('value')).toEqual('12/30/2014');
    });

    xit('add Completion Date using DayPicker', function(){

    });

    it('validate comment label', function(){
      var label = browser.driver.findElement(by.css(project.cpCommentlabelCss));
      expect(label.getText()).toEqual('Comment:');
    });

    xit('add a comment', function(){

    });

    it('save created project', function(){
      var savebtn = browser.driver.findElement(by.css(project.saveBtnCss));
      savebtn.click();
    });

    it('show project - get url', function(){
      var url = browser.driver.getCurrentUrl();
      expect(url).toEqual(process.env.BASE_URL+'/tdstm/project/show');
    });

    it('validate the message', function(){
      browser.driver.executeScript('return $(".message").text()').then(function(text){
        expect(text).toEqual('Project TP01 : Test Project created');
      });
    });

  });//create a project
  
  describe('delete a project', function(){

  });

  
}); //listProjects