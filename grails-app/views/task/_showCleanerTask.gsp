	<g:javascript src="tech_teams.js" />
	<g:javascript src="asset.comment.js" />
	<div class="mainbody" id="mainbody">
	 	<div id="mydiv" onclick="this.style.display = 'none';">
 			<g:if test="${flash.message}">
				<div style="color: red; font-size:15px"><ul>${flash.message}</ul></div>
			</g:if> 
		</div>
		<g:if test="${canPrint}">
			<div class="printViewError">
				<ul>
					<li>
						Please note that in order to print barcode labels you will need to use the
						<strong><a href="https://qz.io/download/" target="_blank">QZ Tray 2.0</a></strong>
						that can be downloaded from
						<a href="https://qz.io/download/" target="_blank">https://qz.io/download/</a>
					</li>
				</ul>
			</div>
		</g:if>
	<g:form name="issueUpdateForm" controller="task" action="update">
		<a name="comments"></a>
		<span id="opened-detail-${assetComment.id}" style="display: none;"></span>
		<input type="hidden" name="id" id="issueId" value="${assetComment.id}" />
		<input type="hidden" name="redirectTo" id="redirectTo" value="taskList" />
		<input type="hidden" name="FormName" id="FormName" />
		<input type="hidden" name="PrinterName" id="PrinterName" />
		<input type="hidden" name="RepPath" id="RepPath" />
		<input type="hidden" name="model" id="model" value="${assetEntity?.model}" />
		<input type="hidden" name="cart" id="cart" value="${assetEntity?.cart}" />
		<input type="hidden" name="shelf" id="shelf" value="${assetEntity?.shelf}" />
		<input type="hidden" name="room" id="room" value="${assetEntity?.targetRoom}" />
		<input type="hidden" name="rack" id="rack" value="${assetEntity?.targetRack}" />
		<input type="hidden" name="upos" id="upos" value="${assetEntity?.targetRackPosition}" />
		<input type="hidden" name="cartQty" id="cartQty" value="${cartQty}" />
		<table style="margin-left: -2px;">
			<tr>
				<td class="heading" colspan=4><a class="heading" href="#comments">Task details:</a></td>
			</tr>
			<tr>
				<td colspan="4">
			</td>
			</tr>		
			<tr>	
				<td valign="top" class="name"><label for="comment">Task:</label></td>
				<td colspan="3">
				  <input type="text"  title="Edit Comment..." id="editComment_${assetComment.id}" name="comment" value="${assetComment.comment}" style="width: 500px"/>
				</td>
			</tr>	
			<tr>
				<td valign="middle" class="name"><label>Dependencies:</label></td>
				<td valign="top" class="name" colspan="3"><label>Predecessors:</label>&nbsp;&nbsp;
				<span style="width: 50%">
							<g:each in="${assetComment.taskDependencies}" var="task">
							<span class="${task.predecessor?.status ? 'task_'+task.predecessor?.status?.toLowerCase() : 'task_na'}" onclick="showAssetComment(${task.predecessor.id})">
								${task.predecessor.category}&nbsp;&nbsp;&nbsp;&nbsp;${task.predecessor}
							</span>
							</g:each>&nbsp;&nbsp;
				</span>
				<label>Successors:</label>
				<span  style="width: 50%">
							<g:each in="${successor}" var="task">
							<span class="${task.assetComment?.status ? 'task_'+task.assetComment?.status?.toLowerCase() : 'task_na'}" onclick="showAssetComment(${task.assetComment.id})">
								${task.assetComment.category}&nbsp;&nbsp;&nbsp;&nbsp;${task.assetComment}
							</span>
							</g:each>
				</span>
				</td>
			</tr>
		 	
			<tr class="prop issue" id="assignedToTrEditId" >
				<td valign="top" class="name"><label for="assignedTo">Assigned:</label></td>
				<td valign="top" id="assignedToEditTdId" style="width: 20%;" colspan="3" >
					<g:select id="assignedToEditId_${assetComment.id}" name="assignedTo" from="${projectStaff}" value="${assetComment.assignedTo?.id}" optionKey="id" noSelection="['':'please select']"></g:select>
				</td>
			</tr> 
			<tr class="prop issue" id="estFinishShowId"  >
				<td valign="top" class="name"><label for="estFinish">Est.Finish:</label></td>
				<td valign="top" class="value" id="estFinishShowId_${assetComment.id}" colspan="3" nowrap="nowrap">
				<tds:convertDate date="${assetComment.estFinish}" format="M/d kk:mm" />
				</td>
			</tr>
			<tr class="prop">
				<td valign="top" class="name"><label for="category">Category:</label></td>
				<td valign="top" class="value" colspan="3"><g:select id="categoryEditId_${assetComment.id}" name="category" from="${com.tds.asset.AssetComment.constraints.category.inList}" value="${assetComment.category}"></g:select>
				<g:if test="${assetComment.moveEvent}">
		   		  <span style="margin-left:60px;">Move Event:</span>
		   		  <span style="margin-left:10px;">${assetComment?.moveEvent.name}</span>
		   		</g:if>
				</td>
				
			</tr>
			<tr>
			<g:if test="${assetComment.assetEntity}">
		   		  <td>Asset:</td><td style="width: 1%">&nbsp;${assetComment?.assetEntity.assetName}</td>
		   		</g:if>
		   	</tr>
		   	<tr class="prop">
				<td valign="top" class="name"><label for="createdBy">Created By:</label></td>
				<td valign="top" class="value" colspan="3"><span id="categoryEditId">${assetComment?.createdBy} on 
				<tds:convertDate date="${assetComment?.dateCreated}" format="M/d" /></span></td>
			</tr>
			<tr class="prop" >
				<td valign="top" class="name">
					<label for="status">Status:</label>
					<input id="currentStatus_${assetComment.id}" name="currentStatus" type="hidden" value="${assetComment.status}" />
				</td>
				<td style="width: 20%;" id="statusEditTrId_${assetComment.id}" colspan="3">
					<g:if test="${statusWarn==1}">
						<g:select id="statusEditId_${assetComment.id}" name="status" from="${com.tds.asset.AssetComment.constraints.status.inList}" value="${assetComment.status}"
						noSelection="['':'please select']"  onChange="showResolve()" disabled="true"></g:select>
					</g:if>
					<g:else>
						<g:select id="statusEditId_${assetComment.id}" name="status" from="${com.tds.asset.AssetComment.constraints.status.inList}" value="${assetComment.status}"
						noSelection="['':'please select']"  onChange="showResolve()"></g:select>
					</g:else>
				</td>	
			</tr>				
			 <tr class="prop">
				<td valign="top" class="name"><label for="notes">Previous Notes:</label></td>
				<td valign="top" class="value" colspan="3"><div id="previousNote">
				 <table style="table-layout: fixed; width: 100%;border: 1px solid green;" >
                   <g:each in="${notes}" var="note" status="i" >
                    <tr>
	                    <td>${note[0]}</td>
	                    <td>${note[1]}</td>
                        <td style="word-wrap: break-word">${note[2]}</td>
                     </tr>
                   </g:each>
				 </table>
				</div></td>
			</tr>
		    <tr class="prop" id="noteId_${assetComment.id}">
				<td valign="top" class="name"><label for="notes">Note:</label></td>
				<td valign="top" class="value" colspan="3">
				   <textarea cols="80" rows="4" id="noteEditId_${assetComment.id}" name="note" style="width:100%;padding:0px;"></textarea>
				</td>
			</tr>
			<tr class="prop" id="resolutionId_${assetComment.id}" style="display: none;">
				<td valign="top" class="name"><label for="resolution">Resolution:</label></td>
				<td valign="top" class="value" colspan="3">
					<textarea cols="100" rows="4" style="width:100%;padding:0px;" id="resolutionEditId_${assetComment.id}" name="resolution" >${assetComment.resolution}</textarea>
				</td>
			</tr> 
			<g:if test="${assetComment.resolvedBy}">
				<tr class="prop">
					<td valign="top" class="name"><label for="resolution">Resolved By:</label></td>
					<td valign="top" class="value">
						<span id="resolvedByTd" >${assetComment.resolvedBy} on <tds:convertDate date="${assetComment?.dateResolved}" format="M/d" /></span>
					</td>
				</tr> 
			</g:if>
			<tr class="prop printView">
					<td valign="top" class="name"><label for="resolution">Label Settings:</label></td>
					<td nowrap="nowrap" colspan="3">
						<select id="printers" ${canPrint ? '' : 'disabled="disabled"' }>
						</select> 
						<b>Quantity: </b>
						<select id="printTimes" ${canPrint ? '' : 'disabled="disabled"' }>
							<g:each in="${1..9}">
								<option value="${it}">${it}</option>
							</g:each>
						</select>
					</td>
			</tr>
			<tr>
			    <td class="buttonR" >
					<g:if test="${permissionForUpdate==true}">
						<button type="button" class="btn btn-default" onclick="validateComment(${assetComment.id})" ><span class="glyphicon glyphicon-ok" aria-hidden="true"></span> Update Task</button>
					</g:if>
					<g:else>
						<button type="button" class="btn btn-default" disabled="disabled" ><span class="glyphicon glyphicon-ok" aria-hidden="true"></span> Update Task</button>
					</g:else>
				</td>
				<td class="buttonR printView" style="text-align:right;padding: 5px 3px;">
					<button type="button" class="btn btn-default"  id="printButton" ${canPrint ? '': 'disabled="disabled"' } onclick="startprintjob();"><span class="glyphicon glyphicon-print" aria-hidden="true"></span> P)rint</button>
					<g:if test="${assetComment.status == "Started" || assetComment.status == "Ready"}">
						<button type="button" class="btn btn-default"  id="printAndDoneButton" onclick="printAndMarkDone(${assetComment.id}, 'Completed', '${assetComment.status}');"><span class="glyphicon glyphicon-print" aria-hidden="true"></span> Print And D)one</button>
					</g:if>
					<g:else>
						<button type="button" class="btn btn-default"  id="printAndDoneButton" disabled="disabled" ><span class="glyphicon glyphicon-print" aria-hidden="true"></span> Print And D)one</button>
					</g:else>
				</td>
				<td class="buttonR" colspan="2" style="text-align:right;padding: 5px 3px;">
					<button type="button" class="btn btn-default" onclick="clearSearch()" ><span class="glyphicon glyphicon-ban-circle" aria-hidden="true"></span> Cancel</button>
				</td>
			</tr>	
		</table>
		 <input type="hidden" name ="assetName" id="assetName" value="${assetComment?.assetEntity?.assetName}">
		 <input type="hidden" name ="assetTag" id="assetTag" value="${assetComment?.assetEntity?.assetTag}">
