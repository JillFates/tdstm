//Text as link on url field and custom fields
/*
# Go to application list page
# create an application
edit an application from the icon and save it -> view app modal shoul d be displayed with the changes updated
click on app name - view app modal is displayed
bulk delete only on e application
bulk delete all selecting from the grid using a filter
# add task
add commentt 
  
*/
'use strict';
var  Menu = require('../../menu/menu.po.js');
var  ListApps = require('../listApps.po.js');
var CreateAppModal = require('./appCreateModal.po.js');
var ViewAppModal = require('./appViewModal.po.js');
describe('createApp', function(){
  var appName= 'App Test 1';
  var appId;

  it('should load Application List page after select Assets List Apps', function(){
    var menu = new Menu();
    menu.goToAssets('applications');
    expect(menu.getCurrentUrl()).toEqual(process.env.BASE_URL+'/tdstm/application/list');
  });

  it('should have as title Application List', function(){
    var appsListPage =  new ListApps();
    expect(appsListPage.titleh.getText()).toEqual('Application List');
  });

  it('should open Create app modal after click on create app button', function(){
    var appsListPage =  new ListApps();
    appsListPage.createAppBtn.click();
    var appModal = new CreateAppModal();
    expect(appModal.isCreateModalOpened()).toBe(true);
  });
    
  it('should validate Create Application modal title', function(){
    var appModal = new CreateAppModal();
    expect(appModal.createModalTitle.getText()).toEqual('Application Detail');
  });

  describe('Create Application Modal', function(){

    var appModal = new CreateAppModal();

    describe('name field', function(){

      it('Should have Name as label for Application Name', function(){
        expect(appModal.nameLabel.getText()).toEqual('Name*');
      });

      it('should be required', function(){
        if(process.env.BROWSER_NAME === 'phantomjs'){
          appModal.saveBtn.click();
        }else{
          appModal.saveBtn.click();
          var alertDialog = browser.driver.switchTo().alert();
          expect(alertDialog.getText()).toEqual('Please provide a name for the asset');
          alertDialog.accept();
        }
        expect(appModal.isCreateModalOpened()).toBe(true);
      });
      
      it('should add a Name', function(){
        appModal.setName(appName);
        expect(appModal.nameField.getAttribute('value')).toEqual(appName);
      });

    }); // name field

    describe('type field',function(){

      it('should have type as label', function(){
        expect(appModal.typeLabel.getText()).toEqual('Type');
      });

      it('should be application by default', function(){
        expect(appModal.typeField.getAttribute('value')).toEqual('Application');
      });

    }); // type field

    it('should save and view', function(){
      var viewAppModal = new ViewAppModal();
      appModal.saveBtn.click();
      expect(appModal.isCreateModalClosed()).toBe(true);
      expect(viewAppModal.isViewModalOpened()).toBe(true);
    });

  }); // Create Application Modal

  describe('view application modal', function(){
    it('should have as title Application Detail', function(){
      var viewAppModal = new ViewAppModal();
      expect(viewAppModal.viewAppTitle.getText()).toEqual('Application Detail');
    });

    xdescribe('displayed data set on create', function(){
      it('should have name as label', function(){

      });
      it('should have the name', function(){

      });
      it('should have type as label', function(){

      });
      it('should have application as type', function(){

      });

    }); // displayed data set on create

    describe('buttons', function(){
      var viewAppModal = new ViewAppModal();
      it('should have Edit button', function(){
        expect(viewAppModal.editBtn.getAttribute('value')).toEqual('Edit');

      });

      it('should have Delete button', function(){
        expect(viewAppModal.deleteBtn.getAttribute('value')).toEqual('Delete');
      });
      it('should have add task button', function(){
         viewAppModal.addTaskCommentBtnList.then(function(list){
          expect(list[0].getText()).toEqual('  Add Task');
        });
      });

      it('should have add comment button', function(){
         viewAppModal.addTaskCommentBtnList.then(function(list){
          expect(list[1].getText()).toEqual('  Add Comment');
        });
      });
    }); // buttons

    describe('close view modal', function(){
      var viewAppModal = new ViewAppModal();
      it('should close view modal after click on x', function(){
        viewAppModal.closeBtn.click();
        expect(viewAppModal.isViewModalClosed()).toBe(true);
      });
    }); // close view modal
  }); //view application modal 

  xit('should displayed confirmation message', function(){
    var appsListPage =  new ListApps();
    expect(appsListPage.confirmMsg.getText()).toEqual('Application App Test 1 created');
  });
  
  describe('search for app', function(){

    var appsListPage =  new ListApps();

    it('should set name to search an app', function(){
      var field = appsListPage.searchNamefield;
      field.clear();
      field.sendKeys(appName);
      expect(field.getAttribute('value')).toEqual(appName);
    });

    it('should validate search results', function(){
      appsListPage.verifySearchResults(1,'application').then(function(list){
        list[0].getAttribute('id').then(function(pid){
          appId = pid;
        });
        expect(list.length).toEqual(1);
      });
    });
  }); // Search for an app

});// createApp