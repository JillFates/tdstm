/**
 * Supports Server Side Pagination and Server  Side Filter Search
 */

import {Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
	selector: 'tds-combobox',
	templateUrl: '../tds/web-app/app-js/shared/components/combo-box/combobox.component.html',
	styles: []
})

export class TDSComboBoxComponent {
	@Output('valueChange') valueChange: EventEmitter<any> = new EventEmitter();
	@Output('selectionChange') selectionChange: EventEmitter<any> = new EventEmitter();
	@Output('filterChange') filterChange: EventEmitter<any> = new EventEmitter();
	@Output('open') open: EventEmitter<any> = new EventEmitter();
	@Output('close') close: EventEmitter<any> = new EventEmitter();
	@Output('focus') focus: EventEmitter<any> = new EventEmitter();
	@Output('blur') blur: EventEmitter<any> = new EventEmitter();

	@Input('placeholder') placeholder: string;
	@Input('filterable') filterable: boolean;
	@Input('required') required: boolean;
	@Input('disabled') disabled: boolean;
	@Input('data') data: any[] = [];

	public onValueChange(value: any): void {
		this.valueChange.emit(value);
	}

	public onSelectionChange(value: any): void {
		this.selectionChange.emit(value);
	}

	public onFilterChange(filter: any): void {
		this.filterChange.emit(filter);
	}

	public onOpen(): void {
		this.open.emit();
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