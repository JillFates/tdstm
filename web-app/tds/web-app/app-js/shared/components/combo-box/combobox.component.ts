/**
 * Supports Server Side Pagination and Server  Side Filter Search
 */

import {Component, EventEmitter, Input, Output, ViewChild, SimpleChanges, OnChanges, ElementRef} from '@angular/core';
import {DomSanitizer, SafeHtml} from '@angular/platform-browser';
import {ComboBoxSearchModel} from './model/combobox-search-param.model';
import {ComboBoxSearchResultModel, RESULT_PER_PAGE} from './model/combobox-search-result.model';
import {ComboBoxComponent} from '@progress/kendo-angular-dropdowns';
import {setTimeout} from 'timers';
import * as R from 'ramda';

declare var jQuery: any;

@Component({
	selector: 'tds-combobox',
	template: `
        <kendo-combobox #innerComboBox
                        [data]="datasource"
                        [(ngModel)]="model"
                        [textField]="'text'"
                        [valueField]="'id'"
                        [placeholder]="placeholder"
                        [filterable]="true"
                        (valueChange)="onValueChange($event)"
                        (selectionChange)="onSelectionChange($event)"
                        (filterChange)="onFilterChange($event)"
                        (open)="onOpen()"
                        (close)="onClose()"
                        (focus)="onFocus()"
                        (blur)="onBlur()"
                        [suggest]="true"
                        [disabled]="disabled"
                        [required]="required"
                        name="testName"
                        style="width: 100%;">
            <ng-template kendoComboBoxItemTemplate let-dataItem>
                <span [innerHTML]="comboBoxInnerSearch(dataItem)"></span>
            </ng-template>
            <ng-template kendoComboBoxFooterTemplate>
                <span #dropdownFooter></span>
            </ng-template>
        </kendo-combobox>
	`,
	styles: []
})

export class TDSComboBoxComponent implements OnChanges {
	// References
	@ViewChild('dropdownFooter') dropdownFooter: ElementRef;
	@ViewChild('innerComboBox') innerComboBox: ComboBoxComponent;
	// Output method handlers
	@Output('valueChange') valueChange: EventEmitter<any> = new EventEmitter();
	@Output('selectionChange') selectionChange: EventEmitter<any> = new EventEmitter();
	@Output('filterChange') filterChange: EventEmitter<any> = new EventEmitter();
	@Output('open') open: EventEmitter<any> = new EventEmitter();
	@Output('close') close: EventEmitter<any> = new EventEmitter();
	@Output('focus') focus: EventEmitter<any> = new EventEmitter();
	@Output('blur') blur: EventEmitter<any> = new EventEmitter();
	// Model
	@Output() modelChange = new EventEmitter<string>();
	@Input('model') model: any;
	@Input('metaParam') metaParam: any;
	// Passing Callback functions
	@Input('serviceRequest') serviceRequest: Function;
	// Params
	@Input('placeholder') placeholder = '';
	@Input('filterable') filterable: boolean;
	@Input('required') required: boolean;
	@Input('disabled') disabled: boolean;
	@Input('searchOnScroll') searchOnScroll = true;
	@Input('reloadOnOpen') reloadOnOpen = false;
	@Input('updateOnChanges') updateOnChanges = false;
	@Input('innerTemplateFormat') innerTemplateFormat: Function;
	// Inner Params
	private datasource: any[] = [{id: '', text: ''}];
	private firstChange = true;
	private comboBoxSearchModel: ComboBoxSearchModel;
	private comboBoxSearchResultModel: ComboBoxSearchResultModel;

	constructor(private sanitized: DomSanitizer) {
	}

	/**
	 * Hook when the new Value is assigned to the ComboBox
	 * @param {SimpleChanges} changes
	 */
	ngOnChanges(changes: SimpleChanges) {
		// To avoid doing extra Rest Call, the initial set in the Combo will be the current value.
		if (changes['model'] && changes['model'].currentValue !== changes['model'].previousValue) {
			this.firstChange = changes['model'].firstChange;
			let model = changes['model'].currentValue;
			if (this.firstChange || this.updateOnChanges) {
				this.addToDataSource(model);
			} else {
				// Found in TM-13710 the unshift operation can displace the component without recreate it from scratch
				// By the inclusion on the ng-repeat operation, this ensure the current value it is always in the datasource as selectable element
				this.addToDataSource(model);
			}
		}
	}

	/**
	 * Add the item to the datasource if this parameter doesn't exists on collection
	 * @param model Item to add
	 */
	addToDataSource(model: any): void {
		if (model.id && !this.datasource.find((item) => item.id === model.id)) {
			this.datasource.push(model);
		}
	}

