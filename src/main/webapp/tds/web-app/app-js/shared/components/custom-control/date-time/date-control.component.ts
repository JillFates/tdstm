import {
	Component,
	forwardRef,
	OnInit,
	Input,
	Output,
	EventEmitter,
	OnChanges,
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

@Component({
	selector: 'tds-date-control',
	template: `
		<kendo-datepicker
			[title]="title"
			[min]="minimum"
			[max]="maximum"
			[value]="dateValue"
			(blur)="onTouched()"
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
/**
 * input: yyyy-MM-dd
 * output: yyyy-MM-dd (value string to be stored as final value)
 */
export class TDSDateControlComponent extends TDSCustomControl implements OnInit, OnChanges  {
	@Output() valueChange: EventEmitter<any> = new EventEmitter();
	@Input('minimum') minimum;
	@Input('maximum') maximum;
	public displayFormat: string;
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
		this.valueChange.emit(value);
	}

	ngOnChanges(inputs: SimpleChanges) {
		const dateConstraints = {
			required: this.required
		};
		if (inputs['_value']) {
			if (!inputs['_value'].currentValue) {
				this.updateDateValue();
			}
			this.setupValidatorFunction(CUSTOM_FIELD_TYPES.Date, dateConstraints);
		}
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