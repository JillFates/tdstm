<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="topNav" />
    <title>Export Assets</title>
	<asset:stylesheet href="css/progressbar.css" />
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

        $(document).on('change','#bundleId',function(event){
            var disabled = false;

            if(!$("#bundleId").val()) {
                disabled = true;
			}

            $("#exportButton").prop('disabled', disabled);
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
	<tds:subHeader title="Export Assets" crumbs="['Assets','Export Assets']"/>
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

					<g:form id="exportForm" action="export" method="post" name="exportForm" role="form" >

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
							<div class="form-group col-lg-3 col-md-4">
								<label for="bundleId">Select one or more bundle(s):</label>
								<select id="bundleId" name="bundle" multiple="multiple" class="form-control medium-height">
									<option value="All">All Bundles</option>
									<option value="${useForPlanningArgName}" selected="selected">Planning Bundles</option>
									<option value="" disabled>──────────</option>
									<g:each status="i" in="${moveBundleList}" var="moveBundle">
										<option value="${moveBundle?.id}">${moveBundle?.name}</option>
									</g:each>
								</select>
							</div>
							<div class="form-group col-lg-3 col-md-4">
								<label for="checkboxGroup">Choose Items to Export:</label>
								<div class="row checkboxGroup" id="checkboxGroup">
									<div class="col-sm-6">
										<ul class="list-group sub-set">
											<li class="list-group-item">
												<div class="checkbox">
													<label for="applicationId"><input type="checkbox" id="applicationId" name="application" value="application" onclick="importExportPreference($(this),'ImportApplication')"
													${prefMap['ImportApplication']=='true' ? 'checked="checked"' :''}/>Application</label>
												</div>
											</li>
											<li class="list-group-item">
												<div class="checkbox">
													<label for="assetId"><input type="checkbox" id="assetId" name="asset" value="asset" onclick="importExportPreference($(this),'ImportServer')"
													${prefMap['ImportServer']=='true' ? 'checked="checked"' :''}/>Devices</label>
												</div>
											</li>
											<li class="list-group-item">
												<div class="checkbox">
													<label for="databaseId"><input type="checkbox" id="databaseId" name="database" value="database" onclick="importExportPreference($(this),'ImportDatabase')"
													${prefMap['ImportDatabase']=='true' ? 'checked="checked"' :''}/>Database</label>
												</div>
											</li>
											<li class="list-group-item">
												<div class="checkbox">
													<label for="filesId"><input type="checkbox" id="filesId" name="files" value="files" onclick="importExportPreference($(this),'ImportStorage')"
													${prefMap['ImportStorage']=='true' ? 'checked="checked"' :''}/>Storage</label>
												</div>
											</li>
											<li class="list-group-item">
												<div class="checkbox">
													<label for="roomId"><input type="checkbox" id="roomId" name="room" value="room" onclick="importExportPreference($(this),'ImportRoom')"
													${prefMap['ImportRoom']=='true' ? 'checked="checked"' :''}/>Room</label>
												</div>
											</li>
										</ul>
									</div>
									<div class="col-sm-6">
										<ul class="list-group sub-set">
											<li class="list-group-item">
												<div class="checkbox">
													<label for="rackId"><input type="checkbox" id="rackId" name="rack" value="rack" onclick="importExportPreference($(this),'ImportRack')"
													${prefMap['ImportRack']=='true' ? 'checked="checked"' :''}/>Rack</label>
												</div>
											</li>
											<li class="list-group-item">
												<div class="checkbox">
													<label for="dependencyId"><input type="checkbox" id="dependencyId" name="dependency" value="dependency" onclick="importExportPreference($(this),'ImportDependency')"
													${prefMap['ImportDependency']=='true' ? 'checked="checked"' :''}/>Dependency</label>
												</div>
											</li>
											<li class="list-group-item">
												<div class="checkbox">
													<label for="cablingId"><input type="checkbox" id="cablingId" name="cabling" value="cable"
															 onclick="importExportPreference($(this),'ImportCabling')"
													${prefMap['ImportCabling']=='true' ? 'checked="checked"' :''}/>Cabling</label>
												</div>
											</li>
											<li class="list-group-item">
												<div class="checkbox">
													<label for="commentId"><input type="checkbox" id="commentId" name="comment" value="comment" onclick="importExportPreference($(this),'ImportComment')"
													${prefMap['ImportComment']=='true' ? 'checked="checked"' :''}/>Comment</label>
												</div>
											</li>
										</ul>
									</div>
								</div>
							</div>
							<div class="form-group">
								<input type="hidden" id="exportFormat" name="exportFormat" value="xlsx" />
							</div>
							<div class="">
								<button type="submit" id="exportButton" class="btn btn-primary">Export Excel&nbsp;&nbsp;<span class="exportIcon glyphicon glyphicon-download" aria-hidden="true"></span></button>
							</div>
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
