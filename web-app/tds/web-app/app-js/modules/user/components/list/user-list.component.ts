import {Component, OnInit} from '@angular/core';
import {UserPreferencesComponent} from '../preferences/user-preferences.component';
import {UserService} from '../../service/user.service';
import {UIDialogService} from '../../../../shared/services/ui-dialog.service';
import {UIPromptService} from '../../../../shared/directives/ui-prompt.directive';
import {UIActiveDialogService} from '../../../../shared/services/ui-dialog.service';
import {UserManageStaffComponent} from '../manage-staff/user-manage-staff.component';
import {PersonModel} from '../../../../shared/components/add-person/model/person.model';

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
		this.dialogService.open(UserManageStaffComponent, [
			{provide: 'id', useValue: 5662},
			{provide: PersonModel, useValue: {}}
		]).catch(result => {
			if (result) {
				console.error(result);
			}
		});
	}
}