	/**
	 * If on the value change the value is undefined and the last combobox value is not empty, it was a canceled filter
	 * @param value
	 */
	public onValueChange(value: any): void {
		this.valueChange.emit(value);
		if (!value && this.comboBoxSearchModel && this.comboBoxSearchModel.value && this.comboBoxSearchModel.value !== '') {
			this.comboBoxSearchModel.query = '';
			this.comboBoxSearchModel.currentPage = 1;
			this.getResultSet();
		}
	}

	public onSelectionChange(value: any): void {
		this.selectionChange.emit(value);
	}

	/**
	 * Filter is being executed on Server and Client Side
	 * @param filter
	 */
	public onFilterChange(filter: any): void {
		this.filterChange.emit(filter);
		if (filter !== '') {
			this.initSearchModel();
			this.comboBoxSearchModel.currentPage = 1;
			this.comboBoxSearchModel.query = filter;
			this.getNewResultSet();
		}
	}

	/**
	 * On Open we emit the value if the parents needs to implements something
	 * but we call the resource on the Rest to get the list of values.
	 */
	public onOpen(): void {
		this.open.emit();
		// At open the first time, we need to get the list of items to show based on the selected element
		if (this.reloadOnOpen || this.firstChange || !this.comboBoxSearchModel || this.comboBoxSearchModel.metaParam !== this.metaParam) {
			this.firstChange = false;
			this.datasource = [];
			this.initSearchModel();
			this.getResultSet();
		} else {
			this.calculateLastElementShow();
		}
	}

	/**
	 * The Search model is being separated from the model attached to the comboBox
	 */
	private initSearchModel(): void {
		this.comboBoxSearchModel = {
			query: '',
			currentPage: 1,
			metaParam: this.metaParam,
			value: (this.model) ? this.model.text : '',
			maxPage: 25
		};
	}

	/**
	 * Populate the Datasource with the set
	 */
	private getResultSet(): void {
		this.serviceRequest(this.comboBoxSearchModel).subscribe((res: ComboBoxSearchResultModel) => {
			this.comboBoxSearchResultModel = res;
			// If in the process the MetaParam is not the same, clean the datasource
			const result = (this.comboBoxSearchResultModel.result || []);
			result.forEach((item: any) => this.addToDataSource(item));

			if (this.searchOnScroll && this.comboBoxSearchResultModel.total > RESULT_PER_PAGE) {
				this.calculateLastElementShow();
			}
		});
	}

	/**
	 * Populate the Datasource with a new Complete Set
	 */
	private getNewResultSet(): void {
		this.serviceRequest(this.comboBoxSearchModel).subscribe((res: ComboBoxSearchResultModel) => {
			this.comboBoxSearchResultModel = res;
			this.datasource = this.comboBoxSearchResultModel.result;
			if (this.searchOnScroll && this.comboBoxSearchResultModel.total > RESULT_PER_PAGE) {
				this.calculateLastElementShow();
			}
		});
	}

	public onClose(): void {
		this.close.emit();
	}

	public onFocus(): void {
		this.focus.emit();
	}

	public onBlur(): void {
		this.blur.emit();
	}

	/**
	 * Reset the Value of the selected Element
	 */
	public resetValue(): void {
		this.comboBoxSearchModel.value = '';
	}

	/**
	 * Keep listening if the element show is the last one
	 * @returns {any}
	 */
	private calculateLastElementShow(): any {
		setTimeout(() => {
			if (this.dropdownFooter && this.dropdownFooter.nativeElement) {
				let nativeElement = this.dropdownFooter.nativeElement;
				let scrollContainer = jQuery(nativeElement.parentNode).find('.k-list-scroller');
				jQuery(scrollContainer).off('scroll');
				jQuery(scrollContainer).on('scroll', (element) => {
					this.onLastElementShow(element.target);
				});
			}
		}, 800);
	}

	/**
	 * Calculate the visible height + pixel scrolled = total height
	 * If Result Set Per Page is less than the max total of result found, continue scrolling
	 * @param element
	 */
	private onLastElementShow(element: any): void {
		if (element.offsetHeight + element.scrollTop === element.scrollHeight) {
			if ((RESULT_PER_PAGE * this.comboBoxSearchResultModel.page) <= this.comboBoxSearchResultModel.total) {
				this.comboBoxSearchModel.currentPage++;
				this.getResultSet();
			}
		}
	}

	/**
	 * Search for matching text of current comboBox filter
	 * @param {any} dataItem
	 * @returns {SafeHtml}
	 */
	public comboBoxInnerSearch(dataItem: any): SafeHtml {
		if (!dataItem.text) {
			dataItem.text = '';
		}

		const regex = new RegExp(this.innerComboBox.text, 'i');
		const text =  (this.innerTemplateFormat) ? this.innerTemplateFormat(dataItem) : dataItem.text;

		const transformedText = text.replace(regex, `<b>$&</b>`);

		return this.sanitized.bypassSecurityTrustHtml(transformedText);
	}
}