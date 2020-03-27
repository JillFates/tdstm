<%@page import="net.transitionmanager.asset.Application"%>
<%@page import="net.transitionmanager.security.Permission"%>
<%@page defaultCodec="html" %> 

<g:set var="assetClass" value="${(new Application()).assetClass}" />

<table style="border: 0">
	<tr>
	
		<td colspan="2"><div class="dialog" 
			<tds:hasPermission permission="${Permission.AssetEdit}">
				ondblclick="EntityCrud.showAssetEditView('${assetClass}', ${applicationInstance?.id});"
			</tds:hasPermission>>
				<g:if test="${errors}">
					<div id="messageDivId" class="message">${errors}</div>
				</g:if>
				<g:render template="show" model="[applicationInstance:applicationInstance, shutdownBy: shutdownBy,
								shutdownById: shutdownById, startupBy: startupBy, startupById: startupById,
								testingBy: testingBy, testingById: testingById]" >

				</g:render>
			</div>
		</td>
	</tr>
	<tr id="deps">
		<g:render template="/assetEntity/dependentShow" model="[assetEntity:applicationInstance]" ></g:render>
	</tr>
	<tr id="commentListId">
		<g:render template="/assetEntity/commentList" model="[asset:applicationInstance, 'prefValue': prefValue, 'viewUnpublishedValue': viewUnpublishedValue]" ></g:render>
	</tr>
	<tr>
		<td colspan="2">
			<div class="buttons">
				<g:form>
					<input type="hidden" name="id" id="applicationId" value="${applicationInstance?.id}" />
					<g:render template="/assetEntity/showButtons" model="[assetEntity:applicationInstance]"/>
				</g:form>
			</div>
		</td>
	</tr>
</table>
<script>
	currentMenuId = "#assetMenu";
	$("#assetMenuId a").css('background-color','#003366')
	
	$(document).ready(function() { 
		changeDocTitle('${raw(escapedName)}');
	})
</script>
