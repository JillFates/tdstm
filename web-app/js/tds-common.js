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

	overrideCloseDialog: function() {
		$.widget( "ui.dialog", $.ui.dialog, {
			close: function(event) {
				var result = this._super();
				if (this.element.length > 0) {
					var dialog = $("#" + this.element[0].id)
					if (dialog.length > 0) {
						dialog.html('');	
					}
				}
				return result;
			}
		});
	}

}

tdsCommon.overrideCloseDialog();