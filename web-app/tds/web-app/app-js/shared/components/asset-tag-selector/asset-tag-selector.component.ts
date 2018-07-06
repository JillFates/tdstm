/**
 * New Asset Tag Selector
 *
 * <tds-asset-tag-selector *ngIf="tagList" [model]="model" [tagList]="tagList" (valueChange)="onTagValueChange($event)"></tds-asset-tag-selector>
 */

import {Component, EventEmitter, Input, Output, SimpleChanges, OnChanges} from '@angular/core';

declare var jQuery: any;

@Component({
	selector: 'tds-asset-tag-selector',
	templateUrl: '../tds/web-app/app-js/shared/components/asset-tag-selector/asset-tag-selector.component.html',
	styles: []
})

export class AssetTagSelectorComponent implements OnChanges {
	@Input('tagList') tagList: any;
	// Output method handlers
	@Output('valueChange') valueChange: EventEmitter<any> = new EventEmitter();
	// Model
	@Input('model') model: any;

	private assetSelectorModel = {
		switch: false,
		tags: []
	};

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

	/**
	 * Hook when new values are assigned to the Multiselect
	 * @param {SimpleChanges} changes
	 */
	ngOnChanges(changes: SimpleChanges) {
		if (changes['model'] && changes['model'].currentValue !== changes['model'].previousValue) {
			// Do something if the model change, like modify the this.assetSelectorModel.tags and the this.assetSelectorModel.switch
			//
		}
		if (changes['tagList'] && changes['tagList'].currentValue !== changes['tagList'].previousValue) {
			// Do something if the tagList change like clearing the selectedTags or defaulting the switch to false
		}
	}

	/**
	 * Process changes made on the tag
	 * @param value
	 */
	public onTagValueChange(value: any): void {
		this.onValueChange();
	}

	/**
	 * Process changes made switch
	 * @param value
	 */
	public onSwitchValueChange(value: any): void {
		this.onValueChange();
	}

	/**
	 * Emit the values to the parent
	 * @param value
	 */
	private onValueChange(): void {
		this.valueChange.emit({
			tags: this.assetSelectorModel.tags,
			operator: (this.assetSelectorModel.switch) ? 'AND' : 'OR'
		});
	}

}