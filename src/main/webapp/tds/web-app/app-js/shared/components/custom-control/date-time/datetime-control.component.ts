import {
	Component,
	forwardRef,
	OnInit,
	OnChanges,
	SimpleChanges, AfterViewInit, EventEmitter, ViewChildren, QueryList
} from '@angular/core';
import {
	NG_VALUE_ACCESSOR,
	NG_VALIDATORS
} from '@angular/forms';
import {
	DatePickerComponent,
	TimePickerComponent,
	PreventableEvent
} from '@progress/kendo-angular-dateinputs';

import {merge} from 'rxjs';
import {CUSTOM_FIELD_TYPES} from '../../../model/constants';
import {DateUtils} from '../../../utils/date.utils';
import {PreferenceService} from '../../../services/preference.service';
import {TDSCustomControl} from '../common/custom-control.component';
import {ValidationRulesFactoryService} from '../../../services/validation-rules-factory.service';

@Component({
	selector: 'tds-datetime-control',
	template: `
		<div class="tds-datetime-control-component">
            <kendo-datepicker
                    [(ngModel)]="dateValue"
                    (blur)="onTouched()"
                    [format]="displayFormat"
                    [tabindex]="tabindex"
                    (valueChange)="onDateChange($event)"
                    class="form-control datepicker">
            </kendo-datepicker>
			<kendo-timepicker
				[value]="dateValue"
                (valueChange)="onTimeChange($event)">
			</kendo-timepicker>
		</div>
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
export class TDSDateTimeControlComponent extends TDSCustomControl implements OnInit, AfterViewInit, OnChanges {
	private readonly KENDO_DATETIME_DISPLAY_FORMAT_MMDD = 'yyyy-MM-dd HH:mm:ss';
	private readonly KENDO_DATETIME_DISPLAY_FORMAT_DDMM = 'yyyy-dd-MM HH:mm:ss';
	protected outputFormat: string;
	protected selectedDate: any;
	protected selectedTime: string;
	public displayFormat: string;
	public dateValue: any;
	public open: EventEmitter<PreventableEvent>;
	public close: EventEmitter<PreventableEvent>;
	public openTime: EventEmitter<PreventableEvent>;
	public closeTime: EventEmitter<PreventableEvent>;
	@ViewChildren(DatePickerComponent) datePicker: QueryList<DatePickerComponent>;
	@ViewChildren(TimePickerComponent) timePicker: QueryList<TimePickerComponent>;

	constructor(
		private userPreferenceService: PreferenceService,
		protected validationRulesFactory: ValidationRulesFactoryService
	) {
		super(validationRulesFactory);
		this.open = new EventEmitter<PreventableEvent>();
		this.close = new EventEmitter<PreventableEvent>();
		this.openTime = new EventEmitter<PreventableEvent>();
		this.closeTime = new EventEmitter<PreventableEvent>();
		this.displayFormat = this.userPreferenceService.getUserDateFormat() === 'DD/MM/YYYY' ? this.KENDO_DATETIME_DISPLAY_FORMAT_DDMM : this.KENDO_DATETIME_DISPLAY_FORMAT_MMDD;
	}

	/**
	 * OnInit set a date value.
	 */
	ngOnInit(): void {
		this.updateValue();
	}

	ngAfterViewInit(): void {
		// setup the listeners to the open/close list events
		this.datePicker.first.open
			.subscribe((event) => this.open.emit(event));

		this.timePicker.first.open
			.subscribe((event) => this.openTime.emit(event));

		this.datePicker.first.close
			.subscribe((event) => this.close.emit(event));

		this.timePicker.first.close
			.subscribe((event) => this.closeTime.emit(event));
	}

	/**
	 * Update the date and time values
	 */
	updateValue(): void {
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
			this.dateValue = value;
			this.value = DateUtils.convertToGMT(value, this.userPreferenceService.getUserTimeZone());
		} else {
			this.resetValue();
		}
		this.onTouched();
	}

	/**
	 * Reset the current date and time values
	 */
	private resetValue(): void {
		this.value = null;
		this.dateValue = '';
		this.selectedDate = '';
		this.selectedTime = '';
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
		} else {
			this.resetValue();
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
