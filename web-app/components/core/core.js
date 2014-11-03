/************************
 * MODULE: Core
 ************************/

/**
 * Create namespaces
 */
var tds = {};

tds.core = tds.core || {};
tds.core.interceptor = tds.core.interceptor || {};
tds.core.service = tds.core.service || {};
tds.core.directive = tds.core.directive || {};
tds.utils = {}

/**
 * Functions used to manage arrays
 */
tds.utils.arrayUtils = function() {

	/*
	 * to remove duplicate elemment in a array .
	 */
	function removeDuplicateElement(arrayName) {
		var newArray = new Array();
		label: for (var i = 0; i < arrayName.length; i++) {
			for (var j = 0; j < newArray.length; j++) {
				if (newArray[j].split("_")[1] == arrayName[i].split("_")[1])
					continue label;
			}
			newArray[newArray.length] = arrayName[i];
		}
		return newArray;
	}

	function chkDuplicates(list1, list2) { // finds any duplicate array elements using the fewest possible comparison
		var i, j, n;
		var concArray = list1.concat(list2);
		n = concArray.length;
		// to ensure the fewest possible comparisons
		for (i = 0; i < n; i++) { // outer loop uses each item i at 0 through n
			for (j = i + 1; j < n; j++) { // inner loop only compares items j at i+1 to n
				if (concArray[i].split("_")[1] == concArray[j].split("_")[1]) return true;
			}
		}
		return false;
	}

	return {
		removeDuplicateElement: removeDuplicateElement,
		chkDuplicates: chkDuplicates
	};

}();


/**
 * Functions used to manage strings
 */
tds.utils.stringUtils = function() {

	// Truncate the text 
	function truncate(text) {
		var trunc = text
		if (text) {
			if (text.length > 30) {
				trunc = trunc.substring(0, 30);
				trunc += '...'
			}
		}
		return trunc;
	}

	function htmlToPlaintext(text) {
		return String(text).replace(/<[^>]+>/gm, '');
	}

	function empty(text) {
		return ((text == null) || (text.trim().length == 0));
	}

	return {
		truncate: truncate,
		htmlToPlaintext: htmlToPlaintext,
		empty: empty
	};

}();

/**
 * Utilities functions
 */
tds.core.utils = function(servRootPath, dateFormat, dateTimeFormat) {

	/**
	 * Functions used to manage dates
	 */
	var dateUtils = function() {

		function formatDueDate(input) {
			var currentDate = ""
			if (input) {
				var datePart = input.match(/\d+/g),
					year = datePart[0].substring(0), // get only two digits
					month = datePart[1],
					day = datePart[2];
				currentDate = month + '/' + day + '/' + year;
			}
			return currentDate
		}

		function formatDate(dateObj) {
			return dateObj.format(dateFormat);
		}

		function formatDateTime(dateObj) {
			return dateObj.format(dateTimeFormat);
		}

		function createDateFromString(dateValue) {
			return moment(dateValue, dateFormat);
		}

		function createDateTimeFromString(dateValue) {
			return moment(dateValue, dateTimeFormat);
		}

		function isValidDate(dateValue) {
			return moment(dateValue, dateFormat, true).isValid();
		}

		function isValidDateTime(dateValue) {
			return moment(dateValue, dateTimeFormat, true).isValid();
		}

		return {
			formatDueDate: formatDueDate,
			formatDate: formatDate,
			formatDateTime: formatDateTime,
			createDateFromString: createDateFromString,
			createDateTimeFromString: createDateTimeFromString,
			isValidDate: isValidDate,
			isValidDateTime: isValidDateTime
		};

	}();

	/**
	 * Functions used to manage urls
	 */
	var urlUtils = function() {

		function applyRootPath(urlPath) {
			return servRootPath + urlPath;
		}

		return {
			applyRootPath: applyRootPath
		};

	}();

	return {
		array: tds.utils.arrayUtils,
		string: tds.utils.stringUtils,
		date: dateUtils,
		url: urlUtils
	}

};

