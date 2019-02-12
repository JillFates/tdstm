<%@page import="net.transitionmanager.security.Permission"%>
    <!-- Collect the nav links, forms, and other content for toggling -->
    <div class="collapse navbar-collapse pull-left navbar-ul-container navbar-ul-container-full-menu" id="navbar-collapse">
        <g:if test="${currProject}">
            <ul class="nav navbar-nav">
                <tds:hasPermission permission="${Permission.AdminMenuView}">
                    <li class="dropdown menu-parent-admin">
                        <a href="#" class="dropdown-toggle" data-toggle="dropdown">Admin
                            <span class="caret"></span>
                        </a>
                        <ul class="dropdown-menu menu-item-expand" role="menu">
                            <li class="menu-parent-item">Administration</li>
                            <li class="menu-child-item menu-admin-portal">
                                <g:link controller="admin" action="home">Admin Portal</g:link>
                            </li>
                            <tds:hasPermission permission="${Permission.LicenseAdministration}">
                                <li class="menu-child-item menu-admin-license-admin">
                                    <a href="/tdstm/module/license/admin/list">License Admin</a>
                                </li>
                            </tds:hasPermission>
                            <g:if test="${isLicenseManagerEnabled}">
                                <li class="menu-child-item menu-admin-license-manager">
                                    <a href="/tdstm/app/license/manager/list">License Manager</a>
                                </li>
                            </g:if>
                            <li class="menu-child-item menu-admin-notice-manager">
                                <a href="/tdstm/module/notice/list">Notices</a>
                            </li>
                            <li class="menu-child-item menu-admin-role">
                                <tds:hasPermission permission="${Permission.RolePermissionView}">
                                    <g:link controller="permissions" action="show">Role Permissions</g:link>
                                </tds:hasPermission>
                            </li>
                            <li class="menu-child-item menu-admin-asset-options">
                                <g:link controller="assetEntity" action="assetOptions">Asset Options</g:link>
                            </li>
                            <li class="menu-child-item">
                                <tds:hasPermission permission="${Permission.HelpMenuView}">
                                    <a href="javascript:window.open('https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TMAdminPortal?cover=print','help');">help</a>
                                </tds:hasPermission>
                            </li>
                            <li class="divider"></li>
                            <li class="menu-parent-item">Manage Clients</li>
                            <li class="menu-child-item menu-list-companies">
                                <g:link controller="partyGroup" action="list" params="[active:'active',tag_s_2_name:'asc']" id="${partyGroup}">List Companies</g:link>
                            </li>
                            <li class="menu-child-item menu-list-staff">
                                <g:link controller="person" id="${partyGroup}">List Staff</g:link>
                            </li>
                            <li class="menu-child-item menu-list-users">
                                <tds:hasPermission permission="${Permission.UserView}">
                                    <g:link controller="userLogin" id="${partyGroup}">List Users</g:link>
                                </tds:hasPermission>
                            </li>
                            <li class="menu-child-item menu-client-import-accounts">
                                <tds:hasPermission permission="${Permission.PersonImport}">
                                    <g:link class="mmlink" controller="admin" action="importAccounts">Import Accounts</g:link>
                                </tds:hasPermission>
                            </li>
                            <li class="menu-child-item menu-client-export-accounts">
                                <tds:hasPermission permission="${Permission.PersonExport}">
                                    <g:link controller="admin" action="exportAccounts">Export Accounts</g:link>
                                </tds:hasPermission>
                            </li>
                            <li class="menu-child-item">
                                <tds:hasPermission permission="${Permission.HelpMenuView}">
                                    <a href="javascript:window.open('https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TMCreatePerson?cover=print','help');">help</a>
                                </tds:hasPermission>
                            </li>
                            <li class="divider"></li>
                            <li class="menu-parent-item">Manage Workflows</li>
                            <li class="menu-child-item menu-list-workflows">
                                <g:link controller="workflow" action="home">List Workflows </g:link>
                            </li>
                            <li class="menu-child-item">
                                <tds:hasPermission permission="${Permission.HelpMenuView}">
                                    <a href="javascript:window.open('https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TMManageWorkflows?cover=print','help');">help</a>
                                </tds:hasPermission>
                            </li>
                            <li class="divider"></li>
                            <li class="menu-parent-item">Manage Model Library</li>
                            <li class="menu-child-item menu-list-manufacturers">
                                <g:link controller="manufacturer" id="${partyGroup}">List Manufacturers</g:link>
                            </li>
                            <li class="menu-child-item menu-list-models">
                                <g:link controller="model" id="${partyGroup}">List Models</g:link>
                            </li>
                            <li class="menu-child-item menu-sync-libraries">
                                <g:link controller="model" action="importExport">Export Mfg & Models</g:link>
                            </li>
                            <li class="menu-child-item">
                                <tds:hasPermission permission="${Permission.HelpMenuView}">
                                    <a href="javascript:window.open('https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TMModelLibrary?cover=print','help');">help</a>
                                </tds:hasPermission>
                            </li>
                        </ul>
                    </li>
                </tds:hasPermission>
                <li class="dropdown menu-parent-projects">
                    <a href="#" class="dropdown-toggle" data-toggle="dropdown">Projects
                        <span class="caret"></span>
                    </a>
                    <ul class="dropdown-menu menu-item-expand" role="menu">
                        <li class="menu-child-item menu-projects-active-projects">
                            <g:link class="mmlink" controller="project" action="list" params="[active:'active']" onclick="hideMegaMenu('projectMegaMenu')">Active Projects</g:link>
                        </li>
                        <g:if test="${currProject}">
                            <li class="menu-child-item menu-projects-current-project">
                                <g:link class="mmlink" controller="projectUtil" onclick="hideMegaMenu('projectMegaMenu')">
                                    <g:if test="${currProject.name.size()>20}">${currProject.name.substring(0,20)+'...'}</g:if>
                                    <g:else>${currProject.name}</g:else> Details</g:link>
                            </li>
                            <li class="menu-child-item menu-projects-project-staff">
                                <g:link class="mmlink" controller="person" action="manageProjectStaff" onclick="hideMegaMenu('projectMegaMenu')">Project Staff</g:link>
                            </li>
                            <tds:hasPermission permission="${Permission.UserSendActivations}">
                                <li class="menu-child-item menu-projects-user-activation">
                                    <g:link class="mmlink" controller="project" action="userActivationEmailsForm" onclick="hideMegaMenu('projectMegaMenu')">User Activation Emails</g:link>
                                </li>
                            </tds:hasPermission>
                            <li class="menu-child-item menu-projects-field-settings">
                                <g:link class="mmlink" controller="module" action="fieldsettings" id="list" onclick="hideMegaMenu('projectMegaMenu')">Asset Field Settings</g:link>
                            </li>
                            <li class="menu-child-item menu-projects-tags">
                                <g:link class="mmlink" controller="module" action="tag" id="list">
                                    Tags
                                </g:link>
                            </li>
                        </g:if>
                        <g:else>
                            <li class="menu-child-warn">No Project Selected</li>
                        </g:else>
                        <tds:hasPermission permission="${Permission.HelpMenuView}">
                            <li class="menu-child-item">
                                <a href="javascript:window.open('https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TMProjectStaff?cover=print','help');">help</a>
                            </li>
                        </tds:hasPermission>
                        <li class="divider"></li>
                        <li class="menu-parent-item">Integrations</li>
                        <li class="menu-child-item ">
                            <g:link class="mmlink" controller="module" action="provider" id="list">
                                Providers
                            </g:link>
                        </li>
                        <li class="menu-child-item ">
                            <g:link class="mmlink" controller="module" action="credential" id="list">
                                Credentials
                            </g:link>
                        </li>
                        <li class="menu-child-item ">
                            <g:link class="mmlink" controller="module" action="datascript" id="list">
                                ETL Scripts
                            </g:link>
                        </li>
                        <li class="menu-child-item ">
                            <g:link class="mmlink" controller="module" action="action" id="list">
                                Actions
                            </g:link>
                        </li>
                    </ul>
                </li>
                <li class="dropdown menu-parent-data-centers">
                    <a href="#" class="dropdown-toggle" data-toggle="dropdown">Locations
                        <span class="caret"></span>
                    </a>
                    <ul class="dropdown-menu menu-item-expand" role="menu">
                        <li class="menu-child-item menu-parent-data-centers-list-rooms">
                            <g:link params="[viewType:'list']" controller="room">List Locations</g:link>
                        </li>
                        <g:if test="${room}">
                            <li class="menu-child-item menu-parent-data-centers-selected-center">
                                <g:link params="[roomId:room.id]" controller="room">Room ${room.location}/${room.roomName}</g:link>
                            </li>
                        </g:if>
                        <tds:hasPermission permission="${Permission.RackView}">
                            <li class="menu-child-item menu-parent-data-centers-rack-elevation">
                                <g:link controller="rackLayouts" action="create">Rack Elevations</g:link>
                            </li>
                        </tds:hasPermission>
                        <tds:hasPermission permission="${Permission.HelpMenuView}">
                            <li class="menu-child-item">
                                <a href="javascript:window.open('https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TMRooms?cover=print','help');">help</a>
                            </li>
                        </tds:hasPermission>
                    </ul>
                </li>
                <tds:hasPermission permission="${Permission.AssetMenuView}">
                    <li class="dropdown menu-parent-assets">
                        <a href="#" class="dropdown-toggle" data-toggle="dropdown">Assets
                            <span class="caret"></span>
                        </a>
                        <ul class="dropdown-menu menu-item-expand" role="menu">
                            <li class="menu-parent-item">Assets</li>
                            <li class="menu-child-item menu-parent-assets-summary-table">
                                <g:link class="mmlink" controller="assetEntity" action="assetSummary">
                                    <asset:image src="icons/application_view_columns.png" width="16" height="16" alt="Summary Table" /> Summary Table
                                </g:link>
                            </li>
                            <li class="menu-child-item menu-parent-assets-asset-manager">
                                <g:link class="mmlink" controller="module" action="asset" id="views">
                                    <i class="fa fa-cog" style="font-size:20px; margin-right: 0px;"></i> View Manager
                                </g:link>
                            </li>
                            <li class="divider"></li>
                            <li class="menu-child-item menu-parent-assets-asset-explorer assets-asset-explorer-child">
                                <g:link class="mmlink" mapping="assetViewShow" id="1" elementId="1">
	                                <asset:image src="icons/magnifier.png" width="16" height="16" /> All Assets
                                </g:link>
                            </li>
                            <li class="menu-child-item menu-parent-assets-application-list assets-asset-explorer-child">
                                <g:link class="mmlink" mapping="assetViewShow" id="7" elementId="7">
                                    <tds:svgIcon name="application_menu" width="16" height="16" /> Applications
                                </g:link>
                            </li>
                            <li class="menu-child-item menu-parent-assets-all-list assets-asset-explorer-child">
                                <g:link class="mmlink" mapping="assetViewShow" id="3" elementId="3">
                                    <tds:svgIcon name="other_menu" width="16" height="16" /> Devices
                                </g:link>
                            </li>
                            <li class="menu-child-item menu-parent-assets-server-list assets-asset-explorer-child">
                                <g:link class="mmlink" mapping="assetViewShow" id="4" elementId="4">
                                    <tds:svgIcon name="serverPhysical_menu" width="16" height="16" /> Servers
                                </g:link>
                            </li>
                            <li class="menu-child-item menu-parent-assets-database-list assets-asset-explorer-child">
                                <g:link class="mmlink" mapping="assetViewShow" id="2" elementId="2">
                                    <tds:svgIcon name="database_menu" width="16" height="16" /> Databases
                                </g:link>
                            </li>
                            <li class="menu-child-item menu-parent-assets-storage-list assets-asset-explorer-child">
                                <g:link class="mmlink" mapping="assetViewShow" id="5" elementId="5">
                                    <tds:svgIcon name="storagePhysical_menu" width="16" height="16" /> Storage - Devices
                                </g:link>
                            </li>
                            <li class="menu-child-item menu-parent-assets-storage-logical-list assets-asset-explorer-child">
                                <g:link class="mmlink" mapping="assetViewShow" id="6" elementId="6">
                                    <tds:svgIcon name="storageLogical_menu" width="16" height="16" /> Storage - Logical
                                </g:link>
                            </li>
                            <li class="divider"></li>
                            <li class="menu-child-item menu-parent-assets-dependencies-list">
                                <g:link class="mmlink" controller="module" action="dependencies" id="list"  elementId="list">
	                                <asset:image src="icons/bricks.png" width="16" height="16" alt="Dependencies" /> Dependencies
                                </g:link>
                            </li>
                            <li class="menu-child-item menu-parent-assets-comments-list" onclick="hideMegaMenu('assetMegaMenu')">
                                <g:link class="mmlink" controller="assetEntity" action="listComment">
	                                <asset:image src="icons/comments.png" width="16" height="16" alt="Comments" /> Comments
                                </g:link>
                            </li>
                            <li class="divider"></li>
                            <tds:hasPermission permission="${Permission.DepAnalyzerView}">
                                <li class="menu-child-item menu-parent-assets-dependency-analyzer">
                                    <g:link class="mmlink" controller="moveBundle" action="dependencyConsole" onclick="hideMegaMenu('assetMegaMenu')">
                                        <asset:image src="icons/brick_magnify.png" width="16" height="16" alt="Dependency Analyzer" /> Dependency Analyzer
                                    </g:link>
                                </li>
                            </tds:hasPermission>
                            <tds:hasPermission permission="${Permission.ArchitectureView}">
                                <li class="menu-child-item menu-parent-assets-architecture-graph">
                                    <g:link class="mmlink" controller="assetEntity" action="architectureViewer" onclick="hideMegaMenu('assetMegaMenu')">
                                        <asset:image src="icons/chart_organisation.png" width="16" height="16" alt="Architecture Graph" /> Architecture Graph
                                    </g:link>
                                </li>
                            </tds:hasPermission>
                            <tds:hasPermission permission="${Permission.HelpMenuView}">
                                <li class="menu-child-item">
                                    <a href="javascript:window.open('https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TMAssetOverview?cover=print','help');">help</a>
                                </li>
                            </tds:hasPermission>
                            <li class="divider"></li>
                            <tds:hasPermission permission="${Permission.AssetExport}">
                                <li class="menu-child-item menu-parent-assets-export-assets">
                                    <g:link controller="assetEntity" action="exportAssets">Export Assets</g:link>
                                </li>
                            </tds:hasPermission>
                            <li class="menu-child-item menu-parent-assets-import-assets-etl">
                                <g:link controller="module" action="importbatch" id="assets">Import Assets (ETL)</g:link>
                            </li>
                            <tds:hasPermission permission="${Permission.AssetImport}">
                                <li class="menu-child-item menu-parent-assets-import-assets">
                                    <g:link controller="assetEntity" action="assetImport">Import Assets (TM Excel)</g:link>
                                </li>
                            </tds:hasPermission>
                            <tds:hasPermission permission="${Permission.DataTransferBatchView}">
                                <li class="menu-child-item menu-parent-assets-manage-dep-batches">
                                    <g:link class="mmlink" controller="module" action="importbatch" id="list">
                                        Manage Import Batches (ETL)
                                    </g:link>
                                </li>
                            </tds:hasPermission>
                            <tds:hasPermission permission="${Permission.AssetImport}">
                                <li class="menu-child-item menu-parent-assets-manage-batches">
                                    <g:link controller="dataTransferBatch" action="list">Manage Import Batches (Excel)</g:link>
                                </li>
                            </tds:hasPermission>
                            <tds:hasPermission permission="${Permission.HelpMenuView}">
                                <li class="menu-child-item">
                                    <a href="javascript:window.open('https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TMImportExport?cover=print','help');">help</a>
                                </li>
                            </tds:hasPermission>
                        </ul>
                    </li>
                </tds:hasPermission>
                <tds:hasPermission permission="${Permission.EventMenuView}">
                    <li class="dropdown menu-parent-planning">
                        <a href="#" class="dropdown-toggle" data-toggle="dropdown">Planning
                            <span class="caret"></span>
                        </a>
                        <ul class="dropdown-menu menu-item-expand" role="menu">
                            <li class="menu-parent-item">Events</li>
                            <li class="menu-child-item menu-parent-planning-event-list">
                                <g:link controller="moveEvent" action="list">List Events</g:link>
                            </li>
                            <g:if test="${currProject && moveEvent}">
                                <li class="menu-child-item menu-parent-planning-event-detail-list">
                                    <g:link controller="moveEvent" action="show" id="${moveEvent.id}">${moveEvent.name} Event Details</g:link>
                                </li>
                            </g:if>
                            <tds:hasPermission permission="${Permission.NewsEdit}">
                                <li class="menu-child-item menu-parent-planning-event-news">
                                    <g:link controller="newsEditor">List Event News</g:link>
                                </li>
                            </tds:hasPermission>
                            <tds:hasPermission permission="${Permission.ReportViewEventPrep}">
                                <li class="menu-child-item menu-planning-pre-checklist">
                                    <g:link controller="reports" action="preMoveCheckList">Pre-event Checklist</g:link>
                                </li>
                            </tds:hasPermission>
                            <tds:hasPermission permission="${Permission.ReportViewEventPrep}">
                                <li class="menu-child-item menu-planning-pre-checklist2">
                                    <g:link controller="module" action="reports" id="preEventCheckList" elementId="preEventCheckList">Pre-event Checklist2</g:link>
                                </li>
                            </tds:hasPermission>
                            <li class="menu-child-item menu-parent-planning-export-runbook">
                                <g:link controller="moveEvent" action="exportRunbook">Export Runbook</g:link>
                            </li>
                            <tds:hasPermission permission="${Permission.HelpMenuView}">
                                <li class="menu-child-item">
                                    <a href="javascript:window.open('https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TMEvents?cover=print','help');">help</a>
                                </li>
                            </tds:hasPermission>
                            <li class="divider"></li>
                            <li class="menu-parent-item">Bundles</li>
                            <li class="menu-child-item menu-parent-planning-list-bundles">
                                <g:link controller="moveBundle" action="list">List Bundles</g:link>
                            </li>
                            <g:if test="${currProject && moveBundle}">
                                <li class="menu-child-item menu-parent-planning-selected-bundle">
                                    <g:link controller="moveBundle" action="show">${moveBundle.name} Bundle Details</g:link>
                                </li>
                            </g:if>
                            <tds:hasPermission permission="${Permission.HelpMenuView}">
                                <li class="menu-child-item">
                                    <a href="javascript:window.open('https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TMBundles?cover=print','help');">help</a>
                                </li>
                            </tds:hasPermission>
                        </ul>
                    </li>
                </tds:hasPermission>
                <tds:hasPermission permission="${Permission.TaskView}">
                    <li class="dropdown menu-parent-tasks">
                        <a onclick="showMegaMenu('#teamMegaMenu')" href="#" class="dropdown-toggle" data-toggle="dropdown">Tasks
                            <span class="caret"></span>
                        </a>
                        <ul class="dropdown-menu menu-item-expand" role="menu">
                            <li class="menu-parent-item">Tasks</li>
                            <li class="menu-child-item menu-parent-tasks-my-tasks">
                                <a href="/tdstm/task/listUserTasks">My Tasks (
                                    <span id="todoCountProjectId">&nbsp;</span>)</a>
                            </li>
                            <tds:hasPermission permission="${Permission.TaskManagerView}">
                                <li class="menu-child-item menu-parent-tasks-task-manager">
                                    <g:link controller="assetEntity" action="listTasks" params="[initSession:true]">Task Manager</g:link>
                                </li>
                            </tds:hasPermission>
                            <tds:hasPermission permission="${Permission.TaskGraphView}">
                                <li class="menu-child-item menu-parent-tasks-task-graph">
                                    <g:link controller="task" action="taskGraph" params="[initSession:true]">Task Graph</g:link>
                                </li>
                            </tds:hasPermission>
                            <tds:hasPermission permission="${Permission.TaskTimelineView}">
                                <li class="menu-child-item menu-parent-tasks-task-timeline">
                                    <g:link controller="task" action="taskTimeline">Task Timeline</g:link>
                                </li>
                            </tds:hasPermission>
                            <tds:hasPermission permission="${Permission.RecipeView}">
                                <li class="menu-child-item menu-parent-tasks-cookbook">
                                    <g:link controller="cookbook" action="index">Cookbook</g:link>
                                </li>
                            </tds:hasPermission>
                            <tds:hasPermission permission="${Permission.RecipeGenerateTasks}">
                                <li class="menu-child-item menu-parent-tasks-generation-history">
                                    <a href="/tdstm/cookbook/index#/generationHistory">Generation History</a>
                                </li>
                            </tds:hasPermission>

                            <tds:hasPermission permission="${Permission.RecipeGenerateTasks}">
                                <li class="menu-child-item menu-parent-tasks-import-tasks">
                                    <g:link class="mmlink" controller="assetEntity" action="importTask">Import Tasks</g:link>
                                </li>
                            </tds:hasPermission>

                            <tds:hasPermission permission="${Permission.HelpMenuView}">
                                <li class="menu-child-item">
                                    <a href="javascript:window.open('https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TMTaskOverview?cover=print','help');">help</a>
                                </li>
                            </tds:hasPermission>
                        </ul>
                    </li>
                </tds:hasPermission>
                <tds:hasPermission permission="${Permission.DashboardMenuView}">
                    <li class="dropdown menu-parent-dashboard">
                        <a href="#" class="dropdown-toggle" data-toggle="dropdown">Dashboards
                            <span class="caret"></span>
                        </a>
                        <ul class="dropdown-menu menu-item-expand" role="menu">
                            <li class="menu-parent-item">Live Dashboards</li>
                            <li class="menu-child-item menu-parent-dashboard-user-dashboard">
                                <g:link controller="dashboard" action="userPortal">User Dashboard</g:link>
                            </li>
                            <tds:hasPermission permission="${Permission.ReportViewPlanning}">
                                <li class="menu-child-item menu-parent-dashboard-planning-dashboard">
                                    <g:link controller="moveBundle" action="planningStats">Planning Dashboard</g:link>
                                </li>
                            </tds:hasPermission>
                            <li class="menu-child-item menu-parent-dashboard-event-dashboard">
                                <g:link controller="dashboard">Event Dashboard</g:link>
                            </li>
                            <%-- Removed until this report will be implemented using tasks
                            <tds:hasPermission permission="${Permission.ShowCartTracker}">
                                <li class="menu-child-item"><g:link controller="cartTracking" action="cartTracking" >Cart Tracker</g:link></li>
                            </tds:hasPermission>
                    --%>
                                <tds:hasPermission permission="${Permission.HelpMenuView}">
                                    <li class="menu-child-item">
                                        <a href="javascript:window.open('https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TMDashboardOverview?cover=print','help');">help</a>
                                    </li>
                                </tds:hasPermission>
                        </ul>
                    </li>
                </tds:hasPermission>
                <tds:hasPermission permission="${Permission.ReportMenuView}">
                    <li class="dropdown menu-parent-reports">
                        <a href="#" class="dropdown-toggle" data-toggle="dropdown">Reports
                            <span class="caret"></span>
                        </a>
                        <ul class="dropdown-menu menu-item-expand" role="menu">
                            <li class="menu-parent-item">Planning</li>
                            <li class="menu-child-item menu-reports-application-profiles">
                                <a href="/tdstm/reports/applicationProfiles">Application Profiles</a>
                            </li>
                            <li class="menu-child-item menu-reports-application-conflicts">
                                <a href="/tdstm/reports/applicationConflicts">Application Conflicts</a>
                            </li>
                            <li class="menu-child-item menu-reports-server-conflicts">
                                <a href="/tdstm/reports/serverConflicts">Server Conflicts</a>
                            </li>
                            <li class="menu-child-item menu-reports-database-conflicts">
                                <a href="/tdstm/reports/databaseConflicts">Database Conflicts</a>
                            </li>
                            <tds:hasPermission permission="${Permission.ReportViewPlanning}">
                                <li class="menu-child-item menu-reports-task-report">
                                    <a href="/tdstm/reports/retrieveBundleListForReportDialog?reportId=Task+Report">Task Report</a>
                                </li>
                            </tds:hasPermission>
                            <tds:hasPermission permission="${Permission.ReportViewProjectDailyMetrics}">
                                <li class="menu-child-item menu-reports-activity-metrics">
                                    <a href="/tdstm/reports/projectActivityMetrics">Activity Metrics</a>
                                </li>
                            </tds:hasPermission>
                            <tds:hasPermission permission="${Permission.ReportViewEventDay}">
                                <li class="divider"></li>
                                <li class="menu-parent-item">Event Day</li>
                                <li class="menu-child-item menu-reports-application-migration">
                                    <a href="/tdstm/reports/applicationMigrationReport" onclick="hideMegaMenu('reportsMegaMenu')">Application Event Results</a>
                                </li>
                                <tds:hasPermission permission="${Permission.HelpMenuView}">
                                    <li class="menu-child-item">
                                        <a href="javascript:window.open('https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TranManHelp?cover=print','help');">help</a>
                                    </li>
                                </tds:hasPermission>
                            </tds:hasPermission>
                        </ul>
                    </li>
                </tds:hasPermission>
            </ul>
        </g:if>
    </div>
    <!-- /.navbar-collapse -->
