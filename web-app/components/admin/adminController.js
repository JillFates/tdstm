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
		$("#udUsername").text('Username: ' + username)
		$("#udFailedLogins").text('Failed logins: ' + failedLoginAttempts)
		$("#udLockedOutUntil").text('Account locked out for: ' + lockedOutUntil)
		$("#unlockUserDialog").dialog({
	      buttons : {
	        "Confirm" : function() {
	          adminService.unlockUser(id, element, refresh);
	        },
	        "Cancel" : function() {
	          $(this).dialog("close");
	        }
	      }
	    });

	    $("#unlockUserDialog").dialog("open");
	    $("#unlockUserDialog").parent().find(".ui-dialog-buttonpane").css('width', 'auto')

		/*
		var confirmed = confirm('Username: ' + username + '\n'
			+ 'Failed logins: ' + failedLoginAttempts + '\n'
			+ 'Account locked out for: ' + lockedOutUntil + '\n'
			+ 'You are about to unlock user account, press Okay to continue or press Cancel to abort');
		if (confirmed)
			adminService.unlockUser(id, element, refresh);
		*/
	}
};

tds.admin.module = angular.module('tdsAdmin', ['tdsCore']);
