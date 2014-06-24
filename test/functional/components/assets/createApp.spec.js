'use strict';
var  Menu = require('../menu/menu.po.js');
var  ListApps = require('./listApps.po.js');
var CreateAppModal = require('./createApp.po.js');
describe('Create App', function(){
  var menu = new Menu();
  var appsListPage =  new ListApps();
  var appModal = new CreateAppModal();
  var appName= 'App Test 1';

  it('should load Application List page after select Assets List Apps', function(){
    menu.goToAssets('listApps');
    expect(appsListPage.getTitle().getText()).toEqual('Application List');
  });

  it('should open Create app modal after click on create app button', function(){
    appsListPage.clickOnCreateAppBtn();
    expect(appModal.isCreateModalOpened()).toBe(true);
  });
  
  it('should validate Create Application modal title', function(){
    expect(appModal.getTitle().getText()).toEqual('Create Application');
  });

  describe('Create Application Modal' , function(){

    it('Should have Name as label for Application Name', function(){
      expect(appModal.getNameLabel().getText()).toEqual('Name*');
    });

    it('should add a Name', function(){
      var field = appModal.getNameField();
      field.sendKeys(appName);
      expect(field.getAttribute('value')).toEqual(appName);
    });

    it('should save and close', function(){
      appModal.clickSaveCloseBtn();
      expect(appModal.isCreateModalClosed()).toBe(true);
    });

    it('should displayed confirmation message', function(){
      expect(appsListPage.getConfirmMsg().getText()).toEqual('Application App Test 1 created');
    });
  

  }); // Create Application Modal

//   describe('search for app', function(){


//     xit('search by app name', function(){
// //       assets.searchAppByName(appName);
//     });

//     it('results should be correct', function(){
//       assets.verifySearchResultsReturnId(true).then(function(pid){
//         appId = pid;
//       }); 
//     });

//     it('app without task/comments by default should open create task modal', function(){
//       assets.clickOnCommentTaskIcon(true,'Application',appName,appId);
//     });
// // / browser.navigate().refresh();

//     describe('add a task',function(){
// // default info:  task category status
//       describe('Task Text Area', function(){
//         it('validate task label', function(){
//           expect($(assets.commentLabelCss).getText()).toEqual('Task:');
//         });
//         it('task default values is empty', function(){
//           expect($(assets.commentTextAreaCss).getAttribute('value')).toEqual('');

//         });
//         it('task should be marked as required',function(){
//           expect($(assets.commentRequiredCss).getAttribute('class')).toEqual('error-msg');
//         });

//         it('add task text', function(){
//           var textArea = $(assets.commentTextAreaCss);
//           var message= 'This is task 01 //n and I am adding some text';
//           textArea.sendKeys(message);
//           expect(textArea.getAttribute('value')).toEqual(message);
//         });
        
//         it('task should not be marked as required',function(){
//           expect($(assets.commentRequiredCss).getAttribute('class')).toEqual('error-msg ng-hide');
//         }); 

//       }); //Task Text Area

//       describe('person/team', function(){
//         it('verify person team label', function(){
//           expect($(assets.personTeamLabelCss).getText()).toEqual('Person/Team:');
//         });

//         it('verify that person is unassigned by default', function(){
//           browser.executeScript('return $("#assignedToId #assignedTo option:selected").text()').then(function(text){
//             expect(text).toEqual('Unassigned');
//           });

//         });

//         it('person list should be empty', function(){
//           // since no person are assigned to the project yet.
//           $$(assets.personOptionsCss).then(function(options){
//             expect(options.length).toEqual(1);
//             expect(options[0].getText()).toEqual('Unassigned');
//           });
//         });

//         it('verify fixed assigned label',function(){
//           expect($(assets.fixedAssignedLabelCss).getText()).toEqual('Fixed Assignment');
//         });

//         it('Fixed Assignment checkbox should be disabled if person is unassigned', function(){
//           expect($(assets.fixedAssignedCheckCss).getAttribute('disabled')).toEqual('true');
//         });

//         it('Fixed Assignment checkbox should be uncheched if person is unassigned', function(){
//           expect($(assets.fixedAssignedCheckCss).getAttribute('value')).toEqual('0');
//         });
        
//          xit('select a person from the list', function(){

//         });
//         xit('Fixed Assignment checkbox should be enable', function(){

//         });

//         it('verify team is Unassigned by default', function(){
//           expect($(assets.teamOptionsSelectedCss).getText()).toEqual('Unassigned');
//         });
//         xit('verify team options', function(){
//           var options = ['​Unassigned​','Account Manager​','App Coordinator​','Database Admin','Database Admin-MSSQL​',
//                         'Database Admin-Oracle​','Logistics Technician','Move Manager​','Move Technician​','Move Technician',
//                         'Network Admin​','Project Admin​','Project Manager​','Storage Admin​','System Admin​','System Admin-Linux​',
//                         'System Admin-Win​','​Technician​'];
//           // var ops= 
//           $$(assets.teamOptionsCss).then(function(options){
//             expect(options.length).toEqual(18);
//             var i =0;
//             options.forEach(function(op){
//               console.log('i',i);
//               expect(op.getText()).toEqual(option[i]);
//               i++;
//             });
//             // expect(options[0].getText()).toEqual('​Unassigned​');
//             // expect(options[1].getText()).toEqual('Account Manager​');
//             // expect(options[2].getText()).toEqual('App Coordinator​');
//             // expect(options[3].getText()).toEqual('Database Admin');
//             // expect(options[4].getText()).toEqual('Database Admin-MSSQL​');
//             // expect(options[5].getText()).toEqual('Database Admin-Oracle​');
//             // expect(options[6].getText()).toEqual('Logistics Technician');
//             // expect(options[7].getText()).toEqual('Move Manager​');
//             // expect(options[8].getText()).toEqual('Move Technician​');
//             // expect(options[9].getText()).toEqual('Move Technician');
//             // expect(options[10].getText()).toEqual('Network Admin​');
//             // expect(options[11].getText()).toEqual('Project Admin​');
//             // expect(options[12].getText()).toEqual('Project Manager​');
//             // expect(options[13].getText()).toEqual('Storage Admin​');
//             // expect(options[14].getText()).toEqual('System Admin​');
//             // expect(options[15].getText()).toEqual('System Admin-Linux​');
//             // expect(options[16].getText()).toEqual('System Admin-Win​');
//             // expect(options[17].getText()).toEqual('​Technician​');
//           });
//         });

//         it('select a team',function(){
//           $$(assets.teamOptionsCss).then(function(options){`
//             options[12].click();
//           });
//           // expect($(assets.teamOptionsSelectedCss).getText()).toEqual('Project Manager​');
//           // expect(element(by.selectedOption('model_name')).getText()).toEqual('xxxxxx');   
//           // expect(element(by.selectedOption('ac.assignedTo')).getText()).toEqual('xxxxxx');   
       
//           element(by.model('ac.assignedTo')).findElements(by.tagName('option')).then(function(options){
//             console.log('oprtion.length',options.length);
//           });
//         });

//         it('Fixed Assignment checkbox should be disabled if team is assigned', function(){
//           expect($(assets.fixedAssignedCheckCss).getAttribute('disabled')).toEqual('true');
//         });

//         it('Fixed Assignment checkbox should be uncheched if team is assigned', function(){
//           expect($(assets.fixedAssignedCheckCss).getAttribute('value')).toEqual('0');
//         });
//       }); //person/team

//       xit('asset Name should be Application on the first dropdown', function(){

//       });
//       xit('asset Name should be '+appName+' on the 2nd dropdown', function(){

//       });
//       xit('verify team is unassigned by default', function(){

//       });
//       xit('verify teams list', function(){

//       });

//       xit('select a team from the list', function(){

//       });

//       xit('verify default category is General', function(){

//       });
//       xit('change to other category', function(){

//       });
//       xit('duration field should be empty by default', function(){

//       });
//       xit('add duration value', function(){

//       });
//       xit('duration values by default should be selected m', function(){

//       });
//       xit('verify duration list of values', function(){

//       });
//       xit('priority should be 3 by default', function(){

//       });
//       xit('verify priority list of values', function(){

//       });
//       xit('due date should be empty by default', function(){

//       });
//       xit('add a due date',function(){

//       });
//       xit('verify that default status is ready', function(){

//       });
//       xit('change to other status', function(){

//       });

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
//       xit('check estimated start is null by default', function(){

//       });
//       xit('add estimated start',function(){

//       });
//       xit('check estimated finish is null by default', function(){

//       });
//       xit('add estimated finish',function(){

//       });

//     });//add a task

//     describe('add a comment',function(){

//     });// add a comment

  // });//search for app

}); //Applpication List