/**
 * Intercepector used to check if the user is logged in
 */
tds.core.interceptor.LoggedCheckerInterceptor = function() {
	var servicesInterceptor = {
		response: function(response) {
			var loginRedirect = response.headers('x-login-url');
			if (!loginRedirect) {
				try {
					var json = angular.fromJson(response.data);
					if (json.errors && json.errors.length > 0) {
						var errorDiv = angular.element(document.querySelector('#errorModalText'));
						var errorsHTML = "<ul>";
						for (var j = 0; j < json.errors.length; j++) {
							errorsHTML = errorsHTML + "<li>" + json.errors[j] + "</li>";
						}
						errorsHTML = errorsHTML + "</ul>";
						errorDiv.html(errorsHTML);
						$('#errorModal').modal('show');
					} else {
						return response;
					}
				} catch (e) {
					return response;
				}
			} else {
				alert("Your session has expired and need to login again.");
				//location.reload();
				window.location.href = loginRedirect;
			}
		}
	};
	return servicesInterceptor;
};

// Helper function used with the following ajaxPrefilter to redirect the user to the login page
function _goToLogin(url) {
	setTimeout(function() {
		location.href = url;
	}, 500);
}

/**
 * An Ajax Prefilter that is be applied to all Jquery Ajax calls. It will add the following functionality:
 *    1. Add checks for errors in the request and display them in the #messageDiv or as an alert
 *    2. Check for session expiration and redirect the user to the login page accordingly
 */
$.ajaxPrefilter(function(options, originalOptions, jqXHR) {
	//console.log("In the ajaxPrefilter");
	var success = options.success;
    options.success = function(data, textStatus, xhr) {
    	var url = xhr.getResponseHeader('X-Login-URL');
        if (url) {
        	// The session must of expired if the X-Login-URL header was received
        	alert("Your session has expired and need to login again.");
        	_goToLogin(url);
            return;
        } else {
        	// Check for error situations
        	if (xhr.status == 200) {
	        	//console.log("ajaxPrefilter was successful");
        		if (success) {
            		return success(data, textStatus, jqXHR);
        		}
        	} else {
				var errmsg = "Unexpected error occurred";
				var msgDiv = $('#messageDiv');
				if (msgDiv.length) {
					msgDiv.html(errmsg)
				} else {
					alert(errmsg);
				}
				console.log("ajaxPrefilter received an error - " + errmsg);
				return false;
        	}
        }
	};

    /*
    var error = options.error;
    options.error = function(jqXHR, textStatus, errorThrown) {
    	$('#messageDiv').html("An unexpected error occurred and update was unsuccessful.");
        if(typeof(error) === "function") return error(jqXHR, textStatus, errorThrown);
    };
    */
});

/**
 * Interceptor used to check if there are a penging request, it works in conjuntions with the LoadingIndicator directive
 */
tds.core.interceptor.PendingRequestInterceptor = function(q, rootScope) {
	var requestCount = 0;
	return {
		request: function (config) {
			requestCount++;
			rootScope.$broadcast("newServiceRequest");
			return config || q.when(config)
		},
		response: function (response) {
			if ((--requestCount) === 0) {
				rootScope.$broadcast("noPendingRequests");
			}
			return response || q.when(response);
		},
		responseError: function (response) {
			if (!(--requestCount)) {
				rootScope.$broadcast("noPendingRequests");
			}
			return q.reject(response);
		}
	};
};

/**
 * Alerts popup service
 */
