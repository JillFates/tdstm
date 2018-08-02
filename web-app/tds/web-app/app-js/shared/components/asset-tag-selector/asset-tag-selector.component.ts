/**
 * New Asset Tag Selector
 *
 * <tds-asset-tag-selector *ngIf="tagList" [model]="model" [tagList]="tagList" (valueChange)="onTagValueChange($event)"></tds-asset-tag-selector>
 */

import {
	Component,
	EventEmitter,
	Input,
	Output,
	SimpleChanges,
	OnChanges,
	OnInit,
	ViewChild,
} from '@angular/core';
import {TagModel} from '../../../modules/assetTags/model/tag.model';
import {MultiSelectComponent} from '@progress/kendo-angular-dropdowns';

declare var jQuery: any;

@Component({
	selector: 'tds-asset-tag-selector',
	templateUrl: '../tds/web-app/app-js/shared/components/asset-tag-selector/asset-tag-selector.component.html',
	styles: []
})

export class AssetTagSelectorComponent implements OnChanges, OnInit {
	@ViewChild('assetTagSelectorComponent') assetTagSelectorComponent: MultiSelectComponent;
	@Input('tagList') tagList: Array<TagModel>;
	@Input('showSwitch') showSwitch = true;
	// Output method handlers
	@Output('valueChange') valueChange: EventEmitter<any> = new EventEmitter();
	// Model
	@Input('model') model: any;
	// Model coming from the views filters.
	@Input('viewFilterModel') viewFilterModel: string;

	private assetSelectorModel = {
		switch: false,
		tags: []
	};

	ngOnInit(): void {
		if (this.model) {
			this.assetSelectorModel.tags = this.model.tags;
			this.assetSelectorModel.switch = this.model.operator === 'ALL' ? true : false;
		} else if (this.viewFilterModel && this.viewFilterModel.length > 0) {
			let ids = this.viewFilterModel.split('|');
			ids.forEach( item => {
				let tag: TagModel = new TagModel();
				tag.id = parseInt(item, 0);
				this.assetSelectorModel.tags.push(tag);
			});
		}
	}

	/**
	 * Catch when the dropdown is opened
	 * it works to attach classes to list if necessary
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
		if (changes['model'] && changes['model'].currentValue !== changes['model'].previousValue && !changes['model'].isFirstChange()) {
			// Do something if the model change, like modify the this.assetSelectorModel.tags and the this.assetSelectorModel.switch
		}
		if (changes['viewFilterModel'] && changes['viewFilterModel'].currentValue !== changes['viewFilterModel'].previousValue) {
			let currentSelectedValues = changes['viewFilterModel'].currentValue.split(((!this.assetSelectorModel.switch) ? '|' : '&'));
			this.showSwitch = currentSelectedValues && currentSelectedValues.length > 1;
			console.log(this.showSwitch);
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
	 * Resets the component to be empty.
	 */
	public reset(): void {
		this.assetSelectorModel.tags = [];
		this.assetSelectorModel.switch = false;
	}

	/**
	 * Emit the values to the parent
	 * @param value
	 */
	private onValueChange(): void {
		this.valueChange.emit({
			tags: this.assetSelectorModel.tags,
			operator: (this.assetSelectorModel.switch) ? 'ALL' : 'ANY'
		});
	}

	/**
	 * Helper method to open the Dropdown if required to be outside Angular
	 */
	public openTagSelector(): void {
		this.assetTagSelectorComponent.toggle(true);
	}

}