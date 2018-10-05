<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="topNav" />
<title>Event News</title>

<asset:stylesheet href="css/jquery.autocomplete.css" />
<asset:stylesheet href="css/ui.accordion.css" />
<asset:stylesheet href="css/ui.resizable.css" />
<asset:stylesheet href="css/ui.slider.css" />
<asset:stylesheet href="css/ui.tabs.css" />
<asset:stylesheet href="css/jqgrid/ui.jqgrid.css" />
<jqgrid:resources />

	<style>
		/*TODO: REMOVE ON COMPLETE MIGRATION */
		div.content-wrapper {
			background-color: #ecf0f5 !important;
		}
	</style>

<script type="text/javascript">
function onInvokeAction(id) {
    setExportToLimit(id, '');
    createHiddenInputFieldsForLimitAndSubmit(id);
}
</script>

<script type="text/javascript">
$(document).ready(function() {
    $("#createNewsDialog").dialog({ autoOpen: false })
    $("#showEditCommentDialog").dialog({ autoOpen: false })
	$("#showEditCommentDialog").dialog('option', 'width', 'auto');
	$("#showEditCommentDialog").dialog('option', 'position', ['center','top']);
	$("#showEditCommentDialog").dialog('option', 'modal', 'true');
});

 function getCommentDetails(id,type){

	 jQuery.ajax({
			url: contextPath+"/newsEditor/retrieveCommetOrNewsData",
			data: {'id':id , 'commentType':type},
			type:'POST',
			success: function(data) {
				showEditCommentForm( data, id)
			}
		});
	 }

function showEditCommentForm(e , rowId){
	var assetComments = e
	if (assetComments) {
		var tbody = $("#commetAndNewsBodyId > tr");
		tbody.each(function(n, row){
			if(n == rowId) {
		    	$(row).addClass('selectedRow');
		    } else {
		    	$(row).removeClass('selectedRow');
		    }
     	});

		$('#commentId').val(assetComments[0].commentObject.id)
		$('#assetTdId').val(assetComments[0].assetName)
		$('#dateCreatedId').html(assetComments[0].dtCreated);
		if(assetComments[0].personResolvedObj != null){
			$('#resolvedById').html(assetComments[0].personResolvedObj);
		}else{
			$('#resolvedById').html("");
			$('#resolvedByEditId').html("");
		}
		$('#createdById').html(assetComments[0].personCreateObj);
		$('#resolutionId').val(assetComments[0].commentObject.resolution);

		if(assetComments[0].commentObject.commentType != 'issue'){

			$('#commentTypeId').val("news")
			$('#dateResolvedId').html(assetComments[0].dtResolved);
			$('#isResolvedId').val(assetComments[0].commentObject.isArchived)
			$('#commentTdId').val(assetComments[0].commentObject.message)
			if(assetComments[0].commentObject.isArchived != 0){
				$('#isResolvedId').attr('checked', true);
				$("#isResolvedHiddenId").val(1);
			} else {
				$('#isResolvedId').attr('checked', false);
				$("#isResolvedHiddenId").val(0);
			}
			$("#displayOptionTr").hide();
			$("#commentTypeOption").html("<option>News</option>");
			$("#assetTrId").hide();
			$("#showEditCommentDialog").dialog('option','title','Edit News');

		} else {

			$('#commentTypeId').val("issue")
			$('#dateResolvedId').html(assetComments[0].dtResolved);
			$('#isResolvedId').val(assetComments[0].commentObject.isResolved)
			$('#commentTdId').val(assetComments[0].commentObject.comment)
			if(assetComments[0].commentObject.isResolved != 0){
				$('#isResolvedId').attr('checked', true);
				$("#isResolvedHiddenId").val(1);
			} else {
				$('#isResolvedId').attr('checked', false);
				$("#isResolvedHiddenId").val(0);
			}
			if(assetComments[0].commentObject.displayOption == "G"){
				$("#displayOptionGid").attr('checked', true);
			} else {
				$("#displayOptionUid").attr('checked', true);
			}
			$("#displayOptionTr").show();
			$("#commentTypeOption").html("<option>Issue</option>");
			$("#assetTrId").show();
			$("#showEditCommentDialog").dialog('option','title','Edit Issues Comment');

		}
			$("#showEditCommentDialog").dialog( "option", "modal", true );
			$("#showEditCommentDialog").dialog("open");
			$("#createNewsDialog").dialog("close");
		}
}

