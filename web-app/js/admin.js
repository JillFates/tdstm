var purgeDialog, flushDialog;

function processBatch() {
	var value = $('input:radio[name=deleteHistory]:checked').val()
	if (value == "doNothing") {
		flushDialog.dialog('close');
	} else {
		jQuery.ajax({
			url: contextPath + '/admin/processOldData',
			data: { 'deleteHistory': value },
			type: 'POST',
			beforeSend: function (jqXHR) {
				$('#processDivId').show();
				$("#respMsgId").hide();
				$("#processDivId").show()
			},
			success: function (data) {
				$("#processDivId").hide()
				$("#respMsgId").show().html(data)
			},
			error: function (jqXHR, textStatus, errorThrown) {
				$("#processDivId").hide()
				$("#respMsgId").show().html("An unexpected error occurred. Please close and reload form to see if the problem persists")
			}
		})
	}
}

function openFlushDiv() {
	jQuery.ajax({
		url: contextPath + '/admin/retrieveBatchRecords',
		type: 'POST',
		beforeSend: function (jqXHR) {
			flushDialog.dialog('open');
			$("#getRecordsInfoId").show()
			$("#respMsgId").hide()
			$("#processDivId").show()
		},
		success: function (data) {
			$("#respMsgId").html(data)
			$("#processDivId").hide()
			$("#respMsgId").show()
		},
		error: function (jqXHR, textStatus, errorThrown) {
			alert("An unexpected error occurred while opening Flush import div. Please reload form to see if the problem persists")
		}
	})
}

/*
 *this function is used to show the asset type table dialog
 */
function openShowTypeDiv() {
	$("#cleanProcessId").hide();
	$("#cleanProcessDivId").hide();
	jQuery.ajax({
		url: contextPath + '/admin/retrieveAssetTypes',
		type: 'POST',
		beforeSend: function (jqXHR) {
			// $("#showOrCleanTypeId").dialog('option', 'width', '550px');
			// $("#showOrCleanTypeId").dialog('option', 'position', ['center','top']);
			// $("#showOrCleanTypeId").dialog('option', 'modal', 'true');
			purgeDialog.dialog('open');
			$("#showCleanTypeMsgId").hide();
			$("#cleanProcessDivId").show();
		},
		success: function (data) {
			$("#cleanProcessDivId").html(data);
		},
		error: function (jqXHR, textStatus, errorThrown) {
			alert("An unexpected error occurred while opening Show/Clean type div. Please reload form to see if the problem persists.");
		}
	});
}

/*
 *this function is used to Clean unused asset and display appropriate message.
 */
function cleanTypes() {
	$("#cleanProcessId").show()
	jQuery.ajax({
		url: contextPath + '/admin/cleanAssetTypes',
		type: 'POST',
		success: function (data) {
			if (data) {
				$("#showCleanTypeMsgId").html(data)
				$("#showCleanTypeMsgId").show();
			} else {
				$("#showCleanTypeMsgId").html("No unreferenced asset types were found.")
				$("#showCleanTypeMsgId").show();
			}
			jQuery('#showOrCleanTypeId').dialog('close');
		},
		error: function (jqXHR, textStatus, errorThrown) {
			alert("An unexpected error occurred while opening Show/Clean type div. Please reload form to see if the problem persists")
		}
	})
}

/*
 * This function opens the form for the reconcile assets function
 */
function openReconcileAssetsForm() {
	jQuery.ajax({
		url: contextPath + '/admin/countAssetsOutOfSync',
		type: 'GET',
		success: function (text, b, data) {
			;
			$("#outOfSyncAssetCountId").html('Assets out of sync: ' + text);
			$("#reconcileAssetsFormId").css('display', 'block');
		},
		error: function (jqXHR, textStatus, errorThrown) {
			alert("An unexpected error occurred while opening Show/Clean type div. Please reload form to see if the problem persists")
		}
	})
}

/*
 * This function opens the form for the reconcile assets function
 */
function reconcileAssetTypes() {
	jQuery.ajax({
		url: contextPath + '/admin/reconcileAssetTypes',
		type: 'POST',
		success: function (text, b, data) {
			$("#outOfSyncAssetCountId").html('Assets out of sync: ' + text);
			$("#reconcileAssetsFormId").css('display', 'none');
			alert("Device assetTypes reconciled");
		},
		error: function (jqXHR, textStatus, errorThrown) {
			alert("You do not have permissions to use this function");
		}
	})
}

/*
 * This function displays the encrypt string form
 */
function openEncryptStringForm() {
	$("#encryptStringForm").css('display', 'block');
	$("#toEncryptString").val('');
	$("#encryptedString").val('');
}

/*
 * This function hide the encrypt string form
 */
function closeEncryptStringForm() {
	$("#encryptStringForm").css('display', 'none');
}

/*
 * This function send the value to encrypt
 */
function sendValueToEncrypt() {
	jQuery.ajax({
		url: contextPath + '/admin/encryptValue?toEncryptString=' + $("#toEncryptString").val() + '&encryptSalt=' + $("#encryptSalt").val() + '&encryptAlghoritm=' + $("#encryptAlghoritm").val(),
		type: 'POST',
		success: function (text, b, data) {
			$("#encryptedString").val(text);
		},
		error: function (jqXHR, textStatus, errorThrown) {
			alert("Can't encrypt value");
		}
	})
}

$(function () {
	purgeDialog = $("#showOrCleanTypeId").dialog({
		autoOpen: false,
		width: '550px',
		position: { at: 'center top' },
		modal: true,
		close: function () {
			location.reload();
		}
	});
	flushDialog = $("#flushOldBatchId").dialog({
		autoOpen: false,
		width: '550px',
		position: { at: 'center top' },
		modal: true,
		close: function () {
			location.reload();
		}
	});
});

