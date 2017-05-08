<%@page import="net.transitionmanager.security.Permission"%>
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<meta name="layout" content="topNav" />
		<title>Bundle List</title>
		<script src="${resource(dir:'js',file:'jquery.form.js')}"></script>
		<style>
			/*TODO: REMOVE ON COMPLETE MIGRATION */
			div.content-wrapper {
				background-color: #ecf0f5 !important;
			}
		</style>
	</head>
	<body>
		<tds:subHeader title="Bundle List" crumbs="['Planning','Bundles', 'List']"/>
		<section>
			<div>
				<div class="box-body">
					<g:if test="${flash.message}">
						<div id="messageDivId" class="message" >${flash.message}</div>
					</g:if>
					<div >
						<div id="messageId" class="message" style="display:none">
						</div>
					</div>

					<div id="gridBundleList"></div>

				</div>
				<!-- /.box-body -->
			</div>
		</section>
	<script type="text/javascript">
		currentMenuId = "#eventMenu";
		$(".menu-parent-planning-list-bundles").addClass('active');
		$(".menu-parent-planning").addClass('active');
		var currentDtFormat, currentTz;

		/**
		 * Implementing Kendo Grid for Bundle List
		 */
		function loadGridBundleList() {
		 var grid =	$("#gridBundleList").kendoGrid({
				toolbar: kendo.template('<tds:hasPermission permission="${Permission.BundleEdit}"><button type="button" class="btn btn-default action-toolbar-btn" onClick=\"window.location.href=\'#=contextPath#/moveBundle/create\'\"><span class="glyphicon glyphicon-plus" aria-hidden="true"></span> Create</button></tds:hasPermission> <div onclick="loadGridBundleList()" class="action-toolbar-refresh-btn"><span class="glyphicon glyphicon-refresh" aria-hidden="true"></span></div>'),
				dataSource: {
					type: "json",
					transport: {
						read: {
							url: contextPath + '/moveBundle/retrieveBundleList'
						}
					},
					schema: {
						model: {
							fields: {
								bundleId: { type: "number" },
								name: { type: "string"},
								description: { type: "string" },
								planning: { type: "boolean" },
								assetqty: { type: "number" },
								startDate: { type: "date" },
								completion: { type: "date" }
							}
						}
					},
					sort: {
						field: "name",
						dir: "asc"
					}
				},
				columns: [
					{
						field: "bundleId",
						hidden: true,
					},
					{
						field: "name",
						title: "Name",
						template: "<a class='cell-url-element' href='#=contextPath#/moveBundle/show/#=bundleId#'>#=name#</a>"
					},
					{
						field: "description",
						title: "Description"
					},
					{
						field: "planning",
						title: "Planning",
						template: "#if(planning){ #<span class='glyphicon glyphicon-ok' aria-hidden='true'></span># } else { }#"
					},
					{
						field: "assetqty",
						title: "Asset Qty",
						filterable: {
							cell: {
								template: function(args) {
									args.element.kendoNumericTextBox({
										format: "n0",
										decimals: 0
									});
								}
							}
						}
					},
					{
						field: "startDate",
						title: "Start Time",
						template:"#= displayFormatedDate(startDate)#",
						filterable: {
							cell: {
								template: function(args) {
									args.element.kendoDatePicker({ 
										animation: false, format:tdsCommon.kendoDateFormat()
									});
								},
								operator:'gte'
							}
						}
					},
					{
						field: "completion",
						title: "Completion Time",
						template: "#= displayFormatedDate(completion)#",
						filterable: {
							cell: {
								template: function(args) {
									args.element.kendoDatePicker({ 
										animation: false, format:tdsCommon.kendoDateFormat()
									});
								},
								operator:'gte'
							}
						}
					}
				],
                height: 540,
				sortable: true,
				filterable: {
					mode: "row",
					operators: {
						date: {
							gte: "Is after or equal to",
							lte: "Is before or equal to"
						}
					}
				},
				pageable: {
					pageSize: 20
				},
				dataBound: function(e) {
					console.log("dataBound");
				},
				filtering:function(e){
					console.log(e);
				}
			}).data("kendoGrid");

			grid.dataSource.originalFilter = grid.dataSource.filter;
			grid.dataSource.filter = function() {
				var filter = arguments[0];
				if(filter && filter.filters){
					filter.filters.forEach(function(f){
						if(f.field == 'startDate' || f.field == 'completion'){
							if(f.operator == 'lte'){
								f.value.setHours(23);
								f.value.setMinutes(59);
								f.value.setSeconds(59);
							} else {
								f.value.setHours(0);
								f.value.setMinutes(0);
								f.value.setSeconds(0);
							}

						}
					});
				}
				var result = grid.dataSource.originalFilter.apply(this, arguments);
    			return result;
			}

		}

		function displayFormatedDate(date){
			if(date && moment(date).isValid()){
				return moment(date).format(tdsCommon.defaultDateTimeFormat());
			} else {
				return '';
			}
		}
		$(function(){
			loadGridBundleList();
		});
	</script>

	</body>
</html>
