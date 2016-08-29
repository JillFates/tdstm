'use strict';
var UserMenu = require('./userMenu.po.js');
var Menu = require('../menu/menu.po.js');

describe('User Menu', function() {
  var userMenu = new UserMenu();

  it('should have 4 list items', function() {
    userMenu.toggle(); // open
    userMenu.getListItems().then(function(list) {
      expect(list.length).toEqual(4);
    });
  });

  describe('Account Details', function() {

		it('should have "Account Details" as a link', function() {
			expect(userMenu.getListItem('accountDetails').isDisplayed()).toBe(true);
		});

    it('should display Account Details modal on click', function() {
      userMenu.getListItem('accountDetails').click(); // closes user menu
      expect(userMenu.isModalOpen('accountDetails')).toEqual(true);
    });

    it('should have "Edit person" as modal title', function() {
      expect(userMenu.getModalTitle('accountDetails')).toEqual('Edit Person');
    });

    it('should close Account Details modal',function () {
      userMenu.closeModal('accountDetails');
      expect(userMenu.isModalOpen('accountDetails')).toEqual(false);
    });

  }); // end Account Details

	describe('Date and Timezone', function() {

		it('should have "Date and Timezone" as a link', function() {
			userMenu.toggle(); // open
			expect(userMenu.getListItem('dateAndTimezone').isDisplayed()).toBe(true);
		});

		it('should display Date and Timezone modal on click', function() {
			userMenu.getListItem('dateAndTimezone').click(); // closes user menu
			expect(userMenu.isModalOpen('dateAndTimezone')).toEqual(true);
		});

		it('should have title containing user\'s name', function() {
			userMenu.getUserName().then(function(un) {
				 expect(userMenu.getModalTitle('dateAndTimezone')).toEqual(un+' Date and Timezone');
			});
		});

		it('should close Date and Timezone modal',function () {
			userMenu.closeModal('dateAndTimezone');
			expect(userMenu.isModalOpen('dateAndTimezone')).toEqual(false);
		});

	}); // end Date and Timezone

  describe('Edit Preferences', function() {

		it('should have "Edit preferences" as a link', function() {
			userMenu.toggle(); // open
			expect(userMenu.getListItem('editPreferences').isDisplayed()).toBe(true);
		});

    it('should display Edit Preferences modal on click', function() {
      userMenu.getListItem('editPreferences').click(); // closes user menu
      expect(userMenu.isModalOpen('editPreferences')).toEqual(true);
    });

    it('should have title containing user\'s name', function() {
			userMenu.getUserName().then(function(un) {
				expect(userMenu.getModalTitle('editPreferences')).toEqual(un+' Preferences');
			});
    });

    it('should close Edit Preferences modal',function () {
      userMenu.closeModal('editPreferences');
      expect(userMenu.isModalOpen('editPreferences')).toEqual(false);
    });

  }); // end Edit Preferences

}); // end User Menu
