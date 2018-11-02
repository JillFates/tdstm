import {Component} from '@angular/core';
import {PreferenceService} from '../../../services/preference.service';
import {IntlService} from '@progress/kendo-angular-intl';
import {DateControlCommons} from './date-control-commons';
import {DateUtils} from '../../../utils/date.utils';

@Component({
	selector: 'tds-date-control',
	template: `
		<div>
            <kendo-datepicker [(value)]="dateValue"
							  [format]="displayFormat"
                              (valueChange)="onValueChange($event)">
			</kendo-datepicker>
		</div>
	`
})
/**
 * input: yyyy-MM-dd
 * output: yyyy-MM-dd
 */
export class DateControlComponent extends DateControlCommons {

	constructor(userPreferenceService: PreferenceService, intl: IntlService) {
		super(userPreferenceService, intl, 'yyyy-MM-dd',
			DateUtils.translateDateFormatToKendoFormat(DateUtils.DEFAULT_FORMAT_DATE));
		this.userPreferenceService.getUserDatePreferenceAsKendoFormat()
			.subscribe((dateFormat) => {
				// DateUtils.formatUserDateTime(userTimeZone, dateTimeString);
				this.displayFormat = dateFormat;
			});
	}

	onValueChange($event: Date): void {
		this.value = this.intl.formatDate($event, this.outputFormat);
		console.log(this.value)
		this.valueChange.emit(this.value);
	}
}