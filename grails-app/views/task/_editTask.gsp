<div draggable id="editTaskPopup" class="ui-dialog ui-widget ui-widget-content ui-corner-all ui-front" style="width: 1000px" tabindex="-1">
	<div class="ui-dialog-titlebar ui-widget-header ui-corner-all ui-helper-clearfix">
		<span id="ui-id-5" class="ui-dialog-title">{{(isEdit)?'Edit Task':'Create Task'}}</span>
		<button class="ui-button ui-widget ui-state-default ui-corner-all ui-button-icon-only ui-dialog-titlebar-close" role="button" aria-disabled="false" title="close" ng-click="close()">
		<span class="ui-button-icon-primary ui-icon ui-icon-closethick"></span>
		<span class="ui-button-text">close</span>
		</button>
	</div>
	<div id="editCommentDialog" title="Edit Comment/Issue" class="comment-dialog-content">
	<loading-indicator></loading-indicator>
	<form name="form" class="css-form" novalidate>
		<div class="dialog" style="border: 1px solid #5F9FCF">
			<div>
				<table id="updateCommentTable" class="inner">
					<tr class="prop">
						<td valign="top" class="name" id="commentEditTdId">
							<label ><b>Task <span ng-show="isEdit" id="taskNumberEditId">#<b>{{ac.taskNumber == null ? '&nbsp;' : ac.taskNumber}}</b></span>:</b></label>&nbsp;<span class="error-msg" ng-show="form.comment.$error.required"><b>*</b></span>
						</td>
						<td valign="top" class="value">
								<textarea required cols="80" rows="2" id="commentEditId" name="comment" ng-model="ac.comment" ng-maxlength="4000"></textarea>
								<pre class="error-msg" ng-show="form.comment.$error.maxlength">Task maximum length is 4000 characters.</pre>
							</td>
						</tr>
						<tr class="prop" id="assignedToTrEditId">
							<td valign="middle" class="name"><label for="assignedTo">Person/Team:</label></td>
							<td valign="middle" id="assignedToEditTdId" nowrap="nowrap">
								<span>
									<assigned-to-select comment-id='ac.commentId' ng-model='ac.assignedTo' ng-change="checkHardAssigned()"></assigned-to-select>
								</span>&nbsp;/&nbsp;
								<staff-roles ng-model='ac.role'></staff-roles>
								<br><input type="checkbox" id="hardAssignedEdit" name="hardAssignedEdit" value="1" ng-model="ac.hardAssigned" ng-true-value="1" ng-false-value="0" ng-disabled="ac.assignedTo == ''" />&nbsp;
								<label for="hardAssignedEdit">Fixed Assignment</label>
								&nbsp;&nbsp;&nbsp;
								<input type="checkbox" id="sendNotificationEdit" name="sendNotificationEdit" ng-model="ac.sendNotification"/>&nbsp;
								<label for="sendNotificationEdit">Send Notifications</label>
						</td>
					</tr>
					<tr class="prop" id="moveEventEditTrId">
						<td valign="top" class="name"><label for="moveEvent">Event:</label></td>
						<td valign="top">
							<tds:select ng-model="ac.moveEvent" ng-disabled="!enableMoveEvent" datasource="ds.moveEvents" id="moveEvent" name="moveEvent"  from="${MoveEvent.findAllByProject(Project.get(session.getAttribute('CURR_PROJ').CURR_PROJ ))}"
							optionKey='id' optionValue="name" noSelection="['':'please select']"></tds:select>
						</td>
					</tr>
					<tr class="prop">
						<td valign="top" class="name">
							<label for="category">Category:</label>
						</td>
						<td>
							<tds:select required="true" ng-model="ac.category" datasource="ds.categories" id="category" name="category" from="${com.tds.asset.AssetComment.constraints.category.inList}"
							noSelection="['':'please select']"></tds:select>
						</td>
					</tr>
					<tr class="prop" id="workFlowTransitionEditTrId" ng-show="ds.workflows.length > 0">
						<td valign="top" class="name"><label for="workFlowTransitionEditId">Workflow Step:</label></td>
						<td valign="top" class="value">
							<span id="workFlowTransitionEditId">
							<workflow-transition-select workflows="ds.workflows" comment-id='ac.commentId' category='ac.category'
							 asset-id='ac.assetEntity' ng-model='ac.workflowTransition'></workflow-transition-select>
							</span>
							<input type="checkbox" ng-model="ac.override" id="override" name="override" value="0"
						ng-true-value="1" ng-false-value="0" />
							<label for="overrideEdit">Overridden</label>
						</td>
					</tr>
					<tr>
						<td valign="top" class="name"><label for="asset">Asset:</label></td>
						<td valign="top">
							<select ng-model="commentInfo.currentAssetClass" ng-change="assetClassChanged()" ng-options="assetClass.key as assetClass.label for assetClass in commentInfo.assetClasses"></select>
							<select ng-model="commentInfo.currentAsset"  ng-options="asset.id as asset.name for asset in commentInfo.assets"></select>
						</td>
					</tr>
					<tr class="prop">
						<td><label for="instructionsLink">Instructions Link:</label></td>
						<td> <tm-linkable-url> </tm-linkable-url></td>
					</tr>
					<tr class="prop" id="durationEditId">
						<td valign="top" class="name" style="vertical-align: middle"><label for="durationEdit">Estimated Duration:</label></td>
						<td valign="top" class="value">
							<tdsdurationpicker ng-model="acData.durationTime" duration="ac.duration" scale="ac.durationScale" scales="ds.durationScales" ng-change="updateEstFinish()"></tdsdurationpicker>
						</td>
					</tr>
					<tr class="prop" id="estStartEditTrId">
						<td valign="top" class="name"><label for="estStartTrId">Estimated Start/Finish:</label></td>
						<td valign="top" class="value" nowrap="nowrap">
							<input type="text" duration="ac.duration" scale="ac.durationScale" scales="ds.durationScales" date-begin="ac.estStart" date-end="ac.estFinish" ng-model="acData.estRange" ng-change="updateDuration()" class="ctrl-rangepicker" size="45" style="display:inline;" name="estRange" id="estRange"	value="" tdsrangepicker readonly />
							<div class="daterangepicker_clear_filter" ng-click="clearDateRangePickerValue('estRange')"><span class="clear_filter">×</span></div>
							<span class="error-msg" ng-show="form.estRange.$error.dateRange">Invalid range.</span>
						</td>
					</tr>
					<tr class="prop">
						<td valign="top" class="name" style="vertical-align: middle"><label for="priority ">Priority:</label></td>
						<td valign="top" class="value" colspan="4">
							<span id="priorityEditSpanId">	<tds:select ng-model="ac.priority" datasource="ds.priorities" id="priority" class="ynselect" name="priority" from="${1..5}">
								</tds:select>
							</span> &nbsp;&nbsp;&nbsp;
							<span id="dueDateEditSpanId">
								<label for="dueDateEditId">Due Date:</label>
								<input kendo-date-picker class="dateEditRange" name="dueDate" id="dueDate" k-ng-model="ac.dueDate"  k-format="'MM/dd/yyyy'" k-parse-formats ="['MM/dd/yyyy']" />
								<span class="error-msg" ng-show="form.dueDate.$error.date">Invalid date.</span>
							</span>
						</td>
					</tr>
					<tr class="prop" id="actStartShow" ng-show="isEdit">
						<td valign="top" class="name"><label for="actStartEditId">Actual Start/Finish:</label></td>
						<td valign="top" class="value" id="actStartEditId">
						   {{ (acData.atStart?acData.atStart:'') + ( (acData.dtResolved?(' - ' + acData.dtResolved):''))  }}
						</td>
					</tr >
					<tr class="prop">
						<td valign="top" class="name"><label for="status">Status:</label></td>
						<td>
							<status-select comment-id='ac.commentId' ng-model='ac.status'></status-select>
						</td>
					</tr>
					<%-- Dependencies section --%>
					<tr class="prop">
						<td valign="top" class="name">
							<label for="dependencies">Dependencies:</label>
						</td>
						<td>
						    <table style="border: none;">
								<tr class="prop" id="predecessorHeadTrId">
									<td valign="top" class="name" style="width: 50%">
										<label>Predecessors</label>
										<a class="button" href="javascript:" ng-click="$broadcast('addDependency','predecessor')"> Add </a>
									</td>
									<td valign="top" class="name" style="width: 50%">
										<label>Successors</label>
										<a class="button" href="javascript:" ng-click="$broadcast('addDependency','successor')"> Add </a>
									</td>
								</tr>
								<tr>
									<td style="vertical-align: top;">
										<task-dependencies ng-model="dependencies.predecessors" deleted="dependencies.deletedPredecessors" comment-id='ac.commentId' event-name="predecessor" move-event='ac.moveEvent' prefix="pred"></task-dependencies>
									</td>
									<td style="vertical-align: top;">
										<task-dependencies ng-model="dependencies.successors" deleted="dependencies.deletedSuccessors" comment-id='ac.commentId' event-name="successor" move-event='ac.moveEvent' prefix="succ"></task-dependencies>
									</td>
								</tr>
						    </table>
						</td>
					</tr>
					<tr id="processDiv" ng-show="havePredecessor">
						<td></td>
						<td><img id="processingId" src="${resource(dir:'images',file:'processing.gif')}" ng-show="!predecessorLoaded" /></td>
					</tr>
					<tr ng-show="isEdit">
						<td valign="top" class="name"><label for="createdBy">Created By:</label></td>
						<td valign="top" class="value" id="createdById">{{acData.personCreateObj?(acData.personCreateObj+" at "+acData.dtCreated):""}}</td>
					</tr>
				</table>
			</div>
			<div id="editResolveDiv" ng-show="isEdit">
				<table id="updateResolveTable" style="border: 0px;">
					<tr class="prop" ng-show="acData.notes.length > 0">
						<td valign="top" class="name"><label for="notes">Previous Notes:</label></td>
						<td valign="top" class="value" colspan="3">
							<div id="previousNote" style="width: 100%;">
								<table style="border:0px">
									<tr ng-repeat="note in acData.notes">
										<td style="width:20%">{{note[0]}}</td><td style="width:20%">{{note[1]}}</td><td style="width:60%"><span>{{note[2]}}</span></td>
									</tr>
								</table>
							</div>
						</td>
					</tr>
					<tr class="prop">
						<td valign="top" class="name"><label for="notes">Note:</label></td>
						<td valign="top" class="value" colspan="3">
							<textarea cols="80" rows="4" id="noteEditId" name="note" ng-model="ac.note"></textarea>
						</td>
					</tr>
				</table>
			</div>
		</div>

		<div class="buttons" ng-class="form.$invalid?'disabledButton' : ''">
			<button type="button" id="saveAndCloseBId" class="btn btn-default tablesave" ng-click="saveComment(true, form.$invalid)"><span class="glyphicon glyphicon-floppy-disk" aria-hidden="true"></span> Save</button>
			<button type="button" class="btn btn-default tablesave cancel" ng-click="close()"><span class="glyphicon glyphicon-ban-circle" aria-hidden="true"></span> Cancel</button>
		</div>
			
	</form>
	</div>
</div>