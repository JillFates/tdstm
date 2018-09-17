//menu and other header functions...loaded from projectHeader

var timerId;
timerId = window.setTimeout("timeOut()",(60000 * 120));

function resetTimer() {
	window.clearTimeout(timerId);
	timerId = window.setTimeout("timeOut()",(60000 * 120));
}
function timeOut() {
	new Ajax.Request('/tdstm/auth/signOut',{asynchronous:true,evalScripts:true,onComplete:function(e){sessionExpireOverlay()}});;
}
function sessionExpireOverlay() {
	window.parent.location = self.location;
}

$(document).keydown(function(){ resetTimer(); });
$(document).mousedown(function(){ resetTimer(); });

//Replace the number on the "My Tasks" menu with the number of tasks assigned to the user
/*
$("#MyTasksMenuId").ready(getTaskCount());
function getTaskCount(){
	new Ajax.Request('/tdstm/task/retrieveUserToDoCount',{asynchronous:true,evalScripts:true,onComplete:function(e){setTaskCount(e)}});;
}
function setTaskCount( e ){
	var count = eval("(" + e.responseText + ")")[0].count;
	if(typeof count != 'undefined')
		$("#MyTasksMenuId").html("My Tasks: " + count);
}
*/
/*---------------------------------------------------
* Script to load the marquee to scroll the live news
*--------------------------------------------------*/
if (typeof(marqueeInit) == "function") {
	marqueeInit({
		uniqueid: 'head_mycrawler',
		inc: 8, //speed - pixel increment for each iteration of this marquee's movement
		mouse: 'cursor driven', //mouseover behavior ('pause' 'cursor driven' or false)
		moveatleast: 4,
		neutral: 150,
		savedirection: false
	});	
}


// Update person details 
function updatePersonDetails( e ){
	if (tdsCommon.isValidWsResponse(e, "Can't retrive person details", false)) {
		console.log(e);
		var personDetails = eval("(" + e.responseText + ")");
		
		// If the user account is not local, we should hide all password-related functionality.
		if(!personDetails.isLocal){
			$(".js-password").each(function(e){
				$(this).css("display", "none");
			});
		}

		$("#personId").val(personDetails.person.id)
		$("#firstNameId").val(personDetails.person.firstName);
		$("#middleNameId").val(personDetails.person.middleName);
		$("#lastNameId").val(personDetails.person.lastName);
		$("#nickNameId").val(personDetails.person.nickName);
		$("#emailId").val(personDetails.person.email);
		$("#titleId").val(personDetails.person.title);
		$("#expiryDateId").val(personDetails.expiryDate);
		$("#personDialog").dialog('option', 'width', 540);
		$("#personDialog").dialog('option', 'modal', true);
		$("#personDialog").dialog("open");

        $('.ui-widget-overlay').addClass('old-legacy-content');
	}
}
function changePersonDetails () {
    var dateRegExpForExp = /^(0[1-9]|1[012])[/](0[1-9]|[12][0-9]|3[01])[/](19|20)\d\d ([0-1][0-9]|[2][0-3])(:([0-5][0-9])){1,2} ([APap][Mm])$/;

	var returnVal = true 
	var firstName = $("#firstNameId").val()
	var oldPassword = $("#personDialog #oldPasswordId").val()
	var newPassword = $("#personDialog #passwordId").val()
	var email = $("#emailId").val()
	var expiryDate = $("#expiryDateId").val()
	var powerType = $("#powerTypeId").val()
	var startPage = $("#startPage").val()
	
	if (expiryDate + "" == "undefined") {
		expiryDate = "null"
	}
	if (!firstName) {
		alert("First Name should not be blank ")
		returnVal = false
	} else if ( email && !tdsCommon.isValidEmail(email)) {
		alert(email + " is not a valid e-mail address ")
		returnVal = false
	} else if (expiryDate != "null" && !expiryDate) {
		alert("Expiry Date should not be blank ")
		returnVal = false
	} else if (expiryDate != "null" && !dateRegExpForExp.test(expiryDate)) {
		alert("Expiry Date should be in 'mm/dd/yyyy HH:MM AM/PM' format")
		returnVal = false
	} else if (oldPassword + newPassword != "") {
		if (!oldPassword) {
			alert("Old Password should not be blank ")
			returnVal = false
		} else if (!PasswordValidation.checkPassword($("#personDialog #passwordId")[0])) {
			alert("New Password does not meet all the requirements ")
			returnVal = false
		}
	}
	if (returnVal) {

		var parameters = 'id=' + $('#personId').val()
        +'&firstName='+$('#firstNameId').val() +'&lastName='+$('#lastNameId').val() +'&middleName='+$('#middleNameId').val()
        +'&nickName='+$('#nickNameId').val()+'&title='+$('#titleId').val()+'&oldPassword='+$('#personDialog #oldPasswordId').val()
        +'&newPassword='+$('#personDialog #passwordId').val() +'&confirmPassword='+$('#personDialog #confirmPasswordId').val()
        +'&timeZone='+$('#timeZoneId').val()+'&email='+$('#emailId').val()+'&expiryDate='+expiryDate
        +'&powerType='+powerType+'&startPage='+startPage

        jQuery.ajax({
            url: tdsCommon.createAppURL('/person/updateAccount'),
            type:'POST',
			data: parameters,
            success: function(response) {
            	tdsCommon.prepareJQueryAjaxResponse(response);
                updateWelcome(response);
            },
            error: function(jqXHR, textStatus, errorThrown) {
                console.log("/person/updateAccount - " + errorThrown);
            }
        });
	}
}

