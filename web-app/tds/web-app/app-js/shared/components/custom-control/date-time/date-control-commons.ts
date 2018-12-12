import {EventEmitter, Input, OnInit, Output} from '@angular/core';
import {IntlService} from '@progress/kendo-angular-intl';
import {PreferenceService} from '../../../services/preference.service';
import {DateUtils} from '../../../utils/date.utils';
import {TDSCustomControl} from '../common/custom-control';

export abstract class DateControlCommons extends TDSCustomControl implements OnInit {

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
		super();
		this.outputFormat = outputFormat;
	}

	/**
	 * OnInit set a date value.
	 */
	ngOnInit(): void {
		this.dateValue = this.value ? DateUtils.toDate(this.value) : null;
	}

	abstract onValueChange($event: Date);
}