tds.core.service.AlertsService = function(rootScope, timeout) {

	var stdErrorMsg = 'An unexpected error occurred. Please close and reload form to see if the problem persists'

	rootScope.alerts = {};

	rootScope.alerts.list = [];

	var showGenericMsg = function() {
		addAlertMsg(stdErrorMsg);
	};

	var addAlertMsg = function(message) {
		addAlert({
			type: 'danger',
			msg: message
		});
	};

	var addAlert = function(obj) {
		if (obj.closeIn) {
			removeAlertAfter(obj.closeIn);
		}

		rootScope.alerts.list.push({
			type: obj.type,
			msg: obj.msg,
			hidden: false
		});
	};

	var closeAlert = function(index) {
		index = (index) ? index : rootScope.alerts.list.length - 1;
		rootScope.alerts.list[index].hidden = true;
		timeout(function() {
			rootScope.alerts.list.splice(index, 1);
		}, 500);
	};

	var removeAlertAfter = function(time) {
		time = (time) ? time : 1000;
		timeout(function() {
			rootScope.alerts.closeAlert();
		}, time);
	};

	rootScope.alerts.closeAlert = closeAlert;

	return {
		showGenericMsg: showGenericMsg,
		addAlertMsg: addAlertMsg,
		addAlert: addAlert,
		closeAlert: closeAlert,
		removeAlertAfter: removeAlertAfter
	};

};

// Singleton used to access alerts service from non-angular code
tds.Alerts = function() {

	var addAlertMsg = function(message) {
		var injector = angular.element('[ng-app]').injector();
		injector.invoke(function($rootScope, alerts) {
			alerts.addAlertMsg(message);
			$rootScope.$apply();
		});
	}

	var addAlert = function(obj) {
		var injector = angular.element('[ng-app]').injector();
		injector.invoke(function($rootScope, alerts) {
			alerts.addAlert(obj);
			$rootScope.$apply();
		});
	}

	return {
		addAlertMsg: addAlertMsg,
		addAlert: addAlert
	}

}();


/**
 * Application Common Data
 */
tds.core.service.commonDataService = function() {

	var servers = [];
	var applications = [];
	var dbs = [];
	var storages = [];
	var others = [];
	var roles = [];
	var categories = [];
	var durationScales = [];
	var loginPerson = {};

	var loadServers = function(value) {
		servers = value
	};
	var loadApplications = function(value) {
		applications = value
	};
	var loadDbs = function(value) {
		dbs = value
	};
	var loadStorages = function(value) {
		storages = value
	};
	var loadOthers = function(value) {
		others = value
	};
	var loadRoles = function(value) {
		roles = value
	};
	var loadCategories = function(value) {
		categories = value
	};
	var loadDurationScales = function(values) {
		durationScales = [];
		for (var i=0; i<values.length;i++) {
			if (values[i].id != '') {
				durationScales.push(values[i]);
			}
		}
	};
	var loadLoginPerson = function(value) {
		loginPerson = value;
	}

	var getAssetTypeList = function(assetType) {
		var result = [];
		switch (assetType) {
			case "Application":
				result = applications;
				break;
			case "Database":
				result = dbs;
				break;
			case "Storage":
				result = storages;
				break;
			case "Server":
				result = servers;
				break;
			case "Other":
				result = others;
				break;
		}
		return result;
	};

	var setAssetTypeList = function(assetType, values) {
		switch (assetType) {
			case "Application":
				applications = values;
				break;
			case "Database":
				dbs = values;
				break;
			case "Storage":
				storages = values;
				break;
			case "Server":
				servers = values;
				break;
			case "Other":
				others = values;
				break;
		}
	};

	var getRoles = function() {
		return roles;
	};
	var getCategories = function() {
		return categories;
	};
	var getDurationScales = function() {
		return durationScales;
	};
	var getLoginPerson = function() {
		return loginPerson;
	}

	return {
		getRoles: getRoles,

		loadServers: loadServers,
		loadApplications: loadApplications,
		loadDbs: loadDbs,
		loadStorages: loadStorages,
		loadOthers: loadOthers,
		loadRoles: loadRoles,
		loadCategories: loadCategories,
		loadDurationScales: loadDurationScales,
		loadLoginPerson: loadLoginPerson,

		getAssetTypeList: getAssetTypeList,
		setAssetTypeList: setAssetTypeList,
		getCategories: getCategories,
		getDurationScales: getDurationScales,
		getLoginPerson: getLoginPerson
	};

}();

/**
 * Extends angular to improves blur
 */
