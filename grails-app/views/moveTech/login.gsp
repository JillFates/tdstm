<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="refresh" content="0; url=/tdstm/">
<title>Login</title>
<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'tds-main.css')}" />
<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'qvga.css')}" />
<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'tds.css')}" />
    <tds:favicon />
<g:javascript library="application" />
<meta name="viewport" content="height=device-height,width=220" />
<script type="text/javascript">
        window.addEventListener('load', function(){
                setTimeout(scrollTo, 0, 0, 1);
        }, false);
   /*========================================
	*	Check cookie if exist fill username
	*=======================================*/
	function getCookie(user_name){
		var username = "${username}"
		if(!username){
			username = ""
			if (document.cookie.length>0){
		  		user_start=document.cookie.indexOf(user_name + "=");
		  		if (user_start!=-1) { 
		    		user_start=user_start + user_name.length+1 ;
		   			user_end=document.cookie.indexOf(";",user_start);
		    		if (user_end==-1) user_end=document.cookie.length
	
		    		username = unescape(document.cookie.substring(user_start,user_end));
		    	}
		  	}
			document.loginForm.username.value = username
		}
		document.loginForm.username.focus()
	}
	/*==========================
	*  Set username in cookie.
	*==========================*/

	function setCookie(user_name,expirehours){
		var exdate=new Date();
		var value = document.loginForm.username.value
		exdate.setHours(exdate.getHours()+expirehours);
		document.cookie = user_name+ "=" +escape(value)+((expirehours==null) ? "" : "; expires="+exdate);
		return true
	}
</script>

</head>
<body onload="getCookie('username');">
<div id="spinner" class="spinner" style="display: none;">
<img src="${resource(dir:'images',file:'spinner.gif')}" alt="Spinner" />
</div>
<div class="mainbody" style="width: auto;">
<div class="colum_techlogin_login" style="float:left;">
<div class="w_techlog_login"><g:form action="signIn" name="loginForm" onsubmit="return setCookie('username',2)">
        <input type="hidden" name="targetUri" value="${targetUri}" />
        <input type="hidden" name="browserName" id="browserName" value="" />
        <g:if test="${flash.message}">
        <div style="width: 200px;" class="message">${flash.message}</div>
        </g:if>
        <div style="float: left; padding-left: 2px; width: 98%; margin-top: 2px;">
		<img src="${resource(dir:'images',file:'tds_movetech.jpg')}" border="0" alt="tds" />
        </div>

        <div style="float: left; width: 100%; margin: 4px 0; text-align: center;">
        <table style="border: 0px;">
                <tbody>
                        <tr>
                                <td style="height: 2px;"></td>
                        </tr>
                        <tr>
                                <td style="text-align: center;">
                                <span style="color: #328714; font: bold 15px arial;">Transition Manager - Mobile</span>
                                </td>
                        </tr>
                        <tr>
                                <td style="text-align:center;"><label>Username:</label>
                                <input type="text" size="13" name="username" value="${username}" onfocus="select()" autocorrect="off" autocapitalize="off" /></td>
                        </tr>
                        <tr>
                                <td class="buttonR" style="text-align: center;">
                                <input type="submit" value="Login" /></td>
                        </tr>
                </tbody>
        </table>
        </div>
</g:form></div>
<div class="left_bcornerlog"></div>
<div class="right_bcornerlog"></div>
</div>
</div>
<div class="logo"></div>
</body>
</html>
