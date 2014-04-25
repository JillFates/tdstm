//menu and other header functions...loaded from projectHeader

	    var timerId;
	    timerId = window.setTimeout("timeOut()",(60000 * 120));
	    
	    function resetTimer() {
	        window.clearTimeout(timerId);
	        timerId = window.setTimeout("timeOut()",(60000 * 120));
	    }
	    function timeOut()
	    {
	         new Ajax.Request('/tdstm/auth/signOut',{asynchronous:true,evalScripts:true,onComplete:function(e){sessionExpireOverlay()}});;
	    }
	    function sessionExpireOverlay()
	    {
	    	window.parent.location = self.location;
	    }
			$(document).keydown(function(){ resetTimer(); });
			$(document).mousedown(function(){ resetTimer(); });

		//Replace the number on the "My Tasks" menu with the number of tasks assigned to the user
		/*
		$("#MyTasksMenuId").ready(getTaskCount());
		function getTaskCount(){
			new Ajax.Request('/tdstm/clientTeams/getToDoCount',{asynchronous:true,evalScripts:true,onComplete:function(e){setTaskCount(e)}});;
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
		marqueeInit({
			uniqueid: 'head_mycrawler',
			inc: 8, //speed - pixel increment for each iteration of this marquee's movement
			mouse: 'cursor driven', //mouseover behavior ('pause' 'cursor driven' or false)
			moveatleast: 4,
			neutral: 150,
			savedirection: false
		});

		// Update person details 
		function updatePersonDetails( e ){
			var personDetails = eval("(" + e.responseText + ")");
			$("#personId").val(personDetails.person.id)
			$("#firstNameId").val(personDetails.person.firstName);
			$("#middleNameId").val(personDetails.person.middleName);
			$("#lastNameId").val(personDetails.person.lastName);
			$("#nickNameId").val(personDetails.person.nickName);
			$("#emailId").val(personDetails.person.email);
			$("#titleId").val(personDetails.person.title);
			$("#expiryDateId").val(personDetails.expiryDate);
			$("#personDialog").dialog('option', 'width', 540)
		    $("#personDialog").dialog("open")
		}
		function changePersonDetails(){
			var returnVal = true 
	    	var firstName = $("#firstNameId").val()
	    	var oldPassword = $("#oldPasswordId").val()
	    	var newPassword = $("#newPasswordId").val()
	        var email = $("#emailId").val()
	        var expiryDate = $("#expiryDateId").val()
	        var powerType = $("#powerTypeId").val()
	        var startPage = $("#startPage").val()
	        
	        if(expiryDate + "" == "undefined"){
	        	expiryDate = "null"
			}
			if(!firstName) {
	            alert("First Name should not be blank ")
	            returnVal = false
	        } else if( email && !emailRegExp.test(email)){
	        	 alert(email + " is not a valid e-mail address ")
	             returnVal = false
	        } else if(expiryDate != "null" && !expiryDate){
	        	alert("Expiry Date should not be blank ")
	            returnVal = false
	        } else if(expiryDate != "null" && !dateRegExpForExp.test(expiryDate)){
		        alert("Expiry Date should be in 'mm/dd/yyyy HH:MM AM/PM' format")
		        returnVal = false
	        } else if(oldPassword+newPassword != ""){
	            if(!oldPassword){
		        	alert("Old Password should not be blank ")
		            returnVal = false
		        } else if(!checkPassword($("#newPasswordId")[0])){
		        	alert("New Password does not meet all the requirements ")
		            returnVal = false
		        }
	        }
	        if(returnVal){
				new Ajax.Request('/tdstm/person/checkPassword',{asynchronous:true,evalScripts:true,onComplete:function(e){updateWelcome(e)},parameters:'id=' + $('#personId').val() 
											+'&firstName='+$('#firstNameId').val() +'&lastName='+$('#lastNameId').val() +'&middleName='+$('#middleNameId').val()
											+'&nickName='+$('#nickNameId').val()+'&title='+$('#titleId').val()+'&oldPassword='+$('#oldPasswordId').val()
											+'&newPassword='+$('#newPasswordId').val()
											+'&timeZone='+$('#timeZoneId').val()+'&email='+$('#emailId').val()+'&expiryDate='+expiryDate
											+'&powerType='+powerType+'&startPage='+startPage});
	        }
		}
		function checkPassword(field) {
			var requirements = 0;
			var password = field.value;
			var username;
			if(field.id == "newPasswordId")
				username = $('#prefUsernameId').val().toLowerCase();
			else
				username = $('#username').val().toLowerCase();
			var context = 0;
			var noMatch = {item:null};
			var score = 0;
			if (field.id == "newPasswordId" && $("[id='lengthRequirementId']").length > 1) {
				context = 1;
			}
			score += passwordMatch(password.toLowerCase(), $("[id='usernameRequirementId']")[context], new RegExp('.*' + username + '.*'), 'Password must not contain the username', {item:null});
			score += passwordMatch(password, $("[id='lengthRequirementId']")[context], /.{8}/, 'Password must be at least 8 characters long', {item:null});
			requirements += passwordMatch(password, $("[id='lowercaseRequirementId']")[context], /[a-z]+/, 'Lowercase characters', noMatch);
			requirements += passwordMatch(password, $("[id='uppercaseRequirementId']")[context], /[A-Z]+/, 'Uppercase characters', noMatch);
			requirements += passwordMatch(password, $("[id='numericRequirementId']")[context], /[0-9]+/, 'Numeric characters', noMatch);
			requirements += passwordMatch(password, $("[id='symbolRequirementId']")[context], /.*[~!@#\$%\^&\*_\-\+=`\|\\\(\)\{\}\[\]:;"'<>\,\.\?\/].*/, 'Nonalphanumeric characters', noMatch);
			if (passwordMatch(requirements + "", $("[id='passwordRequirementsId']")[context], /[3-4]+/, 'Password must contain at least 3 of these requirements: ', noMatch) == 1){
				score++;
				if(noMatch.item != null)
					noMatch.item.style.color = "#555555";
			}
			
			return (score == 3);
		}
		function passwordMatch(password, element, regex, baseText, noMatch){
			var returnVal = 0;
			color = '#cc0000';
			text = '';

			if (!(element.id == 'usernameRequirementId') == (password.match(regex) != null)){
				color = '#00aa00';
				text = ' OK';
				returnVal = 1;
			} else {
				noMatch.item = element;
			}
			element.style.color = color;
			element.innerHTML = baseText + text;
			return returnVal;
		}
		function togglePasswordVisibility(box){
			var newState = "text";
			if(box.checked){
				newState = 'password';
			}
			if(box.id == "showPasswordEditId"){
				$("#password")[0].type = newState;
			} else if(box.id == "showPasswordCreateId"){
				$("#oldPasswordId")[0].type = newState;
			} else{
				$("#oldPasswordId")[0].type = newState;
				$("#newPasswordId")[0].type = newState;
			}
		}
		function updateWelcome( e ){
			var ret = eval("(" + e.responseText + ")");
			if(ret[0].pass == "no")
				alert("Old Password is incorrect")
			else if(ret[0].pass == "invalid")
				alert("New Password does not meet the requirements")
			else{
				$("#loginUserId").html(ret[0].name)
				$("#tzId").html(ret[0].tz)
				$("#personDialog").dialog('close')
				window.location.reload()
			}
		}
		function setUserTimeZone( tz ){
			new Ajax.Request('/tdstm/project/setUserTimeZone',{asynchronous:true,evalScripts:true,onComplete:function(e){updateTimeZone(e)},parameters:'tz=' + tz });
		}
		function updateTimeZone( e ){
			var sURL = unescape(window.location);
			window.location.href = sURL;
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
				$(".menu2 .active").removeClass('active').addClass('inActive')
			if($(e).hasClass('inActive'))
				$(e).addClass('active').removeClass('inActive')
			else
				$(e).addClass('inActive').removeClass('active')
			
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
					if($("#teamMegaMenu:visible").length){
						jQuery.ajax({
							url: contextPath+'/clientTeams/getToDoCount',
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
						$("#teamAnchor").css("display","inline")
					}
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
			$("#"+id).removeClass('inActive').addClass('active')
		}
		function closeMegaMenu() {
			if(megamenuitem) $(megamenuitem).removeClass('active').addClass('inActive');
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
		function resetPreference(user){
			new Ajax.Request('/tdstm/person/resetPreferences',{asynchronous:true,evalScripts:true,onSuccess:function(e){changeResetMessage(e)},parameters:'user=' + user });
		}
		function changeResetMessage(e){
			$("#userPrefDivId").html("")
			$('#userPrefDivId').dialog('close')
			window.location.reload()
		}
		
		function editPreference(){
			new Ajax.Request('/tdstm/person/editPreference',{asynchronous:true,evalScripts:true,
						onSuccess:function(e){
							$("#userPrefDivId").html(e.responseText)
							$("#userPrefDivId").dialog('option', 'width', 'auto')
							$("#userPrefDivId").dialog("open")
						}
					})
			
		}
		function removeUserPrefs(prefCode){
			new Ajax.Request('/tdstm/person/removeUserPreference?prefCode='+prefCode,
								{asynchronous:true,evalScripts:true,
									onSuccess:function(e){
										 $("#pref_"+prefCode).remove()
									}
								})
			
		}
		//page load startup stuff
		
		showSubMenu(currentMenuId);
		
		//set up mega menus to align with menu2 items
		//admin and user mega menus are to the edges
		$('#projectMegaMenu').css("left", $('#projectMenuId').offset().left+"px");
		$('#racksMegaMenu').css("left", $('#roomMenuId').offset().left+"px");
		$('#assetMegaMenu').css("left", $('#assetMenuId').offset().left+"px");
		$('#bundleMegaMenu').css("left", $('#eventMenuId').offset().left+"px");
		$('#teamMegaMenu').css("left", $('#teamMenuId').offset().left+"px");
		$('#consoleMegaMenu').css("left", $('#consoleMenuId').offset().left+"px");
		$('#dashboardMegaMenu').css("left", $('#dashboardMenuId').offset().left+"px");
		$('#reportsMegaMenu').css("right", $('#reportsMenuId').offset().right+"px");

		$('#projectMegaMenu').css("top", $('#projectMenuId').offset().bottom +1+"px");
		$('#racksMegaMenu').css("top", $('#roomMenuId').offset().bottom +1+"px");
		$('#assetMegaMenu').css("top", $('#assetMenuId').offset().bottom +1+"px");
		$('#bundleMegaMenu').css("top", $('#eventMenuId').offset().bottom +1+"px");
		$('#teamMegaMenu').css("top", $('#teamMenuId').offset().bottom +1+"px");
		$('#consoleMegaMenu').css("top", $('#consoleMenuId').offset().bottom +1+"px");
		$('#dashboardMegaMenu').css("top", $('#dashboardMenuId').offset().bottom +1+"px");
		$('#reportsMegaMenu').css("top", $('#reportsMenuId').offset().bottom +1+"px");

		var timeout = 500;
		var megamenuitem = 0;
		$(document).click(function(e){
	        if (!$(e.target).is('.tzmenu,#tzId')) {
	        	$(".tzmenu ul").hide();
	        }
	        if (!$(e.target).is('.headerClass')) {
	        	closeMegaMenu();
	        }
		});