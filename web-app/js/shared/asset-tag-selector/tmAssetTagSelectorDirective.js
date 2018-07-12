tds.cookbook.directive.TmAssetTagSelectorDirective = function ($http, utils) {
	return {
		template: `<div class="asset-tag-selector-component">
						<input type="checkbox" class="asset-tag-selector-operator-switch" aria-label="Operator" checked="checked" />
						<select id="asset-tag-selector" class="asset-tag-selector"></select>
						
						<script id="asset-tag-selector-item" type="text/x-kendo-template">
							<div class="asset-tag-selector-single-item">
								<div class="asset-tag-selector-single-item  #:data.css#">
									<i class="fa fa-fw fa-check"></i> #:data.name#
								</div>
							</div>
						</script>
						
						<script id="asset-tag-selector-tag" type="text/x-kendo-template">
							<div class="#:data.css#">#:data.name#</div>
						</script>
					</div>`,
		restrict: 'E',
		scope: {
			assetSelector: '=',
			preAssetSelector: '=',
			selectedEvent: '=',
			selectedBundle: '=',
			onChange: '&'
		},
		controller: function ($scope) {

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

			$scope.$watch('selectedEvent', function (nVal, oVal) {
				setNewDataSource([]);
				getAssetTags();
			}, true);

			$scope.$watch('selectedBundle', function (nVal, oVal) {
				setNewDataSource([]);
				getAssetTags();
			}, true);


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
					for(var i=0; i < $scope.preAssetSelector.tag.length; i++) {
						selectedTags.push($scope.preAssetSelector.tag[i].id);
					}

					var operator = ($(".asset-tag-selector-operator-switch").attr('checked')? true: false);
					if(operator !== $scope.preAssetSelector.and) {
						$(".asset-tag-selector-operator-switch").data('kendoMobileSwitch').toggle();
						$scope.assetSelector.operator = ($(".asset-tag-selector-operator-switch").attr('checked')) ? 'AND' : 'OR';
					}

					if(selectedTags !== '') {
						$("#asset-tag-selector").data("kendoMultiSelect").value(selectedTags);
						selectTags();
					}
				}
			}

			function getAssetTags() {
				if (($scope.selectedEvent && $scope.selectedEvent.id) || ($scope.selectedBundle && $scope.selectedBundle.id)) {
					if ($scope.selectedBundle && $scope.selectedBundle.id !== '') {
						$http.get(utils.url.applyRootPath('/ws/tag?moveBundleId=' + $scope.selectedBundle.id)).success(function (data, status, headers, config) {
							if (data && data.status === 'success') {
								setNewDataSource(data.data);
							}
						}).error(function (data, status, headers, config) {});
					} else {
						$http.get(utils.url.applyRootPath('/ws/tag?moveEventId=' + $scope.selectedEvent.id)).success(function (data, status, headers, config) {
							if (data && data.status === 'success') {
								setNewDataSource(data.data);
							}
						}).error(function (data, status, headers, config) {});
					}
				} else if(($scope.selectedEvent === null || $scope.selectedEvent === "") && ($scope.selectedBundle === null || $scope.selectedBundle === "")) {
					$http.get(utils.url.applyRootPath('/ws/tag')).success(function (data, status, headers, config) {
						if (data && data.status === 'success') {
							setNewDataSource(data.data);
						}
					}).error(function (data, status, headers, config) {});
				}
			}

			function selectTags(e) {
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