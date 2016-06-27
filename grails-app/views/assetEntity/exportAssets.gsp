<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="topNav" />
    <title>Asset Export</title>
	<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'progressbar.css')}" />
	<g:javascript src="import.export.js"/>

	<g:render template="../layouts/responsiveAngularResources" />

	<g:javascript src="progressBar.js" />
	
	<script type="text/javascript">
		$(document).ready(function() {
			$('#exportForm').submit(function(e){
				var form = $('#exportForm');
		        var items = form.serialize();
		        url = form.attr("action");

				$.post(url, items, function(data) {
					var progressBar = tds.ui.progressBar(data.data.key, 5000, 
					function() {
						window.location="downloadExport?key=" + data.data.key;
					}, function() {
					},
					"Exporting assets");
				});
				
		        return false;
		    });
		});
	</script>
	<style>
	  /*TODO: REMOVE ON COMPLETE MIGRATION */
	  div.content-wrapper {
		  background-color: #ecf0f5 !important;
	  }
	</style>
	</head>
	<body>
	<!-- Content Header (Page header) -->
	<section class="content-header">
		<h1>
			Asset Export
		</h1>
		<ol class="breadcrumb">
			<li><a href="#">Assets</a></li>
			<li><a href="#">Manage Data</a></li>
			<li class="active">Asset Export</li>
		</ol>
	</section>
	<g:if test="${flash.message}">
		<div class="message">${flash.message}</div>
	</g:if>
	<!-- Main content -->
	<section>
		<div>
			<div class="box-body">
				<div class="box box-primary">
					<div class="box-header with-border">
						<h3 class="box-title">Export</h3>
					</div><!-- /.box-header -->

					<g:form id="exportForm" action="export" method="post" name="exportForm" role="form" class="medium-size">

						<input type="hidden" value="${projectId}" name="projectIdExport" />
						<div class="box-body">
							<div class="form-group" style="display: none;">
								<label for="dataTransferSet">Export Type:</label>
								<select id="dataTransferSet" name="dataTransferSet" class="form-control">
									<g:each status="i" in="${dataTransferSetExport}" var="dataTransferSet">
										<option value="${dataTransferSet?.id}">${dataTransferSet?.title}</option>
									</g:each>
								</select>
							</div>
							<div class="form-group">
								<label for="bundleId">Select one or more bundle(s):</label>
								<select id="bundleId" name="bundle" multiple="multiple" class="form-control">
									<option value="" selected="selected">All</option>
									<g:each status="i" in="${moveBundleInstanceList}" var="moveBundle">
										<option value="${moveBundle?.id}">${moveBundle?.name}</option>
									</g:each>
								</select>
							</div>
							<div class="form-group">
								<label for="checkboxGroup">Choose Items to Export:</label>
								<div class="row checkboxGroup" id="checkboxGroup">
									<div class="col-md-6">
										<ul class="list-group sub-set">
											<li class="list-group-item">
												<span>
													<input type="checkbox" id="applicationId" name="application" value="application" onclick="importExportPreference($(this),'ImportApplication')"
													${prefMap['ImportApplication']=='true' ? 'checked="checked"' :''}/>&nbsp;
													<label for="applicationId">Application</label>
												</span>
											</li>
											<li class="list-group-item">
												<span>
													<input type="checkbox" id="assetId" name="asset" value="asset" onclick="importExportPreference($(this),'ImportServer')"
													${prefMap['ImportServer']=='true' ? 'checked="checked"' :''}/>&nbsp;
													<label for="assetId">Devices</label>
												</span>
											</li>
											<li class="list-group-item">
												<span>
													<input type="checkbox" id="databaseId" name="database" value="database" onclick="importExportPreference($(this),'ImportDatabase')"
													${prefMap['ImportDatabase']=='true' ? 'checked="checked"' :''}/>&nbsp;
													<label for="databaseId">Database</label>
												</span>
											</li>
											<li class="list-group-item">
												<span>
													<input type="checkbox" id="filesId" name="files" value="files" onclick="importExportPreference($(this),'ImportStorage')"
													${prefMap['ImportStorage']=='true' ? 'checked="checked"' :''}/>&nbsp;
													<label for="filesId">Storage</label>
												</span>
											</li>
											<li class="list-group-item">
												<span>
													<input type="checkbox" id="roomId" name="room" value="room" onclick="importExportPreference($(this),'ImportRoom')"
													${prefMap['ImportRoom']=='true' ? 'checked="checked"' :''}/>&nbsp;
													<label for="roomId">Room</label>
												</span>
											</li>
										</ul>
									</div>
									<div class="col-md-6">
										<ul class="list-group sub-set">
											<li class="list-group-item">
												<span>
													<input type="checkbox" id="rackId" name="rack" value="rack" onclick="importExportPreference($(this),'ImportRack')"
													${prefMap['ImportRack']=='true' ? 'checked="checked"' :''}/>&nbsp;
													<label for="rackId">Rack</label>
												</span>
											</li>
											<li class="list-group-item">
												<span>
													<input type="checkbox" id="dependencyId" name="dependency" value="dependency" onclick="importExportPreference($(this),'ImportDependency')"
													${prefMap['ImportDependency']=='true' ? 'checked="checked"' :''}/>&nbsp;
													<label for="dependencyId">Dependency</label>
												</span>
											</li>
											<li class="list-group-item">
												<span><input type="checkbox" id="cablingId" name="cabling" value="cable"
															 onclick="importExportPreference($(this),'ImportCabling')"
													${prefMap['ImportCabling']=='true' ? 'checked="checked"' :''}/>&nbsp;
													<label for="cablingId">Cabling</label>
												</span>
											</li>
											<li class="list-group-item">
												<span>
													<input type="checkbox" id="commentId" name="comment" value="comment" onclick="importExportPreference($(this),'ImportComment')"
													${prefMap['ImportComment']=='true' ? 'checked="checked"' :''}/>&nbsp;
													<label for="commentId">Comment</label>
												</span>
											</li>
										</ul>
									</div>
								</div>
							</div>
							<tds:hasPermission permission="Export">
							<div class="form-group">
								<label for="exportFormat">Export Format</label>
								<select id="exportFormat" name="exportFormat" class="form-control selectpicker show-tick">
									<option value="xlsx" selected="selected">Excel Workbook (.xlsx)</option>
									<option value="xls">Excel 97-2004 (.xls)</option>
								</select>
							</div>
							<div class="box-footer">
								<%--
									<g:link controller="assetEntity" action="exportSpecialReport">
										<input class="button" type="button" value="Generate Special" onclick="window.location=this.parentNode.href;"/>
									</g:link>
								--%>
								<button type="submit" class="btn btn-primary">Export&nbsp;<span class="exportIcon glyphicon glyphicon-download" aria-hidden="true"></span></button>
							</div>
							</tds:hasPermission>
						</div><!-- /.box-body -->
					</g:form>

				</div>
			</div>
		</div>
	</section>
	<script>
		currentMenuId = "#assetMenu";
		$(".menu-parent-assets-export-assets").addClass('active');
		$(".menu-parent-assets").addClass('active');
	</script>

	</body>
</html>