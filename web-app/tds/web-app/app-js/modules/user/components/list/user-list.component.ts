import {Component, OnInit} from '@angular/core';
import {UIDialogService} from '../../../../shared/services/ui-dialog.service';

import {UserService} from '../../service/user.service';
import {UserViewEditComponent} from '../view-edit/user-view-edit.component';
import {PersonModel} from '../../../../shared/components/add-person/model/person.model';
import {PasswordChangeModel} from '../../../../shared/components/password-change/model/password-change.model';

@Component({
	selector: 'user-list',
	templateUrl: '../tds/web-app/app-js/modules/user/components/list/user-list.component.html'
})
export class UserListComponent implements OnInit {

	constructor(
		private userService: UserService,
		private dialogService: UIDialogService) {
	}

	ngOnInit():void {
		this.dialogService.open(UserViewEditComponent, [
			{ provide: PersonModel, useValue: {} },
			{ provide: PasswordChangeModel, useValue: {} }
		]).catch(result => {
			if(result) {
				console.error(result);
			}
		});
	}
}