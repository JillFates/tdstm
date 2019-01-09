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
		<table>
			<tr>
				<td>
                    <kendo-datepicker
                            [(ngModel)]="dateValue"
                            (blur)="onTouched()"
                            [format]="displayFormat"
                            [tabindex]="tabindex"
                            (valueChange)="onDateChange($event)"
                            class="form-control datepicker">
                    </kendo-datepicker>
				</td>
				<td>
					<kendo-timepicker
						[value]="dateValue"
						(valueChange)="onTimeChange($event)">
					</kendo-timepicker>
				</td>
			</tr>
		</table>
	`,
	providers: [
		{
			provide: NG_VALUE_ACCESSOR,
			useExisting: forwardRef(() => TDSDateTimeControlComponent),
			multi: true
		},
		{
			provide: NG_VALIDATORS,
			useExisting: forwardRef(() => TDSDateTimeControlComponent),
			multi: true
		}
	]
})
/**
 * input: yyyy-MM-dd hh:mm:ss
 * output: yyyy-MM-ddThh:mm:ssZ
 */
export class TDSDateTimeControlComponent extends TDSCustomControl implements OnInit, OnChanges {
	protected outputFormat: string;
	protected displayFormat: string;
	protected dateValue: Date;
	protected selectedDate;
	protected selectedTime;
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
		this.selectedDate = this.dateValue ? DateUtils.convertToGMT(this.dateValue, this.userPreferenceService.getUserTimeZone()).substr(0, 10) : null;
		this.selectedTime = this.dateValue ? this.dateValue.toString().substr(16, 8) : null;
	}

	/**
	 * On value Change on the datepicker component, emits the value to the listeners.
	 * @param {value} Date
	 */
	onDateChange(value): void {
		if (value && value !== null) {
			this.selectedDate = DateUtils.convertToGMT(value, this.userPreferenceService.getUserTimeZone()).substr(0, 10); // Get the date from the given value
			if (!this.selectedTime) {
				this.dateValue = value;
				this.value = DateUtils.convertToGMT(value, this.userPreferenceService.getUserTimeZone());
			} else {
				this.dateValue.setFullYear(value.getFullYear());
				this.dateValue.setMonth(value.getMonth());
				this.dateValue.setDate(value.getDate());
				this.setValueDateTime();
			}
		}
		this.onTouched();
	}

	/**
	 * On value Change on the timepicker component, emits the value to the listeners.
	 * @param {value} Time
	 */
	onTimeChange(value): void {
		if (value && value !== null) {
			this.selectedTime =  value.toString().substr(16, 8); // Get the time from the given value
			if (!this.selectedDate) {
				this.dateValue = value;
				this.value = DateUtils.convertToGMT(value, this.userPreferenceService.getUserTimeZone());
			} else {
				let tempDate = new Date(); // Need to create a new date so the datepicker gets updated.
				tempDate.setTime(this.dateValue.getTime());
				tempDate.setHours(value.getHours());
				tempDate.setMinutes(value.getMinutes());
				tempDate.setSeconds(value.getSeconds());
				this.dateValue = tempDate;
				this.setValueDateTime();
			}
		}
		this.onTouched();
	}

	/**
	 * Combines date and time from Kendo pickers into format to be saved.
	 */
	setValueDateTime(): void {
		if (this.selectedDate && this.selectedTime) {
			this.value = this.selectedDate + 'T' + this.selectedTime + 'Z';
		}
	}

	ngOnChanges(inputs: SimpleChanges) {
		const dateConstraints = {
			required: this.required
		};
		this.setupValidatorFunction(CUSTOM_FIELD_TYPES.DateTime, dateConstraints);
	}
}