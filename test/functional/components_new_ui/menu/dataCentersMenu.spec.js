'use strict';
var  Menu = require('./menu.po.js');
describe('Data Centers Menu', function(){

  var menu = new Menu();

  it('should have \'Data Centers\' as the parent label', function() {
    expect(menu.dataCenters.getParent().getText()).toEqual('Data Centers');
  });

	it('should expand the Data Centers Menu',function(){
		menu.dataCenters.toggle();
		expect(menu.dataCenters.isExpanded()).toBe(true);
	});

  describe('Child Items', function() {

		it('should have the text \'List Rooms\' as its link text', function() {
			expect(menu.dataCenters.getListRooms().findElement(by.tagName('a')).getText())
				.toMatch(/List Rooms/);
		});

		it('should have the target URL \'/tdstm/room/index\'', function() {
			expect(menu.dataCenters.getListRooms().findElement(by.tagName('a')).getAttribute('href'))
				.toContain('/tdstm/room/index');
		});

		it('should have the text \'Rack Elevations\' as its link text', function() {
			expect(menu.dataCenters.getRackElevations().findElement(by.tagName('a')).getText())
				.toMatch(/Rack Elevations/);
		});

		it('should have the target URL \'/tdstm/rackLayouts/create\'', function() {
			expect(menu.dataCenters.getRackElevations().findElement(by.tagName('a')).getAttribute('href'))
				.toContain('/tdstm/rackLayouts/create');
		});

  }); // end Child Items

	it('should close the Data Centers Menu',function(){
		menu.dataCenters.toggle();
		expect(menu.dataCenters.isExpanded()).toBe(false);
	});

}); // end Data Centers Menu
