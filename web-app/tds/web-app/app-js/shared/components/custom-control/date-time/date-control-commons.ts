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

	constructor(
				protected userPreferenceService: PreferenceService,
				protected intl: IntlService,
				outputFormat: string) {
		this.outputFormat = outputFormat;
	}

	ngOnInit(): void {
		this.dateValue = this.value ? DateUtils.toDate(this.value) : new Date();
		this.onValueChange(this.dateValue);
	}

	abstract onValueChange($event: Date);
}