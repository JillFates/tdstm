import {Component, forwardRef, Input, OnChanges, SimpleChanges } from '@angular/core';
import {NG_VALUE_ACCESSOR, NG_VALIDATORS} from '@angular/forms';
import {pathOr} from 'ramda';

import {NumberControlHelper} from './number-control.helper';
import {TDSCustomControl} from '../common/custom-control.component';
import {CUSTOM_FIELD_TYPES} from '../../../model/constants';
import {ValidationRulesFactoryService} from '../../../services/validation-rules-factory.service';

@Component({
	selector: 'tds-number-control',
	host: {
		'(blur)': 'onTouched()'
	},
	styles: [``],
	template: `
		<kendo-numerictextbox
			[autoCorrect]="autoCorrect"
			class="form-control"
			[format]="format"
			[max]="max"
			[min]="min"
			[tabindex]="tabindex"
			[value]="value"
			(valueChange)="onValueChange($event)">
		</kendo-numerictextbox>
	`,
	providers: [
		{
			provide: NG_VALUE_ACCESSOR,
			useExisting: forwardRef(() => NumberControlComponent),
			multi: true
		},
		{
			provide: NG_VALIDATORS,
			useExisting: forwardRef(() => NumberControlComponent),
			multi: true
		}
	]
})
export class NumberControlComponent extends TDSCustomControl implements OnChanges {
	@Input('format') format = NumberControlHelper.DEFAULT_NUMBER_FORMAT;
	@Input('precision') precision: number;
	@Input('max') max: number;
	@Input('min') min: number;
	@Input('allowNegative') allowNegative: boolean;
	@Input('separator') separator: boolean;
	@Input('autoCorrect') autoCorrect = true;

	constructor(protected validationRulesFactory: ValidationRulesFactoryService) {
		super(validationRulesFactory);
	}

	ngOnChanges(inputs: SimpleChanges) {
		const numberConstraints = {
			allowNegative: this.allowNegative,
			max: this.max,
			min: this.min,
			required: this.required
		};

		this.setupValidatorFunction(CUSTOM_FIELD_TYPES.Number, numberConstraints);
	}

	onValueChange(value: number): void {
		this.value = value;
	}
}