angular.module('modNgBlur', [])
	.directive('ngBlur', function() {
		return function(scope, elem, attrs) {
			elem.bind('blur', function() {
				scope.$apply(attrs.ngBlur);
			});
		};
	});

/**
 * updated bar preferences
 * @param timeoutPeriod
 */
function timedUpdate(timeoutPeriod) {
	taskManagerTimePref = timeoutPeriod
	if (B1 != '') {
		if (taskManagerTimePref != 0) {
			B1.Start(timeoutPeriod);
		} else {
			B1.Pause(0);
		}
	} else {
		if (taskManagerTimePref != 0) {
			B2.Start(timeoutPeriod);
		} else {
			B2.Pause(0);
		}
	}
}

/**
 * Service used to administrate windows timed updates
 */
tds.core.service.TimedUpdateService = function(window) {

	var pause = function() {
		if(window.B2 != '') {
			window.B2.Pause(0);
		}
		if(window.B1 != '') {
			window.B1.Pause(0);
		}
	}

	var resume = function() {
		var taskManagerTimePref = window.taskManagerTimePref;
		if (window.B2 != '' && taskManagerTimePref != 0) { 
			B2.Restart(taskManagerTimePref);
		}
		if (window.B1 != '' && taskManagerTimePref != 0){ 
			B1.Restart(taskManagerTimePref); 
		}
	}

	return {
		pause: pause,
		resume: resume
	};
}

/*****************************************
 * Directive used to make a component draggable
 */
tds.core.directive.Draggable = function() {
	return {
		restrict: 'A',
		link: function(scope, element, attrs) {
			var params = {
				revert: false,
				containment: element.parent().parent().parent()
			};
			var titleBar = element.find('.ui-dialog-titlebar');
			if (titleBar.length > 0) {
				params["handle"] = titleBar;
			}
			element.draggable(params);
		}
	};
};

/*****************************************
 * Directive used to center a component
 */
tds.core.directive.Centered = function(window) {
	return {
		restrict: 'A',
		link: function(scope, element, attrs) {
			var width = element.width();
			var topOffset = window.pageYOffset + 100;
			element.css('left', '50%');
			element.css('top', topOffset + 'px');
			element.css('margin-left', '-' + width / 2 + 'px');
			element.css('position', 'absolute');
		}
	};
};

/*****************************************
 * Directive used show an indicator when there is a pending request
 */
tds.core.directive.LoadingIndicator = function(timeout, utils) {
	return {
		restrict: 'EA',
		templateUrl: utils.url.applyRootPath('/components/core/loading-indicator.html'),
		scope: {
			align: '@align',
			enabled: '=enabled',
			visible: '=visible'
		},
		link: function(scope, element, attrs) {
			scope.animClass = "loading-indicator-anim-center";
			switch (scope.align) {
				case "left":
					scope.animClass = "loading-indicator-anim-left";
					break;
			}
			scope.isLoading = false;
			if (!angular.isUndefined(scope.visible)) {
				scope.isLoading = scope.visible;
				scope.$watch('visible', function(nValue, oValue) {
					scope.isLoading = nValue;
				});
			} else {
				scope.$on("newServiceRequest", function () {
					if (angular.isUndefined(scope.enabled) || scope.enabled) {
						scope.isLoading = true;
					}
				});
				scope.$on("noPendingRequests", function () {
					timeout(hideElement, 500);
				});
			}
			var hideElement = function() {
				scope.isLoading = false;
			}
		}
	};
};

/*****************************************
 * Directive datepicker
 */
