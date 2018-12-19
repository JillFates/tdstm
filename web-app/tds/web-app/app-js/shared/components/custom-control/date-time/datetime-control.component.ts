import {
	Component,
	forwardRef,
	OnInit,
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
	selector: 'tds-datetime-control',
	template: `
		<kendo-datepicker
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
			useExisting: forwardRef(() => DateTimeControlComponent),
			multi: true
		},
		{
			provide: NG_VALIDATORS,
			useExisting: forwardRef(() => DateTimeControlComponent),
			multi: true
		}
	]
})
/**
 * input: yyyy-MM-dd hh:mm:ss
 * output: yyyy-MM-ddThh:mm:ssZ
 */
export class DateTimeControlComponent extends TDSCustomControl implements OnInit, OnChanges {
	protected outputFormat: string;
	protected displayFormat: string;
	protected dateValue: Date;
	private readonly KENDO_DATETIME_DISPLAY_FORMAT = 'yyyy-MM-dd HH:mm:ss';

	constructor(
		private userPreferenceService: PreferenceService,
		protected validationRulesFactory: ValidationRulesFactoryService
	) {
		super(validationRulesFactory);
		this.displayFormat = this.KENDO_DATETIME_DISPLAY_FORMAT;
	}

	/**
	 * OnInit set a date value.
	 */
	ngOnInit(): void {
		let localDateFormatted = DateUtils.convertFromGMT(this.value, this.userPreferenceService.getUserTimeZone());
		this.dateValue = this.value ? DateUtils.toDateUsingFormat(localDateFormatted, DateUtils.SERVER_FORMAT_DATETIME) : null;
	}

	/**
	 * On value Change on the component, emits the value to the listeners.
	 * @param {value} Date
	 */
	onValueChange(value: Date): void {
		if (value && value !== null) {
			this.value = DateUtils.convertToGMT(value, this.userPreferenceService.getUserTimeZone());
		} else {
			this.value = null;
		}
		this.onTouched();
	}

	ngOnChanges(inputs: SimpleChanges) {
		const dateConstraints = {
			required: this.required
		};
		this.setupValidatorFunction(CUSTOM_FIELD_TYPES.DateTime, dateConstraints);
	}
}