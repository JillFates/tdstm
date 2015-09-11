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
	 * Check if the response is valid and return the response otherwise show the error appropriately
	 */
	isValidWsResponse: function(response, errorMsg, alerts) {
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

		return ( isValid ? data : false );
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
	 * Used to validate an email address format
	 * @param email
	 * @return boolean true if valid else false
	 */
	isValidEmail: function(email) {
		var emailExp = /^([0-9a-zA-Z]+([_.-]?[0-9a-zA-Z]+)*@[0-9a-zA-Z]+[0-9,a-z,A-Z,.,-]+\.[a-zA-Z]{2,4})+$/ ;
		return emailExp.test(email);
	}

}

tdsCommon.autoClearDialogOnClose();