tds.core.directive.DatePicker = function(utils, dateFormat) {
	return {
		restrict: 'A',
		require: 'ngModel',
		scope: {
			ngModel: '=ngModel'
		},
		link: function(scope, element, attrs, ngModelCtrl) {
			jQuery(
				function($) {
					element.daterangepicker({ 
						timePicker: false, 
						format: dateFormat,
						singleDatePicker: true,
						showDropdowns: false,
						opens: 'left',
						startDate: new Date()
					});
					element.on('apply.daterangepicker', function(ev, picker) {
						updateModel();
					});
					element.on('change', function() {
						updateModel();
					});
				}
			);
			var isValid = function(value) {
				return ((value == "") || utils.date.isValidDate(value));
			}
			var updateModel = function() {
				var valid = isValid(element.val());
				if (valid) {
					ngModelCtrl.$setViewValue(element.val());
				}
				ngModelCtrl.$setValidity('date', valid);
				scope.$apply();
			};
			scope.$watch('ngModel', function(nVal, oVal) {
				if (nVal && (nVal != oVal)) {
					var valid = isValid(nVal);
					if (valid) {
						element.data('daterangepicker').setStartDate(nVal);
						element.data('daterangepicker').updateInputText();
					}
				}
			});
		}
	}
};

/*****************************************
 * Directive tdsrangepicker
 */
tds.core.directive.RangePicker = function(utils, dateTimeFormat) {
	return {
		restrict: 'A',
		require: 'ngModel',
		scope: {
			dateBegin: '=dateBegin',
			dateEnd: '=dateEnd',
			ngModel: '=ngModel'
		},
		link: function(scope, element, attrs, ngModelCtrl) {
			var delim = " - ";

			var updateModel = function() {
				var valid = true;
				var range = element.val();
				ngModelCtrl.$setViewValue(range);
				scope.$apply();

				if (range.length == 0) {
					scope.dateBegin = '',
					scope.dateEnd = '';
				} else {
					var tempArr = range.split(delim)
					var dateBegin = null;
					var dateEnd = null;

					if (tempArr.length > 1) {
						dateBegin = tempArr[0];
						dateEnd = tempArr[1];

					    valid = valid && utils.date.isValidDateTime(dateBegin);
					    valid = valid && utils.date.isValidDateTime(dateEnd);

					    if (valid) {
							scope.dateBegin = dateBegin,
							scope.dateEnd = dateEnd;
					    }
					} else {
						valid = false;
					}
				}

				ngModelCtrl.$setValidity('dateRange', valid);

				if (valid) {
					scope.$apply();
					var expression = attrs['ngChange'];
					if (expression) {
						scope.$parent.$eval(expression);
					}
				}
			}

			var updateView = function() {
				element.data('daterangepicker').setStartDate(scope.dateBegin);
				element.data('daterangepicker').setEndDate(scope.dateEnd);
				element.data('daterangepicker').updateInputText();
			}

			scope.$watch('dateBegin', function(nVal, oVal) {
				if (nVal && (nVal != oVal)) {
					updateView();
				}
			});

			scope.$watch('dateEnd', function(nVal, oVal) {
				if (nVal && (nVal != oVal)) {
					updateView();
				}
			});

			jQuery(
				function($) {
					element.daterangepicker({ 
						timePicker: true, 
						timePickerIncrement: 1, 
						format: dateTimeFormat,
						showDropdowns: false,
						startDate: moment().hours(0).minutes(0).seconds(0),
						endDate: moment().add('d', 1).hours(0).minutes(0).seconds(0)
					});
					element.on('apply.daterangepicker', function(ev, picker) {
						updateModel();
					});
					element.on('change', function() {
						updateModel();
					});
				}
			);

		}
	}
};

/*****************************************
 * Directive tdsdurationpicker
 */
