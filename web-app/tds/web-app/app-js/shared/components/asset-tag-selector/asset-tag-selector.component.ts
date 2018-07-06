/**
 * Supports Server Side Pagination and Server  Side Filter Search
 */

import {Component, EventEmitter, Input, Output, ViewChild, ElementRef} from '@angular/core';

declare var jQuery: any;

@Component({
	selector: 'tds-asset-tag-selector',
	templateUrl: '../tds/web-app/app-js/shared/components/asset-tag-selector/asset-tag-selector.component.html',
	styles: []
})

export class AssetTagSelectorComponent {
	@Input('tagList') tagList: any;

	/**
	 * Catch when the dropdown is opened
	 * it works to attach classes to list if neccsary
	 */
	public onOpen(): void {
		setTimeout(() => {
			// Iterate over the global dropdown to apply specific classes for this component only
			jQuery('.asset-tag-selector-single-container').parent().parent().find('li').removeClass('asset-tag-selector-item-selected');
			jQuery('.asset-tag-selector-single-container').parent().parent().find('.k-state-selected').addClass('asset-tag-selector-item-selected');
		}, 0);

		setTimeout(() => {
			console.log();
		}, 1000)
	}
}