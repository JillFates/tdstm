tds.cookbook.directive.TmAssetTagSelectorDirective = function ($http, utils) {
	return {
		template: `<div class="asset-tag-selector-component">
						<!--<input type="checkbox" class="asset-tag-selector-operator-switch" aria-label="Operator" checked="checked" /> -->
						<select id="asset-tag-selector" class="asset-tag-selector"></select>
						
						<script id="asset-tag-selector-item" type="text/x-kendo-template">
							<div class="asset-tag-selector-single-item #:(data.strike !== undefined)? 'hidden-tag-item': ''#">
								<div class="asset-tag-selector-single-item  #:data.css#">
									<i class="fa fa-fw fa-check"></i> #:data.name#
								</div>
							</div>
						</script>
						
						<script id="asset-tag-selector-tag" type="text/x-kendo-template">
							<div class="asset-tag-text #:data.css# #:(data.strike !== undefined)? 'striked-tag-item': ''#"">#:data.name#</div>
						</script>
					</div>`,
		restrict: 'E',
		scope: {
			assetSelector: '=',
			preAssetSelector: '=',
			onChange: '&'
		},
		controller: function ($scope) {

			// Init the Load
			getAssetTags();

			$scope.assetSelector = {
				tag: [],
				operator: ($(".asset-tag-selector-operator-switch").attr('checked')) ? 'AND' : 'OR'
			};

			$(".asset-tag-selector-operator-switch").kendoMobileSwitch({
				onLabel: "AND",
				offLabel: "OR",
				change: function (e) {
					$scope.assetSelector.operator = ($(".asset-tag-selector-operator-switch").attr('checked')) ? 'AND' : 'OR';
					$scope.onChange();
				}
			});

			$(".asset-tag-selector").kendoMultiSelect({
				dataTextField: "name",
				dataValueField: "id",
				filter: "startswith",
				itemTemplate: $("#asset-tag-selector-item").html(),
				tagTemplate: $("#asset-tag-selector-tag").html(),
				change: selectTags,
				open: selectTags,
			});

			/**
			 * Set the new Set Datasource to the element
			 * @param data
			 */
			function setNewDataSource(data) {
				var widget = $("#asset-tag-selector").getKendoMultiSelect();
				widget.setDataSource(new kendo.data.DataSource({
					data: data
				}));
				widget.dataSource.sync();
				widget.refresh();
				setTimeout(preSelectTags, 600);
			}

			/**
			 * Pre Select Assets
			 */
			function preSelectTags() {
				if($scope.preAssetSelector && $scope.preAssetSelector.tag) {
					var selectedTags = [];
					var widget = $("#asset-tag-selector").getKendoMultiSelect();
					var dataSource = widget.dataSource;

					for(var i=0; i < $scope.preAssetSelector.tag.length; i++) {

						var elementExist = dataSource.data().filter((e) => {
							return e.id === $scope.preAssetSelector.tag[i].id
						});
						if (elementExist.length === 0) {
							dataSource.add({
								name: $scope.preAssetSelector.tag[i].label,
								css: $scope.preAssetSelector.tag[i].css,
								id: $scope.preAssetSelector.tag[i].id,
								strike: true
							});
						}

						selectedTags.push($scope.preAssetSelector.tag[i].id);

					}
					dataSource.sync();

					// Always false on Cookbook context
					$scope.assetSelector.operator = false;

					if(selectedTags !== '') {
						$("#asset-tag-selector").data("kendoMultiSelect").value(selectedTags);
						selectTags();
					}
				}
			}

			function getAssetTags() {
				// At least for Cookbook it will alw
				$http.get(utils.url.applyRootPath('/ws/tag')).success(function (data, status, headers, config) {
					if (data && data.status === 'success') {
						setNewDataSource(data.data);
					}
				}).error(function (data, status, headers, config) {});
			}

			function selectTags(e) {
				//
				$("#asset-tag-selector_listbox > li").find('.hidden-tag-item').parent().addClass("hidden-tag-item");
				// There is no way to know the list of element, this is created on fly outside the boundaries of the directive
				$("#asset-tag-selector_listbox").find("li").removeClass("asset-tag-selector-item-selected");
				$("#asset-tag-selector_listbox").find("li.k-state-selected").addClass("asset-tag-selector-item-selected");

				$scope.assetSelector.tag = $("#asset-tag-selector").data("kendoMultiSelect").dataItems();

				$scope.onChange();
			}
		}
	};
}

tds.cookbook.module.directive('tmAssetTagSelector', ['$http', 'utils', tds.cookbook.directive.TmAssetTagSelectorDirective]);