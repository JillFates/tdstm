<%@page import="net.transitionmanager.security.Permission"%>
    <!-- Collect the nav links, forms, and other content for toggling -->
    <div class="tranman-menu-component navbar-ul-container navbar-ul-container-full-menu">
        <g:if test="${currProject}">
            <ul class="nav navbar-nav">
                <tds:hasPermission permission="${Permission.AdminMenuView}">
                    <li class="dropdown menu-parent-admin">
                        <a href="#" class="dropdown-toggle" data-toggle="dropdown">Admin</a>
                        <ul class="dropdown-menu menu-item-expand" role="menu">
                            <li class="menu-child-item menu-admin-portal">
                                <g:link controller="admin" action="home">Admin Portal</g:link>
                            </li>
                            <g:if test="${ ! isLicenseManagerEnabled }">
                                <tds:hasPermission permission="${Permission.LicenseAdministration}">
                                    <li class="menu-child-item menu-admin-license-admin">
                                        <a href="/tdstm/module/license/admin/list">License Admin</a>
                                    </li>
                                </tds:hasPermission>
                            </g:if>
                            <g:if test="${ isLicenseManagerEnabled }">
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
                            <g:if test="${ ! isLicenseManagerEnabled }">
                                <li class="menu-child-item menu-admin-asset-options">
                                    <g:link controller="assetEntity" action="assetOptions">Asset Options</g:link>
                                </li>
                                <li class="divider"></li>
                                <li class="menu-child-item menu-list-companies">
                                <g:link controller="partyGroup" action="list">List Companies</g:link>
                            </li>
                            <li class="menu-child-item menu-list-staff">
                                <g:link controller="person">List Staff</g:link>
                            </li>
                            <li class="menu-child-item menu-list-users">
                                <tds:hasPermission permission="${Permission.UserView}">
                                    <g:link controller="userLogin">List Users</g:link>
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
                            <li class="divider"></li>
                            <li class="menu-child-item menu-list-manufacturers">
                                <a href="/tdstm/module/manufacturer/list">List Manufacturers</a>
                            </li>
                            <li class="menu-child-item menu-list-models">
                                <a href="/tdstm/module/model/list">List Models</a>
                            </li></g:if>
                        </ul>
                    </li>
                </tds:hasPermission>
                <g:if test="${ ! isLicenseManagerEnabled }">
                    <li class="dropdown menu-parent-projects">
                    <a href="#" class="dropdown-toggle" data-toggle="dropdown">Projects</a>
                    <ul class="dropdown-menu menu-item-expand" role="menu">
                        <li class="menu-child-item menu-projects-active-projects">
                            <g:link class="mmlink" controller="module" action="project" id="list" params="[active:'active']" onclick="hideMegaMenu('projectMegaMenu')">Projects</g:link>
                        </li>
                        <li class="divider"></li>
                        <g:if test="${currProject}">
                            <li class="menu-child-item menu-projects-current-project">
                                <g:link class="mmlink" controller="module" action="project" id="list" params="[show:currProject.id]" onclick="hideMegaMenu('projectMegaMenu')">Project Details</g:link>
                            </li>
                            <li class="menu-child-item menu-projects-project-staff">
                                <g:link class="mmlink" controller="person" action="manageProjectStaff" onclick="hideMegaMenu('projectMegaMenu')">Project Staff</g:link>
                            </li>
                            <tds:hasPermission permission="${Permission.UserSendActivations}">
                                <li class="menu-child-item menu-projects-user-activation">
                                    <g:link class="mmlink" controller="project" action="userActivationEmailsForm" onclick="hideMegaMenu('projectMegaMenu')">User Activation Emails</g:link>
                                </li>
                            </tds:hasPermission>
                            <tds:hasPermission permission="${Permission.ProjectFieldSettingsView}">
                                <li class="menu-child-item menu-projects-field-settings">
                                    <g:link class="mmlink" controller="module" action="fieldsettings" id="list" onclick="hideMegaMenu('projectMegaMenu')">Asset Field Settings</g:link>
                                </li>
                            </tds:hasPermission>
                            <li class="menu-child-item menu-projects-tags">
                                <g:link class="mmlink" controller="module" action="tag" id="list">
                                    Tags
                                </g:link>
                            </li>
                        </g:if>
                        <g:else>
                            <li class="menu-child-warn">No Project Selected</li>
                        </g:else>
                        <li class="divider"></li>
                        <li class="menu-child-item ">
                            <g:link class="mmlink" controller="module" action="provider" id="list">
                                Providers
                            </g:link>
                        </li>
                         <tds:hasPermission permission="${Permission.CredentialView}">
                            <li class="menu-child-item ">
                                <g:link class="mmlink" controller="module" action="credential" id="list">
                                    Credentials
                                </g:link>
                            </li>
                        </tds:hasPermission>
                        <li class="menu-child-item ">
                            <g:link class="mmlink" controller="module" action="datascript" id="list">
                                ETL Scripts
                            </g:link>
                        </li>
                        <tds:hasPermission permission="${Permission.ActionEdit}">
                            <li class="menu-child-item ">
                                <g:link class="mmlink" controller="module" action="action" id="list">
                                    Actions
                                </g:link>
                            </li>
                        </tds:hasPermission>
                    </ul>
                </li>
                <li class="dropdown menu-parent-data-centers">
                    <a href="#" class="dropdown-toggle" data-toggle="dropdown">Locations</a>
                    <ul class="dropdown-menu menu-item-expand" role="menu">
                        <li class="menu-child-item menu-parent-data-centers-list-rooms">
                            <g:link params="[viewType:'list']" controller="room">List Locations</g:link>
                        </li>
                        <tds:hasPermission permission="${Permission.RackView}">
                            <li class="menu-child-item menu-parent-data-centers-rack-elevation">
                                <g:link controller="rackLayouts" action="create">Rack Elevations</g:link>
                            </li>
                        </tds:hasPermission>
                    </ul>
                </li>
                <tds:hasPermission permission="${Permission.AssetMenuView}">
                    <li class="dropdown menu-parent-assets">
                        <a href="#" class="dropdown-toggle" data-toggle="dropdown">Assets</a>
                        <ul class="dropdown-menu menu-item-expand" role="menu">
                           <li class="menu-child-item menu-parent-assets-summary-table">
                                <a href="/tdstm/module/assetsummary/list">Summary</a>
                            </li>
                            <li class="menu-child-item menu-parent-assets-asset-manager">
                                <g:link class="mmlink" controller="module" action="asset" id="views">Manage Views</g:link>
                            </li>
                            <li class="divider"></li>
                            <li class="menu-child-item menu-parent-assets-application-list assets-asset-explorer-child">
                                <g:link class="mmlink" mapping="assetViewShow" id="7" elementId="7">Applications</g:link>
                            </li>
                            <li class="menu-child-item menu-parent-assets-all-list assets-asset-explorer-child">
                                <g:link class="mmlink" mapping="assetViewShow" id="3" elementId="3">Devices</g:link>
                            </li>
                            <li class="menu-child-item menu-parent-assets-server-list assets-asset-explorer-child">
                                <g:link class="mmlink" mapping="assetViewShow" id="4" elementId="4">Servers</g:link>
                            </li>
                            <li class="menu-child-item menu-parent-assets-database-list assets-asset-explorer-child">
                                <g:link class="mmlink" mapping="assetViewShow" id="2" elementId="2">Databases</g:link>
                            </li>
                            <li class="menu-child-item menu-parent-assets-storage-logical-list assets-asset-explorer-child">
                                <g:link class="mmlink" mapping="assetViewShow" id="6" elementId="6">Logical Storage</g:link>
                            </li>
                            <li class="menu-child-item menu-parent-assets-asset-explorer assets-asset-explorer-child">
                                <g:link class="mmlink" mapping="assetViewShow" id="1" elementId="1">All Assets</g:link>
                            </li>
                            <li class="divider"></li>
                            <li class="menu-child-item menu-parent-assets-dependencies-list">
                                <g:link class="mmlink" controller="module" action="dependencies" id="list"  elementId="list">Dependencies</g:link>
                            </li>
                            <li class="menu-child-item menu-parent-assets-comments-list" onclick="hideMegaMenu('assetMegaMenu')">
                                <g:link class="mmlink" controller="module" action="assetcomment" id="list">Comments</g:link>
                            </li>
                            <li class="divider"></li>
                            <tds:hasPermission permission="${Permission.DepAnalyzerView}">
                                <li class="menu-child-item menu-parent-assets-dependency-analyzer">
                                    <g:link class="mmlink" controller="moveBundle" action="dependencyConsole" onclick="hideMegaMenu('assetMegaMenu')">
									Dependency Analyzer
                                    </g:link>
                                </li>
                            </tds:hasPermission>
                            <tds:hasPermission permission="${Permission.DepAnalyzerView}">
                                <li class="menu-child-item menu-parent-assets-dependency-analyzer-clr">
                                    <g:link controller="module" action="taskManager" id="dependency-analyzer">Angular Dependency Analyzer</g:link>
                                </li>
                            </tds:hasPermission>
                            <tds:hasPermission permission="${Permission.ArchitectureView}">
                                <li class="menu-child-item menu-parent-assets-architecture-graph">
                                    <g:link class="mmlink" controller="assetEntity" action="architectureViewer" onclick="hideMegaMenu('assetMegaMenu')">
                                        Architecture Graph
                                    </g:link>
                                </li>
                            </tds:hasPermission>
                            <tds:hasPermission permission="${Permission.ArchitectureView}">
                                <li class="menu-child-item menu-parent-assets-architecture-graph-gojs">
                                    <g:link controller="module" action="taskManager" id="architecture-graph">GoJS Architecture Graph</g:link>
                                </li>
                            </tds:hasPermission>
                            <tds:hasPermission permission="${Permission.DataTransferBatchView}">
                                <li class="divider"></li>
                            </tds:hasPermission>
                            <tds:hasPermission permission="${Permission.AssetExport}">
                                <li class="menu-child-item menu-parent-assets-export-assets">
                                    <a href="/tdstm/module/export/assets">Export Assets</a>
    %{--                                    <g:link controller="assetEntity" action="exportAssets">Export Assets</g:link>--}%
                                    </li>
                                </tds:hasPermission>
                                <tds:hasPermission permission="${Permission.AssetImport}">
                                    <li class="menu-child-item menu-parent-assets-import-assets-etl">
                                        <g:link controller="module" action="importbatch" id="assets">Import Assets (ETL)</g:link>
                                    </li>
                                </tds:hasPermission>
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
                                        <g:link controller="dataTransferBatch" action="list">Manage Import Batches (TM Excel)</g:link>
                                </li>
                            </tds:hasPermission>
                        </ul>
                    </li>
                </tds:hasPermission>
                <tds:hasPermission permission="${Permission.EventMenuView}">
                    <li class="dropdown menu-parent-planning">
                        <a href="#" class="dropdown-toggle" data-toggle="dropdown">Planning</a>
                        <ul class="dropdown-menu menu-item-expand" role="menu">
                            <li class="menu-child-item menu-parent-planning-event-list">
                                <g:link controller="module" action="event" id="list">List Events</g:link>
                            </li>
                            <g:if test="${currProject && moveEvent}">
                                <li class="menu-child-item menu-parent-planning-event-detail-list">
                                    <a href="/tdstm/module/event/list?show=${moveEvent.id}">Event Details - ${moveEvent.name}</a>
                                </li>
                            </g:if>
                            <tds:hasPermission permission="${Permission.NewsEdit}">
                                <li class="menu-child-item menu-parent-planning-event-news">
                                    <a href="/tdstm/module/event-news/list">List Event News</a>
                                </li>
                            </tds:hasPermission>
                            <tds:hasPermission permission="${Permission.ReportViewEventPrep}">
                                <li class="menu-child-item menu-planning-pre-checklist">
                                    <g:link controller="module" action="reports" id="preEventCheckList" elementId="preEventCheckList">Pre-event Checklist</g:link>
                                </li>
                            </tds:hasPermission>
                            <li class="menu-child-item menu-parent-planning-export-runbook">
                                <g:link controller="moveEvent" action="exportRunbook">Export Runbook</g:link>
                            </li>
                            <li class="divider"></li>
                            <li class="menu-child-item menu-parent-planning-list-bundles">
                                <g:link class="mmlink" controller="module" action="bundle" id="list">List Bundles</g:link>
                            </li>
                            <g:if test="${currProject && moveBundle}">
                                <li class="menu-child-item menu-parent-planning-selected-bundle">
                                    <a href="/tdstm/module/bundle/list?show=${moveBundle.id}">Bundle Details - ${moveBundle.name}</a>
                                </li>
                            </g:if>
                        </ul>
                    </li>
                </tds:hasPermission>
                <tds:hasPermission permission="${Permission.TaskView}">
                    <li class="dropdown menu-parent-tasks">
                        <a onclick="showMegaMenu('#teamMegaMenu')" href="#" class="dropdown-toggle" data-toggle="dropdown">Tasks</a>
                        <ul class="dropdown-menu menu-item-expand" role="menu">
                                <tds:hasPermission permission="${Permission.TaskManagerView}">
                                    <li class="menu-child-item menu-parent-tasks-task-manager">
                                    <g:link controller="module" action="taskManager" id="list">Task Manager (<span id="todoCountProjectId"></span>)</g:link>
                                </li>
                                </tds:hasPermission>
                                <tds:hasPermission permission="${Permission.TaskGraphView}">
                                    <li class="menu-child-item menu-parent-tasks-task-graph">
                                        <g:link controller="task" action="taskGraph" params="[initSession:true]">Task Graph</g:link>
                                    </li>
                                </tds:hasPermission>
                                <tds:hasPermission permission="${Permission.TaskGraphView}">
                                <li class="menu-child-item menu-parent-tasks-gojs-graph">
                                    <g:link controller="module" action="taskManager" id="task-graph">GoJS Task Graph</g:link>
                                </li>
                            </tds:hasPermission>
                            <tds:hasPermission permission="${Permission.TaskTimelineView}">
                                    <li class="menu-child-item menu-parent-tasks-task-timeline">
                                        <g:link controller="task" action="taskTimeline">Task Timeline</g:link>
                                    </li>
                                </tds:hasPermission>
                                <tds:hasPermission permission="${Permission.RecipeView}">
                                    <li class="menu-child-item menu-parent-tasks-cookbook">
                                        <g:link controller="recipes" action="index">Recipes</g:link>
                                    </li>
                                </tds:hasPermission>
                                <tds:hasPermission permission="${Permission.RecipeGenerateTasks}">
                                    <li class="menu-child-item menu-parent-tasks-generation-history">
                                        <a href="/tdstm/recipes/index#/generationHistory">Generation History</a>
                                    </li>
                                </tds:hasPermission>

                            <tds:hasPermission permission="${Permission.RecipeGenerateTasks}">
                                <li class="menu-child-item menu-parent-tasks-import-tasks">
                                    <g:link class="mmlink" controller="assetEntity" action="importTask">Import Tasks</g:link>
                                </li>
                            </tds:hasPermission>
                        </ul>
                    </li>
                </tds:hasPermission>
                <tds:hasPermission permission="${Permission.DashboardMenuView}">
                    <li class="dropdown menu-parent-dashboard">
                        <a href="#" class="dropdown-toggle" data-toggle="dropdown">Dashboards</a>
                        <ul class="dropdown-menu menu-item-expand" role="menu">
                            <li class="menu-child-item menu-parent-dashboard-user-dashboard">
                                <g:link class="mmlink" controller="module" action="user" id="dashboard">User Dashboard</g:link>
                            </li>
                            <tds:hasPermission permission="${Permission.ReportViewPlanning}">
                                <li class="menu-child-item menu-parent-dashboard-planning-dashboard">
                                    <a href="/tdstm/module/planning/dashboard">Planning Dashboard</a>
                                </li>
                            </tds:hasPermission>

                                <li class="menu-child-item menu-parent-dashboard-event-dashboard">
                                <g:link class="mlink" controller="module" action="event" id="dashboard">Event Dashboard</g:link>
                            </li>
                            <li class="menu-child-item menu-parent-dashboard-event-dashboard">
                                <g:link class="mlink" controller="module" action="insight" id="dashboard">Insight Dashboard</g:link>
                            </li>
                                <%-- Removed until this report will be implemented using tasks
                            <tds:hasPermission permission="${Permission.ShowCartTracker}">
                                <li class="menu-child-item"><g:link controller="cartTracking" action="cartTracking" >Cart Tracker</g:link></li>
                            </tds:hasPermission>
                    --%>
                        </ul>
                    </li>
                </tds:hasPermission>
                <tds:hasPermission permission="${Permission.ReportMenuView}">
                    <li class="dropdown menu-parent-reports">
                        <a href="#" class="dropdown-toggle" data-toggle="dropdown">Reports</a>
                        <ul class="dropdown-menu menu-item-expand" role="menu">
                            <li class="menu-child-item menu-reports-application-profiles">
                                <g:link controller="module" action="reports" id="applicationProfiles" elementId="applicationProfiles">Application Profiles</g:link>
                            </li>
                            <li class="menu-child-item menu-reports-application-conflicts">
                                <g:link controller="module" action="reports" id="applicationConflicts" elementId="applicationConflicts">Application Conflicts</g:link>
                            </li>
                            <li class="menu-child-item menu-reports-server-conflicts">
                                <g:link controller="module" action="reports" id="serverConflicts" elementId="serverConflicts">Server Conflicts</g:link>
                            </li>
                            <li class="menu-child-item menu-reports-database-conflicts">
                                <g:link controller="module" action="reports" id="databaseConflicts" elementId="databaseConflicts">Database Conflicts</g:link>
                            </li>
                            <tds:hasPermission permission="${Permission.ReportViewPlanning}">
                                <li class="menu-child-item menu-reports-task-report">
                                    <g:link controller="module" action="reports" id="taskReport" elementId="taskReport">Task Report</g:link>
                                </li>
                            </tds:hasPermission>
                            <tds:hasPermission permission="${Permission.ReportViewProjectDailyMetrics}">
                                <li class="menu-child-item menu-reports-activity-metrics">
                                    <g:link controller="module" action="reports" id="projectActivityMetrics" elementId="projectActivityMetrics">Activity Metrics</g:link>
                                </li>
                            </tds:hasPermission>
                            <tds:hasPermission permission="${Permission.ReportViewEventDay}">
                                <li class="divider"></li>
                                    <li class="menu-child-item menu-reports-application-migration">
                                        <g:link controller="module" action="reports" id="applicationEventResults" elementId="applicationEventResults">Application Event Results</g:link>
                                    </li>
                                </tds:hasPermission>
                            </ul>
                        </li>
                    </tds:hasPermission>
                </g:if>
            </ul>
        </g:if>
    </div>
    <!-- /.navbar-collapse -->
