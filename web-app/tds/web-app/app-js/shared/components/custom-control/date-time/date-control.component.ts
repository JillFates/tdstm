import {Component} from '@angular/core';
import {PreferenceService} from '../../../services/preference.service';
import {IntlService} from '@progress/kendo-angular-intl';
import {DateControlCommons} from './date-control-commons';

@Component({
	selector: 'tds-date-control',
	template: `
		<div>
            <kendo-datepicker [(value)]="dateValue"
							  [format]="format"
                              (valueChange)="onValueChange($event)">
			</kendo-datepicker>
		</div>
	`
})
export class DateControlComponent extends DateControlCommons {

	constructor(userPreferenceService: PreferenceService, intl: IntlService) {
		super(userPreferenceService, intl);
		this.userPreferenceService.getUserDatePreferenceAsKendoFormat()
			.subscribe((dateFormat) => {
				// DateUtils.formatUserDateTime(userTimeZone, dateTimeString);
				this.format = dateFormat;
				console.log(this.format);
			});
	}
}