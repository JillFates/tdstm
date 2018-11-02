import {Component} from '@angular/core';
import {PreferenceService} from '../../../services/preference.service';
import {IntlService} from '@progress/kendo-angular-intl';
import {DateControlCommons} from './date-control-commons';

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
 * input: yyyy-MM-ddThh:mm:ssZ or yyyy-MM-dd hh:mm:ss
 * output: yyyy-MM-ddThh:mm:ssZ
 */
export class DateTimeControlComponent extends DateControlCommons {

	constructor(userPreferenceService: PreferenceService, intl: IntlService) {
		super(userPreferenceService, intl, 'yyyy-MM-ddThh:mm:ssZ', 'yyyy-MM-dd hh:mm:ss');
	}

	onValueChange($event: Date): void {
		this.value = this.intl.formatDate($event, this.outputFormat);
		this.valueChange.emit(this.value);
	}
}