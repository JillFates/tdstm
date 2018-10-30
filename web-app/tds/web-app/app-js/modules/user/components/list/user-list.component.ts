import {Component} from '@angular/core';
import {UIDialogService} from '../../../../shared/services/ui-dialog.service';

import {UserService} from '../../service/user.service';
import {UserPreferencesComponent} from '../preferences/user-preferences.component';

@Component({
	selector: 'user-list',
	templateUrl: '../tds/web-app/app-js/modules/user/components/list/user-list.component.html'
})
export class UserListComponent {

	constructor(
		private userService: UserService,
		private dialogService: UIDialogService) {
	}
}