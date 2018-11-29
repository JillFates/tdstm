import {Component} from '@angular/core';
import {PreferenceService} from '../../../services/preference.service';
import {IntlService} from '@progress/kendo-angular-intl';
import {DateControlCommons} from './date-control-commons';
import {DateUtils} from '../../../utils/date.utils';

@Component({
	selector: 'tds-datetime-control',
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
 * input: yyyy-MM-dd hh:mm:ss
 * output: yyyy-MM-ddThh:mm:ssZ
 */
export class DateTimeControlComponent extends DateControlCommons {

	constructor(userPreferenceService: PreferenceService, intl: IntlService) {
		super(userPreferenceService, intl, DateUtils.TDS_OUTPUT_DATETIME_FORMAT);
		this.displayFormat = `${DateUtils.TDS_OUTPUT_DATE_FORMAT} ${DateUtils.TDS_OUTPUT_PIPE_TIME_FORMAT}`;
	}

	/**
	 * Emit value changed.
	 * @param {Date} $event
	 */
	onValueChange($event: Date): void {
		if ($event && $event !== null) {
			this.value = DateUtils.convertAndFormatDateToGMT($event, this.outputFormat) + 'Z';
		} else {
			this.value = null;
		}
		this.valueChange.emit(this.value);
	}
}