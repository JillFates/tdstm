import {Component, ElementRef, EventEmitter, Input, OnInit, Output, ViewChild} from '@angular/core';

@Component({
	selector: 'date-time-control',
	// templateUrl: '../tds/web-app/app-js/shared/components/field-reference-popup/field-reference-popup.component.html'
	template: `
		<div>
            <kendo-datepicker [(value)]="value"
							  [format]="format"
                              (valueChange)="onValueChange($event)">
			</kendo-datepicker>
		</div>
	`
})
export class DateTimeControlComponent {
	@Input('value') value: any;
	@Output() valueChange = new EventEmitter<any>();
	@Input('format') format: any;
	private readonly DEFAULT_TIMEZONE = 'dd-MMM-yyyy HH:mm:ss a';

	constructor() {
		if (!this.format) {
			this.format = this.DEFAULT_TIMEZONE;
		}
	}

	onValueChange($event: any): void {
		this.valueChange.emit(this.value);
	}
}