'use strict';
/*
*Automated using marketing demo project
*/
var Menu = require('../menu/menu.po.js');
var EventDashboard = require('./eventDashboard.po.js');
var News = require('./news.po.js');
describe('Event Dashboard', function(){
  var news1 ={
    'type':'News',
    'message': 'This is news 1 message',
    'messageEdit': 'updated',
    'resolve-archived': false,
    'resolve-archEdit':true,
    'resolution': 'This is not resolved yet',
    'resolutionEdit': 'resolved'
  };
  
  it('should go to All Devices List page after select Assets > All Devices', function(){
    var menu = new Menu();
    menu.goToDashboards('eventDashboard');
    expect(menu.getCurrentUrl()).toEqual(process.env.BASE_URL+'/tdstm/dashboard/index');
  });
  
  describe('Event Dropdown', function(){
  
    it('should have "Event" as label',function(){
      var eventDashboardPage =  new EventDashboard();
      expect(eventDashboardPage.getEventLabel().getText()).toEqual('Event:');
    });
  
    it('should have "" as default',function(){
      var eventDashboardPage =  new EventDashboard();
      expect(eventDashboardPage.getEventSelected()).toContain('Buildout');
    });
  
    it('should have 4 options listed',function(){
      var eventDashboardPage =  new EventDashboard();
        expect(eventDashboardPage.getEventOptionsLength()).toEqual(4);
    });
  
    it('should have the following options listed',function(){
      var eventDashboardPage =  new EventDashboard();
      eventDashboardPage.getEventOptions().then(function(list){
        expect(list[0].getText()).toContain('Buildout');
        expect(list[1].getText()).toContain('M1');
        expect(list[2].getText()).toContain('M2');
        expect(list[3].getText()).toContain('M3');
      });
    });
  
  });//Event Dropdown
  
  describe('News',function(){

    describe('add',function(){
  
      it('should open create News modal', function(){
        var eventDashboardPage =  new EventDashboard();
        eventDashboardPage.getAddNewsBtn().click();
        var newsModal = new News();
        expect(newsModal.isCreateOpened()).toEqual(true);
      });
  
      it('should have "Create News" as title', function(){
        var newsModal = new News();
        expect(newsModal.getCreateTitle().getText()).toEqual('Create News');
      });
  
      describe('fotter buttons', function(){
  
        var newsModal = new News();
        var lista;
  
        it('should have 2 buttons', function(){
          newsModal.getCreateButtons().then(function(list){
            lista = list;
            expect(list.length).toEqual(2);
          });
        });
  
        it('should have Create button', function(){
          expect(lista[0].getAttribute('value')).toEqual('Create');
        });
  
        it('should have Cancel button',function(){
          expect(lista[1].getAttribute('value')).toEqual('Cancel');
        });
  
      }); // Footter buttons

      describe('Type', function(){
  
        var newsModal = new News();
  
        it('should have "Type" as label',function(){
          expect(newsModal.getTypeLabel().getText()).toEqual('Type:');
        });
  
        it('should have News as type and be disabled',function(){
          expect(newsModal.getTypeSelected()).toEqual('News');
        });
  
      });//type

      describe('Message',function(){
        var newsModal = new News();
      
        it('should have "Message" as label', function(){
          expect(newsModal.getMessageLabel().getText()).toEqual('Message:  *');
        });
      
        it('should be empty by default', function(){
          expect(newsModal.getMessageField().getAttribute('value')).toEqual('');
        });
      
        it('should be required', function(){
          newsModal.getCreateBtn().click();
          if(process.env.BROWSER_NAME !== 'phantomjs'){
            var alertDialog = browser.driver.switchTo().alert();
            expect(alertDialog.getText()).toEqual('Please enter Comment');
            alertDialog.accept();        
          }
          expect(newsModal.isCreateOpened()).toBe(true);       
        });
      
        it('should add a message',function(){
          var message = newsModal.getMessageField();
          message.sendKeys(news1['message']);
          expect(message.getAttribute('value')).toEqual(news1['message']);
        });

      }); //Message

      describe('resolved/archived',function(){
        
        var newsModal = new News();
        
        it('should have "Resolved/Archived" as label', function(){
          expect(newsModal.getResolvedArchivedLabel().getText()).toEqual('Resolved / Archived:');  
        });
        
        it('should be unchecked by default',function(){
          expect(newsModal.getResolveArchivedCheck().getAttribute('value')).toEqual('0');
        });

      }); //resolve/archived
      
      describe('Resolution',function(){
      
        var newsModal = new News();
      
        it('should have "Resolution" as label',function(){
          expect(newsModal.getResolutionLabel().getText()).toEqual('Resolution:');  
        });
      
        it('should be empty by defult',function(){
          expect(newsModal.getResolutionField().getAttribute('value')).toEqual('');
        });

        it('should add a resolution',function(){
          var resolution = newsModal.getResolutionField();
          resolution.sendKeys(news1['resolution']);
          expect(resolution.getAttribute('value')).toEqual(news1['resolution']);
        });

      });//Resolution
      
      it('should save the news',function(){
        var newsModal = new News();
        newsModal.getCreateBtn().click();
        expect(newsModal.isCreateClosed()).toEqual(true);  
      });
    }); // add
    describe('edit',function(){
      it('should click on an existing news', function(){

      });
    }); // edit
    describe('delete',function(){

    });//delete
  });//news
  describe('Task Summary section',function(){
    it('should have "Task Summary" as title', function(){
      var eventDashboardPage =  new EventDashboard();
      expect(eventDashboardPage.getTaskSummaryTitle().getText()).toEqual('Task Summary');
    });
  }); // Task Summary
}); // Event Dashboard
