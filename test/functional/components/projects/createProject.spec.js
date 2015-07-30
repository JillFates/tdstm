'use strict';
var  Menu = require('../menu/menu.po.js');
var ListProjects = require('./listProjects.po.js');
var Project = require('./project.po.js');
describe('Create Project', function(){
  var menu = new Menu();
  var listProjectPage =  new ListProjects();
  var projectPage = new Project();
  var projId;

  describe('Create Project', function(){
    
    it('should load list projects page after select Project > List Projects', function(){
      menu.goToProjects('listProjects');
      expect(menu.waitForURL('/tdstm/project/list?active=active')).toBe(true);
    });

    it('should have "Project List - Active Projects" as title',function  () {

      expect(listProjectPage.getTitle().getText()).toEqual('Project List - Active Projects');
    });
    
    describe('cleanup',function(){
      var exist;
      
      it('should find the project to delete', function(){
        listProjectPage.setSearchProjectCode('TP01');
        expect(listProjectPage.getSearchProjectCode().getAttribute('value')).toEqual('TP01');
      });
      
      it('should verify if TP01 project is found', function(){
        var results= 2;
        listProjectPage.verifySearchResults(results).then(function(list){
          if(list.length ===2){
            list[1].getAttribute('id').then(function(text){
              projId = text;
            });
            exist = true;
          }else{
            exist = false;
          }
        });
      });
      
      it('should delete the project if exists', function(){
        if(exist){
          listProjectPage.selectProjectfromListByPos(1,'TP01');
          expect(menu.waitForURL('/tdstm/project/show/'+projId)).toBe(true);
          projectPage.deleteCurrentProject();
          browser.driver.sleep(1000);
          var alertDialog = browser.driver.switchTo().alert();
          var message= 'Warning: This will delete the Test Project project and all of the assets, events, bundles, and any historic data?';
          expect(alertDialog.getText()).toEqual(message);
          alertDialog.accept();
          browser.driver.wait(function() {
            return menu.getCurrentUrl().then(function(url){
              return url === process.env.BASE_URL+'/tdstm/project/list';
            });
          },8000).then(function(){
            expect(menu.getCurrentUrl()).toEqual(process.env.BASE_URL+'/tdstm/project/list');
          });
        }
      });
    
    });//cleanup

    it('should load create project page after hitting create project button',function(){
      listProjectPage.clickOnCreateProjectBtn();
      expect(menu.waitForURL('/tdstm/project/create')).toBe(true);

    });

    it('should have "Create Project" as title', function() {
      expect(projectPage.getTitle().getText()).toEqual('Create Project');

    });

    it('should have project/create url', function(){
      expect(menu.getCurrentUrl())
        .toEqual(process.env.BASE_URL+'/tdstm/project/create');
    });

    it('should select id 2444 as client', function(){
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

    it('should have "Workflow: *" as label for  workflow', function(){
      expect(projectPage.getWorkflowCodeLabel()
        .getText()).toEqual('Workflow: *');
    });

    it('Should select "STD_PROCESS" as workflow', function(){
      projectPage.selectWorkflowCodeByName('STD_PROCESS').click();
      expect(projectPage.getWorkflowSelected()
          .getAttribute('value')).toEqual('STD_PROCESS');
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

    xit('should set Completion Date ', function(){
      projectPage.setCompletionDate('12/30/2014');
      expect(projectPage.getCompletionDate().getAttribute('value')).toEqual('12/30/2014');
    });
// can be added a validation to check that default completion date is > today
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
      expect(menu.waitForURL('/tdstm/project/show')).toBe(true);
    }); 

    it('should be displayed created project confirmation message', function(){
      expect(projectPage.getConfirmMsg()).toEqual('Project TP01 : Test Project created');
    });

    it('should updated Page title with the created project name', function(){
      expect(menu.getHeaderTitle().getText()).toEqual(' TransitionManager™ - Test Project');
    });
  }); // Create Project
}); // Create Project