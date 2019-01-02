<%@page import="net.transitionmanager.security.Permission"%>
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<meta name="layout" content="topNav" />
		<title>Model List</title>
		<asset:stylesheet href="css/jqgrid/ui.jqgrid.css" />
		<asset:stylesheet href="css/ui.datepicker.css" />

		<g:javascript src="model.manufacturer.js" />
		<g:javascript src="entity.crud.js" />
		<g:javascript src="drag_drop.js" />
		<script src="${resource(dir:'js',file:'jquery.form.js')}"></script>
		<jqgrid:resources />
		<g:javascript src="jqgrid-support.js" />
		<script type="text/javascript">
			$(document).ready(function() {
				$("#createModelView").dialog({ autoOpen: false })
				$("#showModelView").dialog({ autoOpen: false })
				$("#showOrMergeId").dialog({ autoOpen: false })
				var listCaption ="Models: \ <tds:hasPermission permission="${Permission.AssetEdit}"> \
					<span class='capBtn'><input type='button' value='Create Model' onclick='createModelManuDetails(\"model\",\"Model\")'/></span> \
					<span class='capBtn'><input type='button' id='compareMergeId' value='Compare/Merge' onclick='compareOrMerge()' disabled='disabled'/></span>\
					<span class='capBtn'><input type='button' id='deleteModelId' value='Bulk Delete' onclick='deleteModels()' disabled='disabled'/></span> </tds:hasPermission>"
				<jqgrid:grid id="modelId" url="'${createLink(action: 'listJson')}'"
					colNames="'Model Name','Manufacturer', '${columnLabelpref['1']}','${columnLabelpref['2']}', '${columnLabelpref['3']}','${columnLabelpref['4']}','Assets ','Version','Source TDS','Model Status'"
					colModel="{name:'modelName', index: 'modelName', width:'150',formatter: myLinkFormatter},
						{name:'manufacturer', width:'100'},
						{name:'${modelPref['1']}',width:'100'},
						{name:'${modelPref['2']}', width:'100'},
						{name:'${modelPref['3']}',width:'100'},
						{name:'${modelPref['4']}',width:'100'},
						{name:'assetsCount',width:'50'},
						{name:'sourceTDSVersion',width:'50'},
						{name:'sourceTDS',width:'60'},
						{name:'modelStatus',width:'60'}"
					sortname="'modelName'"
					caption="listCaption"
					multiselect="true"
					showPager="true"
					rowList="${ raw(com.tdsops.common.ui.Pagination.optionsAsText()) }"
					loadComplete="initModelCheck"
					gridComplete="function(){bindResize('modelId')}"
					onSelectRow="validateModelCount">
					<jqgrid:navigation id="modelId" add="false" edit="false" del="false" search="false"/>
					<jqgrid:refreshButton id="modelId" />
				</jqgrid:grid>
				TDS.jqGridFilterToolbar('modelId');

				<g:each var="key" in="['1','2','3','4']">
					var modelPref= '${modelPref[key]}';
				$("#modelIdGrid_"+modelPref).append('<img src=\'/tdstm/assets/images/select2Arrow.png\' class="selectImage customizeSelect editSelectimage_'+${key}+'" onclick="showSelect(\''+modelPref+'\',\'model\',\''+${key}+'\')">');
				</g:each>

				$.jgrid.formatter.integer.thousandsSeparator='';
				function myLinkFormatter (cellvalue, options, rowObjcet) {
					var value = cellvalue ? cellvalue : ''
					return '<a href="javascript:showOrEditModelManuDetails(\'model\','+options.rowId+',\'Model\',\'show\',\'Show\')">'+value+'</a>'
				}
				function validateModelCount() {
					var checkedLen = $('.cbox:checkbox:checked').length
					if(checkedLen > 1 && checkedLen < 5) {
						$("#compareMergeId").removeAttr("disabled")
					} else {
						$("#compareMergeId").attr("disabled","disabled")
					}
					if(checkedLen > 0) {
						$("#deleteModelId").removeAttr("disabled")
					} else {
						$("#deleteModelId").attr("disabled","disabled")
					}
				}
				function initModelCheck() {
					$('.cbox').change(validateModelCount)
				}
			});
		</script>
		<style>
		/*TODO: REMOVE ON COMPLETE MIGRATION */
		div.content-wrapper {
			background-color: #ecf0f5 !important;
		}
		</style>
	</head>
	<body>
		<tds:subHeader title="Model List" crumbs="['Admin','Models']"/>
		<div class="body fluid">
			<g:if test="${flash.message}">
				<div id="messageDivId" class="message" >${flash.message}</div>
			</g:if>
			<g:hasErrors bean="${modelInstance}">
				<div id="messageDivId" class="message">
					<g:renderErrors bean="${modelInstance}"/>
				</div>
			</g:hasErrors>
			<div >
				<div id="messageId" class="message" style="display:none">
				</div>
			</div>

			<jqgrid:wrapper id="modelId" />

			<g:each var="key" in="['1','2','3','4']">
				<div id="columnCustomDiv_${modelPref[key]}" style="display:none;">
					<div class="columnDiv_${key} customScroll customizeDiv">
						<input type="hidden" id="previousValue_${key}" value="${modelPref[key]}" />
						<g:each var="attribute" in="${attributesList}">
							<label><input type="radio" name="coloumnSelector_${modelPref[key]}" id="coloumnSelector_${modelPref[key]}" value="${attribute}"
								${modelPref[key]== attribute? 'checked' :'' } style="margin-left:11px;"
								onchange="setColumnAssetPref(this.value,'${key}','${com.tdsops.tm.enums.domain.UserPreferenceEnum.Model_Columns}')"
								/>
								${attribute}
							</label>
							<br>
						</g:each>
					</div>
				</div>
			</g:each>
			<span id="spinnerId" style="display: none">Merging ...<asset:image src="images/spinner.gif" /></span>
			<div id="createModelView" style="display: none;" ></div>
			<div id="showModelView" style="display: none;"></div>
			<div id="showOrMergeId" style="display: none;" title="Compare/Merge Models"></div>
		</div>

		<div id="fixedTest" style="position:fixed"></div>
		<script>
			$('.menu-list-models').addClass('active');
			$('.menu-parent-admin').addClass('active');
		</script>
	</body>
</html>
