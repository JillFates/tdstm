<%@page import="com.tds.asset.Application"%>
<%-- <g:set var="assetClass" value="${(new Application()).assetClass}" /> --%>
<g:set var="assetClass" value="Application" />

<table style="border: 0">
	<tr>
	
		<td colspan="2"><div class="dialog" <tds:hasPermission permission='AssetEdit'> ondblclick="editEntity('${redirectTo}', 'Application', ${applicationInstance?.id})"</tds:hasPermission>>
				<g:if test="${errors}">
					<div id="messageDivId" class="message">${errors}</div>
				</g:if>
				<g:render template="show" model="[applicationInstance:applicationInstance]" ></g:render>
			</div>
		</td>
	</tr>
	<tr id="deps">
		<g:render template="../assetEntity/dependentShow" model="[assetEntity:applicationInstance]" ></g:render>
	</tr>
	<tr id="commentListId">
		<g:render template="../assetEntity/commentList" model="[asset:applicationInstance, 'prefValue': prefValue]" ></g:render>
	</tr>
	<tr>
		<td colspan="2">
			<div class="buttons">
				<g:form>
					<input type="hidden" name="id" id="applicationId" value="${applicationInstance?.id}" />
					<g:render template="../assetEntity/showButtons" 
						model="[assetEntity:applicationInstance, config:config, highlightMap:highlightMap, redirectTo:redirectTo, type:'Application', forWhom:assetClass]"/>
				</g:form>
			</div>
		</td>
	</tr>
</table>
<script>
	currentMenuId = "#assetMenu";
	$("#assetMenuId a").css('background-color','#003366')
	
	$(document).ready(function() { 
		changeDocTitle('${escapedName}');
	})
</script>
