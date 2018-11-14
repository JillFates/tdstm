<div id="actionLookupModal" draggable class="ui-dialog ui-widget ui-widget-content ui-corner-all ui-front ui-draggable" tabindex="-1" data-keyboard="false">
	<div class="ui-dialog-titlebar ui-widget-header ui-corner-all ui-helper-clearfix">
		<span id="ui-id-5" class="ui-dialog-title">Action Summary</span>
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
                                <label><b>Dictionary:</b></label>
                            </td>
                            <td valign="top" class="value">
                                <label>${apiAction.agent}</label>
                            </td>
                        </tr>
                        <tr class="prop">
                            <td valign="top" class="name">
                                <label><b>Method:</b></label>
                            </td>
                            <td valign="top" class="value">
                                <label>${apiAction.method}</label>
                            </td>
                        </tr>
                        <tr class="prop">
                            <td valign="top" class="name" >
                                <label><b>Description:</b></label>
                            </td>
                            <td valign="top" class="value">
                                <textarea cols="80" rows="4" name="actionDescription" ng-maxlength="4000" readonly>${apiAction.description}</textarea>
                            </td>
                        </tr>
                        <tr class="prop">
                            <td valign="top" colspan="2">
                                <table id="updateCommentTableInnerTable">
                                    <thead>
                                        <tr>
                                            <th>Parameter Name</th>
                                            <th>Context</th>
                                            <th>Field Name</th>
                                            <th>Value</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <g:each var="methodParam" in="${apiAction.methodParams}">
                                            <tr>
                                                <td>${methodParam.paramName}</td>
                                                <td>${methodParam.contextLabel}</td>
                                                <td>${methodParam.fieldNameLabel}</td>
                                                <td>${apiAction.methodParamsValues[methodParam.paramName]}</td>
                                            </tr>
                                        </g:each>
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