function updateWelcome( e ) {
	var data = tdsCommon.isJQueryAjaxResponse(e) ? tdsCommon.isValidWsJQueryAjaxResponse(e, "An unexpected error occurred while attempting to perform the update.", false)
		: tdsCommon.isValidWsResponse(e, "An unexpected error occurred while attempting to perform the update.", false);
	if (data !== false) {
		$("#loginUserId").html(data.name)
		$("#tzId").html(data.tz)
		$("#personDialog").dialog('close')
		window.location.reload()
	}
}

function setUserTimeZone( tz ){
	new Ajax.Request('/tdstm/project/setUserTimeZone',{asynchronous:true,evalScripts:true,onComplete:function(e){updateTimeZone(e)},parameters:'tz=' + tz });
}
function updateTimeZone( e ){
	var sURL = unescape(window.location);
	window.location.reload();
}
//Function to wait for .5 sec to show megamenu to avoid unintended hover.
var tipTimer = null;
function clearTipTimer() {
	if (tipTimer) {
		clearTimeout(tipTimer);
		tipTimer = null;
	}
}
function hoverMegaMenu(e){
	if($(".megamenu:visible").is(":visible")){
		if(megamenuitem!=e){
			clearTipTimer();
		  tipTimer = setTimeout(function() {
			tipTimer = null;
			showMegaMenu(e);
		  }, 500);
		}
	}
}
function showMegaMenu(e){
	$(".headerClass").removeClass("ui-icon");
	if(megamenuitem!=e)
		$(".menu2 .menuActive").removeClass('menuActive').addClass('inActive')
		$("#userMegaMenu").removeClass('menuActive').addClass('inActive')
	if($(e).hasClass('inActive'))
		$(e).addClass('menuActive').removeClass('inActive')
	else
		$(e).addClass('inActive').removeClass('menuActive')
	
	resetmenu2();
	if(e!=""){
		clearTipTimer()
		megamenuitem = e;
		switch(e){
		case "#adminMegaMenu":
			if($("#adminMegaMenu:visible").length){
				$("#adminMenuId a").css('background-color','lightblue');
				$("#adminMenuId a").css('border-right-color','lightblue');
				$("#adminMenuId a").css('color','#354E81');
				$("#adminAnchor").css("display","inline")
			}
			break;
		case "#projectMegaMenu":
			if($("#projectMegaMenu:visible").length){
				$("#projectMenuId a").css('background-color','lightblue');
				$("#projectMenuId a").css('border-right-color','lightblue');
				$("#projectMenuId a").css('color','#354E81');
				$("#projectAnchor").css("display","inline")
			}
			break;
		case "#racksMegaMenu":
			if($("#racksMegaMenu:visible").length){
				$("#roomMenuId a").css('background-color','lightblue');
				$("#roomMenuId a").css('border-right-color','lightblue');
				$("#roomMenuId a").css('color','#354E81');
				$("#rackMenuId a").css('background-color','lightblue');
				$("#rackMenuId a").css('border-right-color','lightblue');
				$("#rackMenuId a").css('color','#354E81');
				$("#rackAnchor").css("display","inline")
			}
			break;
		case "#assetMegaMenu":
			if($("#assetMegaMenu:visible").length){
				$("#assetMenuId a").css('background-color','lightblue');
				$("#assetMenuId a").css('border-right-color','lightblue');
				$("#assetMenuId a").css('color','#354E81');
				$("#assetAnchor").css("display","inline")
			}
			break;
		case "#bundleMegaMenu":
			if($("#bundleMegaMenu:visible").length){
				$("#eventMenuId a").css('background-color','lightblue');
				$("#eventMenuId a").css('border-right-color','lightblue');
				$("#eventMenuId a").css('color','#354E81');
				$("#bundleMenuId a").css('background-color','lightblue');
				$("#bundleMenuId a").css('border-right-color','lightblue');
				$("#bundleMenuId a").css('color','#354E81');
				$("#bundleAnchor").css("display","inline")
			}
			break;
		case "#teamMegaMenu":
			/*if($("#teamMegaMenu:visible").length){*/
				jQuery.ajax({
					url: contextPath+'/task/retrieveUserToDoCount',
					type:'POST',
					success: function(resp) {
						$("#todoCountProjectId").html(resp.count)
					},
					error: function(jqXHR, textStatus, errorThrown) {
						console.log("Unable to lookup task count - " + errorThrown)
					}
				});
				$("#teamMenuId a").css('background-color','lightblue');
				$("#teamMenuId a").css('border-right-color','lightblue');
				$("#teamMenuId a").css('color','#354E81');
			/*}*/
			break;
		case "#consoleMegaMenu":
			if($("#consoleMegaMenu:visible").length){
				$("#consoleMenuId a").css('background-color','lightblue');
				$("#consoleMenuId a").css('border-right-color','lightblue');
				$("#consoleMenuId a").css('color','#354E81');
				$("#consoleAnchor").css("display","inline")
			}
			break;
		case "#dashboardMegaMenu":
			if($("#dashboardMegaMenu:visible").length){
				$("#dashboardMenuId a").css('background-color','lightblue');
				$("#dashboardMenuId a").css('border-right-color','lightblue');
				$("#dashboardMenuId a").css('color','#354E81');
				$("#dashboardAnchor").css("display","inline")
			}
			break;
		case "#reportsMegaMenu":
			if($("#reportsMegaMenu:visible").length){
				$("#reportsMenuId a").css('background-color','lightblue');
				$("#reportsMenuId a").css('border-right-color','lightblue');
				$("#reportsMenuId a").css('color','#354E81');
				$("#reportAnchor").css("display","inline")
			}
			break;
		case "#userMegaMenu":
			if($("#userMegaMenu:visible").length){
				$("#userMenuId div").css('background-color','lightblue');
			}
			break;
		}
	}
}
function hideMegaMenu( id ){
	$("#"+id).removeClass('inActive').addClass('menuActive')
}
function closeMegaMenu() {
	if(megamenuitem) $(megamenuitem).removeClass('menuActive').addClass('inActive');
	resetmenu2();
}
function resetmenu2 () {
	$("#adminMenuId a").css('background-color','#354E81');
	$("#adminMenuId a").css('border-right-color','#354E81');
	$("#adminMenuId a").css('color','#9ACAEE');
	$("#projectMenuId a").css('background-color','#354E81');
	$("#projectMenuId a").css('border-right-color','#354E81');
	$("#projectMenuId a").css('color','#9ACAEE');
	$("#roomMenuId a").css('background-color','#354E81');
	$("#roomMenuId a").css('border-right-color','#354E81');
	$("#roomMenuId a").css('color','#9ACAEE');
	$("#rackMenuId a").css('background-color','#354E81');
	$("#rackMenuId a").css('border-right-color','#354E81');
	$("#rackMenuId a").css('color','#9ACAEE');
	$("#assetMenuId a").css('background-color','#354E81');
	$("#assetMenuId a").css('border-right-color','#354E81');
	$("#assetMenuId a").css('color','#9ACAEE');
	$("#eventMenuId a").css('background-color','#354E81');
	$("#eventMenuId a").css('border-right-color','#354E81');
	$("#eventMenuId a").css('color','#9ACAEE');
	$("#bundleMenuId a").css('background-color','#354E81');
	$("#bundleMenuId a").css('border-right-color','#354E81');
	$("#bundleMenuId a").css('color','#9ACAEE');
	$("#teamMenuId a").css('background-color','#354E81');
	$("#teamMenuId a").css('border-right-color','#354E81');
	$("#teamMenuId a").css('color','#9ACAEE');
	$("#consoleMenuId a").css('background-color','#354E81');
	$("#consoleMenuId a").css('border-right-color','#354E81');
	$("#consoleMenuId a").css('color','#9ACAEE');
	$("#dashboardMenuId a").css('background-color','#354E81');
	$("#dashboardMenuId a").css('border-right-color','#354E81');
	$("#dashboardMenuId a").css('color','#9ACAEE');
	$("#reportsMenuId a").css('background-color','#354E81');
	$("#reportsMenuId a").css('border-right-color','#354E81');
	$("#reportsMenuId a").css('color','#9ACAEE');
	$("#userMenuId div").css('background-color','');
	$("#adminAnchor").css('color','#9ACAEE')
	$("#projectAnchor").css('color','#9ACAEE')
	$("#rackAnchor").css('color','#9ACAEE')
	$("#assetAnchor").css('color','#9ACAEE')
	$("#bundleAnchor").css('color','#9ACAEE')
	$("#consoleAnchor").css('color','#9ACAEE')
	$("#dashboardAnchor").css('color','#9ACAEE')
	$("#reportAnchor").css('color','#9ACAEE')
	if(currentMenuId == "#adminMenu"){$("#adminMenuId a").css('background-color','#003366')}
	if(currentMenuId == "#projectMenu"){$("#projectMenuId a").css('background-color','#003366')}
	if(currentMenuId == "#roomsMenu"){$("#roomMenuId a").css('background-color','#003366')}
	if(currentMenuId == "#racksMenu"){$("#rackMenuId a").css('background-color','#003366')}
	if(currentMenuId == "#assetMenu"){$("#assetMenuId a").css('background-color','#003366')}
	if(currentMenuId == "#eventMenu"){$("#eventMenuId a").css('background-color','#003366')}
	if(currentMenuId == "#bundleMenu"){$("#bundleMenuId a").css('background-color','#003366')}
	if(currentMenuId == "#teamMenuId"){$("#teamMenuId a").css('background-color','#003366')}
	if(currentMenuId == "#consoleMenu"){$("#consoleMenuId a").css('background-color','#003366')}
	if(currentMenuId == "#dashboardMenu"){$("#dashboardMenuId a").css('background-color','#003366')}
	if(currentMenuId == "#reportsMenu"){$("#reportsMenuId a").css('background-color','#003366')}
	if(currentMenuId == "#userMenu"){$("#userMenuId div").css('background-color','')}
}
function showSubMenu(e) {
	$('#adminMenu').hide();
	$('#projectMenu').hide();
	$('#assetMenu').hide();
	$('#bundleMenu').hide();
	$('#consoleMenu').hide();
	$('#dashboardMenu').hide();
	$('#reportsMenu').hide();
	showMegaMenu('');
	if(e!=""){
	//temp disable of submenu...
	//	$(e).show();
	}
}

