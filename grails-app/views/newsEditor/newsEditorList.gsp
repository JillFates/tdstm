<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="projectHeader" />
<title>Move Event News</title>
<g:javascript library="prototype" />
<g:javascript library="jquery" />
<jq:plugin name="jquery.bgiframe.min" />
<jq:plugin name="jquery.autocomplete" />
<link type="text/css" rel="stylesheet"
	href="${createLinkTo(dir:'css',file:'jquery.autocomplete.css')}" />
<link type="text/css" rel="stylesheet"
	href="${createLinkTo(dir:'css',file:'ui.accordion.css')}" />
<link type="text/css" rel="stylesheet"
	href="${createLinkTo(dir:'css',file:'ui.core.css')}" />
<link type="text/css" rel="stylesheet"
	href="${createLinkTo(dir:'css',file:'ui.dialog.css')}" />
<link type="text/css" rel="stylesheet"
	href="${createLinkTo(dir:'css',file:'ui.resizable.css')}" />
<link type="text/css" rel="stylesheet"
	href="${createLinkTo(dir:'css',file:'ui.slider.css')}" />
<link type="text/css" rel="stylesheet"
	href="${createLinkTo(dir:'css',file:'ui.tabs.css')}" />
<link type="text/css" rel="stylesheet"
	href="${createLinkTo(dir:'css',file:'ui.theme.css')}" />
<g:javascript src="assetcommnet.js" />
<jq:plugin name="ui.core" />
<jq:plugin name="ui.draggable" />
<jq:plugin name="ui.resizable" />
<jq:plugin name="ui.dialog" />
</head>
<body>
<div class="body">

<div>
<g:form action="newsEditorList" name="newsEditorForm">
<input type="hidden" name="projectId" value="${projectId}"/>
	<table style="border: none;" >
		<tr>
			<td nowrap="nowrap">
				<span style="padding-left: 10px;">
				<label for="moveBundle"><b>Bundle:</b></label>&nbsp;
					<select id="moveBundleId" name="moveBundle" onchange="$('#newsEditorForm').submit();">
						<option value="">All</option>
						<g:each status="i" in="${moveBundlesList}" var="moveBundleInstance">
							<option value="${moveBundleInstance?.id}">${moveBundleInstance?.name}</option>
						</g:each>
					</select>
				</span>
				<span  style="padding-left: 10px;">
					<label for="viewFilter"><b>View:</b></label>&nbsp;
					<select id="viewFilterId" name="viewFilter" onchange="$('#newsEditorForm').submit();">
						<option value="all">All</option>
						<option value="active">Active</option>
						<option value="archived">Archived</option>
					</select>
				</span>
				<span  style="padding-left: 10px;"><b>
					Deal with filtering resolve or not, and sorting, and edit vs. display</b>
				</span>
			</td>
		</tr>
	</table>
	</g:form>
</div>
<div style="width: 100%; height: auto; border: 1px solid #5F9FCF; margin-top: 10px; padding: 10px 5px 10px 5px;">
<span style="position: absolute; text-align: center; width: auto; margin: -17px 0 0 10px; padding: 0px 8px; background: #ffffff;"><b>Display
Move News and Issues</b></span>
<table id="assetEntityTable">
	<thead>
		<tr>

			<g:sortableColumn property="createdAt" title="Created At" params="[projectId:projectId, moveBundle:params.moveBundle, viewFilter:params.viewFilter]" />

			<g:sortableColumn property="createdBy" title="Created By" params="[projectId:projectId, moveBundle:params.moveBundle, viewFilter:params.viewFilter]" />

			<g:sortableColumn property="commentType" title="Type" params="[projectId:projectId, moveBundle:params.moveBundle, viewFilter:params.viewFilter]" />

			<g:sortableColumn property="comment" title="Comment" params="[projectId:projectId, moveBundle:params.moveBundle, viewFilter:params.viewFilter]" />

			<g:sortableColumn property="resolution" title="Resolution" params="[projectId:projectId, moveBundle:params.moveBundle, viewFilter:params.viewFilter]" />

			<g:sortableColumn property="resolvedAt" title="Resolved At" params="[projectId:projectId, moveBundle:params.moveBundle, viewFilter:params.viewFilter]" />

			<g:sortableColumn property="resolvedBy" title="Resolved By" params="[projectId:projectId, moveBundle:params.moveBundle, viewFilter:params.viewFilter]" />

		</tr>
	</thead>
	<tbody>
	
	<g:each in="${assetCommentsList}" status="i" var="assetCommentInstance">
		<tr class="${(i % 2) == 0 ? 'odd' : 'even'}" onmouseover="style.backgroundColor='#87CEEE';"	onmouseout="style.backgroundColor='white';">
			<td><tds:convertDateTime date="${assetCommentInstance?.createdAt}"/></td>
			<td>
			<g:if test="${assetCommentInstance?.createdBy}">
			${Person.get(assetCommentInstance?.createdBy)}
			</g:if>
			</td>
			<td>${assetCommentInstance?.commentType}</td>
			<td><tds:truncate value="${assetCommentInstance?.comment}"/></td>
			<td><tds:truncate value="${assetCommentInstance?.resolution}"/></td>
			<td><tds:convertDateTime date="${assetCommentInstance?.resolvedAt}"/></td>
			<td>
			<g:if test="${assetCommentInstance?.resolvedBy}">
			${Person.get(assetCommentInstance?.resolvedBy)}
			</g:if>
			</td>
		</tr>
	</g:each>
	</tbody>
</table>
<div class="paginateButtons" style="padding: 0px;">
<g:form name="paginateRows" action="newsEditorList">
	<table style="border: 0px;">
		<tr>
			<td style="width: 70px;padding: 0px;">
				 <div class="buttons"> <span class="button"><input type="button" value="Create News" class="save" action="Create" ></span></div>
			</td>
			<td style="width: 770px;vertical-align: middle;text-align: right;padding: 0px;">
				<g:if test="${totalCommentsSize > 1 }">
					<g:paginate total="${totalCommentsSize}" params="${params }"/>
				</g:if>
			</td>
		</tr>
	</table>
</g:form>
</div>
</div>

<script type="text/javascript">
var moveBundle = "${params.moveBundle}"
var viewFilter = "${params.viewFilter}"
if(moveBundle){
	$("#moveBundleId").val(moveBundle)
}
if(viewFilter){
	$("#viewFilterId").val(viewFilter)
}
</script>
</body>
</html>
