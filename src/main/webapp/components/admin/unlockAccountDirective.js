tds.admin.directive = tds.admin.directive || {}

tds.admin.directive.tmUnlockAccount = function (service) {
	return function(scope, elem, attrs) {
		$(elem).on('click', function () {
			var data = JSON.parse(attrs.cellvalue);
			scope.admin.showUnlockUserConfirm(data.id, data.username, data.lockedOutTime, data.failedLoginAttempts, elem, attrs.refreshpage);
		});
	};
}

tds.admin.module.directive('tmUnlockAccount', ['adminService', tds.admin.directive.tmUnlockAccount]);