tds.core.directive.DurationPicker = function(utils) {
	return {
		restrict: 'E',
		require: 'ngModel',
		templateUrl: utils.url.applyRootPath('/components/core/duration-template.html'),
		scope: {
			duration: '=duration',
			scale: '=scale',
			scales: '=scales',
			ngModel: '=ngModel'
		},
		link: function(scope, element, attrs, ngModelCtrl) {

			var updateModel = function() {
				var duration = parseInt(scope.duration, 10);
				if (duration) {
					var offset = moment(0).add(scope.scale.toLowerCase(), duration);
					ngModelCtrl.$setViewValue(offset.valueOf());
				}
			}

			var updateView = function() {
				//check with current scale, if not go with a lower one
				var offset = moment(0).add(scope.scale.toLowerCase(), 1);
				var tentativeDuration = parseInt((scope.ngModel / offset.valueOf()), 10)
				if ((tentativeDuration * offset.valueOf()) ==  scope.ngModel) {
					scope.duration = tentativeDuration
				} else {
					for (var i=(scope.scales.length-1); i >= 0; i--) {
						var offset = moment(0).add(scope.scales[i].id.toLowerCase(), 1);
						if ((scope.ngModel >= offset.valueOf()) &&
							((scope.ngModel % offset.valueOf()) == 0)) {
							scope.duration = parseInt((scope.ngModel / offset.valueOf()), 10)
							scope.scale = scope.scales[i].id;
							break;
						}
					}
				}
			}

			scope.$watch('duration', function(nVal, oVal) {
				if (nVal && (nVal != oVal)) {
					updateModel();
				}
			});

			scope.$watch('scale', function(nVal, oVal) {
				if (nVal && (nVal != oVal)) {
					updateModel();
				}
			});

			scope.$watch('ngModel', function(nVal, oVal) {
				if (nVal && (nVal != oVal)) {
					updateView();
				}
			});

		}
	}
};

/*****************************************
 * Directive tdsActionButton
 */
tds.core.directive.ActionButton = function(utils, window) {
	return {
		restrict: 'E',
		templateUrl: utils.url.applyRootPath('/components/core/action-button-template.html'),
		scope: {
			id: '@id',
			label: '@label',
			icon: '@icon',
			link: '@link',
			click: '@click',
		},
		link: function(scope, element, attrs, ngModelCtrl) {
			scope.name = scope.label.toLowerCase().replace(/ /g, '').replace(/\./g,'')
			scope.applyRootPath = function(path) {
				if (path == '') {
					return '#';
				} else {
					return utils.url.applyRootPath(path);
				}
			}
			scope.doAction = function(event) {
				if (scope.click) {
					window[scope.click](event);
				}
			}
		}
	}
};

/*****************************************
 * Core module configuration
 */
tds.core.module = angular.module('tdsCore', ['ngGrid', 'ngResource', 'ui.bootstrap']);

tds.core.module.value('servRootPath', '/tdstm');
tds.core.module.value('dateFormat', 'MM/DD/YYYY');
tds.core.module.value('dateTimeFormat', 'MM/DD/YYYY h:mm A');
tds.core.module.value('appCommonData', tds.core.service.commonDataService);

tds.core.module.factory('utils', ['servRootPath', 'dateFormat', 'dateTimeFormat', tds.core.utils]);

tds.core.module.factory('alerts', ['$rootScope', '$timeout', tds.core.service.AlertsService]);
tds.core.module.factory('windowTimedUpdate', ['$window', tds.core.service.TimedUpdateService]);

tds.core.module.factory('loggedCheckerInterceptor', [tds.core.interceptor.LoggedCheckerInterceptor]);
tds.core.module.factory('pendingRequestInterceptor', ['$q', '$rootScope', tds.core.interceptor.PendingRequestInterceptor]);

tds.core.module.config(['$httpProvider',
	function($httpProvider) {
		$httpProvider.interceptors.push('loggedCheckerInterceptor');
		$httpProvider.interceptors.push('pendingRequestInterceptor');
	}
]);

tds.core.module.directive('draggable', [tds.core.directive.Draggable]);
tds.core.module.directive('centered', ['$window', tds.core.directive.Centered]);
tds.core.module.directive('loadingIndicator', ['$timeout', 'utils', tds.core.directive.LoadingIndicator]);
tds.core.module.directive('tdsdatepicker', ['utils', 'dateFormat', tds.core.directive.DatePicker]);
tds.core.module.directive('tdsrangepicker', ['utils', 'dateTimeFormat', tds.core.directive.RangePicker]);
tds.core.module.directive('tdsdurationpicker', ['utils', tds.core.directive.DurationPicker]);
tds.core.module.directive('tdsactionbutton', ['utils', '$window', tds.core.directive.ActionButton]);