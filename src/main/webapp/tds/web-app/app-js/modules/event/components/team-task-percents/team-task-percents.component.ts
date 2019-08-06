// Angular
import {Component, Input} from '@angular/core';

@Component({
	selector: 'tds-team-task-percents',
	templateUrl: 'team-task-percents.component.html'
})
export class TeamTaskPercentsComponent {
	@Input() teamTaskMatrix = [];
}