</g:form>

	<div class="clear" style="margin:4px;"></div>
		<a name="detail" ></a>
		<g:if test="${assetComment?.assetEntity}">
		 	<div style="float: left;width: 100%">
				<table style="float: left;margin-left: -2px;">
				<tr>
					<td class="heading"><a href="#detail">${assetComment?.assetEntity?.assetType} Details</a></td>
					<td><span style="float:right;"><a href="#top">Top</a></span></td>
				</tr>
				<tr><td colspan=2>
				<dl>
	               <g:if test="${assetComment?.assetEntity?.assetType=='Application'}">
		                <dt>Application Name:</dt><dd>&nbsp;${assetComment?.assetEntity.assetName}</dd>
						<dt>Validation:</dt><dd>&nbsp;${assetComment?.assetEntity.validation}</dd>
						<dt>Plan Status:</dt><dd>&nbsp;${assetComment?.assetEntity.planStatus}</dd>
						<dt>Bundle:</dt><dd>&nbsp;${assetComment?.assetEntity.moveBundle}</dd>
	               </g:if>
	                <g:elseif test="${assetComment?.assetEntity?.assetType=='Database'}">
	                    <dt>Database Name:</dt><dd>&nbsp;${assetComment?.assetEntity.assetName}</dd>
						<dt>DB Size:</dt><dd>&nbsp;${assetComment?.assetEntity.assetName}</dd>
						<dt>DB Format:</dt><dd>&nbsp;${assetComment?.assetEntity.dbFormat}</dd>
						<dt>Bundle:</dt><dd>&nbsp;${assetComment?.assetEntity.moveBundle}</dd>
	                </g:elseif>
	                <g:elseif test="${assetComment?.assetEntity?.assetType=='Files'}">
	                    <dt>Storage Name:</dt><dd>&nbsp;${assetComment?.assetEntity.assetName}</dd>
						<dt>Storage Size:</dt><dd>&nbsp;${assetComment?.assetEntity.size}</dd>
						<dt>Storage Format:</dt><dd>&nbsp;${assetComment?.assetEntity.fileFormat}</dd>
						<dt>Bundle:</dt><dd>&nbsp;${assetComment?.assetEntity.moveBundle}</dd>
	                </g:elseif>
	                <g:else>
						<dt>Asset Tag:</dt><dd>&nbsp;${assetComment?.assetEntity?.assetTag}</dd>
						<dt>Asset Name:</dt><dd>&nbsp;${assetComment?.assetEntity?.assetName}</dd>
						<dt>Model:</dt><dd>&nbsp;${assetComment?.assetEntity?.model}</dd>
						<dt>Serial #:</dt><dd>&nbsp;${assetComment?.assetEntity?.serialNumber}</dd>
						<dt>Current Loc/Pos:</dt><dd>&nbsp;${assetComment?.assetEntity.sourceRack}/${assetComment?.assetEntity.sourceRackPosition}</dd>
					  	<dt>Target Loc/Pos:</dt><dd>&nbsp;${assetComment?.assetEntity.targetRack}/${assetComment?.assetEntity.targetRackPosition}</dd>
						<dt>Source Room:</dt><dd>&nbsp;${assetComment?.assetEntity.sourceRoom}</dd>
						<dt>Target Room:</dt><dd>&nbsp;${assetComment?.assetEntity.targetRoom}</dd>
						<g:if test="${location == 'source'}">			   	
					   		<dt>Plan Status:</dt><dd>&nbsp;${assetComment?.assetEntity.planStatus}</dd>
							<dt>Rail Type:</dt><dd>&nbsp;${assetComment?.assetEntity.railType}</dd>  			   	
						</g:if>
						<g:else>				
					   		<dt>Truck:</dt><dd>&nbsp;${assetComment?.assetEntity.truck}</dd>
					   		<dt>Cart/Shelf:</dt><dd>&nbsp;${assetComment?.assetEntity.cart}/${assetComment?.assetEntity.shelf}</dd>
					   		<dt>Plan Status:</dt><dd>&nbsp;${assetComment?.assetEntity.planStatus}</dd>
							<dt>Rail Type:</dt><dd>&nbsp;${assetComment?.assetEntity.railType}</dd>  			   	
						</g:else>
					</g:else>
				</dl>
			</tr>
			</table>
			</div>
		</g:if>
