import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {PreferenceService} from '../../../services/preference.service';
import {DateUtils} from '../../../utils/date.utils';

@Component({
	selector: 'tds-datetime-control',
	template: `
		<div>
            <kendo-datepicker [(value)]="dateValue"
							  [format]="displayFormat"
							  [tabindex]="tabindex"
                              (valueChange)="onValueChange($event)"
							  class="form-control">
			</kendo-datepicker>
		</div>
	`
})
/**
 * input: yyyy-MM-dd hh:mm:ss
 * output: yyyy-MM-ddThh:mm:ssZ
 */
export class DateTimeControlComponent implements OnInit {

	@Input('value') value: any;
	@Output() valueChange = new EventEmitter<any>();
	@Input('required') required = false;
	protected outputFormat: string;
	protected displayFormat: string;
	protected dateValue: Date;

	private readonly KENDO_DATETIME_DISPLAY_FORMAT = 'yyyy-MM-dd HH:mm:ss';

	constructor(private userPreferenceService: PreferenceService) {
		this.displayFormat = this.KENDO_DATETIME_DISPLAY_FORMAT;
	}

	/**
	 * OnInit set a date value.
	 */
	ngOnInit(): void {
		let localDateFormatted = DateUtils.convertFromGMT(this.value, this.userPreferenceService.getUserTimeZone());
		this.dateValue = this.value ? DateUtils.toDateUsingFormat(localDateFormatted, DateUtils.SERVER_FORMAT_DATETIME) : null;
		this.onValueChange(this.dateValue);
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
		this.valueChange.emit(this.value);
	}
}