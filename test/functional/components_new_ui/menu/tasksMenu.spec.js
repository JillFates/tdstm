'use strict';
var  Menu = require('./menu.po.js');
describe('Tasks Menu', function(){

  var menu = new Menu();

  it('should have \'Tasks\' as the parent label', function() {
    expect(menu.tasks.getParent().getText()).toEqual('Tasks');
  });

	it('should expand the Tasks Menu',function(){
		menu.tasks.toggle();
		expect(menu.tasks.isExpanded()).toBe(true);
	});

  describe('Child Items', function() {

		it('should have the text \'My Tasks ([Optional Number])\' as its link text', function() {
			expect(menu.tasks.getMyTasks().findElement(by.tagName('a')).getText())
				.toMatch(/My Tasks \([\d\s]+\)/);
		});

		it('should have the target URL \'/tdstm/task/listUserTasks\'', function() {
			expect(menu.tasks.getMyTasks().findElement(by.tagName('a')).getAttribute('href'))
				.toContain('/tdstm/task/listUserTasks');
		});

		it('should have the text \'Task Manager\' as its link text', function() {
			expect(menu.tasks.getTaskManager().findElement(by.tagName('a')).getText())
				.toMatch(/Task Manager/);
		});

		it('should have the target URL \'/tdstm/assetEntity/listTasks?initSession=true\'', function() {
			expect(menu.tasks.getTaskManager().findElement(by.tagName('a')).getAttribute('href'))
				.toContain('/tdstm/assetEntity/listTasks?initSession=true');
		});

		it('should have the text \'Task Graph\' as its link text', function() {
			expect(menu.tasks.getTaskGraph().findElement(by.tagName('a')).getText())
				.toMatch(/Task Graph/);
		});

		it('should have the target URL \'/tdstm/task/taskGraph?initSession=true\'', function() {
			expect(menu.tasks.getTaskGraph().findElement(by.tagName('a')).getAttribute('href'))
				.toContain('/tdstm/task/taskGraph?initSession=true');
		});

		it('should have the text \'Task Timeline\' as its link text', function() {
			expect(menu.tasks.getTaskTimeline().findElement(by.tagName('a')).getText())
				.toMatch(/Task Timeline/);
		});

		it('should have the target URL \'/tdstm/task/taskTimeline\'', function() {
			expect(menu.tasks.getTaskTimeline().findElement(by.tagName('a')).getAttribute('href'))
				.toContain('/tdstm/task/taskTimeline');
		});

		it('should have the text \'Cookbook\' as its link text', function() {
			expect(menu.tasks.getCookbook().findElement(by.tagName('a')).getText())
				.toMatch(/Cookbook/);
		});

		it('should have the target URL \'/tdstm/cookbook/index\'', function() {
			expect(menu.tasks.getCookbook().findElement(by.tagName('a')).getAttribute('href'))
				.toContain('/tdstm/cookbook/index');
		});

		it('should have the text \'Generation History\' as its link text', function() {
			expect(menu.tasks.getGenerationHistory().findElement(by.tagName('a')).getText())
				.toMatch(/Generation History/);
		});

		it('should have the target URL \'/tdstm/cookbook/index#/generationHistory\'', function() {
			expect(menu.tasks.getGenerationHistory().findElement(by.tagName('a')).getAttribute('href'))
				.toContain('/tdstm/cookbook/index#/generationHistory');
		});

  }); // end Child Items

	it('should close the Tasks Menu',function(){
		menu.tasks.toggle();
		expect(menu.tasks.isExpanded()).toBe(false);
	});

}); // end Tasks Menu
