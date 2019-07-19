<%@page import="net.transitionmanager.security.Permission"%>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="topNav"/>
    <title>Bundle Details</title>
    <script type="text/javascript">

        /*
         function to invoke ESC key to abandon the field
         */
        document.onkeypress = keyCheck;
        function keyCheck(e) {
            var keyID
            if (window.event) {
                keyID = window.event.keyCode;
            } else {
                keyID = e.keyCode;
            }
            if (keyID == 27) {
                $("span[title='input']").each(function () {
                    $(this).hide(); // hide value input field
                });
                $("span[title='text']").each(function () {
                    $(this).show(); // show the value text
                });
            }
        }
        /*
         * update the value once ajax request success
         */

        function updateStepValue(e, stepId, value) {
            if (e.status == 200) {
                $("#tasksCompletedText_" + stepId).html(value);
                $("#tasksCompletedText_" + stepId).show();
                $("#tasksCompletedInput_" + stepId).hide();
            } else {
                alert("Error : " + e.status + ", Record not created")
            }
        }
        // validate the dial manual input valueinput value
        function validateManualVal(value) {
            var check = true
            if (!isNaN(value)) {
                if (value > 100) {
                    alert("Manual step value should not be greater than 100")
                    check = false
                }
            } else {
                alert("Manual step value should be Alpha Numeric")
                check = false
            }
            return check
        }

        function warning(isDefaultBundle, deleteType) {
            var check = true
            if (isDefaultBundle == 'true') {
                alert("WARNING: You can not delete a project's default bundle");
                check = false;
            } else {
                switch (deleteType) {
                    case 'delete':
                        return confirm('WARNING: Deleting this bundle will remove any teams and any related step data?');
                        break;
                    case 'deleteBundleAndAssets':
                        return confirm('WARNING: Deleting this bundle will remove any teams, any related step data, AND ASSIGNED ASSETS? (NO UNDO)?');
                        break;
                    case 'history':
                        return confirm('WARNING: Are you sure you want to permanently clear transitions for assets in this bundle?');
                        break;
                }
            }
            return check
        }


    </script>
</head>

