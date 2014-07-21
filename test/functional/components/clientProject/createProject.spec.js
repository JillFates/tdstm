'use strict';
var  Menu = require('../menu/menu.po.js');
var ListProjects = require('./listProjects.po.js');
var Project = require('./project.po.js');
describe('Create Project', function(){
  var menu = new Menu();
  var listProjectPage =  new ListProjects();
  var projectPage = new Project();
  it('should load list projects page after select Client/Project ListProjects', function(){
    menu.goToClientProject('listProjects');
    expect(listProjectPage.getTitle().getText()).toEqual('Project List - Active Projects');
  });
 
  it('should load create project page after hitting create project button',function(){
    listProjectPage.clickOnCreateProjectBtn();
    expect(projectPage.getTitle().getText()).toEqual('Create Project');
  });

  it('should have project/create url', function(){
    expect(menu.getCurrentUrl())
      .toEqual(process.env.BASE_URL+'/tdstm/project/create');
  });

  it('should have "Client: *" as label for client', function(){
    expect(projectPage.getClientFieldLabel().getText()).toEqual('Client: *');
  });

  it('should select id 2444 as client', function(){
    // need to update this to search by marketing demo, instead of selecting by position
    // to avoid issues if the database change.
    projectPage.selectAClient('2444');
    expect(projectPage.getclientSelect().getAttribute('value')).toEqual('2444');
  });

  it('should be selected Marketing Demo as Client', function(){
    expect(projectPage.getClientSelected()).toEqual('Marketing Demo');
  });

  it('should have "Project Name: *" as label for Project Name', function(){
    expect(projectPage.getNameFieldLabel().getText()).toEqual('Project Name: *');
  });

  it('should set Project Name', function(){
    projectPage.setProjectName('Test Project');
    expect(projectPage.getProjectName()
      .getAttribute('value')).toEqual('Test Project');
  });

  it('should have "Workflow Code: " as label for  workflow', function(){
      expect(projectPage.getWorkflowCodeLabel()
        .getText()).toEqual('Workflow Code:');
  });

  it('Should select "Demo" as workflow', function(){
      projectPage.selectWorkflowCodeByName('Demo').click();
      expect(projectPage.getWorkflowSelected()
        .getAttribute('value')).toEqual('Demo');
  });

  it('should have "Project Code: *" as label for project code', function(){
    expect(projectPage.getProjectCodeLabel()
      .getText()).toEqual('Project Code: *');
  });

  it('should set project code', function(){
    projectPage.setProjectCode('TP01');
    expect(projectPage.getProjectCode()
      .getAttribute('value')).toEqual('TP01');
  });

  it('should have "Project Type: *" as label for project', function(){
    expect(projectPage.getProjectTypeLabel()
      .getText()).toEqual('Project Type: *');
  });

  it('should have valid options as project type', function(){
    projectPage.getProjectTypeOption().then(function(options){
      expect(options.length).toEqual(3);
      expect(options[0].getText()).toEqual('Standard');
      expect(options[1].getText()).toEqual('Template');
      expect(options[2].getText()).toEqual('Demo');
    });
  });

  it('should have selected "Demo" as Project Type', function(){
      projectPage.selectProjectType(2);
      expect(projectPage.getProjectCodeSelected()
        .getAttribute('value')).toEqual('Demo');
  });

  it('should have "Completion Date: *" as label for completion date', function(){
    expect(projectPage.getComplationDateLabel()
      .getText()).toEqual('Completion Date: *');
  });

  //   xit('validate default completion date', function(){
  // // which is the logic of this field?
  //    var selectop = browser.driver.findElement(by.css(project.cpcompletionDatefieldCss));
  //     expect(selectop.getAttribute('value')).toEqual('08/24/2014');
  //   });

  it('should set Completion Date ', function(){
    projectPage.setCompletionDate('12/30/2014');
    expect(projectPage.getCompletionDate().getAttribute('value')).toEqual('12/30/2014');
  });

  xit('should select a Completion Date using DayPicker', function(){

  });

  it('should have "Comment:" as label for Comment', function(){
    expect(projectPage.getCommentLabel().getText()).toEqual('Comment:');
  });

  xit('should add a comment', function(){
    var comment = 'This is a commente added to this project.';
    projectPage.setComment(comment);
    expect(projectPage.getComment().getText()).toEqual(comment);
  });

  it('should save created project and go to project/show',function(){
    projectPage.save();
    expect(menu.getCurrentUrl())
      .toEqual(process.env.BASE_URL+'/tdstm/project/show');
  }); 


  it('should be displayed created project confirmation message', function(){
    expect(projectPage.getConfirmMsg()).toEqual('Project TP01 : Test Project created');
  });

  it('should updated Page title with the created project name', function(){
    expect(menu.getHeaderTitle().getText()).toEqual(' TransitionManager™ - Test Project');
  });
//xdescribe('validate info from the created project', function(){

//});






}); // Create Project