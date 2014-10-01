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
      xdescribe('Project dropdown',function(){
        it('should have "Project:*" as label',function(){

        });
        xIt('should be required',function(){

        });
        xit('should have existing projects listed',function(){

        });
        it('should have listed by default current project',function(){

        });
      }); // Project dropdown
      xdescribe('Name',function(){
        it('should have "Name:*" as label',function(){

        });
        it('should be empty by default',function(){

        });
        it('should not allow to save without a name',function(){

        });
        it('should add a name',function(){

        });
      });//Name
      xdescribe('Description',function(){
        it('should have "Description:" as label',function(){

        });
        it('should be empty by default',function(){

        });
        it('should add a description',function(){

        });
      });// description
      xdescribe('Bundles',function(){
        it('should have "Bundles:" as label',function(){

        });
        it('should have existing bundles listed as checkbox',function(){

        });
        it('should select a bundle',function(){

        });
      });//Bundles
      xdescribe('Runbook Status',function(){
        it('should have "Runbook Status:" as label',function(){

        });
        it('should have x options',function(){

        });
        it('should have these options',function(){

        });
      }); //Runbook Status
      xdescribe('Runbook bridge1 first', function(){
        it('should have Runbook bridge1 as label',function(){

        });
        it('should be empty by default',function(){

        });
      }); //Runbook bridge1 first
      xdescribe('Video Link',function(){
        it('should have "Video Link" as label',function(){

        });
        it('should be empty by default',function(){

        });
      }); // Video Link
      xdescribe('Status',function(){
        it('should have "Status:" as label',function(){

        });
        it('should have Auto Start selected by default',function(){

        });
        it('should have 3 options listed',function(){

        });
        it('should have these options listed',function(){

        });
      }); // Status
      xdescribe('Estimated Start',function(){
        it('should have "Estimated Start:" as label',function(){

        });
        it('should be empty by default',function(){

        });
        it('should add a date',function(){

        });
        xdescribe('date picker',function(){

        });
      });//Estimated Start
      xdescribe('Buttons',function(){
        it('should have 2 buttons',function(){

        });
        it('should have "Save" as label',function(){

        });
        it('should have "Cancel" as label',function(){

        });
      }); // Buttons
      it('should save created Event',function(){

      });
    });// Create Event Form
  }); // create an event
}); // event list