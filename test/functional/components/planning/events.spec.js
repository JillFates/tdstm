'use strict';
var  Menu = require('../menu/menu.po.js');
var Events = require('./events.po.js');
describe('Event List',function(){
  var menu = new Menu();
  it('should go to Event List Page after select Planning > List Events',function(){
    menu.goToPlanning('listBundles');
    expect(menu.getCurrentUrl()).toEqual(process.env.BASE_URL+'/tdstm/moveEvent/list');
  });
  it('should have Event List as title',function(){
    var eventsPage = new Events();
    expect(eventsPage.getTitle().getText()).toEqual('Event List');
  });
  xit('list should be empty',function(){

  });
  describe('create an event',function(){
    var eventsPage = new Events();
    it('should have a "Create Event" Button',function(){
      expect(eventsPage.getCreateEventBtn().getAttribute('value')).toEqual('Create Event');
      expect(eventsPage.getCreateEventBtn().getAttribute('type')).toEqual('button');
    });
    it('should go to Create Event Page after click on create event button',function(){
      eventsPage.getCreateEventBtn.click();
      expect(menu.getCurrentUrl()).toEqual(process.env.BASE_URL+'/tdstm/moveEvent/create');
    });
    it('should have "Create Event" as title',function(){
        expect(eventsPage.getTitle().getText()).toEqual('Create Event');
    });
    describe('Create Event Form',function(){
      describe('Project dropdown',function(){
        xit('should have "Project:*" as label',function(){

        });
        xit('should be required',function(){

        });
        xit('should have existing projects listed',function(){

        });
        xit('should have listed by default current project',function(){

        });
      }); // Project dropdown
      describe('Name',function(){
        xit('should have "Name:*" as label',function(){

        });
        xit('should be empty by default',function(){

        });
        xit('should not allow to save without a name',function(){

        });
        xit('should add a name',function(){

        });
      });//Name
      describe('Description',function(){
        xit('should have "Description:" as label',function(){

        });
        xit('should be empty by default',function(){

        });
        xit('should add a description',function(){

        });
      });// description
      describe('Bundles',function(){
        xit('should have "Bundles:" as label',function(){

        });
        xit('should have existing bundles listed as checkbox',function(){

        });
        xit('should select a bundle',function(){

        });
      });//Bundles
      describe('Runbook Status',function(){
        xit('should have "Runbook Status:" as label',function(){

        });
        xit('should have x options',function(){

        });
        xit('should have these options',function(){

        });
      }); //Runbook Status
      describe('Runbook bridge1 first', function(){
        xit('should have Runbook bridge1 as label',function(){

        });
        xit('should be empty by default',function(){

        });
      }); //Runbook bridge1 first
      describe('Video Link',function(){
        xit('should have "Video Link" as label',function(){

        });
        xit('should be empty by default',function(){

        });
      }); // Video Link
      describe('Status',function(){
        xit('should have "Status:" as label',function(){

        });
        xit('should have Auto Start selected by default',function(){

        });
        xit('should have 3 options listed',function(){

        });
        xit('should have these options listed',function(){

        });
      }); // Status
      describe('Estimated Start',function(){
        xit('should have "Estimated Start:" as label',function(){

        });
        xit('should be empty by default',function(){

        });
        xit('should add a date',function(){

        });
        describe('date picker',function(){

        });
      });//Estimated Start
      describe('Buttons',function(){
        xit('should have 2 buttons',function(){

        });
        xit('should have "Save" as label',function(){

        });
        xit('should have "Cancel" as label',function(){

        });
      }); // Buttons
      it('should save created Event',function(){

      });
    });// Create Event Form
  }); // create an event
}); // event list