</div>
<script type="text/javascript">
	$(function() {
		if ($('#statusEditId_'+${assetComment.id}).val()=='Completed') {
			$('#noteId_'+${assetComment.id}).hide();
			$('#resolutionId_'+${assetComment.id}).show();
		}
		document.onkeyup = keyCheck;
	});

	function showResolve() {
		if ($('#statusEditId_'+${assetComment.id}).val()=='Completed') {
			$('#noteId_'+${assetComment.id}).hide()
			$('#resolutionId_'+${assetComment.id}).show()
		} else {
			$('#noteId_'+${assetComment.id}).show()
			$('#resolutionId_'+${assetComment.id}).hide()
		}
	}

	function keyCheck( e ) {
		var keyStrokesFlag = $('.keyStrokesHandler').attr('status')
		if(keyStrokesFlag && keyStrokesFlag === 'enable' ) {
			var currentFocus = document.activeElement.id;
			var focusId = currentFocus.substring(0,currentFocus.indexOf("_"));
			if (currentFocus != 'search' && focusId != 'editComment' && focusId != 'noteEditId' ) {
				if (!e && window.event) {
				    e=window.event;
                }
				var keyID = e.keyCode;

				<g:if test="${assetComment.status == "Started" || assetComment.status == "Ready"}">
					if (keyID == KeyEvent.DOM_VK_RETURN || keyID == KeyEvent.DOM_VK_D || keyID == KeyEvent.DOM_VK_NUMPAD4) {
						$("#printAndDoneButton").click()
						return;
					} else if (keyID == KeyEvent.DOM_VK_P) {
						startprintjob();
					}
				</g:if>

				// Input is a numeric value from normal keyboard or extended numeric pad
				if((keyID >= KeyEvent.DOM_VK_0 && keyID <= KeyEvent.DOM_VK_9) || (keyID >= KeyEvent.DOM_VK_NUMPAD0 && keyID <= KeyEvent.DOM_VK_NUMPAD9)){

					var labelQty = 0;
					if(keyID >= KeyEvent.DOM_VK_0 && keyID <= KeyEvent.DOM_VK_9) {
						labelQty = keyID - KeyEvent.DOM_VK_0;
					}

					if(keyID >= KeyEvent.DOM_VK_NUMPAD0 && keyID <= KeyEvent.DOM_VK_NUMPAD9) {
						labelQty = keyID - KeyEvent.DOM_VK_NUMPAD0;
					}

					if (labelQty > 0 && labelQty < 10) {
						var printTimesSelector = $('#printTimes');
						printTimesSelector.focus();

						if ($("input:focus").length < 1) {
							printTimesSelector.val(labelQty);
							printTimesSelector.change();
						}
					}
				}

				//Increment or decrement pages using '-' or '+' keys
				if(keyID == KeyEvent.DOM_VK_PLUS || keyID == KeyEvent.DOM_VK_HYPHEN_MINUS){
					var inc = (keyID == KeyEvent.DOM_VK_PLUS)? 1 : -1;
					var printTimesSelector = $('#printTimes');
					printTimesSelector.focus();
					if ($("input:focus").length < 1) {
						var value =  parseInt(printTimesSelector.val());
						value += inc;

						if(value > 0 && value < 10) {
							printTimesSelector.val(value);
							printTimesSelector.change();
						}
					}
				}

				// On Press C,c or Q, q  or ESC hide the details row
				if(keyID == KeyEvent.DOM_VK_C || keyID == KeyEvent.DOM_VK_NUMPAD3 || keyID == KeyEvent.DOM_VK_Q || keyID == KeyEvent.DOM_VK_F2 || keyID ==  KeyEvent.DOM_VK_ESCAPE) {
					//$('.taskDetailsRow').hide();
					//$('#search').focus();
					clearSearch();
				}
			}
		}
	}

	function printAndMarkDone(id, status, currentStatus) {
		startprintjob({
			onSuccess: function(){
				jQuery.ajax({
					url: '../task/update',
					data: {'id':id,'status':status,'currentStatus':currentStatus,view:'myTask'},
					type:'POST',
					success: function(data) {
						if (typeof data.error !== 'undefined') {
							alert(data.error);
						} else {
							$("#myIssueList").replaceWith(data);
							$("#search").focus();
						}
					},
					error: function(jqXHR, textStatus, errorThrown) {
						alert("An unexpected error occurred while attempting to update task/comment")
					}
				});
			}
		});
	}
 
	function validateComment(objId){
		var status = $('#statusEditId_'+${assetComment.id}).val()
		var params = {
			'comment':$('#editComment_'+objId).val(),
			'resolution':$('#resolutionEditId_'+objId).val(),
			'category':$('#categoryEditId_'+objId).val(),
			'assignedTo':$('#assignedToEditId_'+objId).val(),
			'status':$('#statusEditId_'+objId).val(),
			'currentStatus':$('#currentStatus_'+objId).val(),
			'note':$('#noteEditId_'+objId).val(),
			'id':objId,'view':'myTask',
			'tab': $('#tabId').val()
		};

		jQuery.ajax({
			url: '../task/update',
			data: params,
			type:'POST',
			success: function(data) {
				if (typeof data.error !== 'undefined') {
				} else {
					$('#myTaskList').html(data);
					$('#showStatusId_'+objId).show();
					$('#issueTrId_'+objId).each(function(){
						$(this).removeAttr('onclick');
						$(this).unbind("click").bind("click", function(){
							hideStatus(objId,status);
						});
					});

					if (status=='Started') {
						$('#started_'+objId).hide();
					}

					timerBar.Restart();
				}
			},
			error: function(jqXHR, textStatus, errorThrown) {
				alert("An unexpected error occurred while attempting to update task/comment")
			}
		});
	}

	function truncate( text ){
		var trunc = text;
		if(text){
			if(text.length > 50){
				trunc = trunc.substring(0, 50);
				trunc += '...';
			}
		}
		return trunc;
	}

	function hideStatus(id,status){
		$('#showStatusId_'+id).hide();
		$('#detailTdId_'+id).css('display','none');
		timerBar.Start();
	}
 </script>

