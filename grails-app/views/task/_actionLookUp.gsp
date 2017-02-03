<div draggable class="ui-dialog ui-widget ui-widget-content ui-corner-all ui-front" style="width: 650px" tabindex="-1" data-keyboard="false">
	<div class="ui-dialog-titlebar ui-widget-header ui-corner-all ui-helper-clearfix">
		<span id="ui-id-5" class="ui-dialog-title">Action - RIVERMEADOW</span>
		<button class="ui-button ui-widget ui-state-default ui-corner-all ui-button-icon-only ui-dialog-titlebar-close" role="button" aria-disabled="false" title="close" ng-click="close()">
		<span class="ui-button-icon-primary ui-icon ui-icon-closethick"></span>
		<span class="ui-button-text">close</span>
		</button>
	</div>
	<div id="actionLookUpDialog" class="comment-dialog-content">
	<loading-indicator></loading-indicator>

		<form name="actionLookUpForm" class="css-form">
            <div class="dialog" style="border: 1px solid #5F9FCF">
                <div>
                    <table id="updateCommentTable" class="inner">
                        <tr class="prop">
                            <td valign="top" class="name">
                                <label><b>Agent:</b></label>
                            </td>
                            <td valign="top" class="value">
                                <label>RIVERMEADOW</label>
                            </td>
                        </tr>
                        <tr class="prop">
                            <td valign="top" class="name">
                                <label><b>Method:</b></label>
                            </td>
                            <td valign="top" class="value">
                                <label>getTransportStatus</label>
                            </td>
                        </tr>
                        <tr class="prop">
                            <td valign="top" class="name" >
                                <label><b>Description:</b></label>
                            </td>
                            <td valign="top" class="value">
                                <textarea cols="80" rows="4" name="actionDescription" ng-maxlength="4000">Used to get the status of the Transport of a VM by RiverMeadow and complete task appropriately.</textarea>
                            </td>
                        </tr>
                        <tr class="prop">
                            <td valign="top" colspan="2">
                                <table>
                                    <thead>
                                        <tr>
                                            <th>Parameter</th>
                                            <th>Context</th>
                                            <th>Property</th>
                                            <th>Value</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <tr>
                                            <td>taskId</td>
                                            <td>TASK</td>
                                            <td>id</td>
                                            <td>12323</td>
                                        </tr>
                                        <tr>
                                            <td>serverName</td>
                                            <td>ASSET</td>
                                            <td>assetName</td>
                                            <td>server1</td>
                                        </tr>
                                    </tbody>
                                </table>
                            </td>
                        </tr>
                    </table>
                </div>
            </div>
            <div class="buttons">
                <button type="button" class="btn btn-default tablesave cancel" ng-click="close()">Close</button>
            </div>
		</form>
	</div>
</div>
