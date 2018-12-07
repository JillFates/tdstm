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
                              (valueChange)="onValueChange($event)"
							  class="form-control">
			</kendo-datepicker>
		</div>
	`
})
/**
 * input: yyyy-MM-dd hh:mm:ss
 * output: yyyy-MM-ddThh:mm:ssZ
 */
export class DateTimeControlComponent extends DateControlCommons {

	private readonly KENDO_DATETIME_DISPLAY_FORMAT = 'yyyy-MM-dd HH:mm:ss';

	constructor(userPreferenceService: PreferenceService, intl: IntlService) {
		super(userPreferenceService, intl, DateUtils.TDS_OUTPUT_DATETIME_FORMAT);
		this.displayFormat = this.KENDO_DATETIME_DISPLAY_FORMAT;
	}

	/**
	 * On value Change on the component, emits the value to the listeners.
	 * @param {value} Date
	 */
	onValueChange(value: Date): void {
		if (value && value !== null) {
			this.value = DateUtils.convertToGMT(value, this.userPreferenceService.getUserTimeZone());
		} else {
			this.value = null;
		}
		this.valueChange.emit(this.value);
	}
}