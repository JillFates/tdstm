/*
 * TDS Common Javascript Library
 */

var tdsCommon = {

	config: {
		// The base path of the application URI
		appBaseUri: '/tdstm',
		dateFormatNoYear: null,
		dateFormat: null,
		dateTimeFormat: null,
		dateShortFormat: null,
		jQueryDateFormat: null,
		jQueryDateTimeFormat: null,
		kendoDateFormat: null,
		kendoDateTimeFormat: null
	},

	// creates relative or fully qualified url to for the application
	// @param uri - the URI to append to the application base URI or FQU
	// @param fqu - optional flag if true will create fully qualified URL or default to the relative 
	createAppURL: function (uri, fqu) {
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
	randomString: function (strLength, charSet) {
		var result = [];

		strLength = strLength || 5;
		charSet = charSet || 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';

		while (--strLength) {
			result.push(charSet.charAt(Math.floor(Math.random() * charSet.length)));
		}

		return result.join('');
	},

	// Common method to capitalize the first letter of a string
	capitalize: function (str) {
		return str.charAt(0).toUpperCase() + str.substring(1);
	},

	// Compare 2 strings
	// @param string1 the string to compare
	// @param string2 the string to compare
	// @param ignoreCase whether to use case sensitive comparison
	// @param useLocale whether to use host's current locale while comparing strings
  compareStrings: function(string1, string2, ignoreCase, useLocale) {
  	if (ignoreCase) {
    	if (useLocale) {
      	string1 = string1.toLocaleLowerCase();
      	string2 = string2.toLocaleLowerCase();
    	} else {
      	string1 = string1.toLowerCase();
      	string2 = string2.toLowerCase();
    	}
  	}
  	return string1 === string2;
	},

	// Compare 2 strings using case insensitive
  // @param string1 the string to compare
  // @param string2 the string to compare
  compareStringsIgnoreCase: function(string1, string2) {
		return this.compareStrings(string1, string2, true, false);
	},

	arrayStringIndexIgnoreCase: function(array, string1) {
		var $this = this;
    var index = -1;
    if(array && array.length) {
      array.some(function (element, i) {
        if ($this.compareStringsIgnoreCase(string1, element)) {
          index = i;
          return true;
        }
      });
    }
    return index;
	},

  arrayContainsStringIgnoreCase: function(array, string1) {
    return this.arrayStringIndexIgnoreCase(array, string1) >= 0;
	},

    /**
	 * Applies a more smooth delay, extending the setTimeout
     * @returns {Function}
     */
	delayEvent: (function () {
		var timer = 0;
		return function (callback, ms) {
			clearTimeout(timer);
			timer = setTimeout(callback, ms);
		};
	})(),

	/**
	 * This will override the dialog close event to clear out the HTML content of the DIV automatically. This was 
	 * done to correct a problem with DIVs being populated with content that would not be cleared out and duplicate
	 * DOM IDs would be created causing DOM lookup issues. It also closes any currently open select2 controls that might 
	 * be expanded on the page.
	 *
	 * To disable this behavior add the class 'static-dialog' to the DIV (class="static-dialog" or modal.addClass('static-dialog'))
	 */
	autoClearDialogOnClose: function () {
		$.widget("ui.dialog", $.ui.dialog, {
			close: function (event) {
				var result = this._super();
				if (this.element.length > 0) {
					var dialog = $("#" + this.element[0].id)
					if (dialog.length > 0) {
						// Need to close any Select2 controls that might still be open
						var select2 = dialog.find('.select2-container');
						if (select2 && select2.length > 0) {
							dialog.find('.select2-container').select2('close');
						}

						if (!dialog.hasClass('static-dialog')) {
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
	select2AjaxErrorHandler: function (jqXHR, textStatus, errorThrown) {
		var url = jqXHR.getResponseHeader('X-Login-URL');
		if (url) {
			window.location.href = url;
		} else {
			console.log('select2 ajax error : status=' + jqXHR.status + ' error=' + errorThrown);
			return 'An error occurred during search';
		}
	},

	/**
	 * Check if the response is valid and return the response otherwise show the error appropriately
	 */
	isValidWsResponse: function (response, errorMsg, alerts) {
		var isValid = false;
		var data = false;

		if (response.status != 200) {
			this.displayWsError(response, errorMsg, alerts);
		} else {
			data = eval("(" + response.responseText + ")");
			// See if we have a result that has 'status'
			if (data.status) {
				isValid = (data.status == 'success');
				if (isValid) {
					if (data.data) {
						// Remove the nested data structure to just return the data
						data = data.data
					}
				} else {
					if (data.errors) {
						alert(data.errors);
					} else {
						alert("An error occurred while updating and/or updating information");
					}
				}
			} else {
				// Must be a non-standard ws response so we can only guess that it is okay
				isValid = true;
			}
		}

		return (isValid ? data : false);
	},

    /**
	 * Since we are upgrading some ajax calls to use jQuery.ajax() instead of prototype.Ajax()
     * we need to support also response models by jQuery.ajax() returns;
	 *
     * @param response
     * @param errorMsg
     * @param alerts
     */
	isValidWsJQueryAjaxResponse: function (response, errorMsg, alerts) {
        var isValid = false;
        var data = false;

        if (response.status != 200 && response.status !== 'success') {
            this.displayWsError(response, errorMsg, alerts);
        } else {
            isValid = (response.status == 'success');
            if (isValid) {
                if (response.data) {
                    // Remove the nested data structure to just return the data
                    data = response.data;
                }
            } else {
                if (response.errors) {
                    alert(response.errors);
                } else {
                    alert("An error occurred while updating and/or updating information");
                }
            }
        }

        return (isValid ? data : false);
    },

	prepareJQueryAjaxResponse: function(response){
        response.isJQueryAjax = true;
	},

    isJQueryAjaxResponse: function(response){
        return response.isJQueryAjax === true;
    },

	/**
	 * A common error response handler that display different errors base on http status
	 */
	displayWsError: function (response, errorMsg, alerts) {
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
	displayError: function (msg, alerts) {
		if (alerts) {
			alerts.addAlert({ type: 'danger', msg: msg });
		} else {
			alert(msg);
		}
	},

	/**
	 * Return a duration in a readable human way, show we use https://www.unc.edu/~rowlett/units/symbol.html ?
	 * @param duration (number)
	 * @param scale (char val)
	 * return string representation of the duration in terms of days, hours, minutes
	 */
	formatDuration: function (duration, scale) {
		var startDate = moment().startOf('day');
		var endDate = moment().startOf('day');
		endDate.add(duration, scale);

		var durationDate = moment.duration(endDate.diff(startDate)),
			durationResult = "";

		var days = parseInt(durationDate.asDays());
		if (days > 0) {
			durationResult += days + " day" + ((days > 1) ? "s " : " ");
		}

		var hours = parseInt(durationDate.hours());
		if (hours > 0) {
			durationResult += hours + " hr" + ((hours > 1) ? "s " : " ");
		}

		var minutes = parseInt(durationDate.minutes());
		if (minutes > 0) {
			durationResult += minutes + " min" + ((minutes > 1) ? "s " : " ");
		}


		return durationResult;
	},

	/**
	 * Used to validate an email address format
	 * @param email
	 * @return boolean true if valid else false
	 */
	isValidEmail: function (email) {
		var emailExp = /^([0-9a-zA-Z]+([_.-]?[0-9a-zA-Z]+)*@[0-9a-zA-Z]+[0-9,a-z,A-Z,.,-]+\.[a-zA-Z]{2,4})+$/;
		return emailExp.test(email);
	},

	/**
	 * Check if user date format type is MM/DD/YY
	 */
	isFormatMMDDYYYY: function () {
		var df = $("#userDTFormat").val();
		return ((df != null) && (df == "MM/DD/YYYY"))
	},

	/**
	 * Returns a date format that don't have a year
	 */
	noYearDateFormat: function () {
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
	defaultShortDateFormat: function () {
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
	defaultDateFormat: function () {
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
	defaultDateTimeFormat: function () {
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
	jQueryDateFormat: function () {
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
	jQueryDateTimeFormat: function () {
		if (this.config.jQueryDateTimeFormat == null) {
			this.config.jQueryDateTimeFormat = "mm/dd/yy h:i";
			if (!this.isFormatMMDDYYYY()) {
				this.config.jQueryDateTimeFormat = "'dd/mm/yy h:i";
			}
		}
		return this.config.jQueryDateTimeFormat;
	},
	kendoDateFormat: function () {
		if (this.config.kendoDateFormat == null) {
			this.config.kendoDateFormat = "MM/dd/yyyy";
			if (!this.isFormatMMDDYYYY()) {
				this.config.kendoDateFormat = "dd/MM/yyyy";
			}
		}
		return this.config.kendoDateFormat;
	},
	kendoDateTimeFormat: function () {
		if (this.config.kendoDateTimeFormat == null) {
			this.config.kendoDateTimeFormat = "MM/dd/yyyy hh:mm tt";
			if (!this.isFormatMMDDYYYY()) {
				this.config.kendoDateTimeFormat = "dd/MM/yyyy hh:mm tt";
			}
		}
		return this.config.kendoDateTimeFormat;
	},
	parseDateTimeFromZulu: function (stringValue, format) {
		return moment(stringValue);
	},

	parseDateTimeString: function (stringValue, format) {
		if (typeof (format) === 'undefined') {
			format = this.defaultDateTimeFormat();
		}
		return moment(stringValue, format);
	},

	isValidDate: function (stringValue) {
		var d = moment(stringValue, this.defaultDateFormat());
		return d.isValid()
	},

	isValidDateTime: function (stringValue) {
		var d = moment(stringValue, this.defaultDateTimeFormat());
		return d.isValid()
	},

	formatDateTime: function (momentObj, format) {
		var result = "";
		if (typeof (format) === 'undefined') {
			format = this.defaultDateTimeFormat();
		}
		if (momentObj) {
			result = momentObj.format(format);
		}
		return result;
	},

	jqgridDateCellFormatter: function (cellvalue, options, rowObject) {
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

	jqgridDateTimeCellFormatter: function (cellvalue, options, rowObject) {
		if (cellvalue) {
			var result = "";
			var momentObj = tdsCommon.parseDateTimeFromZulu(cellvalue);
			if (momentObj) {
				momentObj.tz(tdsCommon.timeZone());
				result = momentObj.format(tdsCommon.defaultDateTimeFormat());
			}
			return result;
		} else {
			return '';
		}
	},

	jqgridPrefCellFormatter: function (cellvalue, options, rowObject) {
		var result = cellvalue;
		switch (options.colModel.name) {
			case "lastUpdated":
			case "dateCreated":
				var momentObj = tdsCommon.parseDateTimeFromZulu(cellvalue);
				if (momentObj.isValid()) {
					momentObj.tz(tdsCommon.timeZone());
					result = momentObj.format(tdsCommon.defaultDateTimeFormat());
				} else {
					result = "";
				}
				break;

			case "retireDate":
			case "maintExpDate":
				var momentObj = tdsCommon.parseDateTimeFromZulu(cellvalue);
				if (momentObj.isValid()) {
					// Strip off any time 
					momentObj.tz('GMT');
					result = momentObj.format(tdsCommon.defaultDateFormat());
				} else {
					result = "";
				}
				break;
			case "tagAssets":
				return tdsCommon.createAssetTagForLegacyList(cellvalue);
				break;
			default:
				result = _.escape(result);

		}
		return result;
	},

	createAssetTagForLegacyList: function(cellvalue) {
		var html = '<div class="asset-tag-selector-component-content">';
		try {
			var assetList = JSON.parse(cellvalue);
			for (var i = 0; i < assetList.length; i++) {
				html += '<span class="label tag ' + ('tag-' + assetList[i].color.toLowerCase()) + '"> ' + assetList[i].name + '</span>';
			}
		} catch(e){
			console.error(e);
		}
		return html + '</div>';
	},

	// Used to escape the text cells to prevent XSS 
	jqgridTextCellFormatter: function (cellvalue, options, rowObject) {
		return _.escape(cellvalue);
	},

	parseAndFormatDateTimeFromZulu: function (stringValue, format) {
		var result;
		var momentObj = tdsCommon.parseDateTimeFromZulu(stringValue);
		if (momentObj.isValid()) {
			if (typeof (format) === 'undefined') {
				format = this.defaultDateTimeFormat();
			}
			momentObj.tz(tdsCommon.timeZone());
			result = momentObj.format(format);
		} else {
			result = "";
		}
		return result;
	},

	timeZone: function () {
		var tz = $("#tzId").val();
		return tz;
	},

	addNextDayKendoGridFilter: function (grid, field, value) {
		var nextDay = moment(value).add(1, 'd').toDate();
		grid.dataSource._filter.filters.push({
			field: field,
			operator: "lt",
			value: nextDay
		});
		grid.thead.find('tr th:first').trigger('click');
	},

	/**
	 * Validate if the element has reached the max length
	 * @param element
	 * @param errorHtml
	 * @param maxLength
	 */
	validateMaxTextAreaLength: function(element, errorElementId, saveElementId, maxLength) {
		var currentValue = jQuery(element).val();
		if(currentValue && currentValue.length >= maxLength) {
			jQuery('#'+errorElementId).show();
			jQuery('#'+saveElementId).prop('disabled', true);
		} else {
			jQuery('#'+errorElementId).hide();
			jQuery('#'+saveElementId).prop('disabled', false);
		}
	}

}

tdsCommon.autoClearDialogOnClose();


/*
 * TDS User Preference utils
 */

var UserPreference = function () {

	// opens the edit user date and timezone dialog
	var editDateAndTimezone = function () {
		jQuery.ajax({
			url: '/tdstm/person/editTimezone',
			success: function (e) {
				var prefDialog = $("#userTimezoneDivId")
				prefDialog.html(e);
				prefDialog.dialog('option', 'width', 'auto');
				prefDialog.dialog('option', 'modal', true);
				prefDialog.dialog("open");

                $('.ui-widget-overlay').addClass('old-legacy-content');
			},
			error: function (jqXHR, textStatus, errorThrown) {
				alert("An unexpected error occurred while attempting to update task/comment")
			}
		})
		return false;
	}

	// opens the edit user preferences dialog
	var editPreference = function () {
		jQuery.ajax({
			url: '/tdstm/person/editPreference',
			success: function (e) {
				var prefDialog = $("#userPrefDivId")
				var pageHeight = Math.max($(window).outerHeight(), 200)
				prefDialog.html(e)
				prefDialog.dialog('option', 'width', 'auto');
				prefDialog.dialog('option', 'maxHeight', pageHeight);
				prefDialog.dialog('option', 'containment', 'body');
				prefDialog.dialog('option', 'modal', true);
				prefDialog.dialog("open");

                $('.ui-widget-overlay').addClass('old-legacy-content');
			},
			error: function (jqXHR, textStatus, errorThrown) {
				alert("An unexpected error occurred while attempting to update task/comment")
			}
		})
		return false;
	}

	// saves the new preference values
	var savePreferences = function (formId) {
		var data = $('#' + formId).serialize();
		$.post(tdsCommon.createAppURL('/person/savePreferences'), data, function () {
			window.location.reload();
		})
			.fail(function () {
				alert("Can't update user's preferences");
			});
	}

	// resets the user's preferences to their default values
	var resetPreference = function (user, dateTimezoneOnly) {
		var params = { 'user': user }
		if (dateTimezoneOnly)
			params.dateTimezoneOnly = true
		jQuery.ajax({
			url: '/tdstm/person/resetPreferences',
			data: params,
			success: function (e) {
				changeResetMessage(e)
			}
		});
	}


	// closes the preference dialog then refreshes the page
	var changeResetMessage = function (e) {
		var prefDialog = $("#userPrefDivId")
		prefDialog.html("")
		prefDialog.dialog('close')
		//window.location.reload()
		window.location.href = tdsCommon.createAppURL('/project/list');
	}

	// removes the specified preference for the current user
	var removeUserPrefs = function (prefCode) {

		jQuery.ajax({
            url: '/tdstm/person/removeUserPreference?prefCode=' + prefCode,
            success: function(response) {
            	if(response === 'true'){
                    $("#pref_" + prefCode).remove();
				}
            },
            error: function(jqXHR, textStatus, errorThrown) {
                console.log("/tdstm/person/removeUserPreference - " + errorThrown);
            }
        });

	}

	return {
		editDateAndTimezone: editDateAndTimezone,
		editPreference: editPreference,
		savePreferences: savePreferences,
		resetPreference: resetPreference,
		changeResetMessage: changeResetMessage,
		removeUserPrefs: removeUserPrefs
	}
}();

$.fn.serializeObject = function () {
	var o = {};
	var a = this.serializeArray();
	$.each(a, function () {
		if (o[this.name] !== undefined) {
			if (!o[this.name].push) {
				o[this.name] = [o[this.name]];
			}
			o[this.name].push(this.value || '');
		} else {
			o[this.name] = this.value || '';
		}
	});
	return o;
};

(function ($) {
	$.fn.focusToEnd = function () {
		return this.each(function () {
			var v = $(this).val();
			$(this).focus().val("").val(v);
		});
	};
})(jQuery);
