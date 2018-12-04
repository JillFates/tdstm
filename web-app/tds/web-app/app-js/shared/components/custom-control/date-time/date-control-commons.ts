import {EventEmitter, Input, OnInit, Output} from '@angular/core';
import {IntlService} from '@progress/kendo-angular-intl';
import {PreferenceService} from '../../../services/preference.service';
import {DateUtils} from '../../../utils/date.utils';

export abstract class DateControlCommons implements OnInit {

	@Input('value') value: any;
	@Output() valueChange = new EventEmitter<any>();
	@Input('required') required = false;
	protected outputFormat: string;
	protected displayFormat: string;
	protected dateValue: Date;
	private stringDate: string;

	constructor(
				protected userPreferenceService: PreferenceService,
				protected intl: IntlService,
				outputFormat: string) {
		this.outputFormat = outputFormat;
	}

	/**
	 * OnInit set a date value.
	 */
	ngOnInit(): void {
		this.dateValue = this.value ? DateUtils.toDateUsingFormat(this.value, this.outputFormat) : null;
		// this.dateValue = this.stringDate ? DateUtils.toDate(this.stringDate) : null;
		setTimeout( () => {
			this.onValueChange(this.dateValue);
			}, 200);
	}

	abstract onValueChange($event: Date);
}