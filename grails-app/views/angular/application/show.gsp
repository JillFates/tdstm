<%@page import="com.tds.asset.Application"%>
<%@page import="net.transitionmanager.security.Permission"%>
<%@page defaultCodec="html" %>

<g:set var="assetClass" value="${(new Application()).assetClass}" />

<div class="modal-content tds-angular-component-content">
	<div class="modal-header">
		<button aria-label="Close" class="close" type="button" (click)="cancelCloseDialog()"><span  aria-hidden="true">×</span></button>
		<h4 class="modal-title">Application Detail</h4>
	</div>
	<div class="modal-body">
		<div>
			<table style="border: 0">
				<tr>

					<td colspan="2"><div class="dialog">
							<g:if test="${errors}">
								<div id="messageDivId" class="message">${errors}</div>
							</g:if>
							<g:render template="/angular/application/show" model="[asset:applicationInstance]" ></g:render>
						</div>
					</td>
				</tr>
				<tr id="deps">
					<g:render 
						template="/angular/common/dependentShow" 
						model="[supportAssets:supportAssets, dependentAssets:dependentAssets, assetEntity: applicationInstance]" >
					</g:render>
				</tr>
				<tr id="commentListId">
					<g:render 
						template="/angular/common/commentList" 
						model="[asset:applicationInstance, 'prefValue': prefValue, 'viewUnpublishedValue': viewUnpublishedValue]" >
					</g:render>
				</tr>
			</table>
		</div>
	</div>
	<div class="modal-footer form-group-center">
		<button class="btn btn-default pull-right" (click)="cancelCloseDialog()" type="button"><span  class="glyphicon glyphicon-ban-circle"></span> Close</button>
		<button class="btn btn-default pull-left" (click)="showAssetEditView()" type="button"><span  class="glyphicon glyphicon-pencil"></span> Edit</button>
	</div>
</div>