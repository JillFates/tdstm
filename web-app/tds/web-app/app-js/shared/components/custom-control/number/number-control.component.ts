import {Component, ElementRef, EventEmitter, Input, OnInit, Output, ViewChild} from '@angular/core';
import {NumberConfigurationConstraintsModel} from '../../../../modules/fieldSettings/components/number/number-configuration-constraints.model';
import {NumberControlHelper} from './number-control.helper';
import { formatNumber } from '@telerik/kendo-intl';

@Component({
	selector: 'tds-number-control',
	template: `
		<div>
            <kendo-numerictextbox [format]="format"
								  [(ngModel)]="numberValue"
                                  [min]="minRange" [max]="maxRange"
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
	// @Input('constraints') constraints: any;
	@Input('format') format: string;
	@Input('decimalPlaces') decimalPlaces: number;
	@Input('maxRange') maxRange: number;
	@Input('minRange') minRange: number;
	@Input('required') required: boolean;
	@Input('allowNegatives') allowNegatives: boolean;
	@Input('useThousandSeparator') useThousandSeparator: boolean;
	protected numberValue: number;
	protected constraints: NumberConfigurationConstraintsModel;
	// protected outputFormat: string;

	constructor() {
		// this.outputFormat = '';
		this.constraints = new NumberConfigurationConstraintsModel();
	}

	/**
	 * On Init build the number format.
	 */
	ngOnInit(): void {
		this.numberValue = +this.value;
		this.constraints = NumberControlHelper.buildConfiguration(this.minRange, this.maxRange, this.decimalPlaces, this.format, this.useThousandSeparator, this.allowNegatives, this.required);
		// this.outputFormat = NumberControlHelper.buildFormat(this.constraints);
	}

	/**
	 * Emit the value changed.
	 * @param $event
	 */
	onValueChange($event: any): void {
		this.valueChange.emit(this.numberValue);
	}
}