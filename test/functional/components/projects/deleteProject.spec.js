'use strict';
var  Menu = require('../menu/menu.po.js');
var ListProjects = require('./listProjects.po.js');
var Project = require('./project.po.js');
describe('Delete a project', function(){
  var menu = new Menu();
  var listProjectPage =  new ListProjects();
  var projectPage = new Project();
  var projId;

  it('should load list projects page after select Client/Project ListProjects', function(){
    menu.goToProjects('listProjects');
    expect(listProjectPage.getTitle().getText()).toEqual('Project List - Active Projects');
  });

  it('should find the project to delete', function(){
    listProjectPage.setSearchProjectCode('TP01');
    expect(listProjectPage.getSearchProjectCode().getAttribute('value')).toEqual('TP01');
  });

  it('should only be found TP01 project', function(){
    var results= 1;
    listProjectPage.verifySearchResults(results).then(function(list){
      expect(list.length).toEqual(results);
      list[0].getAttribute('id').then(function(text){
        // expect(list[0].getText()).toEqual(details);
        projId = text;
      });
    });
  });

  it('should select the project found', function(){
    listProjectPage.selectProjectfromListByPos(1,'TP01');
  });

  it('should be redirect to project/show/project+id', function(){
    expect(menu.getCurrentUrl()).toEqual(process.env.BASE_URL+'/tdstm/project/show/'+projId);
  });

  it('should delete the project after clicking on delete button', function(){
    projectPage.deleteCurrentProject();
    var alertDialog = browser.driver.switchTo().alert();
    var message= 'Warning: This will delete the Test Project project and all of the assets, events, bundles, and any historic data?';
    expect(alertDialog.getText()).toEqual(message);
    alertDialog.accept();
  });

  it('should be redirect to project list', function(){
    browser.driver.wait(function() {
      return menu.getCurrentUrl().then(function(url){
        return url === process.env.BASE_URL+'/tdstm/project/list';
      });
    }).then(function(){
      expect(menu.getCurrentUrl()).toEqual(process.env.BASE_URL+'/tdstm/project/list');
    });
  });

  it('should be displayed delete confirmation message', function(){
    expect(projectPage.getConfirmMsg()).toMatch(/Project TP01 : Test Project deleted/);
  });

  it('should find the project to delete', function(){
    listProjectPage.setSearchProjectCode('TP01');
    expect(listProjectPage.getSearchProjectCode().getAttribute('value')).toEqual('TP01');
  });

  it('should not return the deleted project', function(){
    var results= 0;
    listProjectPage.verifySearchResults(results).then(function(text){
      expect(text).toEqual('No results found');
    });
  });

  // it('project not found', function(){
  //   project.verifySearchResults(false);
  // });
}); // Delete a project