function validateNewsAndCommentForm(){
	var resolveBoo = $("#isResolvedId").is(':checked');
	var resolveVal = $("#resolutionId").val();
	if(resolveBoo && resolveVal == ""){
		alert('Please enter Resolution');
		return false;
	} else {
		return true;
	}
}
function updateHidden(checkBoxId,hiddenId){
	var resolve = $("#"+checkBoxId).is(':checked');
	if(resolve){
		$("#"+hiddenId).val(1);
	} else {
		$("#"+hiddenId).val(0);
	}
}
function openCreateNewsDialog(){
	$("#createNewsDialog").dialog('option', 'width', 'auto');
	$("#createNewsDialog").dialog('option', 'position', ['center','top']);
	$('#createNewsDialog').dialog( "option", "modal", true );
	$('#createNewsDialog').dialog('open');
}
function validateCreateNewsForm(){
	var moveEvent = $("#moveEventId").val();
	var resolveBoo = $("#isArchivedId").is(':checked');
	var resolveVal = $("#resolutionNewsId").val();

	if(moveEvent){
		if(resolveBoo && resolveVal == ""){
			alert('Please enter Resolution');
			return false;
		} else {
			return true;
		}
	} else{
		alert("Please Assign MoveEvent to Current Bundle")
		return false;
	}
}


$('#assetMenu').hide();
$('#bundleMenu').hide();
$('#consoleMenu').show();
$('#reportsMenu').hide();

