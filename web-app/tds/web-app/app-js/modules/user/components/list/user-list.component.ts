import {Component, ElementRef, Inject, OnInit, Renderer2} from '@angular/core';
import {UIDialogService} from '../../../../shared/services/ui-dialog.service';

import {UserService} from '../../service/user.service';
import {UserPreferencesComponent} from '../preferences/user-preferences.component';

@Component({
	selector: 'user-list',
	templateUrl: '../tds/web-app/app-js/modules/user/components/list/user-list.component.html'
})
export class UserListComponent implements OnInit {

	constructor(
		private userService: UserService,
		private dialogService: UIDialogService) {
	}

	ngOnInit(): void {
		this.openProviderDialogViewEdit();
	}

	private openProviderDialogViewEdit(): void {
		this.dialogService.open(UserPreferencesComponent, []).then(result => {
			console.log('result here');
		}).catch(result => {
			console.log('Dismissed Dialog');
		});
	}
}