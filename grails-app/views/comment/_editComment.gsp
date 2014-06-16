<div draggable centered id="editCommentPopup" class="ui-dialog ui-widget ui-widget-content ui-corner-all ui-front ui-front" style="width: 700px" tabindex="-1" >
	<div class="ui-dialog-titlebar ui-widget-header ui-corner-all ui-helper-clearfix">
		<span id="ui-id-5" class="ui-dialog-title">{{(isEdit)?'Edit Comment':'Create Comment'}}</span>
		<button class="ui-button ui-widget ui-state-default ui-corner-all ui-button-icon-only ui-dialog-titlebar-close" role="button" aria-disabled="false" title="close" ng-click="close()">
		<span class="ui-button-icon-primary ui-icon ui-icon-closethick"></span>
		<span class="ui-button-text">close</span>
		</button>
	</div>
	<div id="editCommentDialog" title="Edit Comment/Issue" class="comment-dialog-content">
		<loading-indicator></loading-indicator>
		<form name="form" class="css-form" novalidate>
			<div class="dialog" style="border: 1px solid #5F9FCF">
				<table id="updateCommentTable" class="inner">
					<tr class="prop">
						<td valign="top" class="name" id="commentEditTdId">
							<label>Comment:</label>&nbsp;<span class="error-msg" ng-show="form.comment.$error.required"><b>*</b></span>
						</td>
						<td valign="top" class="value" colspan="2">
							<textarea required style="width: 90%;max-width:90%;" ng-maxlength="4000" rows="4" id="commentEditId" name="comment" ng-model="ac.comment"></textarea>
<pre class="error-msg" ng-show="form.comment.$error.maxlength">Comment maximum length is 4000 characters.</pre>
						</td>
					</tr>
					<tr class="prop">
						<td valign="top" class="name">
							<label for="category">Category:</label>
						</td>
						<td colspan="4">
							<tds:select required="true" ng-model="ac.category" datasource="ds.categories" id="category" name="category" from="${com.tds.asset.AssetComment.constraints.category.inList}"
							noSelection="['':'please select']" ng-change="updateWorkflowTransitions(ac.assetEntity, this.value, 'workFlowTransitionId', 'predecessorId','')"></tds:select>
						</td>
					</tr>
					<tr>
						<td valign="top" class="name"><label for="asset">Asset:</label></td>
						<td valign="top">
							<tds:select required="true" id="asset" name="asset" ng-model="ac.assetType" from="['Application','Server','Database','Storage','Other']" datasource="ds.assetTypes"
ng-change="getAssetsList()">
							</tds:select>
							<assets-by-type asset-type="ac.assetType" ng-model="ac.assetEntity"></assets-by-type>
						</td>
					</tr>
					<tr ng-show="isEdit">
						<td valign="top" class="name"><label for="createdBy">Created By:</label></td>
						<td valign="top" class="value" id="createdById" colspan="3">{{acData.personCreateObj?(acData.personCreateObj+" at "+acData.dtCreated):""}}</td>
					</tr>
				</table>
			</div>
		</form>
		<tds:hasPermission permission='CommentCrudView'>
			<div class="buttons">
<span class="button">
				<input class="save" type="button" id="saveAndViewBId" value="Save" ng-click="saveComment(true)" ng-disabled="form.$invalid" />
</span>
			</div>
		</tds:hasPermission>
	</div>
</div>