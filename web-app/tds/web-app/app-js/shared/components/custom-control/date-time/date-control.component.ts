import {
	Component,
	forwardRef,
	OnInit,
	OnChanges,
	Input,
	Output,
	EventEmitter,
	SimpleChanges
} from '@angular/core';
import {
	NG_VALUE_ACCESSOR,
	NG_VALIDATORS
} from '@angular/forms';

import {CUSTOM_FIELD_TYPES} from '../../../model/constants';
import {DateUtils} from '../../../utils/date.utils';
import {PreferenceService} from '../../../services/preference.service';
import {TDSCustomControl} from '../common/custom-control.component';
import {ValidationRulesFactoryService} from '../../../services/validation-rules-factory.service';
import {DateValidationConstraints} from '../../../../shared/model/validation-contraintes.model';

@Component({
	selector: 'tds-date-control',
	template: `
		<kendo-datepicker
			[title]="title"
			[min]="minimum"
			[max]="maximum"
			[value]="dateValue"
			(blur)="onBlur()"
			[format]="displayFormat"
			[tabindex]="tabindex"
			(valueChange)="onValueChange($event)"
			class="form-control">
		</kendo-datepicker>
	`,
	providers: [
		{
			provide: NG_VALUE_ACCESSOR,
			useExisting: forwardRef(() => TDSDateControlComponent),
			multi: true
		},
		{
			provide: NG_VALIDATORS,
			useExisting: forwardRef(() => TDSDateControlComponent),
			multi: true
		}
	]
})
export class TDSDateControlComponent extends TDSCustomControl implements OnChanges, OnInit  {
	@Input('minimum') minimum;
	@Input('maximum') maximum;
	@Output() blur: EventEmitter<any> = new EventEmitter();
	protected displayFormat: string;
	public dateValue: Date;

	constructor(
		private userPreferenceService: PreferenceService,
		protected validationRulesFactory: ValidationRulesFactoryService) {
			super(validationRulesFactory);
			this.displayFormat = userPreferenceService.getUserDateFormatForKendo();
	}

	/**
	 * OnInit set a date value.
	*/
	ngOnInit(): void {
		this.updateDateValue();
	}

	/**
	 * On blur emit the current value to the host component
	*/
	onBlur() {
		this.onTouched();
		this.blur.emit(this.getDateValue());
	}

	/**
	 * On value Change on the component, emits the value to the listeners.
	 * @param {value} Date
	 */
	onValueChange(value: Date): void {
		if (value && value !== null) {
			this.value = DateUtils.formatDate(value, DateUtils.SERVER_FORMAT_DATE)
		} else {
			this.value = null;
		}
		this.onTouched();
	}

	ngOnChanges(inputs: SimpleChanges) {
		const dateConstraints: DateValidationConstraints = {
			required: this.required,
			maxDate: this.maximum,
			minDate: this.minimum
		};

		if (inputs['_value']) {
			if (!inputs['_value'].currentValue) {
				this.updateDateValue();
			}
		}

		this.setupValidatorFunction(CUSTOM_FIELD_TYPES.Date, dateConstraints);
	}
	/**
	 * Format the date value to the fomarmat used by the date control
	 */
	getDateValue(): any {
		if (!this.value) {
			return null;
		}

		let localDateFormatted = DateUtils.getDateFromGMT(this.value);
		return  DateUtils.toDateUsingFormat(localDateFormatted, DateUtils.SERVER_FORMAT_DATE);
	}

	/**
	 * Based on the current value set the corresponding formatted date value
	 */
	updateDateValue() {
		if (this.value) {
			let localDateFormatted = DateUtils.getDateFromGMT(this.value);
			this.dateValue = DateUtils.toDateUsingFormat(localDateFormatted, DateUtils.SERVER_FORMAT_DATE);
		}
		this.dateValue = this.value;
	}
}