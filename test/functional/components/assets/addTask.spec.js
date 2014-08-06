'use strict';
var  Menu = require('../menu/menu.po.js');
var  ListApps = require('./listApps.po.js');
var  TaskModal = require('./task.po.js');
var CreateAppModal = require('./createApp.po.js');

describe('Add Task', function(){
  var menu = new Menu();
  var taskModal = new TaskModal();
  var appsListPage =  new ListApps();
  var appId;
  var appName= 'App Test 2 Tasks';

  describe('Preconditions', function(){
    
    it('should load Application List page after select Assets List Apps', function(){
      menu.goToAssets('listApps');
      expect(appsListPage.titleh.getText()).toEqual('Application List');
    });

    it('should open Create app modal after click on create app button', function(){
      var appsListPage =  new ListApps();
      appsListPage.createAppBtn.click();
      var appModal = new CreateAppModal();
      expect(appModal.isCreateModalOpened()).toBe(true);
    });

    it('should create an app and save', function(){
      var appModal = new CreateAppModal();
      expect(appModal.createAppTitle.getText()).toEqual('Application Detail');
      appModal.createApp(appName);
      expect(appModal.isCreateModalClosed()).toBe(true);
    });

    it('should displayed view Modal', function(){
      var appModal = new CreateAppModal();
      expect(appModal.isViewModalOpened()).toBe(true);
    });
    
    it('should close view modal', function(){
      var appModal = new CreateAppModal();
      appModal.closeBtn.click();
      expect(appModal.isViewModalClosed()).toBe(true);
    });

  }); //preconditions

  describe('search for app', function(){

    xit('should load Application List page after select Assets List Apps', function(){
      menu.goToAssets('listApps');
      expect(appsListPage.titleh.getText()).toEqual('Application List');
    });

    it('should set name to search an app', function(){
      var field = appsListPage.searchNamefield;
      field.sendKeys(appName);
      expect(field.getAttribute('value')).toEqual(appName);
    });

    it('should validate search results', function(){
      appsListPage.verifySearchResults(1).then(function(list){
        list[0].getAttribute('id').then(function(pid){
          appId = pid;
        });
      });
    });
  }); // Search for an app

  it('should have add task icon', function(){
    expect(appsListPage.isAddTaskIconDisplayed(appId)).toBe(true);
  });
  it('should open create task modal', function(){
    // expect(appsListPage.isTaskIconPresent(appId)).toBe(true);
    appsListPage.clickOnTaskIcon(appId);
    expect(taskModal.editTaskModal.isPresent()).toBe(true);
  });

  it('should have Create task as title', function(){
    expect(taskModal.createTaskTitle.getText()).toEqual('Create Task');
  });
// // default info:  task category status
  describe('Task Text Area', function(){

    it('should have as label Task:', function(){
      expect(taskModal.taskLabel.getText()).toEqual('Task :');
    });

    var field = taskModal.taskTextArea;
    it('should displayed empty by default task textarea', function(){
      expect(field.getAttribute('value')).toEqual('');
    });

    it('should have Task mark as required', function(){
      expect(taskModal.commentLabel.getText()).toEqual('Task : *');
    });

    it('should have task textArea as required',function(){
      expect(field.getAttribute('required')).toBe('true');
    });

    it('should add text to the task', function(){
      var message= 'This is task 01  and I am adding some text';
      field.sendKeys(message);
      expect(field.getAttribute('value')).toEqual(message);
    });
      
    it('should not longer have Task marked as required',function(){
      expect(taskModal.commentLabel.getText()).toEqual('Task : ');
    }); 

  }); //Task Text Area

  xdescribe('person/team', function(){
    // This scenario is comment out until this jira is fixed. TM-2920
    it('should have Person/Team as label', function(){
      expect(taskModal.personTeamLabel.getText()).toEqual('Person/Team:');
    });

    it('should have person unassigned by default', function(){
      expect(taskModal.personSelected.getText()).toEqual('Unassigned');
    });

    it('should have person list empty since no person is assigned to this project', function(){
      taskModal.personOptions.then(function(options){
        expect(options.length).toEqual(1);
        expect(options[0].getText()).toEqual('Unassigned');
      });
    });

    describe('Fixed Assignment checkbox', function(){

      it('should have fixed assignment as label',function(){
        expect(taskModal.fixedAssignedLabel.getText()).toEqual('Fixed Assignment');
      });

      it('should be disabled if person is unassigned', function(){
        expect(taskModal.fixedAssigned.getAttribute('disabled')).toEqual('true');
      });

      xit('should be unchecked if person is unassigned', function(){
        // This test is skip since the application is not returning the value for checked property.
        expect(taskModal.fixedAssigned.getAttribute('checked')).toEqual('false');
      });
      
    }); // Fixed Assignment checkbox
      
//          xit('select a person from the list', function(){

//         });
//         xit('Fixed Assignment checkbox should be enable', function(){

//         });
    describe('Team drowpdown',  function(){

      it('should have team Unassigned by default', function(){
        expect(taskModal.teamSelected.getText()).toEqual('Unassigned');
      });
       
      it('should have 20 options', function(){
        expect(taskModal.teamOptions.count()).toEqual(20);
      });
      
      it('should have the following options' ,function(){
        var values = ['','ACCT_MGR','APP_COORD','DB_ADMIN','DB_ADMIN_MS','DB_ADMIN_ORA','CLEANER',
        'MIG_ANALYST','MIG_LEAD','MOVE_MGR','MOVE_TECH', 'MOVE_TECH_SR', 'NETWORK_ADMIN','PROJ_ADMIN','PROJ_MGR','STOR_ADMIN',
        'SYS_ADMIN', 'SYS_ADMIN_LNX', 'SYS_ADMIN_WIN','TECH'];
        expect(taskModal.teamOptions.count()).toEqual(values.length);
        var i = 0;
        taskModal.teamOptions.each(function(val){
          if(i===0){
            expect(val.getText()).toEqual('Unassigned');
            i++;
          }else{
            expect(val.getAttribute('value')).toEqual(values[i]);
            i++;
          }
        });

      });
      xit('k',function(){
        var options = ['Unassigned','Account Manager​','App Coordinator​','Database Admin','Database Admin-MSSQL​',
        'Database Admin-Oracle​','Logistics Technician','Move Manager​','Move Technician​','Move Technician',
        'Network Admin​','Project Admin​','Project Manager​','Storage Admin​','System Admin​','System Admin-Linux​',
        'System Admin-Win​','​Technician​'];
        var j = 0;
        taskModal.teamOptions.each(function(vi){
          var k = options[j];
          expect(vi.getText()).toEqual(k);
          j++;
        });
      });

    });//Team dropdown

    it('Should be selected Project Manager team',function(){
      taskModal.teamOptions.then(function(options){
      options[12].click();
      });
      // var val = 'NETWORK_ADMIN';
      expect(taskModal.teamSelected.getAttribute('value')).toEqual('NETWORK_ADMIN');
    });

    it('should not enable fixed assignment check if team is assigned', function(){
      expect(taskModal.fixedAssigned.getAttribute('disabled')).toEqual('true');
    });

    xit('should not checked fixed assinment checkbox if team is assigned', function(){
      expect(taskModal.fixedAssigned.getAttribute('value')).toEqual('0');
    });

  }); //person/team

  describe('Event Dropdown', function(){
    it('should have event as Label', function(){
      expect(taskModal.eventLabel.getText()).toEqual('Event:');
    });

    it('should be unassigned by default', function(){
      expect(taskModal.eventSelected.getText()).toEqual('please select');
    });

    xit('should be empty since no event is created for this project yet',function(){
      expect(taskModal.eventOptions.count()).toEqual(1); 
    });
  }); // Event Dropdown

  describe('Category dropdown',function(){

    it('should have Category as label', function(){
      expect(taskModal.categoryLabel.getText()).toEqual('Category:');
    });

    it('should be general by default', function(){
      expect(taskModal.categorySelected.getText()).toEqual('general');
    });

    it('should have this options', function(){
      taskModal.categoryOptions.then(function(options){
        expect(options[0].getText()).toEqual('please select');
        expect(options[1].getText()).toEqual('general');
        expect(options[2].getText()).toEqual('discovery');
        expect(options[3].getText()).toEqual('verify');
        expect(options[4].getText()).toEqual('analysis');
        expect(options[5].getText()).toEqual('planning');
        expect(options[6].getText()).toEqual('design');
        expect(options[7].getText()).toEqual('buildout');
        expect(options[8].getText()).toEqual('walkthru');
        expect(options[9].getText()).toEqual('premove');
        expect(options[10].getText()).toEqual('moveday');
        expect(options[11].getText()).toEqual('shutdown');
        expect(options[12].getText()).toEqual('physical');
        expect(options[13].getText()).toEqual('transport');
        expect(options[14].getText()).toEqual('startup');
        expect(options[15].getText()).toEqual('postmove');
      });
    }); // category dropdown
      
    xdescribe('workflow dropdown', function(){

      it('should be displayed if moveday is selected', function(){
        expect(true).toBe(false);
      });

      it('should be displayed if shutdown is selected', function(){
        expect(true).toBe(false);
      });

      it('should be displayed if startup is selected', function(){
        expect(true).toBe(false);
      });

      it('should not be displayed if ..',function(){
        taskModal.categoryOptions.then(function(options){
          expect(options[5].getText()).toEqual('planning');
        });
      });
    }); // workflow dropdown

    it('should be selcted to planning', function(){
      taskModal.categoryOptions.then(function(options){
        options[5].click();
      });
      expect(taskModal.categorySelected.getText()).toEqual('planning');
    });

  }); // Category Dropdown
    
  describe('Assets dropdowns', function(){
    it('should have Assets as label', function(){
      expect(taskModal.assetsLabel.getText()).toEqual('Asset:');
    });

    describe('Asset Type dropdown', function(){
      it('should have Application selected by default', function(){
        expect(taskModal.assetTypeSelected.getText()).toEqual('Application');
      });

      it('should have this options', function(){
        taskModal.assetTypeOptions.then(function(options){
          expect(options.length).toEqual(5);
          expect(options[0].getText()).toEqual('Application');
          expect(options[1].getText()).toEqual('Server');
          expect(options[2].getText()).toEqual('Database');
          expect(options[3].getText()).toEqual('Storage');
          expect(options[4].getText()).toEqual('Other');
        });
      });

    });//Application dropdown
      
    describe('Asset Entity dropdown', function(){
      it('should have the current application selected',function(){
        expect(taskModal.assetEntitySelected.getText()).toEqual(appName);
      });

      xit('should have the existing applications listed',function(){
        taskModal.assetEntityOptions.then(function(options){
          expect(options.length).toEqual(2);
          expect(options[0].getText()).toEqual('Please select');
          expect(options[1].getText()).toEqual(appName);
        });
      });

      xdescribe('application Entity filter by application type', function(){

        xit('should displayed only applications', function(){

        });
        xit('should displayed only', function(){

        });
        
        xit('should displayed only', function(){

        });
        
        xit('should displayed only', function(){

        });
        
        xit('should displayed only', function(){

        });

      }); //application Entity filter by application type

    });//Asset dropdown
  }); //Assets dropdowns
    
  describe('Duration', function(){

    it('should have Duration as label', function(){
      expect(taskModal.durationLabel.getText()).toEqual('Duration:');
    });

    it('should have the field empty by default', function(){
      expect(taskModal.durationField.getAttribute('value')).toEqual('');
    });

    it('should add 3 as duration field',function(){
      var field = taskModal.durationField;
      field.sendKeys(3);
      expect(field.getAttribute('value')).toEqual('3');
    });

    it('should have M selected by default',function(){
      expect(taskModal.durationScaleSelected.getText()).toEqual('M');
    });

    it('should have scale with M,H,D,W options', function(){
      taskModal.durationScaleOptions.then(function(options){
        expect(options.length).toEqual(4);
        expect(options[0].getText()).toEqual('M');
        expect(options[1].getText()).toEqual('H');
        expect(options[2].getText()).toEqual('D');
        expect(options[3].getText()).toEqual('W');
      });
    });
  }); // Duration

  describe('priority',function(){
    it('should have Priority: as label',function(){
      expect(taskModal.priorityLabel.getText()).toEqual('Priority:');
    });

    it('should have this options', function(){
      var options = ['1','2','3','4','5'];
      var i = 0;
      taskModal.priorityOptions.each(function(op){
        expect(op.getText()).toEqual(options[i]);
        i++;
      });
    });

    it('should be 3 by default', function(){
      expect(taskModal.prioritySelected.getText()).toEqual('3');
    });

  }); //priority
    
  describe('Due Date',function(){
    it('should be label as Due Date:', function(){
      expect(taskModal.dueDateLabel.getText()).toEqual('Due Date:');
    });

    it('should be empty by default', function(){
      expect(taskModal.dueDate.getAttribute('value')).toEqual('');
    });

    it('should be added a due date value',function(){
      var field = taskModal.dueDate;
      field.sendKeys('09/17/2014');
      expect(field.getAttribute('value')).toEqual('09/17/2014');
    });

    xit('should add due date using datepicker',function(){
    });

  }); // Due Date

  describe('Estimated started/finished',function(){

    it('should be label as Estimated started/finished',function(){
      expect(taskModal.estimatedStFinLabel.getText()).toEqual('Estimated Start/Finish:');
    });

    it('should be empty by default', function(){
      expect(taskModal.estimatedStFinField.getAttribute('value')).toEqual('');
    });
    //Missing test cases to validate the daypicker and how this behavies with the 
    //duration field.
  }); //Estimated started/Finished

  describe('Status',function(){

    it('should have Status as label',function(){
      expect(taskModal.statusLabel.getText()).toEqual('Status:');
    });

    it('should be Ready by default',function(){
      expect(taskModal.statusSelected.getText()).toEqual('Ready');
    });

    it('should have these values',function(){
      var options = ['Please Select','Planned','Pending','Ready','Started','Completed','Hold'];
      var i = 0;
      taskModal.statusOptions.each(function(status){
        expect(status.getText()).toEqual(options[i]);
        i++;
      });

    });

  }); // status

//       describe('add predecessor',function(){

//         xit('click on add predecessor', function(){
//           //dropdowns are displayed
//         });
//         xit('check the list of categories', function(){

//         });
//         xit('select category', function(){

//         });
//         xit('check list of task', function(){
//           //if there are no task created, this will be empty.

//         });
//         xit('select task', function(){

//         });
//         xit('click on add  predecessor', function(){

//         });
//         xit('select category 2', function(){

//         });
//         xit('select task2',function(){
//           //if there are no task created, this will be empty.

//         });
//         xit('remove first predecessor added', function(){

//         });
//       }); // predecessor
    
//       describe('add successor',function(){
    
//         xit('click on add successor', function(){
//             //dropdowns are displayed
//         });
//         xit('check the list of categories', function(){

//         });
//         xit('select category', function(){

//         });
//         xit('check list of task', function(){
//           //if there are no task created, this will be empty.

//         });
//         xit('select task', function(){

//         });
//         xit('click on add  successor', function(){

//         });
//         xit('select category 2', function(){

//         });
//         xit('select task2',function(){
//           //if there are no task created, this will be empty.

//         });
//         xit('remove 2nd successor added', function(){

//         });
      
//       }); //successor
  it('should save created task',function(){ 
    taskModal.saveTaskBtn.click();
    expect(taskModal.editTaskModal.isPresent()).toBe(false);
  });

}); //Add Task