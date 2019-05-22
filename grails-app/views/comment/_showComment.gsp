<%@page import="net.transitionmanager.security.Permission"%>
<div draggable id="showCommentPopup" class="ui-dialog ui-widget ui-widget-content ui-corner-all ui-front ui-front" style="width: 700px" tabindex="-1" >
	<div class="ui-dialog-titlebar ui-widget-header ui-corner-all ui-helper-clearfix">
		<span id="ui-id-5" class="ui-dialog-title">Comment Details</span>
		<button class="ui-button ui-widget ui-state-default ui-corner-all ui-button-icon-only ui-dialog-titlebar-close" role="button" aria-disabled="false" title="close" ng-click="close()">
		<span class="ui-button-icon-primary ui-icon ui-icon-closethick"></span>
		<span class="ui-button-text">close</span>
		</button>
	</div>
	<div id="showCommentDialog" title="Comment/Issue detail" class="comment-dialog-content">
		<loading-indicator></loading-indicator>
		<div class="dialog" style="border: 1px solid #5F9FCF">
			<div>
				<table id="showCommentTable" style="border: 0px;">
					<tr class="prop">
						<td valign="top" class="name"><label for="comment">Comment:</label></td>
						<td valign="top" class="value" colspan="3">
							<textarea style="width: 90%;max-width:90%;" rows="4" id="commentTdId" readonly="readonly">{{ac.comment}}</textarea>
						</td>
					</tr>
					<tr class="prop">
						<td valign="top" class="name">
							<label for="category">Category:</label>
						</td>
						<td valign="top" class="value" colspan="3">{{ac.category}}</td>
					</tr>
					<tr id="assetShowId" class="prop">
						<td valign="top" class="name" id="assetTdId"><label for="asset">Asset:</label></td>
						<td valign="top" class="value" id="assetShowValueId" colspan="3">
							<a ng-click="getEntityDetails();">{{acData.assetName}}</a>
						</td>
					</tr>
					<tr>
						<td valign="top" class="name"><label for="isResolved">Status:</label></td>
						<td valign="top" class="value" id="isResolved" colspan="3">
							<div ng-if="ac.isResolved == 0">
								Not resolved
							</div>
							<div ng-if="ac.isResolved == 1">
								Archived
							</div>
						</td>
					</tr>
					<tr>
						<td valign="top" class="name"><label for="createdBy">Created By:</label></td>
						<td valign="top" class="value" id="createdById" colspan="3">{{acData.personCreateObj?(acData.personCreateObj+" at "+acData.dtCreated):""}}</td>
					</tr>
				</table>
			</div>
            <tds:hasPermission permission="${Permission.CommentEdit}">
				<div class="buttons" style="white-space: nowrap;">
					<span class="button" class="slide">
					<span class="slide">
					<input class="edit-comment" type="button" value="Edit" id="commentButtonEditId" ng-click="editComment();" ng-show="ac.canEdit" />
					</span>
					<span id="fromAssetId" class="slide">
					<input class="delete-comment" type="button" value="Delete" ng-click="deleteComment()" ng-show="ac.canEdit" />
					</span>
					</span>
				</div>
			</tds:hasPermission>
		</div>
	</div>
</div>
