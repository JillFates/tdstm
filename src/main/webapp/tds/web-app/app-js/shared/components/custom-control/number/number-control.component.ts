import {
	Component,
	forwardRef,
	Input,
	OnChanges,
	SimpleChanges
} from '@angular/core';
import {
	NG_VALUE_ACCESSOR,
	NG_VALIDATORS
} from '@angular/forms';
import {isNil} from 'ramda';

import {CUSTOM_FIELD_TYPES} from '../../../model/constants';
import {NumberControlHelper} from './number-control.helper';
import {TDSCustomControl} from '../common/custom-control.component';
import {ValidationRulesFactoryService} from '../../../services/validation-rules-factory.service';

@Component({
	selector: 'tds-number-control',
	styles: [``],
	template: `
		<kendo-numerictextbox
			[autoCorrect]="autoCorrect"
			class="form-control"
			[decimals]="precision"
			[format]="numberFormat"
			(blur)="onTouched()"
			[max]="max"
			[min]="min"
			[tabindex]="tabindex"
			[value]="numberValue"
			(valueChange)="onValueChange($event)">
		</kendo-numerictextbox>
	`,
	providers: [
		{
			provide: NG_VALUE_ACCESSOR,
			useExisting: forwardRef(() => TDSNumberControlComponent),
			multi: true
		},
		{
			provide: NG_VALIDATORS,
			useExisting: forwardRef(() => TDSNumberControlComponent),
			multi: true
		}
	]
})
export class TDSNumberControlComponent extends TDSCustomControl implements OnChanges {
	@Input('format') format = NumberControlHelper.DEFAULT_NUMBER_FORMAT;
	@Input('precision') precision = 0;
	@Input('max') max: number;
	@Input('min') min: number;
	@Input('allowNegative') allowNegative: boolean;
	@Input('separator') separator: boolean;
	@Input('autoCorrect') autoCorrect = false;

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
		this.onTouched();
	}

	// Kendo doesn't allow to use a string for the kendo number control,
	// so we need to wrap up the value function doing a cast to number
	get numberValue() {
		// if the value is null or undefined leave it as it is
		// just if it has valid value do the conversion
		return isNil(this.value)  ? this.value :  Number(this.value);
	}

	// Kendo format used to define the number of decimal positions
	get numberFormat() {
		return this.precision ? `n${this.precision}`  : 'n'
	}

}