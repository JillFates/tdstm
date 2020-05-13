<%@ page import="net.transitionmanager.security.Permission" %>
<g:set var="licenseCommonService" bean="licenseCommonService"/>
<g:set var="licenseAdminService" bean="licenseAdminService"/>
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
    <g:javascript src="walkme.js" />
    <g:javascript src="PasswordValidation.js" />

    <script type="text/javascript">
	$(document).ready(function() {
            <%-- SETUP CSRF TOKEN FOR ALL LEGACY PAGES --%>
            $.ajaxSetup({
                headers: { '${_csrf?.headerName}': '${_csrf?.token}' }
            });

            // Format User Name in Icon
            $(".user-menu .user-name").each(function() {
            	$(this).text($(this).text().split(" ").map(function(a){
            		return a.substr(0,1).toUpperCase();
            	}).join(""))
            })
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

        function clearStorage() {
            stateManagement.destroyState();
        }

    </script>
</head>
<!-- ADD THE CLASS layout-top-nav TO REMOVE THE SIDEBAR. -->
<body class="hold-transition skin-blue layout-top-nav">
<div class="wrapper">
    <header class="main-header header-component">
        <input id="contextPath" type="hidden" value="${request.contextPath}"/>
        <input id="tzId" type="hidden" value="${tds.timeZone()}"/>
        <input id="userDTFormat" type="hidden" value="${tds.dateFormat()}"/>
        <g:if test="${angularModalDialog}">
        </g:if>
		<g:else>
            <nav class="navbar navbar-static-top">
                <div class="container menu-top-container ${((!isLicenseManagerEnabled)? 'menu-top-container-full-menu' : '')}">
                    <div class="navbar-header branding">
                        <g:if test="${isLicenseManagerEnabled}">
                            <asset:image src="/../tds/web-app/assets/images/transitionLogo.svg" alt="Transition Manager" border="0" />
                        </g:if>
                        <g:else>
                            <g:if test="${setImage}">
                                <img src="${createLink(controller:'project', action:'showImage', id:setImage)}" alt="${currProject.name} project" />
                            </g:if>
                            <g:else>
                                <asset:image src="/../tds/web-app/assets/images/transitionLogo.svg" alt="Transition Manager" border="0" />
                            </g:else>
                        </g:else>
                    </div>

					<button
						type="button"
						class="navbar-toggle collapsed"
						data-toggle="collapse"
						data-target="#mobile-nav"
					>
						<i class="fa fa-bars"></i>
					</button>
					<div id="mobile-nav" class="navbar-item-group">

						<div class="navbar-custom-menu user-navarea">
							<ul class="nav navbar-nav">
								<!-- Notifications Menu -->
								<li class="dropdown notifications-menu">
									<!-- Menu toggle button -->
									<span class="tds-nav-item" id="nav-project-name">
										<g:if test="${currProject}"> ${currProject.name} </g:if>
										<g:if test="${moveEvent}"> : ${moveEvent.name}</g:if>
										<g:if test="${moveBundle}"> : ${moveBundle.name}</g:if>
									</span>
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
							<a
								href="#"
								class="dropdown-toggle"
								data-toggle="dropdown"
								title="${tds.currentPerson()}"
							>
								<span class="user-name">${tds.currentPerson()}</span>
								<span class="user-menu-text"
									>Account Options</span
								>
							</a>
							<ul class="dropdown-menu">
								<li class="menu-child-item">
									<a
										id="editPersonId" name="${userLogin.username}" onclick="UserPreference.editPerson();return false;"
										style="cursor: pointer;"
										>Account Details</a
									>
								</li>
								<li class="menu-child-item">
									<a
										id="editTimezoneId" name="${userLogin.username}" onclick="UserPreference.editDateAndTimezone();return false;"
										style="cursor: pointer;"
										>Date and Timezone</a
									>
								</li>
								<li class="menu-child-item">
									<a
										id="resetPreferenceId" name="${userLogin.username}" onclick="UserPreference.editPreference();return false;"
										style="cursor: pointer;"
										>Edit Preferences</a
									>
								</li>
								<li class="divider"></li>
								<li class="menu-child-item">
									<g:link controller="auth" action="signOut" onclick="clearStorage()"
										>Sign Out</g:link
									>
								</li>
							</ul>
								</li>
								</sec:ifLoggedIn>
							</ul>
						</div>

						<g:if test="${isLicenseManagerEnabled}">
							<g:render template="/layouts/licmanMenu" model="[currProject:currProject, partyGroup: partyGroup, room:room, moveEvent:moveEvent, isLicenseManagerEnabled:isLicenseManagerEnabled]"  />
						</g:if>
						<g:else>
							<g:render template="/layouts/tranmanMenu" model="[currProject:currProject, partyGroup: partyGroup, room:room, moveBundle:moveBundle, moveEvent:moveEvent, isLicenseManagerEnabled:isLicenseManagerEnabled]"  />
						</g:else>


					</div>
				</div>
            </nav> 
		</g:else>
    </header>
    <!-- Full Width Column -->
    <div class="content-wrapper">
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
        <a href="http://www.transitionaldata.com/service/transitionmanager" target="_blank">&nbsp;TransitionManager&trade;</a> 2010-${Calendar.getInstance().get(Calendar.YEAR)} . All rights reserved. Patent Pending
        <!-- /.container -->
    </footer>
</div>

    <%-- DIV for editing User Profile --%>
    <g:render template="/person/personEdit" model="[user:userLogin, minPasswordLength:minPasswordLength]" />

    <%-- DIV for editing User Preferences --%>
    <div id="userPrefDivId" style="display: none;" title="${tds.currentPersonName()} Preferences"></div>

	<%-- DIV for editing User date and timezone --%>
	<div id="userTimezoneDivId" style="display: none;" title="${tds.currentPersonName()} Date and Timezone"></div>

    <g:render template="/layouts/chromeAutofillBug" />

</body>
</html>
