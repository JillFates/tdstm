import {Component} from '@angular/core';
import {PreferenceService} from '../../../services/preference.service';
import {IntlService} from '@progress/kendo-angular-intl';
import {DateControlCommons} from './date-control-commons';

@Component({
	selector: 'tds-date-time-control',
	template: `
		<div>
            <kendo-datepicker [(value)]="dateValue"
							  [format]="format"
                              (valueChange)="onValueChange($event)">
			</kendo-datepicker>
		</div>
	`
})
export class DateTimeControlComponent extends DateControlCommons {

	private readonly DEFAULT_FORMAT = 'yyyy-MM-dd hh:mm:ssZ';

	constructor(userPreferenceService: PreferenceService, intl: IntlService) {
		super(userPreferenceService, intl);
		if (!this.format) {
			this.format = this.DEFAULT_FORMAT;
		}
	}
}