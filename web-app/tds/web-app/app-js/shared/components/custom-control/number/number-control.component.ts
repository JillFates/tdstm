import {Component, EventEmitter, Input, OnInit, Output, } from '@angular/core';
import {NumberControlHelper} from './number-control.helper';

@Component({
	selector: 'tds-number-control',
	template: `
		<div>
            <kendo-numerictextbox [format]="format"
								  [(ngModel)]="numberValue"
                                  [min]="realMinRange" [max]="maxRange"
                                  [autoCorrect]="true"
								  (ngModelChange)="onValueChange($event)"
                                  class="form-control">
			</kendo-numerictextbox>
		</div>
	`
})
export class NumberControlComponent implements OnInit {
	@Input('value') value: any;
	@Output() valueChange = new EventEmitter<any>();
	@Input('format') format = NumberControlHelper.DEFAULT_NUMBER_FORMAT;
	@Input('precision') precision: number;
	@Input('maxRange') maxRange: number;
	@Input('minRange') minRange: number;
	@Input('required') required: boolean;
	@Input('allowNegative') allowNegative: boolean;
	@Input('separator') separator: boolean;
	protected numberValue: number;
	protected realMinRange: number;

	constructor() {
		// Silence is golden.
	}

	/**
	 * On Init build the number format.
	 */
	ngOnInit(): void {
		this.numberValue = this.value ? +this.value : null;
		if (this.allowNegative) {
			this.realMinRange = this.maxRange * -1;
		} else {
			this.realMinRange = this.minRange;
		}
	}

	/**
	 * Emit the value changed.
	 * @param $event
	 */
	onValueChange($event: any): void {
		this.valueChange.emit(this.numberValue);
	}
}