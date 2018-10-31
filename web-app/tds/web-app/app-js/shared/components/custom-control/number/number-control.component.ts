import {Component, ElementRef, EventEmitter, Input, OnInit, Output, ViewChild} from '@angular/core';

@Component({
	selector: 'number-control',
	// templateUrl: '../tds/web-app/app-js/shared/components/field-reference-popup/field-reference-popup.component.html'
	template: `
		<div>
			<!-- [min]="0" [max]="" -->
            <kendo-numerictextbox [format]="format"
								  [(ngModel)]="value"
								  (ngModelChange)="onValueChange($event)"
                                  class="form-control">
			</kendo-numerictextbox>
		</div>
	`
})
export class NumberControlComponent {
	@Input('value') value: any;
	@Output() valueChange = new EventEmitter<any>();
	@Input('format') format: any;
	private readonly DEFAULT_TIMEZONE = 'n';

	constructor() {
		if (!this.format) {
			this.format = this.DEFAULT_TIMEZONE;
		}
	}

	onValueChange($event: any): void {
		this.valueChange.emit(this.value);
	}
}