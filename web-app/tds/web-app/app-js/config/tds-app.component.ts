/**
 * A Component is a Controller, that can permute into a directive or as a Service.
 */

import {Component} from '@angular/core';
import {UserService} from '../shared/services/user.service';

@Component({
	selector: 'tds-app',
	templateUrl: '../tds/web-app/app-js/config/tds-app.component.html',
})

export class TDSAppComponent {

	name = 'Angular';
	color = 'blue';
	userName = '';

	constructor(userService: UserService) {
		this.userName = userService.userName;
	}

}
