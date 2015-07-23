/*
 * TDS Common Javascript Library
 */
 
var tdsCommon = {
	
	config: {
		// The base path of the application URI
		appBaseUri:  '/tdstm',
		dateFormatNoYear: null,
		dateFormat: null,
		dateTimeFormat: null,
		dateShortFormat: null,
		jQueryDateFormat: null,
		jQueryDateTimeFormat: null
	},

	// creates relative or fully qualified url to for the application
	// @param uri - the URI to append to the application base URI or FQU
	// @param fqu - optional flag if true will create fully qualified URL or default to the relative 
	createAppURL: function(uri, fqu) {
		fqu = fqu || false;
		var url = '';
		if (fqu) {
			url = window.location.protocol + '//' + window.location.host
		}
		url += this.config.appBaseUri + uri;
		return url;
	},

	// Generate a random string 
	// @param Integer the length of string to generate (default 5)
	// @param String the characterset to build the random string from (default A-Za-z0-9)
	// @return String the random string
	randomString: function(strLength, charSet) {
		var result = [];

		strLength = strLength || 5;
		charSet = charSet || 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';

		while (--strLength) {
			result.push(charSet.charAt(Math.floor(Math.random() * charSet.length)));
		}

		return result.join('');
	},

	// Common method to capitalize the first letter of a string
	capitalize: function(str) {
		return str.charAt(0).toUpperCase() + str.substring(1);
	},

	/**
	 * This will override the dialog close event to clear out the HTML content of the DIV automatically. This was 
	 * done to correct a problem with DIVs being populated with content that would not be cleared out and duplicate
	 * DOM IDs would be created causing DOM lookup issues. It also closes any currently open select2 controls that might 
	 * be expanded on the page.
	 *
	 * To disable this behavior add the class 'static-dialog' to the DIV (class="static-dialog" or modal.addClass('static-dialog'))
	 */
	autoClearDialogOnClose: function() {
		$.widget( "ui.dialog", $.ui.dialog, {
			close: function(event) {
				var result = this._super();
				if (this.element.length > 0) {
					var dialog = $("#" + this.element[0].id)
					if (dialog.length > 0) {
						// Need to close any Select2 controls that might still be open
						dialog.find('.select2-container').select2('close'); 

						if (! dialog.hasClass('static-dialog')) {
							dialog.html('');
						}
					}
				}
				return result;
			}
		});
	},

	/**
	 * A common error response handler for the Select2 controller when using Ajax and an error occurs
	 */
	select2AjaxErrorHandler: function(jqXHR, textStatus, errorThrown) {
		console.log('select2 ajax error : status=' + jqXHR.status + ' error='+errorThrown);
		if (textStatus == 'parsererror' && jqXHR.responseText.indexOf('Your login session has expired.')>0) {
			alert('Your session has expired and need to login again');
			window.location.href = tdsCommon.createAppURL('/auth/login');
		}
		return 'An error occurred during search';
	},

	/**
	 * Check if the response is valid and if not show a message
	 */
	isValidWsResponse: function(response, errorMsg, alerts) {
		var isValid = true;
		if (response.status != 200) {
			isValid = false;
			this.displayWsError(response, errorMsg, alerts);
		}
		return isValid;
	},

	/**
	 * A common error response handler that display different errors base on http status
	 */
	displayWsError: function(response, errorMsg, alerts) {
		switch (response.status) {
			case 401:
			case 403:
				this.displayError("You don't have permissions to do this operation.", alerts);
				break;
			default:
				if (errorMsg != null) {
					this.displayError(errorMsg, alerts);
				} else {
					this.displayError("An unexpected error occurred. Please try again.", alerts);
				}
		}
	},

	/**
	 * Display a message error using the alerts server or the defauls javascript alert
	 */
	displayError: function(msg, alerts) {
		if (alerts) {
			alerts.addAlert({type: 'danger', msg: msg});
		} else {
			alert(msg);
		}
	},

	/**
	 * Check if user date format type is MM/DD/YY
	 */
	isFormatMMDDYYYY: function() {
		var df = $("#userDTFormat").val();
		return ((df != null) && (df == "MM/DD/YYYY"))
	},

	/**
	 * Returns a date format that don't have a year
	 */
	noYearDateFormat: function() {
		if (this.config.dateFormatNoYear == null) {
			this.config.dateFormatNoYear = "MM/DD h:mm A";
			if (!this.isFormatMMDDYYYY()) {
				this.config.dateFormatNoYear = "DD/MM h:mm A";
			}
		}
		return this.config.dateFormatNoYear;
	},

	/**
	 * Returns default date short format
	 */
	defaultShortDateFormat: function() {
		if (this.config.dateShortFormat == null) {
			this.config.dateShortFormat = "MM/DD/YY";
			if (!this.isFormatMMDDYYYY()) {
				this.config.dateShortFormat = "DD/MM/YY";
			}
		}
		return this.config.dateShortFormat;
	},

	/**
	 * Returns default date format
	 */
	defaultDateFormat: function() {
		if (this.config.dateFormat == null) {
			this.config.dateFormat = "MM/DD/YYYY";
			if (!this.isFormatMMDDYYYY()) {
				this.config.dateFormat = "DD/MM/YYYY";
			}
		}
		return this.config.dateFormat;
	},

	/**
	 * Returns default date time format
	 */
	defaultDateTimeFormat: function() {
		if (this.config.dateTimeFormat == null) {
			this.config.dateTimeFormat = "MM/DD/YYYY h:mm A";
			if (!this.isFormatMMDDYYYY()) {
				this.config.dateTimeFormat = "DD/MM/YYYY h:mm A";
			}
		}
		return this.config.dateTimeFormat;
	},

	/**
	 * Returns jQuery date format
	 */
	jQueryDateFormat: function() {
		if (this.config.jQueryDateFormat == null) {
			this.config.jQueryDateFormat = "mm/dd/yy";
			if (!this.isFormatMMDDYYYY()) {
				this.config.jQueryDateFormat = "dd/mm/yy";
			}
		}
		return this.config.jQueryDateFormat;
	},

	/**
	 * Returns jQuery date time format
	 */
	jQueryDateTimeFormat: function() {
		if (this.config.jQueryDateTimeFormat == null) {
			this.config.jQueryDateTimeFormat = "mm/dd/yy h:i";
			if (!this.isFormatMMDDYYYY()) {
				this.config.jQueryDateTimeFormat = "'dd/mm/yy h:i";
			}
		}
		return this.config.jQueryDateTimeFormat;
	},

	parseDateTimeFromZulu: function(stringValue, format) {
		return moment(stringValue);
	},
	
	parseDateTimeString: function(stringValue, format) {
		if (typeof(format)==='undefined') {
			format = this.defaultDateTimeFormat();
		}
		return moment(stringValue, format);
	},

	isValidDate: function(stringValue) {
		var d = moment(stringValue, this.defaultDateFormat());
		return d.isValid()
	},

	isValidDateTime: function(stringValue) {
		var d = moment(stringValue, this.defaultDateTimeFormat());
		return d.isValid()
	},

	formatDateTime: function(momentObj, format) {
		var result = "";
		if (typeof(format)==='undefined') {
			format = this.defaultDateTimeFormat();
		}
		if (momentObj) {
			result = momentObj.format(format);
		}
		return result;
	},

	jqgridDateCellFormatter: function(cellvalue, options, rowObject) {
		if (cellvalue) {
			var result = "";
			var momentObj = tdsCommon.parseDateTimeFromZulu(cellvalue);
			if (momentObj) {
				momentObj.tz(tdsCommon.timeZone());
				result = momentObj.format(tdsCommon.defaultDateFormat());
			}
			return result;
		} else {
			return 'Never';
		}
	},

	jqgridPrefCellFormatter: function(cellvalue, options, rowObject) {
		var result = cellvalue;
		switch (options.colModel.name) {
			case "lastUpdated":
			case "retireDate":
			case "maintExpDate":
				var momentObj = tdsCommon.parseDateTimeFromZulu(cellvalue);
				if (momentObj.isValid()) {
					momentObj.tz(tdsCommon.timeZone());
					result = momentObj.format(tdsCommon.defaultDateTimeFormat());
				} else {
					result = "";
				}
				break;
		}
		return result;
	},

	parseAndFormatDateTimeFromZulu: function(stringValue, format) {
		var result;
		var momentObj = tdsCommon.parseDateTimeFromZulu(stringValue);
		if (momentObj.isValid()) {
			if (typeof(format)==='undefined') {
				format = this.defaultDateTimeFormat();
			}
			momentObj.tz(tdsCommon.timeZone());
			result = momentObj.format(format);
		} else {
			result = "";
		}
		return result;
	},

	timeZone: function() {
		return $("#tzId").val()
	}

}

tdsCommon.autoClearDialogOnClose();


/*
 * TDS User Preference utils
 */

 var UserPreference = function() {

 	var savePreferences = function(formId) {
		var data = $('#' + formId).serialize();
		$.post(tdsCommon.createAppURL('/person/savePreferences'), data, function() {
			window.location.reload();
		})
		.fail(function() {
			alert("Can't update user's preferences");
		});
 	}

	return {
		savePreferences: savePreferences
	}

 }();