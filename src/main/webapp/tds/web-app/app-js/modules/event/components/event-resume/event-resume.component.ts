// Angular
import {Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import {EventPlanStatus} from '../../model/event.model';
import {PREFERENCES_LIST, PreferenceService} from '../../../../shared/services/preference.service';
import {DateUtils} from '../../../../shared/utils/date.utils';

@Component({
	selector: 'tds-event-resume',
	templateUrl: 'event-resume.component.html'
})
export class EventResumeComponent implements OnChanges {
	public userTimeZone = '';
	public dateTimeFormat = '';
	public datePrefixString = '';
	@Input() event: EventPlanStatus = null;

	constructor(private preferenceService: PreferenceService) {
		this.preferenceService.getPreferences(PREFERENCES_LIST.CURR_TZ, PREFERENCES_LIST.CURRENT_DATE_FORMAT)
		.subscribe((preferences) => {
			this.userTimeZone =  preferences.CURR_TZ;
			this.dateTimeFormat = `${preferences.CURR_DT_FORMAT || this.preferenceService.getUserDateFormat()} ${DateUtils.DEFAULT_FORMAT_TIME}`;
		});
	}

	ngOnInit() {
		this.setClockMode();
	}

	/**
	* Sets the clockMode property
	*/
	private setClockMode(): void {
		if (this.event.clockMode === 'countdown') {
			this.datePrefixString = 'Starts on '
		}
		if (this.event.clockMode === 'elapsed') {
			this.datePrefixString = 'Started on '
		}
		if (this.event.clockMode === 'finished') {
			this.datePrefixString = 'Completed on '
		}
	}

	ngOnChanges(changes: SimpleChanges) {
		this.setClockMode();
		console.log(changes);
	}
}
