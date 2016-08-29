'use strict';
var UserMenu = require('./userMenu.po.js');
//var Menu = require('../menu/menu.po.js');

describe('Sign Out Test', function(){

  var userMenu = new UserMenu();

	it('should sign out from any page using the dropdown user menu', function() {
		userMenu.toggle(); // open
		userMenu.clickSignOut();
		expect(browser.driver.getTitle()).toBe('Login');
	});

}); // end Sign Out Test
