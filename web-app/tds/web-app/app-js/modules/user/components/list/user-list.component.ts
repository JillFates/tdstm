import {Component, ElementRef, Inject, OnInit, Renderer2} from '@angular/core';
import {UIDialogService} from '../../../../shared/services/ui-dialog.service';

import {UserService} from '../../service/user.service';
import {UserPreferencesComponent} from "../preferences/user-preferences.component";

@Component({
	selector: 'user-list',
	templateUrl: '../tds/web-app/app-js/modules/user/components/preferences/user-list.component.html'
})
export class UserListComponent {

	constructor(
		private userService: UserService,
    	private dialogService: UIDialogService) {
		this.openProviderDialogViewEdit();
	}

    private openProviderDialogViewEdit(): void {
        this.dialogService.open(UserPreferencesComponent, []).then(result => {
        }).catch(result => {
            console.log('Dismissed Dialog');
        });
    }
}