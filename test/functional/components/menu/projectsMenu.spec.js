'use strict';
var  Menu = require('./menu.po.js');
describe('Projects Menu', function(){
  describe('Admin Role', function(){
    var menu = new Menu();

    it('should have Projects as label',function(){
      expect(menu.getProjects().getText()).toEqual('Projects');
    });
  
    describe('submenu',function(){
      it('should displayed 5 links', function(){
        menu.getProjectsSubmenu().then(function(list){
          expect(list.length).toEqual(5);
        });
      });

      it('should open Projects Menu',function(){
        menu.getProjects().click();
      });

      it('should have List Projects in the submenu', function(){
        expect(menu.getListProjects().getText()).toEqual('List Projects');
      });

      it('should have Current Project in the submenu', function(){
        expect(menu.getProjectDetails().getText()).toEqual('MarketingDemo Details');
      });

      it('should have Project Staff in the submenu', function(){
        expect(menu.getProjectStaff().getText()).toEqual('Project Staff');
      });

      it('should have Field Settings in the submenu', function(){
        expect(menu.getFieldSettings().getText()).toEqual('Field Settings');
      });
      it('should have help in the submenu', function(){
        expect(menu.getProjectsHelp().getText()).toEqual('help');
      });

      it('should close Projects Menu',function(){
        menu.getProjects().click();
      });
    });//submenu
  }); // Admin Role
});//Projects Menu