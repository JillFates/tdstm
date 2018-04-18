/**
 * Supports Server Side Pagination and Server  Side Filter Search
 */

import {Component, EventEmitter, Input, Output, OnChanges, SimpleChanges} from '@angular/core';
import {ComboBoxSearchModel} from './model/combobox-search-param.model';
import {ComboBoxSearchResultModel} from './model/combobox-search-result.model';

@Component({
	selector: 'tds-combobox',
	templateUrl: '../tds/web-app/app-js/shared/components/combo-box/combobox.component.html',
	styles: []
})

export class TDSComboBoxComponent implements OnChanges {
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
	// Passing Callback functions
	@Input('serviceRequest') serviceRequest: Function;
	// Params
	@Input('placeholder') placeholder = '';
	@Input('filterable') filterable: boolean;
	@Input('required') required: boolean;
	@Input('disabled') disabled: boolean;
	@Input('data') data: any[] = [{id: '', text: ''}];

	private firstChange = true;
	private comboBoxSearchModel: ComboBoxSearchModel;

	/**
	 * Hook when the new Value is assigned to the ComboBox
	 * @param {SimpleChanges} changes
	 */
	ngOnChanges(changes: SimpleChanges) {
		// To avoid doing extra Rest Call, the initial set in the Combo will be the current value.
		if (changes['model'].currentValue !== changes['model'].previousValue) {
			this.firstChange = changes['model'].firstChange;
			if (this.firstChange) {
				let model = changes['model'].currentValue;
				this.data.push({id: model.id, text: model.text});
			}
		}
	}

	public onValueChange(value: any): void {
		this.valueChange.emit(value);
	}

	public onSelectionChange(value: any): void {
		this.selectionChange.emit(value);
	}

	public onFilterChange(filter: any): void {
		this.filterChange.emit(filter);
	}

	/**
	 * On Open we emit the value if the parents needs to implements something
	 * but we call the resource on the Rest to get the list of values.
	 */
	public onOpen(): void {
		this.open.emit();
		// At open the first time, we need to get the list of items to show based on the selected element
		if (this.firstChange) {
			this.firstChange = false;
			this.comboBoxSearchModel  = {
				query: '',
				currentPage: 1,
				metaParam: this.model.metaParam,
				value: this.model.text,
				maxPage: 25
			};
			this.serviceRequest(this.comboBoxSearchModel).subscribe((res: ComboBoxSearchResultModel) => {
				this.data = res.result;
			});
		}
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
}