</script>
</head>
<body>
<tds:subHeader title="Event News" crumbs="['Planning','News and Issues']"/>
<!-- Main content -->
<section>
	<div>
		<div class="box-body">
			<div class="box box-primary">
				<div class="box-header with-border">
					<g:form action="newsEditorList" name="newsEditorForm" method="get" class="form-inline">
						<div class="form-group">
							<label for="moveEvent"><b>Event:</b></label>&nbsp;
							<select id="moveEvent" name="moveEvent" class="form-control" onchange="$('#newsEditorForm').submit();">
								<g:each status="i" in="${moveEventsList}" var="moveEventInstance">
									<option value="${moveEventInstance?.id}">${moveEventInstance?.name}</option>
								</g:each>
							</select>
						</div>
						<div class="form-group">
							<label for="moveBundleId"><b>Bundle:</b></label>&nbsp;
							<select id="moveBundleId" name="moveBundle" class="form-control" onchange="$('#newsEditorForm').submit();">
								<option value="">All</option>
								<g:each status="i" in="${moveBundlesList}" var="moveBundleInstance">
									<option value="${moveBundleInstance?.id}">${moveBundleInstance?.name}</option>
								</g:each>
							</select>
						</div>
						<div class="form-group">
							<label for="viewFilterId"><b>View:</b></label>&nbsp;
							<select id="viewFilterId" name="viewFilter" class="form-control" onchange="$('#newsEditorForm').submit();">
								<option value="all">All</option>
								<option value="active">Active</option>
								<option value="archived">Archived</option>
							</select>
						</div>
					</g:form>
				</div><!-- /.box-header -->
				<div>
					<div id="newsAndIssuesList"></div>
				</div>
				<div id="showEditCommentDialog" title="Edit Issue Comment" style="display: none;" class="static-dialog">
					<g:form action="updateNewsOrComment" method="post" name="editCommentForm">
						<div class="dialog" style="border: 1px solid #5F9FCF">
							<input name="id" value="" id="commentId" type="hidden"/>
							<input name="commentType" value="" id="commentTypeId" type="hidden"/>
							<input name="projectId" value="${projectId}" type="hidden"/>
							<input name="moveBundle" value="${params.moveBundle}" type="hidden"/>
							<input name="moveEvent" value="${params.moveEvent}" type="hidden"/>
							<input name="viewFilter" value="${params.viewFilter}" type="hidden"/>
							<div>
								<table id="showCommentTable" style="border: 0px">

									<tr>
										<td valign="top" class="name"><label for="dateCreated">Created At:</label></td>
										<td valign="top" class="value" id="dateCreatedId" ></td>
									</tr>
									<tr>
										<td valign="top" class="name"><label for="createdBy">Created By:</label></td>
										<td valign="top" class="value" id="createdById" ></td>
									</tr>
									<tr>
										<td valign="top" class="name"><label>Comment Type:</label></td>
										<td valign="top" class="value" >
											<select disabled="disabled" id="commentTypeOption">
												<option>Issue</option>
											</select>
										</td>
									</tr>
									<tr id="displayOptionTr">
										<td valign="top" class="name" nowrap="nowrap"><label for="category">User / Generic Cmt:</label></td>
										<td valign="top" class="value" id="displayOption" >
											<input type="radio" name="displayOption" value="U" checked="checked" id="displayOptionUid"/>&nbsp;
											<span style="vertical-align: text-top;">User Comment</span>&nbsp;&nbsp;&nbsp;
											<input type="radio" name="displayOption" value="G" id="displayOptionGid"/>&nbsp;
											<span style="vertical-align:text-top;">Generic Comment&nbsp;</span>
										</td>
									</tr>
									<tr class="prop" id="assetTrId">
										<td valign="top" class="name"><label for="assetTdId">Asset:</label></td>
										<td valign="top" class="value"><input type="text" disabled="disabled" id="assetTdId"/></td>
									</tr>
									<tr class="prop">
										<td valign="top" class="name"><label for="comment">Comment:</label></td>
										<td valign="top" class="value" ><textarea cols="80" rows="5" id="commentTdId" name="comment" onkeyup="textCounter(this.id,255)" onkeydown="textCounter(this.id,255)"></textarea> </td>
									</tr>
									<tr class="prop">
										<td valign="top" class="name" nowrap="nowrap"><label for="isResolved" >Resolved / Archived:</label></td>
										<td valign="top" class="value" id="resolveTdId">
											<input type="checkbox" id="isResolvedId" value="0" onclick="updateHidden('isResolvedId','isResolvedHiddenId')"/>
											<input type="hidden" name="isResolved" value="0" id="isResolvedHiddenId"/>
										</td>
									</tr>
									<tr class="prop">
										<td valign="top" class="name"><label for="resolution">Resolution:</label></td>
										<td valign="top" class="value" ><textarea cols="80" rows="5" id="resolutionId" name="resolution" onkeyup="textCounter(this.id,255)" onkeydown="textCounter(this.id,255)"></textarea> </td>
									</tr>
									<tr>
										<td valign="top" class="name"><label for="dateResolved">Resolved At:</label></td>
										<td valign="top" class="value" id="dateResolvedId" ></td>
									</tr>
									<tr>
										<td valign="top" class="name"><label for="resolvedBy">Resolved By:</label></td>
										<td valign="top" class="value" id="resolvedById" ></td>
									</tr>
								</table>
							</div>
							<div class="buttons">
								<span class="button">
									<input class="save" type="submit" value="Update" onclick="return validateNewsAndCommentForm()"/>
								</span>
								<span class="button">
									<input class="delete" type="button" value="Cancel" onclick="this.form.reset();$('#showEditCommentDialog').dialog('close');"/>
								</span>
							</div>
						</div>
					</g:form>
				</div>
				<div id="createNewsDialog" title="Create News" style="display: none;" class="static-dialog">
					<g:form action="saveNews" method="post" name="createNewsForm" id="createNewsForm">
						<input name="projectId" value="${projectId}" type="hidden"/>
						<input name="moveBundle" value="${params.moveBundle}" type="hidden"/>
						<input name="viewFilter" value="${params.viewFilter}" type="hidden"/>
						<input name="moveEvent.id" value="${moveEventId}" type="hidden" id="moveEventId"/>
						<div class="dialog" style="border: 1px solid #5F9FCF">
							<table id="createCommentTable" style="border: 0px">
								<tr>
									<td colspan="2"><div class="required"> Fields marked ( * ) are mandatory </div> </td>
								</tr>
								<tr>
									<td valign="top" class="name"><label>Comment Type:</label></td>
									<td valign="top" class="value" >
										<select disabled="disabled">
											<option>News</option>
										</select>
									</td>
								</tr>
								<tr class="prop">
									<td valign="top" class="name"><label for="messageId"><b>Comment:&nbsp;<span style="color: red">*</span></b></label></td>
									<td valign="top" class="value"><textarea cols="80" rows="5"id="messageId" name="message" onkeyup="textCounter(this.id,255)" onkeydown="textCounter(this.id,255)"></textarea></td>
								</tr>
								<tr class="prop">
									<td valign="top" class="name" nowrap="nowrap"><label for="isArchivedId" >Resolved / Archived:</label></td>
									<td valign="top" class="value" id="archivedTdId">
										<input type="checkbox" id="isArchivedId" value="0" onclick="updateHidden('isArchivedId','isArchivedHiddenId')"/>
										<input type="hidden" name="isArchived" value="0" id="isArchivedHiddenId"/>
									</td>
								</tr>
								<tr class="prop">
									<td valign="top" class="name"><label for="resolutionNewsId">Resolution:</label></td>
									<td valign="top" class="value" ><textarea cols="80" rows="5"
										id="resolutionNewsId" name="resolution" onkeyup="textCounter(this.id,255)" onkeydown="textCounter(this.id,255)"></textarea> </td>
								</tr>
							</table>
						</div>
						<div class="buttons">
							<span class="button">
								<input class="save" type="submit" value="Save" onclick="return validateCreateNewsForm()"/>
							</span>
							<span class="button">
								<input class="delete" type="button" value="Cancel" onclick="$('#createNewsDialog').dialog('close');"/>
							</span>
						</div>
					</g:form>
				</div>

				<script type="text/javascript">
					var moveBundle = "${params.moveBundle}"
					var viewFilter = "${params.viewFilter}"
					var moveEvent = "${params.moveEvent}"
					if(moveBundle){
						$("#moveBundleId").val(moveBundle)
					}
					if(viewFilter){
						$("#viewFilterId").val(viewFilter)
					}
					if(moveEvent){
						$("#moveEvent").val(moveEvent)
					} else {
						$("#moveEvent").val("${tds.currentMoveEventId()}")
					}
					/*------------------------------------------------------------------
					* function to Unhighlight the Asset row when the edit DIV is closed
					*-------------------------------------------------------------------*/
					$("#showEditCommentDialog").bind('dialogclose', function(){
						var assetTable = $("#commetAndNewsBodyId > tr");
						assetTable.each(function(n, row){
							$(row).removeClass('selectedRow');
						});
					});
					$("#createNewsDialog").bind('dialogclose', function() {
						$("#createNewsForm")[0].reset();
					});
					/*
					 * validate the text area size
					*/
					function textCounter(fieldId, maxlimit) {
						var value = $("#"+fieldId).val()
						if (value.length > maxlimit) { // if too long...trim it!
							$("#"+fieldId).val(value.substring(0, maxlimit));
							return false;
						} else {
							return true;
						}
					}

					$(".menu-parent-planning-event-news").addClass('active');
					$(".menu-parent-planning").addClass('active');

					/**
					 * Implementing Kendo Grid for Bundle List
					 */
					function loadNewsAndIssuesList() {
						var moveEvent = '${moveEventId}';
						var viewFilter = '${viewFilter}';
						var moveBundle = '${bundleId}';

						$("#newsAndIssuesList").kendoGrid({
							toolbar: kendo.template('<button type="button" class="btn btn-default action-toolbar-btn" onClick=\"openCreateNewsDialog()\"><span class="glyphicon glyphicon-plus" aria-hidden="true"></span> Create News</button> <div onclick="loadNewsAndIssuesList()" class="action-toolbar-refresh-btn"><span class="glyphicon glyphicon-refresh" aria-hidden="true"></span></div>'),
							dataSource: {
								type: "json",
								transport: {
									read: {
										url: '${createLink(action: 'getEventNewsList')}',
										data: {moveEvent: moveEvent, viewFilter:viewFilter, moveBundle:moveBundle}
									}
								},
								schema: {
									model: {
										fields: {
											createdAt: { type: "date" },
											createdBy: { type: "string"},
											commentType: { type: "string" },
											comment: { type: "string" },
											resolution: { type: "string" },
											resolvedAt: { type: "date" },
											resolvedBy: { type: "string" },
											newsId: {type: "number"}
										}
									}
								},
								sort: {
									field: "name",
									dir: "asc"
								}
							},
							columns: [
								{
									field: "newsId",
									hidden: true
								},
								{
									field: "createdAt",
									title: "Created At",
                                    format: "{0: " + tdsCommon.kendoDateFormat() + "}",
									width: 180
								},
								{
									field: "createdBy",
									title: "Created By",
									width: 180
								},
								{
									field: "commentType",
									title: "Comment Type",
									width: 180
								},
								{
									field: "comment",
									title: "Comment"
								},
								{
									field: "resolution",
									title: "Resolution"
								},
								{
									field: "resolvedAt",
									title: "Resolved At",
                                    format: "{0: " + tdsCommon.kendoDateFormat() + "}",
									width: 180
								},
								{
									field: "resolvedBy",
									title: "Resolved By",
									width: 180
								}
							],
							selectable: "row",
							change: function(e) {
								var selectedRow = this.select();
								var item = this.dataItem(selectedRow);
								return getCommentDetails(item.newsId, item.commentType);
							},
							sortable: true,
							filterable: {
								mode: "row"
							},
							pageable: {
								pageSize: ${raw(com.tdsops.common.ui.Pagination.MAX_DEFAULT)},
								pageSizes: [ ${ raw(com.tdsops.common.ui.Pagination.optionsAsText()) } ]
							}
						});
					}

					loadNewsAndIssuesList();
				</script>
			</div>
		</div>
		<!-- /.box-body -->
	</div>
</section>
</body>
</html>
