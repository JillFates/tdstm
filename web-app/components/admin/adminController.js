/************************
 * MODULE: Admin
 ************************/

/**
 * Create namespaces
 */
tds.admin = tds.admin || {};
tds.admin.controller = tds.admin.controller || {};
tds.admin.service = tds.admin.service || {};

tds.admin.controller.MainController = function (scope, modal, utils, adminService) {
	this.showUnlockUserConfirm = function (id, username, lockedOutUntil, failedLoginAttempts, element, refresh) {
		var confirmed = confirm('Username: ' + username + '\n'
			+ 'Failed logins: ' + failedLoginAttempts + '\n'
			+ 'Account locked out for: ' + lockedOutUntil + '\n'
			+ 'You are about to unlock user account, press Okay to continue or press Cancel to abort');
			
		if (confirmed)
			adminService.unlockUser(id, element, refresh);
	}
};

tds.admin.module = angular.module('tdsAdmin', ['tdsCore']);
