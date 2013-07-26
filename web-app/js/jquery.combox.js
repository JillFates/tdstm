(function($) {
$.widget( "custom.combobox", {
						_create : function() {
							this.wrapper = $("<span>").addClass(
									"custom-combobox")
									.insertAfter(this.element);
							this.element.hide();
							this._createAutocomplete();
							this._createShowAllButton();
						},
						_createAutocomplete : function() {
							var selected = this.element.children(":selected"), value = selected
									.val() ? selected.text() : "";
							this.input = $("<input>")
									.appendTo(this.wrapper)
									.val(value)
									.attr("title", "")
									.addClass(
											"custom-combobox-input ui-widget ui-widget-content ui-state-default ui-corner-left")
									.autocomplete({
										delay : 0,
										minLength : 0,
										source : $.proxy(this, "_source")
									}).tooltip({
										tooltipClass : "ui-state-highlight"
									})
									
									;
							this._on(this.input, {
								autocompleteselect : function(event, ui) {
									ui.item.option.selected = true;
									this._trigger("select", event, {
										item : ui.item.option
									});
									var eventName = null;
									var valueToReplace = null;
									var replacingValue = null
									if(this.element.attr("onchange") !== undefined){
										eventName = "onchange";
										valueToReplace = "this.value";
										replacingValue = this.element.val()
										this.element.attr(eventName).replace(valueToReplace, this.element.val());
										this.element.trigger(eventName);
									}
								},
								autocompletechange : "_removeIfInvalid"
							});
						},
						_createShowAllButton : function() {
							var input = this.input, wasOpen = false;
							console.log(input.parent().prev())
							$("<a>").attr("tabIndex", -1).appendTo(
									this.wrapper).button({
								icons : {
									primary : "ui-icon-triangle-1-s"
								},
								text : false,
							}).removeClass("ui-corner-all").addClass(
									"custom-combobox-toggle ui-corner-right")
									.mousedown(
											function() {
												wasOpen = input.autocomplete(
														"widget")
														.is(":visible");
											}).click(function() {
										input.focus();
										if(input.parent().prev().attr("onmousedown") !== undefined ){
											eventName = "onmousedown";
											valueToReplace = "this.name";
											input.parent().prev().attr(eventName).replace(valueToReplace, input.parent().prev().attr("name"));
											input.parent().prev().trigger(eventName);
										}
										
										// Close if already visible
										if (wasOpen) {
											return;
										}
										// Pass empty string as value to search
										// for, displaying all results
										input.autocomplete("search", "");
									})
						},
						_source : function(request, response) {
							var matcher = new RegExp($.ui.autocomplete
									.escapeRegex(request.term), "i");
							response(this.element.children("option").map(
									function() {
										var text = $(this).text();
										if (this.value
												&& (!request.term || matcher
														.test(text)))
											return {
												label : text,
												value : text,
												option : this
											};
									}));
						},
						_removeIfInvalid : function(event, ui) {
							// Selected an item, nothing to do
							if (ui.item) {
								return;
							}
							// Search for a match (case-insensitive)
							var value = this.input.val(), valueLowerCase = value
									.toLowerCase(), valid = false;
							this.element
									.children("option")
									.each(
											function() {
												if ($(this).text()
														.toLowerCase() === valueLowerCase) {
													this.selected = valid = true;
													return false;
												}
											});
							// Found a match, nothing to do
							if (valid) {
								return;
							}
							// Remove invalid value
							this.input.val("").attr("title",
									value + " didn't match any item").tooltip(
									"open");
							this.element.val("");
							this._delay(function() {
								this.input.tooltip("close").attr("title", "");
							}, 2500);
							this.input.data("ui-autocomplete").term = "";
						},
						_destroy : function() {
							this.wrapper.remove();
							this.element.show();
						}
					});
})(jQuery);