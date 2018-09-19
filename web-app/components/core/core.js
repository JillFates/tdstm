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
tds.ui = {}

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
tds.core.utils = function(servRootPath) {

	/**
	 * Functions used to manage dates
	 */
	var dateUtils = function() {

		function formatDueDate(input) {
			var currentDate = ""
			if (input) {
				var momentObj = tdsCommon.parseDateTimeFromZulu(input);
				momentObj.tz(tdsCommon.timeZone());
				currentDate = tdsCommon.formatDateTime(momentObj, tdsCommon.defaultShortDateFormat());
			}
			return currentDate
		}

		function defaultDateFormat() {
			return tdsCommon.defaultDateFormat();
		}

		function kendoDateFormat() {
            return tdsCommon.kendoDateFormat();
		}

		function kendoDateTimeFormat() {
            return tdsCommon.kendoDateTimeFormat();
        }

		function formatDate(dateObj) {
			return tdsCommon.formatDateTime(dateObj, tdsCommon.defaultDateFormat());
		}

		function formatDateTime(dateObj) {
			return tdsCommon.formatDateTime(dateObj, tdsCommon.defaultDateTimeFormat());
		}

		function createDateFromString(dateValue) {
			return tdsCommon.parseDateTimeString(dateValue, tdsCommon.defaultDateFormat());
		}

		function createDateTimeFromString(dateValue) {
			return tdsCommon.parseDateTimeString(dateValue, tdsCommon.defaultDateTimeFormat());
		}

		function isValidDate(dateValue) {
			return moment(dateValue, tdsCommon.defaultDateFormat(), true).isValid();
		}

		function isValidDateTime(dateValue) {
			return moment(dateValue, tdsCommon.defaultDateTimeFormat(), true).isValid();
		}

		function formatDuration(duration, scale) {
			return tdsCommon.formatDuration(duration, scale.toLowerCase());
		}

		return {
			formatDueDate: formatDueDate,
			formatDate: formatDate,
			formatDateTime: formatDateTime,
			createDateFromString: createDateFromString,
			createDateTimeFromString: createDateTimeFromString,
			formatDuration: formatDuration,
			isValidDate: isValidDate,
			isValidDateTime: isValidDateTime,
            kendoDateFormat: kendoDateFormat,
            kendoDateTimeFormat: kendoDateTimeFormat,
            defaultDateFormat: defaultDateFormat
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
tds.core.interceptor.LoggedCheckerInterceptor = function($log) {
	var servicesInterceptor = {
		response: function(jsonResponse) {
			var loginRedirect = jsonResponse.headers('x-login-url');

			if (!loginRedirect) {
				var contentType = jsonResponse.headers('content-type')
				var isJson = (contentType && contentType.indexOf("json") > -1);
				if (isJson) {
					try {
						var json = angular.fromJson(jsonResponse.data);
						if (json.errors && json.errors.length > 0) {
							var errorDiv = angular.element(document.querySelector('#errorModalText'));
							var errorsHTML = "<ul>";
							for (var j = 0; j < json.errors.length; j++) {
								var emsg = '';
								if (json.errors[j] instanceof String) {
									emsg = json.errors[j];
								} else if (typeof(json.errors[j].detail) !== 'undefined') {
									emsg = json.errors[j].detail
								} else {
									emsg = json.errors[j]
								}
								errorsHTML = errorsHTML + "<li>" + emsg + "</li>";
							}
							errorsHTML = errorsHTML + "</ul>";
							errorDiv.html(errorsHTML);
							$('#errorModal').modal('show');
						}
					} catch (e) {
						$log.error("Invalid ServiceResult JSON format.", e);
					}
				}
				return jsonResponse
			} else {
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

/* Helper function used by the angular interceptor and AJAX prefilter to handle errors */
function handleRequestResponse (jsonResponse, loginRedirect) {
	if (!loginRedirect) {
		try {
			var json = angular.fromJson(jsonResponse.data);
			if (json.errors && json.errors.length > 0) {
				var errorDiv = angular.element(document.querySelector('#errorModalText'));
				var errorsHTML = "<ul>";
				for (var j = 0; j < json.errors.length; j++) {
					var emsg = '';
					if (json.errors[j] instanceof String) {
						emsg = json.errors[j];
					} else if (typeof(json.errors[j].detail) !== 'undefined') {
						emsg = json.errors[j].detail
					} else {
						emsg = json.errors[j]
					}
					errorsHTML = errorsHTML + "<li>" + emsg + "</li>";
				}
				errorsHTML = errorsHTML + "</ul>";
				errorDiv.html(errorsHTML);
				$('#errorModal').modal('show');
			}
			return jsonResponse
		} catch (e) {
			return jsonResponse;
		}
	} else {
		window.location.href = loginRedirect;
	}
}

/**
 * An Ajax Prefilter that is be applied to all Jquery Ajax calls. It will add the following functionality:
 *    1. Add checks for errors in the request and display them in the #messageDiv or as an alert
 *    2. Check for session expiration and redirect the user to the login page accordingly
 */
$.ajaxPrefilter(function(options, originalOptions, jqXHR) {
	var success = options.success;
	options.success = function(data, textStatus, xhr) {
		var url = xhr.getResponseHeader('X-Login-URL');
		
		if (!url) {
			
			handleRequestResponse({'data':data}, url);
			// Check for error situations
			if (xhr.status == 200 && data.status != 'error') {
				if (success) {
					return success(data, textStatus, jqXHR);
				}
			} else {
				/*
					There're scenarios were additional actions are required,
					even if the server responded with errors.

					@see pub.saveToShow (entity.crud.js)
				 */
				if(typeof this.successWithErrors != "undefined"){
					this.successWithErrors(data)
				} else if (data.errors) {
					try {
						var json = angular.fromJson(data);
						if (!json || !json.errors || json.errors.length <= 0) {
							tdsCommon.displayWsError(xhr, data.errors, false);
						}
					} catch (e) {
						console.warn('Unidentified Error Parameters');
					}
                }
				return false;
			}
		} else {
			// The session must of expired if the X-Login-URL header was received
			_goToLogin(url);
			return;
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
		// After deleting the last one, the list is empty
		if(rootScope.alerts.list[index]){
			rootScope.alerts.list[index].hidden = true;
			timeout(function() {
				rootScope.alerts.list.splice(index, 1);
			}, 500);
		}
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
 * Service used to administrate windows timed updates
 */
tds.core.service.TimedUpdateService = function(window) {

	var pause = function() {
		if (typeof timerBar !== 'undefined')
			timerBar.Pause();
	}

	var resume = function() {
		if (typeof timerBar !== 'undefined')
			timerBar.attemptResume();
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
tds.core.directive.DatePicker = function(utils) {
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
						locale: {
							format: tdsCommon.defaultDateFormat(),
						},
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
tds.core.directive.RangePicker = function(utils) {
	return {
		restrict: 'A',
		require: 'ngModel',
		scope: {
			idHtmlParentContainer: '=',
			dateBegin: '=',
			dateEnd: '=',
			duration: '=',
			scale: '=',
			scales: '=',
			durationLocked: '=durationLocked',
			ngModel: '=ngModel'
		},
		link: function(scope, element, attrs, ngModelCtrl) {
			var delim = " - ";

			var updateModel = function() {
				var valid = true;
				var range = element.val();

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

				if(scope.ngModel != null && scope.ngModel != '') {
					ngModelCtrl.$setValidity('dateRange', valid);
				}

				if (valid) {
					ngModelCtrl.$setValidity('dateRange', valid);
					if (scope.$root.$$phase != '$apply' && scope.$root.$$phase != '$digest') {
						scope.$apply();
					}
					executeParentUpdate();
				}
			}

			var executeParentUpdate = function() {
				var expression = attrs['ngChange'];
				if (expression) {
					scope.$parent.$eval(expression);
				}
			}

			scope.$watch('dateBegin', function(nVal, oVal) {
				if (nVal && (nVal != oVal)) {
					element.data('daterangepicker').setStartDate(scope.dateBegin);
				}
			});

			scope.$watch('dateEnd', function(nVal, oVal) {
				if (nVal && (nVal != oVal)) {
					element.data('daterangepicker').setEndDate(scope.dateEnd);
					executeParentUpdate();
				}
			});

			jQuery(
				function($) {
					element.daterangepicker({
						timePicker: true,
						timePickerIncrement: 1,
						initialStartEndDate: false,
						locale: {
							format: tdsCommon.defaultDateTimeFormat(),
						},
						showDropdowns: true,
						startDate: scope.dateBegin,
						endDate: scope.dateEnd,
						duration: scope.duration,
						parseDateTimeString: tdsCommon.parseDateTimeString
					});

					element.on('show.daterangepicker', function(ev, picker) {
						element.data('daterangepicker').changeDurationLocked(scope.durationLocked);
						element.data('daterangepicker').changeInitialStartEndDate(false);
						element.data('daterangepicker').hidePickerTime();

						$('.modal').on('scroll', function(){ element.data('daterangepicker').move(); });
						$(window).resize(function(){
							if(element.data('daterangepicker'))
								element.data('daterangepicker').move();
						});
						var modalParent =  $('#'+scope.idHtmlParentContainer).closest('.modal-dialog');

						if(modalParent && modalParent.length > 0) {
							modalParent.height($('#'+scope.idHtmlParentContainer).height() + 236);
						}

						if(scope.duration !== "") {
							element.data('daterangepicker').hidePickerTime();
							element.data('daterangepicker').setDuration(scope.duration);
						}

						if((scope.dateBegin !== "" && scope.dateBegin != null) && (scope.dateEnd !== "" && scope.dateEnd != null)) {
							element.data('daterangepicker').setStartDate(scope.dateBegin);
							element.data('daterangepicker').setEndDate(scope.dateEnd);
							element.data('daterangepicker').updateView();

						}

						$('[data-toggle="popover"]').popover();
					});
					element.on('apply.daterangepicker', function(ev, picker) {
						scope.durationLocked = element.data('daterangepicker').getDurationLocked();
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
			durationLocked: '=durationLocked',
			ngModel: '=ngModel'
		},
		link: function(scope, element, attrs, ngModelCtrl) {

			scope.durationpicker = {
				day: 0,
				hour: 0,
				minutes: 0
			};

			// This control if the Lock Duration needs to be updated
			var modifyLockDuration = true;

			/**
			 * Modify the view
			 */
			var updateModel = function() {

				if(!scope.scale){
					scope.scale = 'M';
				}

				var duration = parseInt(scope.duration, 10);

				if (duration || duration == 0) {
					var startDate = moment().startOf('day');
					var endDate = moment().startOf('day');

					endDate.add(scope.scale.toLowerCase(), duration);

					var duration = moment.duration(endDate.diff(startDate));

					scope.durationpicker.day = parseInt(duration.asDays());
					scope.durationpicker.hour = parseInt(duration.hours());
					scope.durationpicker.minutes = parseInt(duration.minutes());

					modifyLockDuration = false;

					updateNGModel();

				}
			}


			var updateNGModel = function() {
				// Update Model to ms for start/end date
				ngModelCtrl.$setViewValue(
					(scope.durationpicker.day * 8.64e+7) +
					(scope.durationpicker.hour * 3.6e+6) +
					(scope.durationpicker.minutes * 60000) // Update Model to ms for start/end date
				);
			}

			var updateView = function() {
				//check with current scale, if not go with a lower one
				var offset = moment(0).add(1, scope.scale.toLowerCase());
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

			scope.$watch('durationpicker', function(nVal, oVal) {
				if (nVal && (nVal != oVal) ) {
					updateNGModel();
					if(modifyLockDuration) {
						scope.durationLocked = true;
					}
					modifyLockDuration = true;
				}
			}, true);

			scope.$watch('duration', function(nVal, oVal) {
				if (!isNaN(nVal) && (nVal !== oVal)) {
					updateModel();
				}
			}, true);

			scope.$watch('ngModel', function(nVal, oVal) {
				if (!isNaN(nVal) && (nVal !== oVal)) {
					updateView();
				}
			}, true);

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
					return path;
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
tds.core.module.value('appCommonData', tds.core.service.commonDataService);

tds.core.module.factory('utils', ['servRootPath', tds.core.utils]);

tds.core.module.factory('alerts', ['$rootScope', '$timeout', tds.core.service.AlertsService]);
tds.core.module.factory('windowTimedUpdate', ['$window', tds.core.service.TimedUpdateService]);

tds.core.module.factory('loggedCheckerInterceptor', ['$log', tds.core.interceptor.LoggedCheckerInterceptor]);
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
tds.core.module.directive('tdsdatepicker', ['utils', tds.core.directive.DatePicker]);
tds.core.module.directive('tdsrangepicker', ['utils', tds.core.directive.RangePicker]);
tds.core.module.directive('tdsdurationpicker', ['utils', tds.core.directive.DurationPicker]);
tds.core.module.directive('tdsactionbutton', ['utils', '$window', tds.core.directive.ActionButton]);