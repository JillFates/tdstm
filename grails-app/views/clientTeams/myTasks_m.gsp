<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title>My Tasks</title>
<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'main.css')}" />
<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'tds.css')}" />
<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'qvga.css')}" />
<%--<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.core.css')}" />
	 <link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.move_tech_dialog.css')}" /> 
	 <link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.theme.css')}" />--%>
<link rel="shortcut icon" href="${resource(dir:'images',file:'tds.ico')}" type="image/x-icon" />
<meta name="viewport" content="height=device-height,width=220" />
	
<script type="text/javascript">    	
	window.addEventListener('load', function(){
		setTimeout(scrollTo, 0, 0, 1);
	}, false);

	function setFocus(){
		document.bundleTeamAssetForm.search.value = '';
		document.bundleTeamAssetForm.search.focus();
	}
	
	function assetSubmit(searchVal){
	document.bundleTeamAssetForm.search.value = searchVal; 
	document.bundleTeamAssetForm.submit();
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
		document.bundleTeamAssetForm.search.value = displayStr;
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

</script>      
</head>
<body onorientationchange="updateOrientation();">
	<div id="spinner" class="spinner" style="display: none;"><img src="${resource(dir:'images',file:'spinner.gif')}" alt="Spinner" /></div>
	<div class="mainbody" style="width: 220px;" >
		<div id="mobtitle">TransitionManager&trade; - Mobile</div>
	<div class="menu4">
		<ul>
			<li><g:link class="mobmenu" controller="clientTeams" params="[projectId:project?.id]">Teams</g:link></li>
			<li><g:link class="mobmenu" action='home' params='["bundleId":bundleId,"teamId":teamId,"location":location,"projectId":projectId]'>Home</g:link></li>
			<li><g:link class="mobmenu mobselect" action="myTasks" params='["bundleId":bundleId,"teamId":teamId,"location":location,"projectId":project?.id,"tab":"Todo"]'>Tasks</g:link></li>
			<li><a href="#" class="mobmenu">Asset</a></li>
		</ul>
	</div>
	<div class="timebar" ><div id="timebar" ></div>
	</div>
	<div class="mobbody">
      		<g:form method="post" name="bundleTeamAssetForm" action="assetSearch">
      					
	        <input name="bundleId" type="hidden" value="${bundleId}" />
		<input name="teamId" type="hidden" value="${teamId}" />
		<input name="location" type="hidden" value="${location}" />
		<input name="projectId" type="hidden" value="${projectId}" />
		<input name="tab" type="hidden" value="${tab}" />								              	
		<div id="mydiv" onclick="this.style.display = 'none';setFocus()">						            
 			<g:if test="${flash.message}">
				<br />
				<div style="color: red;"><ul>${flash.message}</ul></div>
			</g:if> 
		</div>		
		<div style="float:left; width:220px; margin:2px 0; ">              								
		<table style="border:0px;width:220px;">
		<tr>
			<td style="border-bottom:2px solid #507028;"><b>Tasks:</b></td>
			<td id="todoId" class="tab">
				<g:if test="${tab && tab == 'Todo'}">
				  <g:link class="tab_select" action="myTasks"  params='["bundleId":bundleId,"teamId":teamId,"location":location,"projectId":projectId,"tab":"Todo"]'>Todo&nbsp;(${todoSize})</g:link>
				</g:if>
				<g:else>
				  <g:link class="tab_deselect" action="myTasks"  params='["bundleId":bundleId,"teamId":teamId,"location":location,"projectId":projectId,"tab":"Todo"]'>Todo&nbsp;(${todoSize})</g:link>
				</g:else>
			</td>
			<td id="allId" class="tab">
				<g:if test="${tab == 'All'}">
				  <g:link class="tab_select" action="myTasks" params='["bundleId":bundleId,"teamId":teamId,"location":location,"projectId":projectId,"tab":"All"]'>All&nbsp;(${allSize})</g:link>
				</g:if>
				<g:else>
				  <g:link class="tab_deselect" action="myTasks" params='["bundleId":bundleId,"teamId":teamId,"location":location,"projectId":projectId,"tab":"All"]'>All&nbsp;(${allSize})</g:link>
				</g:else>
			</td>
			<td class="tab_search"><input  type="text" size="4" value="" id="search" name="search" autocorrect="off" autocapitalize="off" /></td>
		</tr>
		</table>
		</div> 
            	<div id="assetTableDiv" style="float:left;width:220px; ">
             			<table id="assetTable" style="height:80px;">
              				<thead>
                				<tr>
                  				<g:sortableColumn class="sort_column col1" style="width:60px;" action="myTasks" property="asset_tag" title="AssetTag" params="['bundleId':bundleId, 'teamId':teamId, 'tab':tab,'location':location,'projectId':projectId ]"></g:sortableColumn>
                  				<g:sortableColumn class="sort_column col2" style="width:60px; display:none;" action="myTasks" property="asset_name" title="AssetName" params="['bundleId':bundleId, 'teamId':teamId, 'tab':tab,'location':location,'projectId':projectId ]"></g:sortableColumn>
                  				<g:sortableColumn class="sort_column col3" style="width:60px;" action="myTasks" property="source_rack" title="Rack/Pos" params="['bundleId':bundleId, 'teamId':teamId, 'tab':tab,'location':location,'projectId':projectId ]"></g:sortableColumn>
                  				<g:sortableColumn class="sort_column col4" action="myTasks" property="model" title="Model" params="['bundleId':bundleId, 'teamId':teamId, 'tab':tab,'location':location,'projectId':projectId ]"></g:sortableColumn>
						</tr>
					</thead>
					<tbody>
						<g:each status="i" in="${assetList}" var="assetList">
							<tr class="${assetList.cssVal}"  onclick="assetSubmit('${assetList?.item?.assetTag}');">
								<td class="asset_details_block col1">${assetList?.item?.assetTag}</td>
								<td class="asset_details_block col2" style="display:none;">${assetList?.item?.assetName}</td>
								<g:if test="${location == 'source'}">
								<td class="asset_details_block col3">${assetList?.item?.sourceRack}/${assetList?.item?.sourceRackPosition}</td>
								</g:if>
								<g:else>
								<td class="asset_details_block col3">${assetList?.item?.targetRack}/${assetList?.item?.targetRackPosition}</td>
								</g:else>
								<td class="asset_details_block col4">${assetList?.item?.model}</td>
							</tr>
						</g:each>
					</tbody>
				</table>
			</div>
		</div>
      		</g:form>
  	</div>
<script type="text/javascript" >
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
//			this.oop.obj.innerHTML=sec+' sec';
			if (sec>0){
				this.to=setTimeout(function(){ oop.Time(); },1000);
			}
		}
	}

	var B1=new Bar({
		ID:'timebar'
	});

	B1.Start(100);
	
function onScan(ev){
	var scan = ev.data;
	document.forms[0].search.value = scan.value;
	document.forms[0].submit();
}
document.addEventListener("BarcodeScanned", onScan, false);

</script>
</body>
</html>
