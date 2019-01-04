<%@ page import="net.transitionmanager.security.Permission" %>
<g:set var="licenseCommonService" bean="licenseCommonService"/>
<g:set var="environmentService" bean="environmentService"/>
<!DOCTYPE html>
<html>
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
    // Only for environments where the License Manager is true Enabled
    def isLicenseManagerEnabled = licenseCommonService.isManagerEnabled()
    def buildInfo = environmentService.getVersionText()
%>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <title><g:layoutTitle default="Grails" /></title>

    <!-- Tell the browser to be responsive to screen width -->
    <meta content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no" name="viewport">

    <g:if test="${topNavClean}">
        <g:render template="/layouts/responsiveResources" model="[isLicenseManagerEnabled:isLicenseManagerEnabled]"  />
    </g:if>
    <g:if test="${!topNavClean}">
        <g:render template="/layouts/responsiveStandardResources" model="[isLicenseManagerEnabled:isLicenseManagerEnabled]" />
    </g:if>

    <g:layoutHead />

    <asset:stylesheet href="css/dropDown.css" />
    <asset:stylesheet href="css/daterangepicker-bs3.css" />

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

            $('.licensing-error-warning').popover({placement: 'bottom', container: 'body' });
            $('.licensing-error-warning').click(function(event) { event.preventDefault(); });

        });

        function updateEventHeader( e ){
            var newsAndStatus = eval("(" + e.responseText + ")")
            $("#head_mycrawlerId").html(newsAndStatus[0].news);
            $("#head_crawler").addClass(newsAndStatus[0].cssClass)
            $("#moveEventStatus").html(newsAndStatus[0].status)
        }

    </script>
</head>
<!-- ADD THE CLASS layout-top-nav TO REMOVE THE SIDEBAR. -->
<body class="hold-transition skin-blue layout-top-nav">
<div class="wrapper">
    <header class="main-header">
        <input id="contextPath" type="hidden" value="${request.contextPath}"/>
        <input id="tzId" type="hidden" value="${tds.timeZone()}"/>
        <input id="userDTFormat" type="hidden" value="${tds.dateFormat()}"/>
        <nav class="navbar navbar-static-top">
            <div class="container menu-top-container ${((!isLicenseManagerEnabled)? 'menu-top-container-full-menu' : '')}">
                <div class="navbar-header">
                    <button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#navbar-collapse">
                        <i class="fa fa-bars"></i>
                    </button>
                    <g:if test="${isLicenseManagerEnabled}">
                        <asset:image id="logo-header" src="images/TMHeaderLogoManager.png" alt="TransitionManager" border="0" />
                    </g:if>
                    <g:else>
                        <g:if test="${setImage}">
                            <img src="${createLink(controller:'project', action:'showImage', id:setImage)}" alt="${currProject.name} project" style="height: 30px;  margin-top: 8px;"/>
                        </g:if>
                        <g:else>
                            <asset:image id="logo-header" src="images/TMHeaderLogo.png" alt="TransitionManager" border="0" />
                        </g:else>
                    </g:else>
                </div>

                <g:if test="${isLicenseManagerEnabled}">
                    <g:render template="/layouts/licmanMenu" model="[currProject:currProject, partyGroup: partyGroup, room:room, moveEvent:moveEvent, isLicenseManagerEnabled:isLicenseManagerEnabled]"  />
                </g:if>
                <g:else>
                    <g:render template="/layouts/tranmanMenu" model="[currProject:currProject, partyGroup: partyGroup, room:room, moveBundle:moveBundle, moveEvent:moveEvent, isLicenseManagerEnabled:isLicenseManagerEnabled]"  />
                </g:else>


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
                        <li>
                            <g:if test="${!isLicenseManagerEnabled}">
                                <tds:licenseWarning />
                            </g:if>
                        </li>
                        <sec:ifLoggedIn>
                            <!-- User Account Menu -->
                            <li class="dropdown user user-menu">
                                <!-- Menu Toggle Button -->
                                <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                                    <!-- The user image in the navbar-->
                                    <asset:image src="images/personIcon.png" class="user-image" alt="${session.getAttribute("LOGIN_PERSON").name }" />
                                    <!-- hidden-xs hides the username on small devices so only the image appears. -->
                                    <span class="hidden-xs user-name">${tds.currentPersonName()}</span>
                                </a>
                                <ul class="dropdown-menu user-dialog-dropdown-menu">
                                    <!-- Menu Body -->
                                    <li class="user-body">
                                        <ul class="list-group">
                                            <tds:hasPermission permission="${Permission.PersonView}">
                                                <li class="list-group-item">
                                                    <a href="#" style="cursor: pointer;" id="editPersonId" name="${userLogin.username}" onclick="UserPreference.editPerson();return false;"><span class="glyphicon glyphicon-user user-menu-icon-badge"></span> Account Details</a>
                                                </li>
                                            </tds:hasPermission>
                                            <li class="list-group-item"><a href="#" style="cursor: pointer;" id="editTimezoneId" name="${userLogin.username}" onclick="UserPreference.editDateAndTimezone();return false;"><span class="glyphicon glyphicon-time user-menu-icon-badge"></span> Date and Timezone</a></li>
                                            <li class="list-group-item"><a href="#" style="cursor: pointer;" id="resetPreferenceId" name="${userLogin.username}" onclick="UserPreference.editPreference();return false;"><span class="glyphicon glyphicon-pencil user-menu-icon-badge"></span> Edit Preferences</a></li>
                                        <!-- <li class="list-group-item"><g:link class="home mmlink" controller="task" action="listUserTasks" params="[viewMode:'mobile',tab:tab]">Use Mobile Site</g:link></li> -->
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
        <g:if test="${currProject}">
        <%-- Include the News crawler if there is an event in progress --%>
            <g:if test="${currProject?.runbookOn && moveEvent && (moveEvent?.newsBarMode == 'on' || (moveEvent?.newsBarMode == 'auto' && moveEvent?.estStartTime))}">
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
                        ${remoteFunction(controller:'moveEvent', action:'retrieveMoveEventNewsAndStatus', params:'\'id='+moveEvent.id+'\'',onComplete:'updateEventHeader(XMLHttpRequest)')}
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
        <div class="pull-right hidden-xs">
            <span>${buildInfo}</span>
        </div>
        <strong><a href="http://www.transitionaldata.com/service/transitionmanager" target="_blank">&nbsp;TransitionManager&trade;</a> 2010-${Calendar.getInstance().get(Calendar.YEAR)} .</strong> All rights reserved.
        <!-- /.container -->
    </footer>
</div>

    <%-- DIV for editing User Profile --%>
    <g:render template="/person/personEdit" model="[user:userLogin, minPasswordLength:minPasswordLength]" />

    <%-- DIV for editing User Preferences --%>
    <div id="userPrefDivId" style="display: none;" title="${tds.currentPersonName()} Preferences"></div>

	<%-- DIV for editing User date and timezone --%>
	<div id="userTimezoneDivId" style="display: none;" title="${tds.currentPersonName()} Date and Timezone"></div>

</body>
</html>
<%
    flash.remove('message');
%>
