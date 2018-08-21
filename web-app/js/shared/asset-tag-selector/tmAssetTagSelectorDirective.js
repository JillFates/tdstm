var currentAngularModule = tds.cookbook || tds.comments;
currentAngularModule.directive.TmAssetTagSelectorDirective = function ($http, utils) {
	return {
		template: `<div class="asset-tag-selector-component">
						<!-- <input type="hidden" class="tag-hidden-val" name="tagIds" /> -->
						<!-- <input type="hidden" class="match-hidden-val" name="tagMatch" /> -->
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
			preSelectedOperator: '=',
			disabledOperator: '=',
			hideOperator: '=',
			onChange: '&',
		},
		link: function ($scope, element) {
			// Init the Load
			// Get All Tags
			getAssetTags();

			createSwitchButton();

			$scope.assetSelector = {
				tag: [],
				operator: ($(element).find(".asset-tag-selector-operator-switch").attr('checked')) ? 'ALL' : 'ANY'
			};

			$(element).find(".asset-tag-selector").kendoMultiSelect({
				dataTextField: "name",
				dataValueField: "id",
				filter: "startswith",
				itemTemplate: $(element).find("#asset-tag-selector-item").html(),
				tagTemplate: $(element).find("#asset-tag-selector-tag").html(),
				change: function() {
					selectTags();
					$scope.onChange();
				},
				open: selectTags,
			});

			($scope.assetSelector.tag.length > 1)? $(element).find('.km-switch').show(): $(element).find('.km-switch').hide();

			/**
			 * Regenerate the Switch, Kendo does not allow to change the value on fly
			 */
			function createSwitchButton() {
				if (!$scope.hideOperator) {
					$(element).find(".asset-tag-selector-operator-switch").remove();
					$(element).find(".km-switch").remove();
					$(element).find(".asset-tag-selector-component").append('<input type="checkbox" class="asset-tag-selector-operator-switch" aria-label="Operator"/>');

					if ($scope.preSelectedOperator && $scope.preSelectedOperator !== '') {
						$(element).find(".asset-tag-selector-operator-switch").attr('checked', ($scope.preSelectedOperator === 'ALL') ? true : false);
					}

					if ($scope.disabledOperator) {
						$(element).find('.asset-tag-selector-operator-switch').attr("disabled", true);
					}

					$(element).find(".asset-tag-selector-operator-switch").kendoMobileSwitch({
						onLabel: "ALL",
						offLabel: "ANY",
						change: function (e) {
							$scope.assetSelector.operator = ($(element).find(".asset-tag-selector-operator-switch").attr('checked')) ? 'ALL' : 'ANY';
							// $(element).find(".match-hidden-val").val(($(element).find(".asset-tag-selector-operator-switch").attr('checked')) ? 'ALL' : 'ANY');
							$scope.onChange();
						}
					});
				}
			}

			/**
			 * Set the new Set Datasource to the element
			 * @param data
			 */
			function setNewDataSource(data) {
				var widget = $(element).find("#asset-tag-selector").getKendoMultiSelect();
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
					var widget = $(element).find("#asset-tag-selector").getKendoMultiSelect();
					var dataSource = widget.dataSource;

					for(var i=0; i < $scope.preAssetSelector.tag.length; i++) {

						if($scope.preAssetSelector.tag[i] && $scope.preAssetSelector.tag[i] !== null) {
							var elementExist = dataSource.data().filter((e) => {
								return e.id === $scope.preAssetSelector.tag[i].id || e.id === $scope.preAssetSelector.tag[i];
							});
							if (elementExist.length === 0) {
								dataSource.add({
									name: $scope.preAssetSelector.tag[i].label,
									css: $scope.preAssetSelector.tag[i].css,
									id: $scope.preAssetSelector.tag[i].id,
									strike: true
								});
							}

							selectedTags.push($scope.preAssetSelector.tag[i].id || $scope.preAssetSelector.tag[i]);
						}

					}
					dataSource.sync();

					// Always false on Cookbook context
					$scope.assetSelector.operator = false;

					if(selectedTags !== '') {
						$(element).find("#asset-tag-selector").data("kendoMultiSelect").value(selectedTags);
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

			var getTagsIds = function(tags) {
				var tagIds = [];
				tags.each( function(a){
					tagIds.push(parseInt(a.id));
				})
				return tagIds;
			};

			function selectTags(e) {
				//
				$(element).find("#asset-tag-selector_listbox > li").find('.hidden-tag-item').parent().addClass("hidden-tag-item");
				// There is no way to know the list of element, this is created on fly outside the boundaries of the directive
				$(element).find("#asset-tag-selector_listbox").find("li").removeClass("asset-tag-selector-item-selected");
				$(element).find("#asset-tag-selector_listbox").find("li.k-state-selected").addClass("asset-tag-selector-item-selected");

				$scope.assetSelector.tag = $(element).find("#asset-tag-selector").data("kendoMultiSelect").dataItems();

				if($scope.assetSelector.tag.length > 1) {
					$(element).find('.km-switch').show();
				} else {
					createSwitchButton();
					$scope.assetSelector.operator = ($(element).find(".asset-tag-selector-operator-switch").attr('checked')) ? 'ALL' : 'ANY';
					// Hide new element
					$(element).find('.km-switch').hide();
				}

				/*
				Not being used, but leave it here since it can help for legacy pages
				$(element).find(".tag-hidden-val").remove();
				$scope.assetSelector.tag.each(function (a) {
					$(element).append('<input type="hidden" class="tag-hidden-val" name="tagIds" value="' + parseInt(a.id) + '" />');
				});
				$(element).find(".match-hidden-val").val(($(element).find(".asset-tag-selector-operator-switch").attr('checked')) ? 'ALL' : 'ANY');*/
			}
		}
	};
}

currentAngularModule.module.directive('tmAssetTagSelector', ['$http', 'utils', currentAngularModule.directive.TmAssetTagSelectorDirective]);