<script type="text/javascript" language="javascript">
//=============================================================================
// PRINT HERE
//=============================================================================
	window.PREFS.PRINTER_COPIES = '${lblQty}';
	window.PREFS.PRINTER_NAME = '${prefPrinter}';

	function startprintjob(opts) {
		var printerName = $('#printers').val();
		var printerCopies = $("#printTimes").val();

		//Save Printer Preference
		window.PREFS.PRINTER_NAME   = printerName;
		window.PREFS.PRINTER_COPIES = parseInt(printerCopies);

		jQuery.ajax({
				url: "${createLink(controller:'task', action: 'setLabelQuantityPref')}",
				data: JSON.stringify({
					"PRINTER_NAME":printerName,
					"PRINT_LABEL_QUANTITY":printerCopies
				}),
				type: 'POST',
				contentType: "application/json; charset=utf-8",
				dataType: 'json'
		});

		opts = opts || {};
		var NOP = function(){};
		var onSuccess = opts.onSuccess || NOP;
		var onFail = opts.onFail || NOP;
		var onDone = opts.onDone || NOP;
	    
		window.QZObj.print({
				printerName: printerName,
				data:{
					assetName : $("#assetName").val(),
					assetTag  : $("#assetTag").val(),
					modelName : $("#model").val(),
					cart      : $("#cart").val(),
					shelf     : $("#shelf").val(),
					roomTarget: $("#room").val(),
					rackTarget: $("#rack").val(),
					targetRackPosition: $("#upos").val(),
					printTimes: printerCopies
				},
				onSuccess:function(qz){
					window.NOTIFICATION.show({
						title: String(qz.getPrinter()),
						message: 'Labels sent to printer'
					}, "success");
		    
					onSuccess(qz);
				},
				onFail: function(error, qz){
					window.NOTIFICATION.show({
						title: "Error",
						message: error
					}, "error");
					onFail(error, qz);
				},
				onDone : onDone
		});
	}

//=============================================================================
// Add a new option to select element
//=============================================================================
	function AddOption (selElement, text, value){
	    opt = new Option(text, value, false, true);
		// selElement.options[0] = opt;
	}

//=============================================================================
// Set default data for TFORMer Runtime Properties
//=============================================================================
	function InitData(){
		// Nothing here yet
	}

	function focusOnPrintAndDone(){
		// To check the Instructions for enable the Clean Button
		if(!$('#printAndDoneButton').is(':disabled')){
			//push to the bottom of the event queue
			setTimeout(function() {
				$('#printAndDoneButton').focus();
			}, 0);
		}
	}
</script>
<script type="text/javascript">
	currentMenuId = "#teamMenuId";
	$("#teamMenuId a").css('background-color','#003366');
	
	if(window.QZObj && window.QZObj.isActive()){
		var prefPrinterCopies = window.PREFS.PRINTER_COPIES;

		$(".printView").show();

		$('#printTimes').val(prefPrinterCopies);
		if(window.QZObj.loadPrinters) {
			window.QZObj.loadPrinters();
		}
	}else{
		console.log("ShowError");
		$(".printViewError").show();
	}

</script>
