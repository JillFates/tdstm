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
										animation: false, format:tdsCommon.kendoDateFormat(),
										change: function() {
											var nextDay = moment(this.value()).add(1,'d').toDate();
											grid.dataSource._filter.filters.push({
												field:"startDate",
												operator:"lt",
												value:nextDay
											});
											grid.thead.find('tr th:first').trigger('click');
										} 
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
										animation: false, format:tdsCommon.kendoDateFormat(),
										change: function() {
											var nextDay = moment(this.value()).add(1,'d').toDate();
											grid.dataSource._filter.filters.push({
												field:"completion",
												operator:"lt",
												value:nextDay
											});
											grid.thead.find('tr th:first').trigger('click');
										} 
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
					mode: "row"
				},
				pageable: {
					pageSize: 20
				},
				refresh:function(){
					console.log(this);
				}
			}).data("kendoGrid");
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
