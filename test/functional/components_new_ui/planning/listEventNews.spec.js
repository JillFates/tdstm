'use strict';
var Menu = require('../menu/menu.po.js');
var ListEventNews = require('./listEventNews.po.js');
// var News = require('../dashboards/news.po.js');

describe('List Event News', function() {

  var menu = new Menu();
  var listEventNews = new ListEventNews();
  // var newsModal = new News();

  it('should go to List Event News page', function() {
    menu.goToPlanning('listEventNews');
    expect(menu.getCurrentUrl('/tdstm/newsEditor/newsEditorList')).toEqual(process.env.BASE_URL+'/tdstm/newsEditor/newsEditorList');
  });

  xit('should have "Event News" as title',function () {

  });

  describe('Event Dropdown', function() {

  }); // Event Dropdown

  describe('Bundle Dropdown', function() {

  }); // Bundle Dropdown

  describe('View dropdown', function() {

  }); // View Dropdown

  describe('Event News', function() {

    it('should have "Create News" Button', function() {
      expect(listEventNews.getCreateNewsButton().getAttribute('value')).toEqual('Create News');

    });

    describe('Create News Modal', function() {

      it('should display Create News Modal after click on the button ', function() {
        listEventNews.getCreateNewsButton().click();
        expect(listEventNews.isCreateOpened()).toEqual(true);

      });

      it('should have "Create News" as title', function() {
        expect(listEventNews.getModalTitle().getText()).toEqual('Create News');
      });

      it('should close Crete News Modal after click on cancel button', function() {
        listEventNews.getModalCancelBtn().click();
        expect(listEventNews.isCreateModalClosed()).toEqual(true);

      });

      xit('should display Create News Modal after click on Create News button ', function() {
        //Currently the modal is displayed empty - you need to refresh the page and click again on the button to see the complete modal
      });

    }); // Create News Modal

  }); // Event News Table

}); // List Event News