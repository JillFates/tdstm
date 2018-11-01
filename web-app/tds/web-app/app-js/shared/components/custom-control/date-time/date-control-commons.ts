import {EventEmitter, Input, OnInit, Output} from '@angular/core';
import {IntlService} from '@progress/kendo-angular-intl';
import {PreferenceService} from '../../../services/preference.service';
import {DateUtils} from '../../../utils/date.utils';

export class DateControlCommons implements OnInit {

	@Input('value') value: any;
	@Output() valueChange = new EventEmitter<any>();
	protected format: any;
	protected dateValue: Date;

	constructor(protected userPreferenceService: PreferenceService, protected intl: IntlService) {
		// Silence is golden.
	}

	ngOnInit(): void {
		this.dateValue = this.value ? DateUtils.compose(this.value) : new Date();
		this.onValueChange(this.dateValue);
	}

	protected onValueChange($event: Date): void {
		this.value = this.intl.formatDate($event, this.format);
		console.log(this.value)
		this.valueChange.emit(this.value);
	}
}