function setPower( p ){
	new Ajax.Request('/tdstm/project/setPower',{asynchronous:true,evalScripts:true,onComplete:function(e){updateTimeZone( e )},parameters:'p=' + p });
}

//page load startup stuff

showSubMenu(currentMenuId);

//set up mega menus to align with menu2 items
//admin and user mega menus are to the edges
var menus = [['project', 'project'], ['racks', 'room'], ['asset', 'asset'], ['bundle', 'event'], ['team', 'team'], ['console', 'console'], ['dashboard', 'dashboard'], ['reports', 'reports']];

// iterate through each menu to position the megamenus
for (var i = 0; i < menus.length; ++i) {
	
	// only perform calculations on menus that are shown for this user
	if ($('#' + menus[i] + 'MegaMenu').length > 0 && $('#' + menus[i][1] + 'MenuId').length > 0) {
	
		// the reports menu uses the right offset instead of the left
		if (menus[i] != 'reports')
			$('#' + menus[i][0] + 'MegaMenu').css("left", $('#' + menus[i][1] + 'MenuId').offset().left + "px");
		else
			$('#' + menus[i][0] + 'MegaMenu').css("left", $('#' + menus[i][1] + 'MenuId').offset().right + "px");
		
		$('#' + menus[i][0] + 'MegaMenu').css("top", $('#' + menus[i][1] + 'MenuId').offset().bottom + 1 + "px");
	}
}

var timeout = 500;
var megamenuitem = 0;
// Global click method should be avoided completely. This will be refactored with the new incoming menu
$(document).click(function(e){
	if(e){
		var htmlElement = $(e.target);
		if(htmlElement && htmlElement.length && htmlElement.get(0).tagName && htmlElement.get(0).tagName !== 'svg'){
			if (!$(e.target).is('.tzmenu,#tzId')) {
				$(".tzmenu ul").hide();
			}
			if (!$(e.target).is('.headerClass')) {
				closeMegaMenu();
			}
		}
	}
});
