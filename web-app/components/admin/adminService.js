tds.admin.service.AdminService = function (utils, http, q) {
	
	var unlockUser = function (userId, element, refresh) {
		var deferred = q.defer();
		http.put(utils.url.applyRootPath('/ws/admin/unlockAccount?id=' + userId))
			.success(function (data, status, headers, config) {
				deferred.resolve(data);
				$(element).remove();
				location.reload();
			})
			.error(function (data, status, headers, config) {
				deferred.reject(data);
			});
		return deferred.promise;
	};
	
	return { unlockUser:unlockUser };
};


tds.admin.module.factory('adminService', ['utils', '$http', '$q', tds.admin.service.AdminService]);
tds.admin.controller.MainController.$inject = ['$scope', '$modal', 'utils', 'adminService'];