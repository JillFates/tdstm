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

	private readonly DISPLAY_FORMAT = 'yyyy-MM-dd hh:mm:ss';

	constructor(userPreferenceService: PreferenceService, intl: IntlService) {
		super(userPreferenceService, intl, DateUtils.TDS_OUTPUT_DATETIME_FORMAT);
		this.displayFormat = this.DISPLAY_FORMAT;
	}

	onValueChange($event: Date): void {
		this.value = this.intl.formatDate($event, this.outputFormat);
		this.valueChange.emit(this.value);
	}
}