<body>
<tds:subHeader title="Bundle Detail" crumbs="['Planning','Bundles', 'Detail']"/>
<div class="body">
    <div class="nav" style="border: 1px solid #CCCCCC; width: 300px; margin:9px 14px 0px">
        <span class="menuButton"><g:link class="list" action="list">Bundle List</g:link></span>
        <tds:hasPermission permission="${Permission.BundleEdit}">
            <span class="menuButton"><g:link class="create" action="create">Create Bundle</g:link></span>
        </tds:hasPermission>
    </div>

    <div class="body" style="width: 330px;">
        <div class="steps_table" style="float: none;">
            <span class="span"><b>Bundle Details</b></span>
            <g:if test="${flash.message}">
                <div class="message">${flash.message}</div>
            </g:if>
            <table>
                <tbody>

                <tr class="prop">
                    <td valign="top" class="name">Name:</td>

                    <td valign="top" class="value">
                        ${fieldValue(bean: moveBundleInstance, field: 'name')}
                        <g:if test="${isDefaultBundle}">(Default)</g:if>
                    </td>

                </tr>

                <tr class="prop">
                    <td valign="top" class="name">Description:</td>

                    <td valign="top" class="value">${fieldValue(bean: moveBundleInstance, field: 'description')}</td>

                </tr>

                <tr class="prop">
                    <td valign="top" class="name">From:</td>

                    <td valign="top" class="value">${moveBundleInstance.sourceRoom}</td>

                </tr>

                <tr class="prop">
                    <td valign="top" class="name">To:</td>

                    <td valign="top" class="value">${moveBundleInstance.targetRoom}</td>

                </tr>

                <tr class="prop">
                    <td valign="top" class="name">Start Time:</td>

                    <td valign="top" class="value"><tds:convertDateTime date="${moveBundleInstance?.startTime}"
                                                                        format="12hrs"/></td>

                </tr>

                <tr class="prop">
                    <td valign="top" class="name">Completion Time:</td>

                    <td valign="top" class="value"><tds:convertDateTime date="${moveBundleInstance?.completionTime}"
                                                                        format="12hrs"/></td>

                </tr>

                <tr class="prop">
                    <td valign="top" class="name">Project Manager:</td>

                    <td valign="top" class="value">
                        <g:if test="${projectManager}">
                            ${projectManager?.partyIdTo?.lastNameFirstAndTitle}
                        </g:if>
                    </td>

                </tr>

                <tr class="prop">
                    <td valign="top" class="name">Event Manager:</td>

                    <td valign="top" class="value">
                        <g:if test="${moveManager}">
                            ${moveManager?.partyIdTo?.lastNameFirstAndTitle}
                        </g:if>
                    </td>

                </tr>
                <tr class="prop">
                    <td valign="top" class="name">Event:</td>

                    <td valign="top" class="value">
                        <g:each var="e" in="${moveBundleInstance.moveEvent}">
                            <li><g:link controller="moveEvent" action="show"
                                        id="${e.id}">${e?.encodeAsHTML()}</g:link></li>
                        </g:each>
                    </td>

                </tr>

                <tr class="prop">
                    <td valign="top" class="name">Order:</td>

                    <td valign="top"
                        class="value">${fieldValue(bean: moveBundleInstance, field: 'operationalOrder')}</td>

                </tr>

                <tr class="prop">
                    <td valign="top" class="name">WorkFlow Code</td>

                    <td valign="top" class="value">${fieldValue(bean: moveBundleInstance, field: 'workflowCode')}</td>

                </tr>

                <tr class="prop">
                    <td valign="top" class="name">Use For Planning:</td>

                    <td valign="top" class="value">
                        <g:if test="${moveBundleInstance.useForPlanning == true}">
                            <input type="checkbox" id="useForPlanning" name="useForPlanning" checked="checked"
                                   disabled="disabled"/>
                        </g:if>
                        <g:else>
                            <input type="checkbox" id="useForPlanning" name="useForPlanning" disabled="disabled"/>
                        </g:else></td>

                </tr>
                </tbody>
            </table>
        </div>

        <div class="buttons" style="float: left;">
            <g:form>
                <input type="hidden" name="id" value="${moveBundleInstance?.id}"/>
                <input type="hidden" name="projectId" value="${projectId}"/>
                <tds:hasPermission permission="${Permission.BundleEdit}">
                    <span class="button"><g:actionSubmit class="edit" value="Edit"/></span>
                    <g:if test="${!isDefaultBundle}">
                        <span class="button"><g:actionSubmit class="delete"
                             onclick="return warning('${isDefaultBundle}', 'delete')"
                             value="Delete"/>
                        </span>
                        <span class="button"><g:actionSubmit class="delete" action="deleteBundleAndAssets"
                             onclick="return warning('${isDefaultBundle}', 'deleteBundleAndAssets')"
                             value="Delete Bundle &amp; Assets"/>
                        </span>
                    </g:if>
                </tds:hasPermission>
            </g:form>
        </div>
    </div>

    <div class="body" style="margin: 0;">
        <div class="steps_table">
            <span class="span"><b>Dashboard Steps</b></span>
            <table id="assetEntityTable">
                <thead>
                <tr>
                    <th>Dashboard Label</th>
                    <th>Start</th>
                    <th>Completion</th>
                    <th>Duration</th>
                    <th>Type
                        <input type="hidden" name="moveBundleId" id="moveBundleId" value="${moveBundleInstance?.id}"/>
                        <input type="hidden" name="projectId" value="${projectId}"/></th>
                    <th>Be Green</th>
                </tr>
                </thead>
                <tbody id="commetAndNewsBodyId">
                <g:each in="${dashboardSteps}" status="i" var="dashboardStep">
                    <tr class="${(i % 2) == 0 ? 'even' : 'odd'}" id="commentsRowId_${dashboardStep.moveBundleStep.id}">
                        <td>
                            ${dashboardStep.moveBundleStep.label}
                        </td>
                        <td><tds:convertDateTime date="${dashboardStep.moveBundleStep.planStartTime}"/></td>
                        <td><tds:convertDateTime date="${dashboardStep.moveBundleStep.planCompletionTime}"/></td>
                        <td><tds:formatIntoHHMMSS value="${dashboardStep.moveBundleStep?.planDuration}"/></td>
                        <td>
                            <g:if test="${dashboardStep.moveBundleStep?.calcMethod != 'L'}">
                                Manual
                            </g:if>
                            <g:else>Linear</g:else>
                        </td>
                        <td>
                            <span id="beGreenDiv_${dashboardStep.moveBundleStep.id}" style="text-align: center;">
                                <g:if test="${dashboardStep.moveBundleStep?.showInGreen}">
                                    <input type="checkbox" name="beGreen_${dashboardStep.moveBundleStep.id}"
                                           id="beGreen_${dashboardStep.moveBundleStep.id}" checked="checked"
                                           disabled="disabled"/>
                                </g:if>
                                <g:else>
                                    <input type="checkbox" name="beGreen_${dashboardStep.moveBundleStep.id}"
                                           id="beGreen_${dashboardStep.moveBundleStep.id}" disabled="disabled"/>
                                </g:else>
                            </span>
                        </td>
                    </tr>
                </g:each>
                </tbody>
            </table>
        </div>
    </div>
    <script>
        currentMenuId = "#eventMenu";
        $(".menu-parent-planning-selected-bundle").addClass('active');
        $(".menu-parent-planning").addClass('active');
        $(document).ready(function () {
            $("#bForBundle").dialog({autoOpen: false})

        });

        // Every time we land on a page that update the state, we make the proper change inside the model
        if (stateManagement ) {
            stateManagement.setBundle(${moveBundleInstance?.id}, '${moveBundleInstance?.name}');
        }
    </script>
</div>
</body>
</html>
