/*
 * TDS Common Javascript Library
 */
 
var tdsCommon = {
	
	config: {
		// The base path of the application URI
		appBaseUri:  '/tdstm'
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
	 * DOM IDs would be created causing DOM lookup issues.
	 *
	 * To disable this behavior add the class 'static-dialog' to the DIV (style="static-dialog" or modal.addClass('static-dialog'))
	 */
	autoClearDialogOnClose: function() {
		$.widget( "ui.dialog", $.ui.dialog, {
			close: function(event) {
				var result = this._super();
				if (this.element.length > 0) {
					var dialog = $("#" + this.element[0].id)
					if (dialog.length > 0 && !dialog.hasClass('static-dialog')) {
						dialog.html('');	
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
	}

}

tdsCommon.autoClearDialogOnClose();