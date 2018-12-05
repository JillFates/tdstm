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
export class DateControlComponent extends DateControlCommons {

	constructor(userPreferenceService: PreferenceService, intl: IntlService) {
		super(userPreferenceService, intl, DateUtils.TDS_OUTPUT_DATE_FORMAT);
		this.displayFormat = this.userPreferenceService.getUserDateFormatForKendo();
	}

	/**
	 * Emit value changed.
	 * @param {Date} $event
	 */
	onValueChange($event: Date): void {
		if ($event && $event !== null) {
			this.value = this.intl.formatDate($event, this.outputFormat);
		} else {
			this.value = null;
		}
		this.valueChange.emit(this.value);
	}
}