'use strict';
var  Menu = require('./menu.po.js');
describe('Tasks Menu', function(){
  describe('Admin Role', function(){
    var menu = new Menu();

    it('should have Tasks as label',function(){
      expect(menu.getTasks().getText()).toEqual('Tasks');
    });

    describe('submenu',function(){
      it('should displayed 6 links', function(){
        menu.getTasksSubmenu().then(function(list){
          expect(list.length).toEqual(6);
        });
      });

      it('should open Tasks Menu',function(){
        menu.getTasks().click();
      });
      it('should have My Tasks in the submenu', function(){
        expect(menu.getMyTasks().getText()).toMatch(/My Tasks \((\d*|\s)\)/);
      });
      it('should have Task Manager in the submenu', function(){
        expect(menu.getTaskManager().getText()).toEqual('Task Manager');
      });
      it('should have Task Graph in the submenu', function(){
        expect(menu.getTaskGraph().getText()).toEqual('Task Graph');
      });
      it('should have Task timeline in the submenu', function(){
        expect(menu.getTaskTimeline().getText()).toEqual('Task Timeline');
      });
      it('should have Cookbook in the submenu', function(){
        expect(menu.getCookbook().getText()).toEqual('Cookbook');
      });
      it('should have TasksHelp in the submenu', function(){
        expect(menu.getTasksHelp().getText()).toEqual('help');
      });

      it('should close Tasks Menu',function(){
        menu.getTasks().click();
      });
    });//submenu
  }); // Admin Role
});//Tasks Menu
