import {EventEmitter, Input, OnInit, Output} from '@angular/core';
import {PreferenceService} from '../../../services/preference.service';
import {DateUtils} from '../../../utils/date.utils';

export abstract class DateControlCommons implements OnInit {

	@Input('value') value: any;
	@Output() valueChange = new EventEmitter<any>();
	@Input('required') required = false;
	protected outputFormat: string;
	protected displayFormat: string;
	protected dateValue: Date;

	constructor(protected userPreferenceService: PreferenceService, outputFormat: string) {
		this.outputFormat = outputFormat;
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

	abstract onValueChange($event: Date);
}