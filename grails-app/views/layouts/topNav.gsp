<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <title><g:layoutTitle default="Grails" /></title>

    <!-- Tell the browser to be responsive to screen width -->
    <meta content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no" name="viewport">

    <g:if test="${topNavClean}">
        <g:render template="../layouts/responsiveResources" />
    </g:if>
    <g:if test="${!topNavClean}">
        <g:render template="../layouts/responsiveStandardResources" />
    </g:if>

    <g:layoutHead />

    <link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'dropDown.css')}" />
    <link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'daterangepicker-bs3.css')}" />

    <g:javascript src="tdsmenu.js" />
    <g:javascript src="PasswordValidation.js" />

    <script type="text/javascript">
	$(document).ready(function() {
            $("#personDialog").dialog({ autoOpen: false });
            $("#userPrefDivId").dialog({ autoOpen: false });
            $("#userTimezoneDivId").dialog({ autoOpen: false });

            // Due to some issue with textarea overriding the value at intial load
            $('textarea').each(function(){
                $(this).val($(this).text());
            });
            $(".headerClass").mouseover(function(){
                $(this).parent().find('a').addClass('mouseover');
                $(this).parent().find('a').removeClass('mouseout');
            });
            $(".headerClass").mouseout(function(){
                if (!$(this).parent().find(".megamenu").is(":visible")) {
                    $(this).parent().find('a').removeClass('mouseover');
                } else {
                    $('.headerClass').removeClass('mouseover');
                }
            });

            $.datepicker.setDefaults({dateFormat: tdsCommon.jQueryDateFormat()});
            $.datetimepicker.setDefaults({dateFormat: tdsCommon.jQueryDateFormat()});

            $('div.ui-dialog.ui-widget').find('button.ui-dialog-titlebar-close').html('<span class="ui-button-icon-primary ui-icon ui-icon-closethick"></span>');
        });

        function updateEventHeader( e ){
            var newsAndStatus = eval("(" + e.responseText + ")")
            $("#head_mycrawlerId").html(newsAndStatus[0].news);
            $("#head_crawler").addClass(newsAndStatus[0].cssClass)
            $("#moveEventStatus").html(newsAndStatus[0].status)
        }

    </script>
</head>
<%
	def moveBundle = tds.currentMoveBundle() ?: null
	def moveEvent = tds.currentMoveEvent() ?: null
	def currProject = tds.currentProject() ?: null
	def room = tds.currentRoom() ?: null
	def person = tds.currentPerson() ?: null
	String partyGroup = tds.partyGroup() ?: null
	String setImage = tds.setImage() ?: null
	def userLogin = tds.userLogin() ?: null
	int minPasswordLength = tds.minPasswordLength()
%>

