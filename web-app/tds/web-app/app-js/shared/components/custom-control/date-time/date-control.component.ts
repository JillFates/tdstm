import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {PreferenceService} from '../../../services/preference.service';
import {DateUtils} from '../../../utils/date.utils';
import {TDSCustomControl} from '../common/custom-control';

@Component({
	selector: 'tds-date-control',
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
 * input: yyyy-MM-dd
 * output: yyyy-MM-dd (value string to be stored as final value)
 */
export class DateControlComponent extends TDSCustomControl implements OnInit  {

	@Input('value') value: any;
	@Output() valueChange = new EventEmitter<any>();
	@Input('required') required = false;
	protected displayFormat: string;
	protected dateValue: Date;

	constructor(private userPreferenceService: PreferenceService) {
		super();
		this.displayFormat = userPreferenceService.getUserDateFormatForKendo();
	}

	/**
	 * OnInit set a date value.
	 */
	ngOnInit(): void {
		let localDateFormatted = DateUtils.getDateFromGMT(this.value);
		this.dateValue = this.value ? DateUtils.toDateUsingFormat(localDateFormatted, DateUtils.SERVER_FORMAT_DATE) : null;
		this.onValueChange(this.dateValue);
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
		this.valueChange.emit(this.value);
	}
}