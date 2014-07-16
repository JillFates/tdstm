<div draggable centered id="showTaskPopup" class="ui-dialog ui-widget ui-widget-content ui-corner-all ui-front" role="dialog" style="width: 1000px" tabindex="-1">

    <div class="ui-dialog-titlebar ui-widget-header ui-corner-all ui-helper-clearfix">
        <span id="ui-id-5" class="ui-dialog-title">Task Details</span>
        <button class="ui-button ui-widget ui-state-default ui-corner-all ui-button-icon-only ui-dialog-titlebar-close" role="button" aria-disabled="false" title="close" ng-click="close()">
        <span class="ui-button-icon-primary ui-icon ui-icon-closethick"></span>
        <span class="ui-button-text">close</span>
        </button>
    </div>
    <div id="showCommentDialog" title="Comment/Issue detail" class="comment-dialog-content">
        <div class="dialog" style="border: 1px solid #5F9FCF">
            <div>
                <table id="showCommentTable" style="border: 0px;">
                    <tr class="prop">
                        <td valign="top" class="name"><label for="comment"><b><span id="taskNumberId">Task #<b>{{ac.taskNumber}}</b></span>:</b></label></td>
                        <td valign="top" class="value" colspan="3">
                            <textarea cols="80" rows="2" id="commentTdId" readonly="readonly">{{ac.comment}}</textarea>
                        </td>
                    </tr>
                    <tr id="assignedToTrId">
                        <td valign="top" class="name"><label for="assignedTo">Person/Team:</label></td>
                        <td valign="top" class="value" id="" colspan="3">
                            <span id="assignedToTdId">{{acData.assignedTo}}</span>&nbsp;/&nbsp;<span id="roleTdId">{{acData.roles}}</span>&nbsp;&nbsp;
                            <br><input type="checkbox" id="hardAssignedShow" name="hardAssignedShow" value="0"
                            ng-checked="ac.hardAssigned == 1" ng-disabled="true" ng-model="ac.hardAssigned" />&nbsp;&nbsp;
                            <label for="hardAssignedShow" >Fixed Assignment</label>&nbsp;&nbsp;
                        </td>
                    </tr>
                    <tr id="moveShowId" class="prop">
                        <td valign="top" class="name" id="eventTdId"><label for="moveEvent">Event:</label></td>
                        <td valign="top" class="value" id="eventName" colspan="3">{{acData.eventName}}</td>
                    </tr>
                    <tr id="categoryTrId">
                        <td valign="top" class="name"><label for="category">Category:</label></td>
                        <td valign="top" class="value" id="categoryTdId" style="width:15%">{{ac.category}}</td>
                        <td>
                        </td>
                    </tr>
                    <tr id="workFlowShow" ng-show="(acData.workflow)">
                        <td valign="top" class="name" nowrap="nowrap"><label for="workFlowShowId">WorkFlow Step:</label></td>
                        <td valign="top" class="value" id="workFlowShowId">{{acData.workflow}}</td>
                        <td valign="top" class="name" colspan="2"><input type="checkbox" id="overrideShow" name="overrideShow" value="0"
                            ng-true-value="1" ng-false-value="0" ng-disabled="true" ng-model="ac.override" />
                            <label for="override" >Overridden</label>
                        </td>
                    </tr>
                    <tr id="assetShowId" class="prop">
                        <td valign="top" class="name" id="assetTdId"><label for="asset">Asset:</label></td>
                        <td valign="top" class="value" id="assetShowValueId" colspan="3">
                            <a ng-click="getEntityDetails();">{{acData.assetName}}</a>
                        </td>
                    </tr>
                    <tr id="workFlowShow">
                        <td valign="top" class="name"><label for="durationShowId">Duration:</label></td>
                        <td valign="top" class="value"colspan="3" nowrap="nowrap">
                            <span id="durationShowId" >{{ac.duration}}</span>
                            <span id="durationScale" >{{ac.durationScale}}</span>
                            &nbsp;&nbsp;&nbsp;&nbsp;
                            <span ><label for="priorityShowId">Priority:</label></span>
                            <span id="priorityShowId"><b ng-show="ac.priority == 1 || ac.priority == 2">{{ac.priority}}</b><span ng-show="!(ac.priority == 1 || ac.priority == 2)">{{ac.priority}}</span></span>
                            &nbsp;&nbsp;&nbsp;&nbsp;
                            <span ><label for="dueDateShowId">Due Date:</label></span>
                            <span id="dueDateShowId">{{ac.dueDate}}</span>
                        </td>
                    </tr>
                    <tr id="estStartShow">
                        <td valign="top" class="name" nowrap="nowrap"><label for="estStartShowId">Estimated Start:</label></td>
                        <td valign="top" class="value" id="estStartShowId" nowrap="nowrap">{{acData.etStart}}</td>
                        <td valign="top" class="name" nowrap="nowrap"><label for="estFinishShowId">Estimated Finish:</label></td>
                        <td valign="top" class="value" id="estFinishShowId" nowrap="nowrap">{{acData.etFinish}}</td>
                    </tr>
                    <tr id="actStartShow">
                        <td valign="top" class="name"><label for="actStartShowId">Actual Start:</label></td>
                        <td valign="top" class="value" id="actStartShowId">{{acData.atStart}}</td>
                        <td valign="top" class="name" nowrap="nowrap" width="10%"><label for="actFinishShowId">Actual Finish:</label></td>
                        <td valign="top" class="value" id="actFinishShowId" nowrap="nowrap">{{acData.dtResolved}}</td>
                    </tr >
                    <tr class="prop">
                        <td valign="top" class="name"><label for="status">Status:</label></td>
                        <td valign="top" ng-class="acData.cssForCommentStatus" id="statusShowId" colspan="1" style="width: 20%">{{ac.status}}&nbsp;</td>
                    </tr>
                    <%-- Dependencies Section --%>
                    <tr>
                        <td valign="top">Dependencies:</td>
                        <td valign="top" colspan="3">
                            <table class="inner">
                                <tr>
                                    <td width="50%">Predecessors:</td>
                                    <td width="50%">Successors:</td>
                                </tr>
                                <tr>
                                    <td>
                                        <table cellspacing="0" style="border:0px;">
                                            <tbody>
                                                <tr ng-repeat="predecessor in acData.predecessorList" style="cursor:pointer;" class="task_{{predecessor.status.toLowerCase()}}" ng-click="viewTask(predecessor.taskId)">
                                                    <td>{{predecessor.category}}</td><td>{{predecessor.taskNumber}}:{{predecessor.desc}}</td>
                                                </tr>
                                            </tbody>
                                        </table>
                                    </td>
                                    <td>
                                        <table cellspacing="0" style="border:0px;">
                                            <tbody>
                                                <tr ng-repeat="successor in acData.successorList" style="cursor:pointer;" class="task_{{successor.status.toLowerCase()}}" ng-click="viewTask(successor.taskId)">
                                                    <td>{{successor.category}}</td><td>{{successor.taskNumber}}:{{successor.desc}}</td>
                                                </tr>
                                            </tbody>
                                        </table>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                    <tr>
                        <td valign="top" class="name"><label for="createdBy">Created By:</label></td>
                        <td valign="top" class="value" id="createdById" colspan="3">{{acData.personCreateObj?(acData.personCreateObj+" at "+acData.dtCreated):""}}</td>
                    </tr>
                    <tr class="prop" ng-show="acData.notes.length > 0">
                        <td valign="top" class="name"><label for="previousNotes">Prev. Notes:</label></td>
                        <td valign="top" class="value" colspan="3">
                            <div id="previousNotesShowId">
                                <table style="border:0px">
                                    <tr ng-repeat="note in acData.notes" >
                                        <td style="width:20%">{{note[0]}}</td><td style="width:20%">{{note[1]}}</td><td style="width:60%"><span>{{note[2]}}</span></td>
                                    </tr>
                                </table>
                            </div>
                        </td>
                    </tr>
                    <tr class="prop" id="predecessorTrShowId" style="display: none">
                        <td valign="top" class="name"><label for="predecessorShowId">Predecessor:</label></td>
                        <td valign="top" class="value" id="predecessorShowId" colspan="3"></td>
                    </tr>
                </table>
                <table id="showResolveTable" style="border: 0px">
                    <tr class="prop">
                        <td valign="top" class="name"><label for="resolution">Resolution:</label></td>
                        <td valign="top" class="value" colspan="6">
                            <div id="resolutionId">{{ac.resolution}}</div>
                        </td>
                    </tr>
                    <tr>
                        <td valign="top" class="name" nowrap="nowrap"><label for="resolvedBy">Resolved By:</label></td>
                        <td valign="top" class="value" id="resolvedById" nowrap="nowrap">{{acData.personResolvedObj}}</td>
                    </tr>
                </table>
                
                <tds:hasPermission permission='CommentCrudView'>
                    <div>
                        <div class="buttons" style="white-space: nowrap;">
                            <span class="button" class="slide">
                            <span class="slide">
                            <input class="edit" type="button" value="Edit" id="commentButtonEditId" ng-click="editComment();" />
                            </span>
                            <span id="fromAssetId" class="slide">
                            <input class="delete" type="button" value="Delete" ng-click="deleteComment()" />
                            </span>
                            </span>
                            <action-bar comment='ac' show-details='false' update-table='false'></action-bar>
                        </div>
                    </div>
                </tds:hasPermission>
            </div>
        </div>
        <loading-indicator></loading-indicator>
    </div>
</div>