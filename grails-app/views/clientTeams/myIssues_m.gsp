<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title>My Tasks - Transition Manager</title>
	<meta name="viewport" content="initial-scale=.86; width=device-width ;" />
	<jq:plugin name="jquery"/>
	<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'main.css')}" />
	<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'tds.css')}" />
	<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'qvga.css')}" />
	<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.theme.css')}" />
	<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.core.css')}" />
	<link rel="shortcut icon" href="${resource(dir:'images',file:'tds.ico')}" type="image/x-icon" />
	<script type="text/javascript" src="${resource(dir:'js' ,file:'ui.datepicker.js') }" />
	
	<script type="text/javascript">    	
		window.addEventListener('load', function(){
			setTimeout(scrollTo, 0, 0, 1);
		}, false);

		function setFocus(){
			document.issueAssetForm.search.focus();
		}
	
		function updateOrientation(){
			var displayStr = "Orientation : ";
			switch(window.orientation) {
	                    case 0,180:
				displayStr += "Portrait";
				var elems = document.getElementsByClassName("col2");
				for(var i = 0; i < elems.length; i++) elems[i].style.display = "none";
	                    break;
	                    case -90,90:
				displayStr += "Landscape";
				var elems = document.getElementsByClassName("col2");
				for(var i = 0; i < elems.length; i++) elems[i].style.display = "block";
	                    break;
	                }
			document.issueAssetForm.search.value = displayStr;
		}
	</script>      
</head>
<body onorientationchange="updateOrientation();">
	<div class="mainbody" style="width: 100%;">
	   <div style="width: 100%;" id="mobtitle">TransitionManager&trade; - Mobile</div>
	     <div style="width: 100%;" id="myTaskListMobile">
		 	<g:render template="tasks_m"/>
		 </div>
		<g:link class="mobfooter" action="listTasks" params="[viewMode:'web', tab:tab]">Use Full Site</g:link>
	</div>
  </div>
<script type="text/javascript">
	$( function() {
	  $('#issueTimebar').width($('#issueTable').width())
	});

	function setFocus(){
		document.issueAssetForm.search.focus();
    }
	function issueDetails(id){
		jQuery.ajax({
			url: 'showIssue',
			data: {'issueId':id},
			type:'POST',
			success: function(data) {
				B1.Pause()
				$('#showStatusId_'+id).css('display','none')
				$('#issueTr_'+id).attr('onClick','cancelButton('+id+')');
				$('#detailId_'+id).html(data)
				$('#detailTdId_'+id).css('display','table-row')
				//$('#detailId_'+id).css('display','block')
				$('#taskLinkId').removeClass('mobselect')
			}
		});
	}
	function cancelButton(id){
		B1.Start(60);
		//$('#myIssueList').css('display','block')
		$('#detailTdId_'+id).css('display','none')
		$('#taskLinkId').addClass('mobselect')
		$('#showStatusId_'+id).css('display','table-row')
		$('#issueTr_'+id).attr('onClick','issueDetails('+id+')');
	}
	function changeStatus(id,status,user){
		// Disable status change buttons to prevent double-clicking
		$('#started_button_'+id).removeAttr('onclick');
		$('#done_button_'+id).removeAttr('onclick');
		
		jQuery.ajax({
			url: '../task/update',
			data: {'id':id,'status':status,'assignedTo':user,'view':'myTask','tab':$('#tab_m').val()},
			type:'POST',
			success: function(data) {
				 $('#myTaskListMobile').html(data)
				 $('#showStatusId_'+id).show()
				 $('#issueTrId_'+id).attr('onClick','hideStatus('+id+',"'+status+'")')
				 if(status=='Started'){
				 	$('#started_'+id).hide()
				 }
				 B1.Start(60);
			}
		});
		
	}

	function openStatus(id,status){
		if(status=='Started'){
			$('#started_'+id).css('display','none')
			$('#image_'+id).css('display','none')
	    }
		$('#showStatusId_'+id).show()
		$('#issueTrId_'+id).attr('onClick','hideStatus('+id+',"'+status+'")');
	}
	
	function hideStatus(id,status){
		$('#showStatusId_'+id).hide()
		$('#detailTdId_'+id).css('display','none')
		$('#issueTrId_'+id).attr('onClick','openStatus('+id+',"'+status+'")');
		B1.Start(60);
	}
	
	function changeAction(){
		 document.issueAssetForm.action = 'listTasks'
	}

	function retainAction(){
		 document.issueAssetForm.action = 'showIssue'
	}
	function pageRefresh(){
		document.issueAssetForm.action = 'listTasks'
		document.issueAssetForm.submit()
	}
	function setTab(tab){
		$("#tabId").val(tab)
	}
	
