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
	template: `
		<div class="asset-tag-selector-component">
		    <kendo-switch *ngIf="showSwitch && switchVisible"
		            class="asset-tag"
		            [(ngModel)]="assetSelectorModel.switch"
		            [onLabel]="'ALL'"
		            [offLabel]="'ANY'"
		            (valueChange)="onSwitchValueChange($event)">
		    </kendo-switch>
		    <kendo-multiselect
		            id="asset-tag-selector-component"
		            #assetTagSelectorComponent
		            [data]="tagList"
		            [(ngModel)]="assetSelectorModel.tags"
		            [textField]="'name'"
		            [valueField]="'id'"
		            (valueChange)="onTagValueChange($event)"
		            (open)="onOpen()">
		        <ng-template kendoMultiSelectTagTemplate let-dataItem>
		            <div class="{{dataItem.css}}" [title]="dataItem.name">{{ dataItem.name }}</div>
		        </ng-template>
		        <ng-template kendoMultiSelectItemTemplate let-dataItem>
		            <div class="asset-tag-selector-single-container">
		                <div class="asset-tag-selector-single-item  {{dataItem.css}}">
		                    <i class="fa fa-fw fa-check"></i> {{ dataItem.name }}
		                </div>
		            </div>
		        </ng-template>
		    </kendo-multiselect>
		    <span class="component-action-open" (click)="openTagSelector()"></span>
		</div>
	`,
	styles: []
})

export class AssetTagSelectorComponent implements OnChanges, OnInit {
	@ViewChild('assetTagSelectorComponent') assetTagSelectorComponent: MultiSelectComponent;
	@Input('tagList') tagList: Array<TagModel>;
	// Used to control if the Switch is require for the UI
	@Input('showSwitch') showSwitch = true;
	// Output method handlers
	@Output('valueChange') valueChange: EventEmitter<any> = new EventEmitter();
	// Model
	@Input('model') model: any;
	// Model coming from the views filters.
	@Input('viewFilterModel') viewFilterModel: string;

	// Use to control if the Switch becomes visible
	private switchVisible = false;

	private assetSelectorModel = {
		switch: false,
		tags: []
	};

	ngOnInit(): void {
		if (this.model) {
			this.assetSelectorModel.tags = this.model.tags;
			this.assetSelectorModel.switch = this.model.operator === 'ALL' ? true : false;
		} else if (this.viewFilterModel && this.viewFilterModel.length > 0) {
			// Re-draw the element based on the Filter Model
			let operatorType = (this.viewFilterModel.indexOf('|') > 0) ? '|' : '&';
			let ids = this.viewFilterModel.split(operatorType);
			ids.forEach(item => {
				let tag: TagModel = new TagModel();
				tag.id = parseInt(item, 0);
				this.assetSelectorModel.tags.push(tag);
			});
			this.switchVisible = this.assetSelectorModel.tags.length > 1;
			this.assetSelectorModel.switch = operatorType === '&';
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
			// Do something if the View filter Model change
		}
		if (changes['tagList'] && changes['tagList'].currentValue !== changes['tagList'].previousValue) {
			// Do something if the tagList change like clearing the selectedTags or defaulting the switch to false
		}
		this.onShowHideSwitch();
	}

	/**
	 * Process changes made on the tag
	 * @param value
	 */
	public onTagValueChange(value: any): void {
		this.onValueChange();
		this.onShowHideSwitch();
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

	/**
	 * The Switch operator will be shown only if the number of tags is major to 1
	 */
	private onShowHideSwitch(): void {
		this.switchVisible = this.assetSelectorModel.tags.length > 1;
	}

}