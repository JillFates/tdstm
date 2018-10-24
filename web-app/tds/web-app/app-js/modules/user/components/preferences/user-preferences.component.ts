import {Component, ElementRef, Inject, OnInit, Renderer2} from '@angular/core';
import {UserService} from '../../service/user.service';

@Component({
	selector: 'user-preferences',
	templateUrl: '../tds/web-app/app-js/modules/user/components/preferences/user-preferences.component.html'
})
export class UserPreferencesComponent {

	constructor( private userService: UserService) {
	}
}