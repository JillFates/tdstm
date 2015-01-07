'use strict';
var  Menu = require('../menu/menu.po.js');
var ListProjects = require('./listProjects.po.js');
var Project = require('./project.po.js');
describe('Create Project', function(){
  var menu = new Menu();
  var listProjectPage =  new ListProjects();
  var projectPage = new Project();
  var projId;

  xdescribe('Require field validations', function(){

    it('should load list projects page after select Project > ListProjects', function(){
      menu.goToProjects('listProjects');
      expect(listProjectPage.getTitle().getText()).toEqual('Project List - Active Projects');
    });
      
    it('should attempt to create and submit a project with no workflow code',function(){
    		browser.driver.findElement(by.css('input[class="create"]')).click();
  		browser.driver.findElement(by.id('name')).sendKeys('TEST_PROJECT');		
  		browser.driver.findElement(by.id('projectCode')).sendKeys('TESTY');
  		browser.driver.findElement(by.css('input[class="save"')).click();
  		expect(listProjectPage.getTitle().getText()).toEqual('Create Project');
  		browser.sleep(500);	
    });
    
    it('should attempt to submit a project with no project name',function(){
  		browser.driver.findElement(by.id('name')).clear();	
  		browser.driver.findElement(by.id('workflowCode')).click;
  		browser.driver.findElement(by.css('option[value="STD_PROCESS"')).click();
  		browser.driver.findElement(by.css('input[class="save"')).click();
  		expect(listProjectPage.getTitle().getText()).toEqual('Create Project');
  		browser.sleep(500);
    });
    
    it('should attempt to submit a project with no project code',function(){
  		browser.driver.findElement(by.id('name')).sendKeys('TEST_PROJECT');
  		browser.driver.findElement(by.id('projectCode')).clear();
  		browser.driver.findElement(by.css('input[class="save"')).click();
  		expect(listProjectPage.getTitle().getText()).toEqual('Create Project');
  		browser.sleep(500);
    });
    
    it('should attempt to submit a project with no creation date',function(){
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
  describe('Create Project', function(){
    
    it('should load list projects page after select Client/Project ListProjects', function(){
      menu.goToProjects('listProjects');
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
          expect(menu.getCurrentUrl()).toEqual(process.env.BASE_URL+'/tdstm/project/show/'+projId);
          if(process.env.BROWSER_NAME === 'phantomjs'){
            browser.driver.executeScript('$(\'input[value="Delete"]\').attr("onclick","").click("true")');
            projectPage.deleteCurrentProject();
          }else{
            projectPage.deleteCurrentProject();
            var alertDialog = browser.driver.switchTo().alert();
            var message= 'Warning: This will delete the Test Project project and all of the assets, events, bundles, and any historic data?';
            expect(alertDialog.getText()).toEqual(message);
            alertDialog.accept();
          }
          browser.driver.wait(function() {
            return menu.getCurrentUrl().then(function(url){
              return url === process.env.BASE_URL+'/tdstm/project/list';
            });
          }).then(function(){
            expect(menu.getCurrentUrl()).toEqual(process.env.BASE_URL+'/tdstm/project/list');
          });
        }
      });
    });//cleanup

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
          .getText()).toEqual('Workflow:');
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

    //   xit('validate default completion date', function(){
    // // which is the logic of this field?
    //    var selectop = browser.driver.findElement(by.css(project.cpcompletionDatefieldCss));
    //     expect(selectop.getAttribute('value')).toEqual('08/24/2014');
    //   });

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
      expect(menu.getCurrentUrl())
        .toEqual(process.env.BASE_URL+'/tdstm/project/show');
    }); 

    it('should be displayed created project confirmation message', function(){
      expect(projectPage.getConfirmMsg()).toEqual('Project TP01 : Test Project created');
    });

    it('should updated Page title with the created project name', function(){
      expect(menu.getHeaderTitle().getText()).toEqual(' TransitionManager™ - Test Project');
    });
  }); // Create Project
}); // Create Project