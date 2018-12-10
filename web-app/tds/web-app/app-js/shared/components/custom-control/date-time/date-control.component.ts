import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {PreferenceService} from '../../../services/preference.service';
import {DateUtils} from '../../../utils/date.utils';

@Component({
	selector: 'tds-date-control',
	template: `
		<div>
            <kendo-datepicker [(value)]="dateValue"
							  [format]="displayFormat"
                              (valueChange)="onValueChange($event)"
							  class="form-control">
			</kendo-datepicker>
		</div>
	`
})
/**
 * input: yyyy-MM-dd
 * output: yyyy-MM-dd (value string to be stored as final value)
 */
export class DateControlComponent implements OnInit  {

	@Input('value') value: any;
	@Output() valueChange = new EventEmitter<any>();
	@Input('required') required = false;
	protected outputFormat: string;
	protected displayFormat: string;
	protected dateValue: Date;

	constructor(private userPreferenceService: PreferenceService) {
		this.displayFormat = userPreferenceService.getUserDateFormatForKendo();
	}

	/**
	 * OnInit set a date value.
	 */
	ngOnInit(): void {
		let localDateFormatted = DateUtils.convertFromGMT(this.value, this.userPreferenceService.getUserTimeZone());
		this.dateValue = this.value ? DateUtils.toDateUsingFormat(localDateFormatted, this.outputFormat) : null;
		setTimeout( () => {
			this.onValueChange(this.dateValue);
		}, 200);
	}

	/**
	 * On value Change on the component, emits the value to the listeners.
	 * @param {value} Date
	 */
	onValueChange(value: Date): void {
		if (value && value !== null) {
			this.value = DateUtils.formatDate(value, DateUtils.TDS_OUTPUT_DATE_FORMAT)
		} else {
			this.value = null;
		}
		this.valueChange.emit(this.value);
	}
}