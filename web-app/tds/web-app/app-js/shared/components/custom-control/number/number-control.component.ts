import {Component, EventEmitter, Input, OnInit, Output, } from '@angular/core';
import {NumberControlHelper} from './number-control.helper';
import {TDSCustomControl} from '../common/custom-control';

@Component({
	selector: 'tds-number-control',
	styles: [``],
	template: `
		<div>
            <kendo-numerictextbox [format]="format"
                                  [(ngModel)]="numberValue"
                                  [min]="realMinRange" [max]="maxRange"
                                  [autoCorrect]="true"
                                  [tabindex]="tabindex"
                                  (ngModelChange)="onValueChange($event)"
                                  class="form-control">
            </kendo-numerictextbox>
		</div>
	`
})
export class NumberControlComponent extends TDSCustomControl implements OnInit {
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
		super();
	}

	/**
	 * On Init build the number format.
	 */
	ngOnInit(): void {
		this.numberValue = this.value ? +this.value : 0;
		// double check
		this.numberValue = Number.isNaN(this.numberValue) ? 0 : this.numberValue;
		this.realMinRange = this.minRange;
	}

	/**
	 * Emit the value changed.
	 * @param $event
	 */
	onValueChange($event: any): void {
		this.numberValue = Math.trunc($event);
		this.valueChange.emit(this.numberValue);
	}
}