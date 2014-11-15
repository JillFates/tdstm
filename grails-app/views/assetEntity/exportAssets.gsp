<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="projectHeader" />
    <title>Asset Export</title>
	<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'progressbar.css')}" />
	<g:javascript src="import.export.js"/>
	<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'bootstrap.css')}" />
	<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'tds-bootstrap.css')}" />

	<g:javascript src="bootstrap.js" />
	<g:javascript src="angular/angular.min.js" />
	<script type="text/javascript" src="${resource(dir:'components/core',file:'core.js')}"></script>
	<g:javascript src="progressBar.js" />
	
	<script type="text/javascript">
		$(document).ready(function() {
			$.fn.serializeForm = function() {
			    data = {};
			    url = this.attr("action");
			    items = this.serializeArray();
			    $.each(items,function(i,item) {
			        data[item['name']]=item['value'];
			    });
			    return data;
			}
			
			$('#exportForm').submit(function(e){
				var form = $('#exportForm');
		        items = {};
		        items = form.serializeForm();
		        url = form.attr("action");

				$.post(url, items, function(data) {
					var progressBar = tds.ui.progressBar(data.data.key, 5000, 
					function() {
						window.location="downloadExport?key=" + data.data.key;
					}, function() {
					},
					"<h1>Exporting assets</h1>");
				});
				
		        return false;
		    });
		});
	</script>
	</head>
	<body>
	<div class="body">
		<h1>Asset Export</h1>

		<g:if test="${flash.message}">
		<div class="message">${flash.message}</div>
		</g:if>

		<g:form id="exportForm" action="export" method="post" name="exportForm">
        
        <input type="hidden" value="${projectId}" name="projectIdExport" />
        <div class="dialog">
          <table style="width:500px; margin-left:10px;">
            <tbody>
            <thead>
              <tr><th colspan="3">Export</th></tr>
            </thead>
            <tbody>
				<tr>
					<td valign="top" class="name">Export Type:</td>
					<td valign="top" class="value">
						<select id="dataTransferSet" name="dataTransferSet">                    
							<g:each status="i" in="${dataTransferSetExport}" var="dataTransferSet">
								<option value="${dataTransferSet?.id}">${dataTransferSet?.title}</option>
							</g:each>
						</select>
					</td>
					<td></td>
				</tr>

				<tr>
					<td valign="top" class="name">Select on or more bundle(s):</td>
					<td valign="top" class="value">
						<select MULTIPLE id="bundleId" name="bundle">
							<option value="" selected="selected">All</option>
							<g:each status="i" in="${moveBundleInstanceList}" var="moveBundle">
								<option value="${moveBundle?.id}">${moveBundle?.name}</option>
							</g:each>
						</select>
					</td>
					<td></td>
				</tr>                           
              <tr>
              	<td>Choose Items to export:</td>
              	<td style="vertical-align:top;">
	                <span><input type="checkbox" id="applicationId" name="application" value="application" 
	                		onclick="importExportPreference($(this),'ImportApplication')"
	                		${prefMap['ImportApplication']=='true' ? 'checked="checked"' :''}/>&nbsp;
	                		<label for="applicationId">Application</label></span>&nbsp;
					<br>
	                <span><input type="checkbox" id="assetId" name="asset" value="asset" 
	                		onclick="importExportPreference($(this),'ImportServer')"
	                		${prefMap['ImportServer']=='true' ? 'checked="checked"' :''}/>&nbsp;
	                		<label for="assetId">Devices</label></span>&nbsp;
					<br>
	                <span><input type="checkbox" id="databaseId" name="database" value="database"
	                		onclick="importExportPreference($(this),'ImportDatabase')"
	                	 	${prefMap['ImportDatabase']=='true' ? 'checked="checked"' :''}/>&nbsp;
	                		<label for="databaseId">Database</label></span>&nbsp;
					<br>
	                <span><input type="checkbox" id="filesId" name="files" value="files"
	                		onclick="importExportPreference($(this),'ImportStorage')"  
	                		${prefMap['ImportStorage']=='true' ? 'checked="checked"' :''}/>&nbsp;
	                		<label for="filesId">Storage</label></span>&nbsp;
					<br>
	                <span><input type="checkbox" id="roomId" name="room" value="room" 
	                		onclick="importExportPreference($(this),'ImportRoom')"
	                		${prefMap['ImportRoom']=='true' ? 'checked="checked"' :''}/>&nbsp;
	                		<label for="roomId">Room</label></span>&nbsp;
	            </td><td style="vertical-align:top;">
	                <span><input type="checkbox" id="rackId" name="rack" value="rack" 
	                		onclick="importExportPreference($(this),'ImportRack')"
	                		${prefMap['ImportRack']=='true' ? 'checked="checked"' :''}/>&nbsp;
	                		<label for="rackId">Rack</label></span>&nbsp;
					<br>
	                <span><input type="checkbox" id="dependencyId" name="dependency" value="dependency" 
	                		onclick="importExportPreference($(this),'ImportDependency')"
	                		${prefMap['ImportDependency']=='true' ? 'checked="checked"' :''}/>&nbsp;
	                		<label for="dependencyId">Dependency</label></span>&nbsp;
					<br>
               		<span><input type="checkbox" id="cablingId" name="cabling" value="cable" 
		               		onclick="importExportPreference($(this),'ImportCabling')"
		               		${prefMap['ImportCabling']=='true' ? 'checked="checked"' :''}/>&nbsp;
		               		<label for="cablingId">Cabling</label></span>&nbsp;
					<br>
					<span><input type="checkbox" id="commentId" name="comment" value="comment" 
		               		onclick="importExportPreference($(this),'ImportComment')"
		               		${prefMap['ImportComment']=='true' ? 'checked="checked"' :''}/>&nbsp;
		               		<label for="commentId">Comment</label></span>&nbsp;
	                </td>
				</tr>

			<tds:hasPermission permission="Export">
			<tr>
				<td class="buttonR" colspan="3">
					<input class="button" type="submit" value="Export to Excel"/> 
					<%--
					<g:link controller="assetEntity" action="exportSpecialReport">
						<input class="button" type="button" value="Generate Special" onclick="window.location=this.parentNode.href;"/>
					</g:link>
					--%>
				</td>
			</tr>
			</tds:hasPermission>

            </tbody>
          </table>
        </div>
    </g:form>
    </div>

	<script>
		currentMenuId = "#assetMenu";
		$("#assetMenuId a").css('background-color','#003366')
	</script>

	</body>
</html>