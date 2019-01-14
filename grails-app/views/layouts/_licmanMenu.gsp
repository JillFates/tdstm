<%@page import="net.transitionmanager.security.Permission"%>
<!-- Collect the nav links, forms, and other content for toggling -->
<div class="collapse navbar-collapse pull-left navbar-ul-container " id="navbar-collapse">
    <g:if test="${currProject}">
        <ul class="nav navbar-nav">
            <tds:hasPermission permission="${Permission.AdminMenuView}">
                <li class="dropdown menu-parent-admin">
                    <a href="#" class="dropdown-toggle" data-toggle="dropdown">Admin <span class="caret"></span></a>
                    <ul class="dropdown-menu menu-item-expand" role="menu">
                        <li class="menu-parent-item">Administration</li>
                        <li class="menu-child-item menu-admin-portal"><g:link controller="admin" action="home">Admin Portal</g:link> </li>
                        <g:if test="${isLicenseManagerEnabled}">
                            <li class="menu-child-item menu-admin-license-manager"><a href="/tdstm/module/license/manager/list">License Manager</a></li>
                        </g:if>
                        <li class="menu-child-item menu-admin-license-manager"><a href="/tdstm/app/notice/list">Notices</a></li>
                        <li class="menu-child-item menu-admin-role">
                            <tds:hasPermission permission="${Permission.RolePermissionView}">
                                <g:link controller="permissions" action="show">Role Permissions</g:link>
                            </tds:hasPermission>
                        </li>
                        <li class="menu-child-item">
                            <tds:hasPermission permission="${Permission.HelpMenuView}">
                                <a href="javascript:window.open('https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TMAdminPortal?cover=print','help');" >help</a>
                            </tds:hasPermission>
                        </li>
                        <li class="divider"></li>
                        <li class="menu-parent-item">Manage Clients</li>
                        <li class="menu-child-item menu-list-companies"><g:link controller="partyGroup" action="list" params="[active:'active',tag_s_2_name:'asc']" id="${partyGroup}">List Companies</g:link></li>
                        <li class="menu-child-item menu-list-staff"><g:link controller="person" id="${partyGroup}">List Staff</g:link></li>
                        <li class="menu-child-item menu-list-users">
                            <tds:hasPermission permission="${Permission.UserView}">
                                <g:link controller="userLogin" id="${partyGroup}">List Users</g:link>
                            </tds:hasPermission>
                        </li>
                        <li class="menu-child-item menu-client-import-accounts">
                            <tds:hasPermission permission="${Permission.PersonImport}">
                                <g:link class="mmlink" controller="admin" action="importAccounts" >Import Accounts</g:link>
                            </tds:hasPermission>
                        </li>
                        <li class="menu-child-item menu-client-export-accounts">
                            <tds:hasPermission permission="${Permission.PersonExport}">
                                <g:link controller="admin" action="exportAccounts" >Export Accounts</g:link>
                            </tds:hasPermission>
                        </li>
                        <li class="menu-child-item">
                            <tds:hasPermission permission="${Permission.HelpMenuView}">
                                <a href="javascript:window.open('https://ops.tdsops.com/twiki/bin/view/Main/DataCenterMoves/TMCreatePerson?cover=print','help');" >help</a>
                            </tds:hasPermission>
                        </li>
                    </ul>
                </li>
            </tds:hasPermission>
        </ul>
    </g:if>
</div><!-- /.navbar-collapse -->
