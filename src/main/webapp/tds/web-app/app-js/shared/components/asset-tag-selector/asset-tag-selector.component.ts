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
import { MultiSelectComponent, PreventableEvent } from '@progress/kendo-angular-dropdowns';

import { from } from 'rxjs';
import { switchMap, map } from 'rxjs/operators';

declare var jQuery: any;

@Component({
	selector: 'tds-asset-tag-selector',
	template: `
		<div class="{{classComponent}}">
		    <kendo-switch *ngIf="showSwitch && switchVisible"
		            class="asset-tag-selector-switch"
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
					placeholder="{{placeholder}}"
					[filterable]="true"
					[tabindex]="440"
		            (open)="onOpen()">
		        <ng-template kendoMultiSelectTagTemplate let-dataItem>
		            <div class="{{dataItem.css}}" [title]="dataItem.name">{{ dataItem.name }}</div>
		        </ng-template>
		        <ng-template kendoMultiSelectItemTemplate let-dataItem>
		            <div class="asset-tag-selector-single-container">
						<div class="asset-tag-selector-single-item tag-kendo-selector {{dataItem.css}}">
							<i class="fa fa-fw fa-check"></i> {{ dataItem.name }}
						</div>
		            </div>
		        </ng-template>
		    </kendo-multiselect>
		    <span class="component-action-open" (click)="openTagSelector()"></span>
		</div>
		<tds-button
			*ngIf="showClearButton && assetSelectorModel.tags && assetSelectorModel.tags.length"
			class="clear-button"
			(click)="clearTags()"
			[title]="'Clear Filter'"
			icon="times-circle"
			[small]="true"
			[flat]="true">
		</tds-button>
	`,
	styles: []
})

export class AssetTagSelectorComponent implements OnChanges, OnInit {
	@ViewChild('assetTagSelectorComponent', {static: false}) assetTagSelectorComponent: MultiSelectComponent;
	@Input() popupClass = '';
	@Input('tagList') sourceTagList: Array<TagModel>;
	// Used to control if the Switch is require for the UI
	@Input('showSwitch') showSwitch = true;
	// Output method handlers
	@Output('valueChange') valueChange: EventEmitter<any> = new EventEmitter();
	// Model
	@Input('model') model: any;
	// Model coming from the views filters.
	@Input('viewFilterModel') viewFilterModel: string;
	// Optional Place holder
	@Input('placeholder') placeholder = '';
	@Input('showClearButton') showClearButton: boolean;

	@Input('class') classList;

	// Use to control if the Switch becomes visible
	public switchVisible = false;
	public tagList: Array<TagModel> = [];
	public assetSelectorModel = {
		switch: false,
		tags: []
	};
	public classComponent = '';

	ngOnInit(): void {
		this.tagList = this.sourceTagList.slice();
		this.classComponent = `asset-tag-selector-component ${this.classList || ''}`;
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
	 * Filter tags as user types a search criteria to narrow the number of elements to render
	 */
	ngAfterViewInit() {
		this.assetTagSelectorComponent.filterChange.asObservable()
		.pipe(
			switchMap((searchText: string) => from([this.sourceTagList])
				.pipe(
					map((data: Array<TagModel>) => {
						return data.filter((tag: TagModel) => {
								return tag.name.toLowerCase().indexOf(searchText.toLowerCase()) !== -1
						}); }
					)
				)
			)
		)
		.subscribe(filteredResults => this.tagList = filteredResults);
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
			jQuery('.asset-tag-selector-single-container').parent().closest('kendo-popup').addClass(this.popupClass);
		}, 0);
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
		if (changes['sourceTagList'] && changes['sourceTagList'].currentValue !== changes['sourceTagList'].previousValue) {
			// Do something if the tagList change like clearing the selectedTags or defaulting the switch to false
			this.tagList = changes['sourceTagList'].currentValue.slice();
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

	/**
	 * Clear current tag selection.
	 */
	clearTags(): void {
		this.reset();
		this.onValueChange();
	}

}