<!-- ADD THE CLASS layout-top-nav TO REMOVE THE SIDEBAR. -->
<body class="hold-transition skin-blue layout-top-nav">
<div class="wrapper">
    <header class="main-header">
        <input id="contextPath" type="hidden" value="${request.contextPath}"/>
        <input id="tzId" type="hidden" value="${tds.timeZone()}"/>
        <input id="userDTFormat" type="hidden" value="${tds.dateFormat()}"/>
        <nav class="navbar navbar-static-top">
            <div class="container menu-top-container">
                <div class="navbar-header">
                    <button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#navbar-collapse">
                        <i class="fa fa-bars"></i>
                    </button>
                    <g:if test="${setImage}">
                        <img src="${createLink(controller:'project', action:'showImage', id:setImage)}" alt="${currProjObj.name} project" style="height: 30px;  margin-top: 8px;"/>
                    </g:if>
                    <g:else>
                        <img id="logo-header" src="${resource(dir:'images',file:'TMHeaderLogo.png')}" alt="Transition Manager project" border="0" />
                    </g:else>
                </div>

                <!-- Collect the nav links, forms, and other content for toggling -->
                <div class="collapse navbar-collapse pull-left navbar-ul-container" id="navbar-collapse">
                    <g:if test="${currProject}">
                        <ul class="nav navbar-nav">
                            <tds:hasPermission permission='AdminMenuView'>
                                <li class="dropdown menu-parent-admin">
                                    <a href="#" class="dropdown-toggle" data-toggle="dropdown">Admin <span class="caret"></span></a>
                                    <ul class="dropdown-menu menu-item-expand" role="menu">
                                        <li class="menu-parent-item">Administration</li>
                                        <li class="menu-child-item menu-admin-portal"><g:link controller="admin" action="home">Admin Portal</g:link> </li>
                                        <li class="menu-child-item menu-admin-license-manager"><a href="/tdstm/app/#/license/admin/list">License Admin</a></li>
                                        <li class="menu-child-item menu-admin-license-manager"><a href="/tdstm/app/#/license/manager/list">License Manager</a></li>
                                        <li class="menu-child-item menu-admin-license-manager"><a href="/tdstm/app/#/notice/list">Notices</a></li>
                                        <li class="menu-child-item menu-admin-role">
                                            <tds:hasPermission permission='RolePermissionView'>
                                                <g:link controller="permissions" action="show">Role Permissions</g:link>
                                            </tds:hasPermission>
                                        </li>
                                        <li class="menu-child-item menu-admin-asset-options"><g:link controller="assetEntity" action="assetOptions">Asset Options</g:link></li>
                                        <li class="menu-child-item">
                                            <tds:hasPermission permission='HelpMenuView'>
                                                <a href="javascript:window.open('https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TMAdminPortal?cover=print','help');" >help</a>
                                            </tds:hasPermission>
                                        </li>
                                        <li class="divider"></li>
                                        <li class="menu-parent-item">Manage Clients</li>
                                        <li class="menu-child-item menu-list-companies"><g:link controller="partyGroup" action="list" params="[active:'active',tag_s_2_name:'asc']" id="${partyGroup}">List Companies</g:link></li>
                                        <li class="menu-child-item menu-list-staff"><g:link controller="person" id="${partyGroup}">List Staff</g:link></li>
                                        <li class="menu-child-item menu-list-users">
                                            <tds:hasPermission permission='UserLoginView'>
                                                <g:link controller="userLogin" id="${partyGroup}">List Users</g:link>
                                            </tds:hasPermission>
                                        </li>
                                        <li class="menu-child-item menu-client-import-accounts">
                                            <tds:hasPermission permission='PersonImport'>
                                                <g:link class="mmlink" controller="admin" action="importAccounts" >Import Accounts</g:link>
                                            </tds:hasPermission>
                                        </li>
                                        <li class="menu-child-item menu-client-export-accounts">
                                            <tds:hasPermission permission='PersonExport'>
                                                <g:link controller="admin" action="exportAccounts" >Export Accounts</g:link>
                                            </tds:hasPermission>
                                        </li>
                                        <li class="menu-child-item">
                                            <tds:hasPermission permission='HelpMenuView'>
                                                <a href="javascript:window.open('https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TMCreatePerson?cover=print','help');" >help</a>
                                            </tds:hasPermission>
                                        </li>
                                        <li class="divider"></li>
                                        <li class="menu-parent-item">Manage Workflows</li>
                                        <li class="menu-child-item menu-list-workflows"><g:link controller="workflow" action="home">List Workflows </g:link></li>
                                        <li class="menu-child-item">
                                            <tds:hasPermission permission='HelpMenuView'>
                                                <a href="javascript:window.open('https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TMManageWorkflows?cover=print','help');">help</a>
                                            </tds:hasPermission>
                                        </li>
                                        <li class="divider"></li>
                                        <li class="menu-parent-item">Manage Model Library</li>
                                        <li class="menu-child-item menu-list-manufacturers"><g:link controller="manufacturer" id="${partyGroup}">List Manufacturers</g:link></li>
                                        <li class="menu-child-item menu-list-models"><g:link controller="model" id="${partyGroup}">List Models</g:link></li>
                                        <li class="menu-child-item menu-sync-libraries"><g:link controller="model" action="importExport">Sync Libraries</g:link></li>
                                        <li class="menu-child-item">
                                            <tds:hasPermission permission='HelpMenuView'>
                                                <a href="javascript:window.open('https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TMModelLibrary?cover=print','help');">help</a>
                                            </tds:hasPermission>
                                        </li>
                                    </ul>
                                </li>
                            </tds:hasPermission>
                            <li class="dropdown menu-parent-projects">
                                <a href="#" class="dropdown-toggle" data-toggle="dropdown">Projects<span class="caret"></span></a>
                                <ul class="dropdown-menu menu-item-expand" role="menu">
                                    <li class="menu-child-item menu-projects-active-projects"><g:link class="mmlink" controller="project" action="list" params="[active:'active']" onclick="hideMegaMenu('projectMegaMenu')">Active Projects</g:link></li>
                                    <g:if test="${currProject}">
                                        <li class="menu-child-item menu-projects-current-project"><g:link class="mmlink" controller="projectUtil" onclick="hideMegaMenu('projectMegaMenu')"><g:if test="${currProject.name.size()>20}">${currProject.name.substring(0,20)+'...'}</g:if><g:else>${currProject.name}</g:else> Details</g:link></li>
                                        <li class="menu-child-item menu-projects-project-staff"><g:link class="mmlink" controller="person" action="manageProjectStaff"  onclick="hideMegaMenu('projectMegaMenu')">Project Staff</g:link></li>
                                        <tds:hasPermission permission='SendUserActivations'>
                                            <li class="menu-child-item menu-projects-user-activation"><g:link class="mmlink" controller="project" action="userActivationEmailsForm" onclick="hideMegaMenu('projectMegaMenu')">User Activation Emails</g:link></li>
                                        </tds:hasPermission>
                                        <li class="menu-child-item menu-projects-field-settings"><g:link class="mmlink" controller="project" action="fieldImportance" onclick="hideMegaMenu('projectMegaMenu')">Field Settings</g:link></li>
                                    </g:if>
                                    <g:else>
                                        <li class="menu-child-warn">No Project Selected</li>
                                    </g:else>
                                    <tds:hasPermission permission='HelpMenuView'>
                                        <li class="menu-child-item"><a  href="javascript:window.open('https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TMProjectStaff?cover=print','help');" >help</a></li>
                                    </tds:hasPermission>
                                </ul>
                            </li>
                            <li class="dropdown menu-parent-data-centers">
                                <a href="#" class="dropdown-toggle" data-toggle="dropdown">Data Centers<span class="caret"></span></a>
                                <ul class="dropdown-menu menu-item-expand" role="menu">
                                    <li class="menu-parent-item">Rooms and Racks</li>
                                    <li class="menu-child-item menu-parent-data-centers-list-rooms" ><g:link  params="[viewType:'list']" controller="room" >List Rooms</g:link></li>
                                    <g:if test="${room}">
                                        <li class="menu-child-item menu-parent-data-centers-selected-center"><g:link params="[roomId:room.id]" controller="room">Room ${room.location}/${room.roomName}</g:link></li>
                                    </g:if>
                                    <tds:hasPermission permission='AssetEdit'>
                                        <li class="menu-child-item menu-parent-data-centers-rack-elevation"><g:link controller="rackLayouts" action="create" >Rack Elevations</g:link></li>
                                    </tds:hasPermission>
                                    <tds:hasPermission permission='HelpMenuView'>
                                        <li class="menu-child-item"><a href="javascript:window.open('https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TMRooms?cover=print','help');">help</a></li>
                                    </tds:hasPermission>
                                </ul>
                            </li>
                            <tds:hasPermission permission='AssetMenuView'>
                                <li class="dropdown menu-parent-assets">
                                    <a href="#" class="dropdown-toggle" data-toggle="dropdown">Assets<span class="caret"></span></a>
                                    <ul class="dropdown-menu menu-item-expand" role="menu">
                                        <li class="menu-parent-item">Assets</li>
                                        <li class="menu-child-item menu-parent-assets-summary-table">
                                            <g:link class="mmlink" controller="assetEntity" action="assetSummary">
                                                <g:img uri="/icons/application_view_columns.png" width="16" height="16" alt="Summary Table"/>
                                                Summary Table
                                            </g:link>
                                        </li>
                                        <li class="menu-child-item menu-parent-assets-application-list">
                                            <g:link class="mmlink" controller="application" action="list" onclick="hideMegaMenu('assetMegaMenu')">
                                                <tds:svgIcon name="application" width="16" height="16" />
                                                Applications
                                            </g:link>
                                        </li>
                                        <li class="menu-child-item menu-parent-assets-server-list">
                                            <g:link class="mmlink" controller="assetEntity" action="list" params="[filter:'server']" onclick="hideMegaMenu('assetMegaMenu')">
                                                <tds:svgIcon name="serverPhysical" width="16" height="16" />
                                                Servers
                                            </g:link>
                                        </li>
                                        <li class="menu-child-item menu-parent-assets-all-list" >
                                            <g:link class="mmlink" controller="assetEntity" action="list" params="[filter:'all']" onclick="hideMegaMenu('assetMegaMenu')">
                                                <tds:svgIcon name="other" width="16" height="16" />
                                                All Devices
                                            </g:link>
                                        </li>
                                        <li class="menu-child-item menu-parent-assets-database-list">
                                            <g:link class="mmlink" controller="database"    action="list" onclick="hideMegaMenu('assetMegaMenu')">
                                                <tds:svgIcon name="database" width="16" height="16" />
                                                Databases
                                            </g:link>
                                        </li>
                                        <li class="menu-child-item menu-parent-assets-storage-list">
                                            <g:link class="mmlink" controller="assetEntity" action="list" params="[filter:'storage']" onclick="hideMegaMenu('assetMegaMenu')">
                                                <tds:svgIcon name="storagePhysical" width="16" height="16" />
                                                Storage-Devices
                                            </g:link>
                                        </li>
                                        <li class="menu-child-item menu-parent-assets-storage-logical-list">
                                            <g:link class="mmlink" controller="files"       action="list" onclick="hideMegaMenu('assetMegaMenu')">
                                                <tds:svgIcon name="storageLogical" width="16" height="16" />
                                                Storage-Logical
                                            </g:link>
                                        </li>
                                        <li class="menu-child-item menu-parent-assets-comments-list">
                                            <g:link class="mmlink" controller="assetEntity" action="listComment" onclick="hideMegaMenu('assetMegaMenu')">
                                                <g:img uri="/icons/comments.png" width="16" height="16" alt="Comments"/>
                                                Comments
                                            </g:link>
                                        </li>
                                        <li class="menu-child-item menu-parent-assets-dependencies-list">
                                            <g:link class="mmlink" controller="assetEntity" action="listDependencies" onclick="hideMegaMenu('assetMegaMenu')">
                                                <g:img uri="/icons/bricks.png" width="16" height="16" alt="Dependencies"/>
                                                Dependencies
                                            </g:link>
                                        </li>
                                        <tds:hasPermission permission='DepAnalyzerView'>
                                            <li class="menu-child-item menu-parent-assets-dependency-analyzer">
                                                <g:link class="mmlink" controller="moveBundle" action="dependencyConsole" onclick="hideMegaMenu('assetMegaMenu')">
                                                    <g:img uri="/icons/brick_magnify.png" width="16" height="16" alt="Dependency Analyzer"/>
                                                    Dependency Analyzer
                                                </g:link>
                                            </li>
                                        </tds:hasPermission>
                                        <tds:hasPermission permission='ArchitectureView'>
                                            <li class="menu-child-item menu-parent-assets-architecture-graph">
                                                <g:link class="mmlink" controller="assetEntity" action="architectureViewer" onclick="hideMegaMenu('assetMegaMenu')">
                                                    <g:img uri="/icons/chart_organisation.png" width="16" height="16" alt="Architecture Graph"/>
                                                    Architecture Graph
                                                </g:link>
                                            </li>
                                        </tds:hasPermission>
                                        <tds:hasPermission permission='HelpMenuView'>
                                            <li class="menu-child-item"><a href="javascript:window.open('https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TMAssetOverview?cover=print','help');">help</a></li>
                                        </tds:hasPermission>
                                        <li class="divider"></li>
                                        <li class="menu-parent-item">Manage Data</li>
                                        <tds:hasPermission permission='Import'>
                                            <li class="menu-child-item menu-parent-assets-import-assets"><g:link controller="assetEntity" action="assetImport" >Import Assets</g:link></li>
                                        </tds:hasPermission>
                                        <tds:hasPermission permission='Import'>
                                            <li class="menu-child-item menu-parent-assets-manage-batches"><g:link controller="dataTransferBatch" action="index" >Manage Batches</g:link></li>
                                        </tds:hasPermission>
                                        <tds:hasPermission permission='Export'>
                                            <li class="menu-child-item menu-parent-assets-export-assets"><g:link controller="assetEntity" action="exportAssets" >Export Assets</g:link></li>
                                        </tds:hasPermission>
                                        <tds:hasPermission permission='HelpMenuView'>
                                            <li class="menu-child-item"><a href="javascript:window.open('https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TMImportExport?cover=print','help');">help</a></li>
                                        </tds:hasPermission>
                                    </ul>
                                </li>
                            </tds:hasPermission>
                            <tds:hasPermission permission='EventMenuView'>
                                <li class="dropdown menu-parent-planning">
                                    <a href="#" class="dropdown-toggle" data-toggle="dropdown">Planning<span class="caret"></span></a>
                                    <ul class="dropdown-menu menu-item-expand" role="menu">
                                        <li class="menu-parent-item">Events</li>
                                        <li class="menu-child-item menu-parent-planning-event-list"><g:link controller="moveEvent" action="list" >List Events</g:link> </li>
                                        <g:if test="${currProject && moveEvent}">
                                            <li class="menu-child-item menu-parent-planning-event-detail-list"><g:link controller="moveEvent" action="show" id="${moveEvent.id}">${moveEvent.name} Event Details</g:link></li>
                                        </g:if>
                                        <tds:hasPermission permission='ShowListNews'>
                                            <li class="menu-child-item menu-parent-planning-event-news"><g:link controller="newsEditor" >List Event News</g:link></li>
                                        </tds:hasPermission>
                                        <tds:hasPermission permission="ShowMovePrep">
                                            <li class="menu-child-item"><g:link controller="reports" action="preMoveCheckList" >Pre-event Checklist</g:link></li>
                                        </tds:hasPermission>
                                        <li class="menu-child-item menu-parent-planning-export-runbook"><g:link controller="moveEvent" action="exportRunbook">Export Runbook</g:link></li>
                                        <tds:hasPermission permission='HelpMenuView'>
                                            <li class="menu-child-item"><a href="javascript:window.open('https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TMEvents?cover=print','help');" >help</a></li>
                                        </tds:hasPermission>
                                        <li class="divider"></li>
                                        <li class="menu-parent-item">Bundles</li>
                                        <li class="menu-child-item menu-parent-planning-list-bundles"><g:link controller="moveBundle" action="list">List Bundles</g:link> </li>
                                        <g:if test="${currProject && moveBundle}">
                                            <li class="menu-child-item menu-parent-planning-selected-bundle"><g:link controller="moveBundle" action="show">${moveBundle.name} Bundle Details</g:link></li>
                                            <li class="menu-child-item menu-parent-planning-bundled-assets"><g:link controller="moveBundleAsset" action="assignAssetsToBundle" params="[bundleId:moveBundle.id]">Bundled Assets</g:link> </li>
                                        </g:if>
                                        <tds:hasPermission permission='HelpMenuView'>
                                            <li class="menu-child-item"><a href="javascript:window.open('https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TMBundles?cover=print','help');">help</a></li>
                                        </tds:hasPermission>
                                    </ul>
                                </li>
                            </tds:hasPermission>
                            <tds:hasPermission permission='BundleMenuView'>
                                <li class="dropdown menu-parent-tasks">
                                    <a onclick="showMegaMenu('#teamMegaMenu')" href="#" class="dropdown-toggle" data-toggle="dropdown">Tasks<span class="caret"></span></a>
                                    <ul class="dropdown-menu menu-item-expand" role="menu">
                                        <li class="menu-parent-item">Tasks</li>
                                        <li class="menu-child-item menu-parent-tasks-my-tasks"><a href="/tdstm/task/listUserTasks">My Tasks (<span id="todoCountProjectId">&nbsp;</span>)</a></li>
                                        <tds:hasPermission permission='ViewTaskManager'>
                                            <li class="menu-child-item menu-parent-tasks-task-manager"><g:link controller="assetEntity" action="listTasks"  params="[initSession:true]">Task Manager</g:link></li>
                                        </tds:hasPermission>
                                        <tds:hasPermission permission='ViewTaskGraph'>
                                            <li class="menu-child-item menu-parent-tasks-task-graph"><g:link controller="task" action="taskGraph"  params="[initSession:true]" >Task Graph</g:link></li>
                                        </tds:hasPermission>
                                        <tds:hasPermission permission='ViewTaskTimeline'>
                                            <li class="menu-child-item menu-parent-tasks-task-timeline"><g:link controller="task" action="taskTimeline">Task Timeline</g:link></li>
                                        </tds:hasPermission>
                                        <tds:hasPermission permission="ViewRecipe">
                                            <li class="menu-child-item menu-parent-tasks-cookbook"><g:link controller="cookbook" action="index">Cookbook</g:link></li>
                                        </tds:hasPermission>
                                        <tds:hasPermission permission="GenerateTasks">
                                            <li class="menu-child-item menu-parent-tasks-generation-history"><a href="/tdstm/cookbook/index#/generationHistory">Generation History</a></li>
                                        </tds:hasPermission>

                                         <tds:hasPermission permission='GenerateTasks'>
                                            <li class="menu-child-item menu-parent-tasks-import-tasks">
                                                <g:link class="mmlink" controller="assetEntity" action="importTask" >Import Tasks</g:link>
                                            </li>
                                        </tds:hasPermission>

                                        <tds:hasPermission permission='HelpMenuView'>
                                            <li class="menu-child-item"><a href="javascript:window.open('https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TMTaskOverview?cover=print','help');">help</a></li>
                                        </tds:hasPermission>


                                    </ul>
                                </li>
                            </tds:hasPermission>
                            <tds:hasPermission permission='DashBoardMenuView'>
                                <li class="dropdown menu-parent-dashboard">
                                    <a href="#" class="dropdown-toggle" data-toggle="dropdown">Dashboards<span class="caret"></span></a>
                                    <ul class="dropdown-menu menu-item-expand" role="menu">
                                        <li class="menu-parent-item">Live Dashboards</li>
                                        <li class="menu-child-item menu-parent-dashboard-user-dashboard"><g:link controller="dashboard" action="userPortal" >User Dashboard</g:link></li>
                                        <tds:hasPermission permission='ShowPlanning'>
                                            <li class="menu-child-item menu-parent-dashboard-planning-dashboard"><g:link controller="moveBundle" action="planningStats" >Planning Dashboard</g:link></li>
                                        </tds:hasPermission>
                                        <li class="menu-child-item menu-parent-dashboard-event-dashboard"><g:link controller="dashboard">Event Dashboard</g:link></li>
                                    <!-- <%-- Removed until this report will be implemented using tasks
                                            <tds:hasPermission permission='ShowCartTracker'>
                                                <li class="menu-child-item"><g:link controller="cartTracking" action="cartTracking" >Cart Tracker</g:link></li>
                                            </tds:hasPermission>
                                        --%> -->
                                        <tds:hasPermission permission='HelpMenuView'>
                                            <li class="menu-child-item"><a href="javascript:window.open('https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TMDashboardOverview?cover=print','help');" >help</a></li>
                                        </tds:hasPermission>
                                    </ul>
                                </li>
                            </tds:hasPermission>
                            <tds:hasPermission permission='ReportMenuView'>
                                <li class="dropdown menu-parent-reports">
                                    <a href="#" class="dropdown-toggle" data-toggle="dropdown">Reports<span class="caret"></span></a>
                                    <ul class="dropdown-menu menu-item-expand" role="menu">
                                        <tds:hasPermission permission='ShowDiscovery'>
                                            <li class="menu-parent-item">Discovery</li>
                                            <li class="menu-child-item menu-reports-cabling-conflict"><a href="/tdstm/reports/retrieveBundleListForReportDialog?reportId=CablingConflict" >Cabling Conflict</a> </li>
                                            <li class="menu-child-item menu-reports-cabling-data"><a href="/tdstm/reports/retrieveBundleListForReportDialog?reportId=CablingData">Cabling Data</a> </li>
                                            <li class="menu-child-item menu-reports-power"><a href="/tdstm/reports/powerReport">Power</a> </li>
                                            <tds:hasPermission permission='HelpMenuView'>
                                                <li class="menu-child-item"><a href="javascript:window.open('https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TranManHelp?cover=print','help');">help</a></li>
                                            </tds:hasPermission>
                                        </tds:hasPermission>
                                        <li class="divider"></li>
                                        <li class="menu-parent-item">Planning</li>
                                        <li class="menu-child-item menu-reports-application-profiles"><a href="/tdstm/reports/applicationProfiles">Application Profiles</a> </li>
                                        <li class="menu-child-item menu-reports-application-conflicts"><a href="/tdstm/reports/applicationConflicts">Application Conflicts</a> </li>
                                        <li class="menu-child-item menu-reports-server-conflicts"><a href="/tdstm/reports/serverConflicts">Server Conflicts</a> </li>
                                        <li class="menu-child-item menu-reports-database-conflicts"><a href="/tdstm/reports/databaseConflicts" >Database Conflicts</a> </li>
                                        <tds:hasPermission permission='ShowPlanning'>
                                            <li class="menu-child-item menu-reports-task-report"><a href="/tdstm/reports/retrieveBundleListForReportDialog?reportId=Task+Report">Task Report</a> </li>
                                        </tds:hasPermission>
                                        <li class="menu-child-item menu-reports-report-summary"><g:link controller="reports" params="[projectId:currProject?.id]">Report Summary</g:link></li>
                                        <tds:hasPermission permission='ShowProjectDailyMetrics'>
                                            <li class="menu-child-item menu-reports-activity-metrics"><a href="/tdstm/reports/projectActivityMetrics">Activity Metrics</a> </li>
                                        </tds:hasPermission>
                                        <tds:hasPermission permission='ShowMovePrep'>
                                            <li class="divider"></li>
                                            <li class="menu-parent-item">Event Prep</li>
                                            <tds:hasPermission permission="ShowMovePrep">
                                                <li class="menu-child-item menu-reports-pre-checklist"><a href="/tdstm/reports/preMoveCheckList">Pre-event Checklist</a> </li>
                                            </tds:hasPermission>
                                            <li class="menu-child-item menu-reports-login-badges"><a href="/tdstm/reports/retrieveBundleListForReportDialog?reportId=Login+Badges">Login Badges</a> </li>
                                            <li class="menu-child-item menu-reports-asset-tags"><a href="/tdstm/reports/retrieveBundleListForReportDialog?reportId=Asset+Tag">Asset Tags</a> </li>
                                            <li class="menu-child-item menu-reports-transport-worksheets"><a href="/tdstm/reports/retrieveBundleListForReportDialog?reportId=Transportation+Asset+List">Transport Worksheets</a></li>
                                            <tds:hasPermission permission='HelpMenuView'>
                                                <li class="menu-child-item"><a href="javascript:window.open('https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TranManHelp?cover=print','help');">help</a></li>
                                            </tds:hasPermission>
                                        </tds:hasPermission>
                                        <tds:hasPermission permission='ShowMoveDay'>
                                            <li class="divider"></li>
                                            <li class="menu-parent-item">Event Day</li>
                                            <li class="menu-child-item menu-reports-application-migration"><a href="/tdstm/reports/applicationMigrationReport" onclick="hideMegaMenu('reportsMegaMenu')">Application Migration Results</a> </li>
                                            <li class="menu-child-item menu-reports-issue-report"><a href="/tdstm/reports/retrieveBundleListForReportDialog?reportId=Issue+Report" >Issue Report</a> </li>
                                            <li class="menu-child-item"><a href="/tdstm/reports/retrieveBundleListForReportDialog?reportId=CablingQA">Cabling QA</a> </li>
                                            <tds:hasPermission permission='HelpMenuView'>
                                                <li class="menu-child-item"><a href="javascript:window.open('https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TranManHelp?cover=print','help');">help</a></li>
                                            </tds:hasPermission>
                                        </tds:hasPermission>
                                    </ul>
                                </li>
                            </tds:hasPermission>
                        </ul>
                    </g:if>
                </div><!-- /.navbar-collapse -->
            <!-- Navbar Right Menu -->
                <div class="navbar-custom-menu">
                    <ul class="nav navbar-nav">
                        <!-- Notifications Menu -->
                        <li class="dropdown notifications-menu">
                            <!-- Menu toggle button -->
                            <a href="#" id="nav-project-name" class="dropdown-toggle" data-toggle="dropdown">
                                <g:if test="${currProject}"> ${currProject.name} </g:if>
                                <g:if test="${moveEvent}"> : ${moveEvent.name}</g:if>
                                <g:if test="${moveBundle}"> : ${moveBundle.name}</g:if>
                            </a>
                        </li>
                        <sec:ifLoggedIn>
                            <!-- User Account Menu -->
                            <li class="dropdown user user-menu">
                                <!-- Menu Toggle Button -->
                                <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                                    <!-- The user image in the navbar-->
                                    <img src="${resource(dir:'images',file:'personIcon.png')}" class="user-image" alt="${session.getAttribute("LOGIN_PERSON").name }">
                                    <!-- hidden-xs hides the username on small devices so only the image appears. -->
                                    <span class="hidden-xs user-name">${tds.currentPersonName()}</span>
                                </a>
                                <ul class="dropdown-menu user-dialog-dropdown-menu">
                                    <!-- Menu Body -->
                                    <li class="user-body">
                                        <ul class="list-group">
                                            <li class="list-group-item"><g:remoteLink controller="person" action="retrievePersonDetails" id="${person?.id}" onComplete="updatePersonDetails(XMLHttpRequest)"><span class="glyphicon glyphicon-user user-menu-icon-badge"></span> Account Details</g:remoteLink></li>
                                            <li class="list-group-item"><a href="#" style="cursor: pointer;" id="editTimezoneId" name="${userLogin.username}" onclick="UserPreference.editDateAndTimezone()"><span class="glyphicon glyphicon-time user-menu-icon-badge"></span> Date and Timezone</a></li>
                                            <li class="list-group-item"><a href="#" style="cursor: pointer;" id="resetPreferenceId" name="${userLogin.username}" onclick="UserPreference.editPreference()"><span class="glyphicon glyphicon-pencil user-menu-icon-badge"></span> Edit preferences</a></li>
                                        <!-- <li class="list-group-item"><g:link class="home mmlink" controller="task" action="listUserTasks" params="[viewMode:'mobile',tab:tab]">Use Mobile Site</g:link></li> -->
                                            <g:if test="${person?.modelScore}">
                                                <li class="list-group-item"><a href="/tdstm/person/list/18?maxRows=25&tag_tr_=true&tag_p_=1&tag_mr_=25&tag_s_5_modelScore=desc"><span class="glyphicon glyphicon-info-sign user-menu-icon-badge"></span> Model Score <span class="badge">${person?.modelScore}</span></a></li>
                                            </g:if>
                                        </ul>
                                    </li>
                                    <!-- Menu Footer-->
                                    <li class="user-footer">
                                        <div class="pull-right">
                                            <g:link controller="auth" action="signOut" class="btn btn-default btn-flat"><span class="glyphicon glyphicon-log-out user-menu-icon-badge"></span> Sign Out</g:link>
                                        </div>
                                    </li>
                                </ul>
                            </li>
                        </sec:ifLoggedIn>
                    </ul>
                </div><!-- /.navbar-custom-menu -->
            </div><!-- /.container-fluid -->
        </nav>
    </header>
    <!-- Full Width Column -->
    <div class="content-wrapper">
        <g:if test="${currProj}">
        <%-- Include the News crawler if there is an event in progress --%>
            <g:if test="${currProjObj?.runbookOn && moveEvent && (moveEvent?.newsBarMode == 'on' || (moveEvent?.newsBarMode == 'auto' && moveEvent?.estStartTime))}">
                <g:if test="${moveEvent?.newsBarMode == 'auto'}">
                    <g:if test="${moveEvent?.estStartTime.getTime() < new Date().getTime()}">
                        <g:set var="showNewsBar" value="true" />
                    </g:if>
                </g:if>
                <g:elseif test="moveEvent?.newsBarMode == 'on'">
                    <g:set var="showNewsBar" value="true" />
                </g:elseif>

                <g:if test="${showNewsBar}">
                    <div class="menu3" id="head_crawler" >
                        <div id="crawlerHead">${moveEvent.name} Event Status <span id="moveEventStatus"></span>. News: </div>
                        <div id="head_mycrawler"><div id="head_mycrawlerId" style="width: 1200px; height:25px; vertical-align:bottom" > </div></div>
                    </div>
                    <script type="text/javascript">
                        ${remoteFunction(controller:'moveEvent', action:'retrieveMoveEventNewsAndStatus', params:'\'id='+moveEventId+'\'',onComplete:'updateEventHeader(XMLHttpRequest)')}
                    </script>
                </g:if>
            </g:if>
        </g:if>
        <div class="container">
            <g:layoutBody />
        </div>
        <!-- /.container -->
    </div>
    <!-- /.content-wrapper -->
    <footer class="main-footer">
        <div class="container">
            <div class="pull-right hidden-xs">
                <b></b>
            </div>
            <strong><a href="http://www.transitionaldata.com/service/transitionmanager" target="_blank">&nbsp;TransitionManager&trade;</a> 2015-2016 .</strong> All
        rights reserved.
        </div>
        <!-- /.container -->
    </footer>
</div>

    <%-- DIV for editing User Profile --%>
    <g:render template="../person/personEdit" model="[user:userLogin, minPasswordLength:minPasswordLength]" />

    <%-- DIV for editing User Preferences --%>
    <div id="userPrefDivId" style="display: none;" title="${tds.currentPersonName()} Preferences"></div>

	<%-- DIV for editing User date and timezone --%>
	<div id="userTimezoneDivId" style="display: none;" title="${tds.currentPersonName()} Date and Timezone"></div>

</body>
</html>
