function processBatch(){
		var value=$('input:radio[name=deleteHistory]:checked').val()
		if(value=="doNothing"){
			 $("#flushOldBatchId").dialog('close');
		} else {
			jQuery.ajax({
				url: contextPath+'/admin/processOldData',
				data: {'deleteHistory':value},
				type:'POST',
				beforeSend:function(jqXHR){
					$('#processDivId').show(); 
					$("#respMsgId").hide();
					$("#processDivId").show()
				},
				success: function(data) {
					$("#processDivId").hide()
					$("#respMsgId").show().html(data)
				},
				error:function(jqXHR, textStatus, errorThrown){
					$("#processDivId").hide()
					$("#respMsgId").show().html("An unexpected error occurred. Please close and reload form to see if the problem persists")
				}
			})
		}
	}
	
	function openFlushDiv(){
		 jQuery.ajax({
			url: contextPath+'/admin/getBatchRecords',
			type:'POST',
			beforeSend:function(jqXHR){
				 $("#flushOldBatchId").dialog('option', 'width', '500px')
				 $("#flushOldBatchId").dialog('option', 'position', ['center','top']);
				 $("#flushOldBatchId").dialog('open');
				 $("#getRecordsInfoId").show()
				 $("#respMsgId").hide()
				 $("#processDivId").show()
			},
			success: function(data) {
				 $("#respMsgId").html(data)
				 $("#processDivId").hide()
				 $("#respMsgId").show()
			},
			error:function(jqXHR, textStatus, errorThrown){
				alert("An unexpected error occurred while opening Flush import div. Please reload form to see if the problem persists")
			}
		 })
	}
	
	/*
	 *this function is used to show the asset type table dialog
	 */
	function openShowTypeDiv(){
		$("#cleanProcessId").hide()
		$("#cleanProcessDivId").hide()
		 jQuery.ajax({
				url: contextPath+'/admin/getAssetTypes',
				type:'POST',
				beforeSend:function(jqXHR){
					$("#showOrCleanTypeId").dialog('option', 'width', '500px')
				    $("#showOrCleanTypeId").dialog('option', 'position', ['center','top']);
					$("#showOrCleanTypeId").dialog('open')
					$("#showCleanTypeMsgId").hide()
					$("#cleanProcessDivId").show()
				},
				success: function(data) {
					$("#cleanProcessDivId").html(data)
				},
				error:function(jqXHR, textStatus, errorThrown){
							alert("An unexpected error occurred while opening Show/Clean type div. Please reload form to see if the problem persists")
				}
		   })
	 }
	/*
	 *this function is used to Clean unused asset and display appropriate message.
	 */
	 function cleanTypes(){
		 $("#cleanProcessId").show()
		 jQuery.ajax({
				url: contextPath+'/admin/cleanAssetTypes',
				type:'POST',
				success: function(data) {	
					if(data){
						$("#showCleanTypeMsgId").html(data)
						$("#showCleanTypeMsgId").show();
					} else {
						$("#showCleanTypeMsgId").html("No Unused asset type found")
						$("#showCleanTypeMsgId").show();
				   }
					jQuery('#showOrCleanTypeId').dialog('close')
				},
				error:function(jqXHR, textStatus, errorThrown){
							alert("An unexpected error occurred while opening Show/Clean type div. Please reload form to see if the problem persists")
				}
		})
	  }
	 
