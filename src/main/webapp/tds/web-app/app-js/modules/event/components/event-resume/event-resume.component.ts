// Angular
import {Component, Input, } from '@angular/core';
import {EventPlanStatus} from '../../model/event.model';

@Component({
	selector: 'tds-event-resume',
	templateUrl: 'event-resume.component.html'
})
export class EventResumeComponent {
	@Input() event: EventPlanStatus = null;

}