function zxcAnimate(mde,obj,srt){
	this.to=null;
	this.obj=typeof(obj)=='object'?obj:document.getElementById(obj);
	this.mde=mde.replace(/\W/g,'');
	this.data=[srt||0];
	return this;
}

zxcAnimate.prototype.animate=function(srt,fin,ms,scale,c){
	clearTimeout(this.to);
	this.time=ms||this.time||0;
	this.neg=srt<0||fin<0;
	this.data=[srt,srt,fin];
	this.mS=this.time*(!scale?1:Math.abs((fin-srt)/(scale[1]-scale[0])));
	this.c=typeof(c)=='string'?c.charAt(0).toLowerCase():this.c?this.c:'';
	this.inc=Math.PI/(2*this.mS);
	this.srttime=new Date().getTime();
	this.cng();
}

zxcAnimate.prototype.cng=function(){
	var oop=this,ms=new Date().getTime()-this.srttime;
	this.data[0]=(this.c=='s')?(this.data[2]-this.data[1])*Math.sin(this.inc*ms)+this.data[1]:(this.c=='c')?this.data[2]-(this.data[2]-this.data[1])*Math.cos(this.inc*ms):(this.data[2]-this.data[1])/this.mS*ms+this.data[1];
	this.apply();
	if (ms<this.mS) this.to=setTimeout(function(){oop.cng()},10);
	else {
		this.data[0]=this.data[2];
		this.apply();
	 if (this.Complete) this.Complete(this);
	}
}

zxcAnimate.prototype.apply=function(){
	if (isFinite(this.data[0])){
		if (this.data[0]<0&&!this.neg) this.data[0]=0;
		if (this.mde!='opacity') this.obj.style[this.mde]=Math.floor(this.data[0])+'px';
		else zxcOpacity(this.obj,this.data[0]);
	}
}

function zxcOpacity(obj,opc){
	if (opc<0||opc>100) return;
	obj.style.filter='alpha(opacity='+opc+')';
	obj.style.opacity=obj.style.MozOpacity=obj.style.WebkitOpacity=obj.style.KhtmlOpacity=opc/100-.001;
}
	setFocus();

	function Bar(o){
	var obj=document.getElementById(o.ID);
		this.oop=new zxcAnimate('width',obj,0);
		this.max=obj.parentNode.offsetWidth;
		this.to=null;
	}
	Bar.prototype={
		Start:function(sec){
			clearTimeout(this.to);
			this.oop.animate(0,this.max,sec*1000);
			this.srt=new Date();
			this.sec=sec;
			this.Time();
		},
		Time:function(sec){
			var oop=this,sec=this.sec-Math.floor((new Date()-this.srt)/1000);
		//	this.oop.obj.innerHTML=sec+' sec';
			if (sec>0){
				this.to=setTimeout(function(){ oop.Time(); },1000);
			}else{
				pageRefresh();
			}
		},
		Pause:function(){
			clearTimeout(this.to);
		}
	}

	var B1=new Bar({
		ID:'issueTimebar'
	});

	B1.Start(60);
	
	function onScan(ev){
		var scan = ev.data;
		document.forms[0].search.value = scan.value;
		document.forms[0].submit();
	}
	document.addEventListener("BarcodeScanned", onScan, false);
	
</script>
</body>
</html>
