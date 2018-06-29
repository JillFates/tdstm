tds.cookbook.directive.TmAssetTagSelectorDirective = function ($http, utils) {
	return {
		template: `<div class="asset-tag-selector-component">
						<input type="checkbox" class="asset-tag-selector-operator-switch" aria-label="Operator" checked="checked" />
						<select class="asset-tag-selector"></select>
						
						<script id="asset-tag-selector-item" type="text/x-kendo-template">
							<div class="asset-tag-selector-single-item">
								<div class="asset-tag-selector-single-item  #:data.css#">
									<i class="fa fa-fw fa-check"></i> #:data.Name#
								</div>
							</div>
						</script>
						
						<script id="asset-tag-selector-tag" type="text/x-kendo-template">
							<div class="#:data.css#">#:data.Name#</div>
						</script>
					</div>`,
		restrict: 'E',
		controller: function ($scope) {
			$(".asset-tag-selector-operator-switch").kendoMobileSwitch({
				onLabel: "AND",
				offLabel: "OR"
			});
			$(".asset-tag-selector").kendoMultiSelect({
				itemTemplate: $("#asset-tag-selector-item").html(),
				tagTemplate: $("#asset-tag-selector-tag").html(),
				dataSource: {
					transport: {
						read: (e) => {
							$http.get(utils.url.applyRootPath('/ws/tag')).success(function (data, status, headers, config) {
								return e.success(data);
							}). error(function (data, status, headers, config) {
								return e.success([]);
							});
						}
					}
				},
				select: function (e) {
					if($(e.item).hasClass("asset-tag-selector-item-selected")) {
						$(e.item).removeClass("asset-tag-selector-item-selected");
					} else {
						$(e.item).addClass("asset-tag-selector-item-selected");
					}
				}
			});
		}
	};
}

tds.cookbook.module.directive('tmAssetTagSelector', ['$http', 'utils', tds.cookbook.directive.TmAssetTagSelectorDirective]);