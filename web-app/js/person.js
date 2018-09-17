var Person = function () {
	
	// internal variables used to keep track of tab state
	var currentTabShow = "generalInfoShowId"
	var currentHeaderShow = "generalShowHeadId"
	
	// resets the tab state variables to their defaults
	var resetTabsState = function () {
		currentTabShow = "generalInfoShowId"
		currentHeaderShow = "generalShowHeadId"
	}
	
	var showBulkDeleteModal = function() {
		$('#bulkDeleteMessages').html("");
		$('#bulkModalDeleteBtn').show();
		$('#bulkModalCancelBtn').show();
		$('#bulkModalCloseBtn').hide();
		$("#bulkDeleteModal").modal("show");
	}
	
	// Used to Call the Person bulkDelete service call from the Person List
	var bulkDelete = function() {
		var deleteIfAssocWithAssets = $('#deleteIfAssocWithAssets').prop('checked');
		var ids = new Array()
		$('.cbox:checkbox:checked').each(function(){
			ids.push(this.id.split("_")[2])
		})
		jQuery.ajax({
			url: tdsCommon.createAppURL('/person/bulkDelete'),
			data: {'ids':ids, 'deleteIfAssocWithAssets': deleteIfAssocWithAssets},
			type:'POST',
			success: function(data, stat, xhr) {
				var url = xhr.getResponseHeader('X-Login-URL');
				if (url) {
					window.location.href = url;
				} else {
					if (data.status == 'success') {
						$('#bulkModalDeleteBtn').hide();
						$('#bulkModalCancelBtn').hide();
						$('#bulkModalCloseBtn').show();

						var responseMsg = '<b>Results:</b><br><lu>' +
							'<li>People deleted: ' + data.data.deleted +
							'</li><li>People skipped: ' + data.data.skipped +
							'</li><li>Asset assocations cleared: ' + data.data.cleared;

						for (var i=0; i<data.data.messages.length;i++) {
							responseMsg += '<li>' + data.data.messages[i] + '</li>';
						}

						responseMsg += '</lu>';

						$('#bulkDeleteMessages').html(responseMsg);

						//location.reload(true);
					} else {
						alert('Error deleting people - ' + data.errors.join());
					}
				}
			}
		}).fail(function(response) {
			tdsCommon.displayWsError(response, "Error deleting people.", false);
		});
	}

	var closePopup = function() {
		location.reload(true);
	}
	
	// gets the person dialog div or creates it if it doesn't exist
	var getPersonDiv = function () {
		var id = 'personGeneralViewId'
		var personDiv = $('#' + id)
		if (personDiv.length == 0) {
			personDiv = $('<div>')
				.attr('id', id)
				.attr('title', 'Manage Staff')
				.css('display', 'none')
			$('body').append(personDiv)
			personDiv.dialog({ autoOpen: false })
			
			// hack to get the close button to show up while using bootstrap
			$('div.ui-dialog.ui-widget').find('button.ui-dialog-titlebar-close').html('<span class="ui-button-icon-primary ui-icon ui-icon-closethick"></span>')
		}
		
		return personDiv
	}
	
	// performs the ajax call then loads the data into the person dialog
	var showPersonDialog = function (personId, renderPage, redirectTo) {
		if (personId && personId != 0) {
			var personDiv = getPersonDiv()
			jQuery.ajax({
				url : contextPath+'/person/loadGeneral',
				data : {
					'personId': personId,
					'tab': renderPage
				},
				type : 'POST',
				success : function(data) {
					if (renderPage == 'generalInfoShow')
						resetTabsState()
					if (redirectTo == 'edit') {
						currentTabShow = currentTabShow.replace('Show','Edit')
						currentHeaderShow = currentHeaderShow.replace('Show','Edit')
					}
					personDiv.html(data)
					personDiv.dialog({ autoOpen: false })
					personDiv.dialog('option', 'width', '420px')
					personDiv.dialog('option', 'position', ['center','top'])
					personDiv.dialog('option', 'modal', 'true')
					if (currentTabShow == "generalInfoShowId")
						personDiv.dialog('option', 'title', 'Manage Staff - ' + $('span[id="firstNameId"]').text() + ' ' + $('span[id="middleNameId"]').text() + ' ' + $('span[id="lastNameId"]').text())

					$('div.ui-dialog.ui-widget').find('button.ui-dialog-titlebar-close').html('<span class="ui-button-icon-primary ui-icon ui-icon-closethick"></span>');
					
					$("#"+currentTabShow).show()
					
					if (currentTabShow != "generalInfoEditId")
						$('#generalInfoEditId').hide();
					
					$(".mobmenu").removeClass("mobselect")
					$("#"+currentHeaderShow).addClass("mobselect")
					
					personDiv.dialog('open');
					$('.ui-widget-overlay').addClass('old-legacy-content');
					$('.ui-dialog').addClass('old-legacy-content');
				},
				error : function (response) {
					tdsCommon.displayWsError(response, "Error retrieving person information.", false)
				}
			})
		}
	}
	
	// switches the view to a different tab in the person dialog
	var switchTab = function (divId, header) {
		$(".person").hide()
		$(".mobmenu").removeClass("mobselect")
		currentTabShow = divId
		currentHeaderShow = header
		$("#"+currentHeaderShow).addClass("mobselect")
		$("#"+currentTabShow).show()
	}
	
	// makes the Ajax call to update person info.
	var updatePerson = function (tab,form) {
		var validate = validatePersonForm(form)
		if (validate) {
			var params = $('#'+form).serialize()
			params+= "&tab=" + tab;
			jQuery.ajax({
				url:$('#'+form).attr('action'),
				data: params,
				type:'POST',
				success: function(data) {
					if (data.status == 'error') {
						for(var e = 0; e < data.errors.length; ++e)
							alert(data.errors[e])
					} else {					
						getPersonDiv().html(data)
						var newCurrentTabShow = currentTabShow.replace('Edit','Show')
						var newCurrentHeaderShow = currentHeaderShow.replace('Edit','Show')
						switchTab(newCurrentTabShow, newCurrentHeaderShow)
					}
				},
				error: function(jqXHR, textStatus, errorThrown) {
					tdsCommon.displayWsError(jqXHR, "An unexpected error occurred while attempting to update Person.", false);
				}
			});
		}
	}
	
	// closes the person dialog and resets the tab state variables
	var closePersonDiv = function (divId) {
		resetTabsState()
		$('#'+divId).dialog('close')
	}
	
	
	// opens the dialog for merging and comparing people
	var compareOrMerge = function () {
		var ids = new Array()
		$('.cbox:checkbox:checked').each(function(){
			ids.push(this.id.split("_")[2])
		})
		jQuery.ajax({
			url: contextPath+'/person/compareOrMerge',
			data: {'ids':ids},
			type:'POST',
			success: function(data) {
				$("#showOrMergeId").html(data)
				$("#showOrMergeId").dialog('option', 'width', 'auto');
				$("#showOrMergeId").dialog('option', 'modal', 'true');
				$("#showOrMergeId").dialog('option', 'position', ['center','top']);
				$("#showOrMergeId").dialog('open')
			},
			error: function(jqXHR, textStatus, errorThrown) {
				tdsCommon.displayWsError(jqXHR, "Error retrieving users information.", false);
			}
		});
	}


	/**
	 * Validate person form
	 */
	function validatePersonForm(form) {
		var mobileExp=/^([0-9 +-])+$/
		var returnVal = true
		var allFields = $("form[name = "+form+"] input[type = 'text']");

		jQuery.each(allFields , function(i, field) {
			field.value= $.trim(field.value)
		});

		var firstName = $(
			"form[name = "+form+"] input[name = 'firstName']")
			.val()
		var email = $(
			"form[name = "+form+"] input[name = 'email']")
			.val()
		var workPhone = $(
			"form[name = "+form+"] input[name = 'workPhone']")
			.val().replace(/[\(\)\.\-\ ]/g, '')
		var mobilePhone = $(
			"form[name = "+form+"] input[name = 'mobilePhone']")
			.val().replace(/[\(\)\.\-\ ]/g, '')
		if (!firstName) {
			alert("First Name should not be blank ")
			returnVal = false
		}
		if (email && !tdsCommon.isValidEmail(email)) {
			alert(email + " is not a valid e-mail address ")
			returnVal = false
		}
		if (workPhone && !(mobileExp.test(workPhone))) {
			alert("The Work phone number contains illegal characters.");
			returnVal = false
		}
		if (mobilePhone && !(mobileExp.test(mobilePhone))) {
			alert("The Mobile phone number contains illegal characters.");
			returnVal = false
		}
		return returnVal
	}
	
	// performs the person merge
	var mergePerson = function () {
		var returnStatus =  confirm('This will merge the selected Person(s)');
		if(returnStatus ){
			var targetModelId 
			var modelToMerge = new Array()
			$('input[name=mergeRadio]:radio:checked').each(function(){
				targetModelId = this.id.split("_")[1]
			})
			if(!targetModelId){
				alert("Please select Target Model")
				return
			}
			$('input[name=mergeRadio]:radio:not(:checked)').each(function(){
				modelToMerge.push(this.id.split("_")[1])
			})
			var params = {};
			$(".input_"+targetModelId).each(function(){
				if(this.name!='manufacturer' && this.name!='createdBy' && this.name!='updatedBy')
					params[this.name] = this.value;
			})
			params['toId'] = targetModelId;
			params['fromId'] = modelToMerge;
			jQuery.ajax({
				url: contextPath+'/person/mergePerson',
				data: params,
				type:'POST',
				beforeSend: function(jqXHR){
					$("#showOrMergeId").dialog('close')
					$("#messageId").html($("#spinnerId").html())
					$("#messageId").show()
				},
				success: function(data) {
					$("#spinnerId").hide()
					$("#messageId").html(data)
					$(".ui-icon-refresh").click()
					if(validateMergeCount)
                    	validateMergeCount();
				},
				error: function(jqXHR, textStatus, errorThrown) {
					$("#spinnerId").hide()
					$("#messageId").hide()
					tdsCommon.displayWsError(jqXHR, "An unexpected error occurred while attempting to Merge Persons.", false);
				}
				
			});
		} else {
			return false
		}
	}

	// switches which person will be merged to by the merge operation
	var switchTarget = function (targetId) {
		
		$(".editAll:visible").children().each(function(){
			var span = $(this).parent().siblings()
			if($(this).attr('type') == 'checkbox'){
				span.children().val(span.children().val())
			}else {
				var toSpan = ''
				$(this).parent().children("input").each(function(i){
					if($(this).siblings().length > 0 && ! $(this).val())
						toSpan = toSpan + 'null'
					else
						toSpan = toSpan + $(this).val()
				})
				if(toSpan.length > 20)
					toSpan = toSpan.substring(0, 20) + '...'
				span.html(toSpan)
			}
		})
		
		$(".editAll").hide()
		$(".showAll").show() 
		$(".editTarget_"+targetId).show()
		$(".showFrom_"+targetId).hide()
	}

	return {
		showBulkDeleteModal: showBulkDeleteModal,
		bulkDelete: bulkDelete,
		closePopup: closePopup,
		showPersonDialog: showPersonDialog,
		switchTab: switchTab,
		updatePerson: updatePerson,
		closePersonDiv: closePersonDiv,
		compareOrMerge: compareOrMerge,
		mergePerson: mergePerson,
		switchTarget: switchTarget
	}
}()


