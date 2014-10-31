<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.core.css')}" />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.dialog.css')}" />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.resizable.css')}" />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.slider.css')}" />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.tabs.css')}" />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.theme.css')}" />
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'rackLayout.css')}" />

		<title>Rack Elevation Report</title>
	</head>
	<body>
		<g:if test="${generateView}">
			<table style="border: none; width: auto">
				<tr>
		</g:if>
		<g:else>
			<div class="body">
		</g:else>

		<g:if test="${rackLayout}">
			<g:each in="${rackLayout}" var="rackLayout">
				<g:if test="${frontView}">
					<g:if test="${generateView}">
						<td class="rack_elevation_td">
					</g:if>
						
					<table cellpadding=2 class="rack_elevation front">
						<tr>
							<td colspan="4" style="border:0px;text-align:center; white-space: nowrap;">
								<div class="close-icon" onclick="$(this).parents('td.rack_elevation_td').remove(); $(this).parents('table.rack_elevation').remove();"></div>
								<h2>${rackLayout?.rack} in ${rackLayout?.room}</h2>
							</td>
						</tr>
						<tr>
							<th></th>
							<th>Device</th>
							<th>Bundle</th>
						</tr>
						${rackLayout?.frontViewRows}
					</table>
					<hr style="page-break-after:always"/>
					<g:if test="${generateView}">
					</td>
					</g:if>
				</g:if>

				<g:if test="${backView}">
					<g:if test="${generateView}">
						<td class="rack_elevation_td">
					</g:if>
					<table cellpadding=2 class="rack_elevation back">
						<tr>
							<td colspan="4" style="border:0px;text-align:center">
								<div class="close-icon" onclick="$(this).parents('td.rack_elevation_td').remove(); $(this).parents('table.rack_elevation').remove();"></div>
								<h2>${rackLayout?.rack} in ${rackLayout?.room} (Back)
								<g:if test="${generateView}">
									<tds:hasPermission permission="AssetEdit">
										<g:if test="${showIconPref == 'true'}">
											<span id="span_${rackLayout?.rackId}" onclick="disableCreateIcon(${rackLayout?.rackId})"> 
												<img src='../images/plus_disabled.gif' />
											</span>
										</g:if>
										<g:else>
											<span id="span_${rackLayout?.rackId}" onclick="enableCreateIcon(${rackLayout?.rackId})"> 
												<img src="../images/plus.gif" />
											</span>
										</g:else>
										<span id="anchor_${rackLayout?.rackId}" onclick="assignPowers(${rackLayout?.rackId})"> 
										</span>
									</tds:hasPermission>
								</g:if>
								</h2>
							</td>
						</tr>
						<tr>
							<th></th>
							<th>Device</th>
							<th>Bundle</th>
							<th>Cabling</th>
						</tr>
						${rackLayout?.backViewRows}
					</table>
					<br class="page-break-after"/>
					<g:if test="${generateView}">
						</td>
					</g:if>
				</g:if>
			</g:each>
		</g:if>
		<g:else>
			<table>
				<tr><td class="no_records">No racks found</td></tr>
			</table>
		</g:else>

		<%-- This may be an extra close div tag --%>
		</div>

		<g:if test="${generateView}">
			</tr>
			</table>
		</g:if>
		<g:else>
			</div>

			<g:render template="../layouts/standardResources" />

			<g:javascript src="asset.tranman.js" />
			<g:javascript src="room.rack.combined.js"/>
			<g:javascript src="cabling.js"/>
			<g:javascript src="entity.crud.js" />
			<g:javascript src="model.manufacturer.js"/>
			<g:javascript src="angular/angular.min.js" />
			<g:javascript src="select2.js" />
			<g:javascript src="angular/plugins/angular-ui.js"/>
			<g:javascript src="angular/plugins/angular-resource.js" />
			<script type="text/javascript" src="${resource(dir:'components/core',file:'core.js')}"></script>
			<script type="text/javascript" src="${resource(dir:'components/comment',file:'comment.js')}"></script>
			<script type="text/javascript" src="${resource(dir:'components/asset',file:'asset.js')}" /></script>
			<g:javascript src="bootstrap.js" />
			<g:javascript src="angular/plugins/ui-bootstrap-tpls-0.10.0.min.js" />
			<g:javascript src="angular/plugins/ngGrid/ng-grid-2.0.7.min.js" />
			<g:javascript src="angular/plugins/ngGrid/ng-grid-layout.js" />

		</g:else>

	</body>
</html>
