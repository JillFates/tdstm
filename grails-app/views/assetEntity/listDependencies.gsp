<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="projectHeader" />
        <title>Dependencies List</title>
		<link type="text/css" rel="stylesheet" href="${resource(dir:'css/jqgrid',file:'ui.jqgrid.css')}" />
		<link type="text/css" rel="stylesheet" href="${g.resource(dir:'css',file:'ui.datepicker.css')}" />
		<g:javascript src="entity.crud.js" />
		<g:javascript src="asset.tranman.js" />
		<g:javascript src="angular/angular.min.js" />
		<g:javascript src="angular/plugins/angular-ui.js"/>
		<g:javascript src="asset.comment.js" />
		<g:javascript src="cabling.js"/>
		<jqgrid:resources />
		<g:javascript src="jqgrid-support.js" />
		
		<script type="text/javascript">
		
			$(document).ready(function() {
				$("#showEntityView").dialog({ autoOpen: false })
				$("#editEntityView").dialog({ autoOpen: false })
				$("#createCommentDialog").dialog({ autoOpen: false })
				$("#showCommentDialog").dialog({ autoOpen: false })
				$("#editCommentDialog").dialog({ autoOpen: false })
				$("#cablingDialogId").dialog({ autoOpen:false })
				var listCaption ="Dependencies: \
					<tds:hasPermission permission='AssetDelete'>\
					<span class='capBtn'><input type='button' id='deleteAssetId' value='Bulk Delete' onclick='deleteAssets(\"dependencies\")' disabled='disabled'/></span>\
					</tds:hasPermission>"
				<jqgrid:grid id="dependencyGridId" url="'${createLink(action: 'listDepJson')}'"
					editurl="'${createLink(action: 'deleteBulkAsset')}'"
					colNames="'Asset','AssetClass', 'Bundle','Type', 'Depends On', 'Dep Class', 'Dep Bundle', '${columnLabelpref['1']}', '${columnLabelpref['2']}', 'Status'"
					colModel="{name:'assetName', index: 'assetName', width:'200',formatter: myLinkFormatter},
								  {name:'assetType', editable: true},
								  {name:'assetBundle', editable: true},
								  {name:'type', editable: true}, 
								  {name:'dependentName', editable: true,formatter: dependentFormatter,width:'200'},
								  {name:'dependentType', editable: true},
								  {name:'dependentBundle', editable: true},
								  {name:'${depPref['1']}', editable: true,width:'100'},
								  {name:'${depPref['2']}',editable:true, width:'100'},
								  {name:'status', editable: true, width:'80'}"
					sortname="'assetName'"
					caption="listCaption"
					multiselect="true"
					loadComplete="initCheck"
					gridComplete="function(){bindResize('dependencyGridId')}"
					onSelectRow="validateMergeCount"
					showPager="true">
					<jqgrid:filterToolbar id="dependencyGridId" searchOnEnter="false" />
					<jqgrid:navigation id="dependencyGridId" add="false" edit="false" del="false" search="false" refresh="false" />
					<jqgrid:refreshButton id="dependencyGridId" />
				</jqgrid:grid>
				<g:each var="key" in="['1','2']">
					var depPref= '${depPref[key]}';
					$("#dependencyGridIdGrid_"+depPref).append('<img src="../images/select2Arrow.png" class="selectImage editSelectimage_'+${key}+'" style="position:relative;float:right;margin-top: -15px;" onclick="showSelect(\''+depPref+'\',\'dependencyGrid\',\''+${key}+'\')">');
				</g:each>
				$.jgrid.formatter.integer.thousandsSeparator='';
				function myLinkFormatter (cellvalue, options, rowObjcet) {
					var value = cellvalue ? cellvalue : ''
						return '<a href="javascript:getEntityDetails(\'dependencies\',\''+rowObjcet[1]+'\',\''+rowObjcet[10]+'\')">'+value+'</a>'
				}
				function dependentFormatter(cellvalue, options, rowObjcet){
					var value = cellvalue ? cellvalue : ''
						return '<a href="javascript:getEntityDetails(\'dependencies\',\''+rowObjcet[5]+'\',\''+rowObjcet[11]+'\')">'+value+'</a>'
				}
				
			})
		</script>
	</head>
	<body>
		<div class="body fluid">
			<h1>Dependencies List</h1>
			<g:if test="${flash.message}">
				<div id="messageDivId" class="message">${flash.message}</div>
			</g:if>
			<div>
				<div id="messageId" class="message" style="display:none"></div>
			</div>
			<div id="showEntityView" style="display: none;"></div>
			<div id="editEntityView" style="display: none;"></div>
			<div id="cablingDialogId" style="display: none;"></div>
			<jqgrid:wrapper id="dependencyGridId" />
			<g:each var="key" in="['1','2']">
				<div id="columnCustomDiv_${depPref[key]}" style="display:none;">
					<div class="columnDiv_${key} customScroll" style="background-color: #F8F8F8 ;height: 133px;position: fixed; top: 148px;width:8%;;z-index: 2147483647; overflow-y: scroll;text-align: left;">
						<input type="hidden" id="previousValue_${key}" value="${depPref[key]}" />
						<g:each var="attribute" in="${attributesList}">
							<label><input type="radio" name="coloumnSelector_${depPref[key]}" id="coloumnSelector_${depPref[key]}" value="${attribute}" 
								${depPref[key]==attribute?'checked':'' } style="margin-left:11px;" 
								onchange="setColumnAssetPref(this.value,'${key}','Dep_Columns')"/> ${attribute}</label><br>
						</g:each>
					</div>
				</div>
			</g:each>
			<g:render template="commentCrud"/> 
			<g:render template="../assetEntity/newDependency" model="['forWhom':'Server', entities:servers]"></g:render>
		</div>
	</body>
</html>