// Angular
import {Component, Input, } from '@angular/core';
import {EventPlanStatus} from '../../model/event.model';
import {PreferenceService} from '../../../../shared/services/preference.service';
import {DateUtils} from '../../../../shared/utils/date.utils';

@Component({
	selector: 'tds-event-resume',
	templateUrl: 'event-resume.component.html'
})
export class EventResumeComponent {
	public userTimeZone = '';
	public dateTimeFormat = '';
	@Input() event: EventPlanStatus = null;

	constructor(private preferenceService: PreferenceService) {
		this.preferenceService.getUserDatePreferenceAsKendoFormat()
		.subscribe(() => {
			this.userTimeZone = this.preferenceService.getUserTimeZone();
			this.dateTimeFormat = `${this.preferenceService.getUserCurrentDateFormatOrDefault()} ${DateUtils.DEFAULT_FORMAT_TIME}`;
		});
	}

}
