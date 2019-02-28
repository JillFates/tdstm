// Angular
import {Component, OnInit} from '@angular/core';
// Component
import {UserPreferencesComponent} from '../../../../../modules/user/components/preferences/user-preferences.component';
import {UserEditPersonComponent} from '../../../../../modules/user/components/edit-person/user-edit-person.component';
import {UserDateTimezoneComponent} from '../../../../../modules/user/components/date-timezone/user-date-timezone.component';
// Service
import {UserContextService} from '../../../../../modules/security/services/user-context.service';
import {UIDialogService} from '../../../../services/ui-dialog.service';
// Model
import {UserContextModel} from '../../../../../modules/security/model/user-context.model';
import {PersonModel} from '../../../../components/add-person/model/person.model';
import {PasswordChangeModel} from '../../../../components/password-change/model/password-change.model';
import {DIALOG_SIZE} from '../../../../model/constants';

declare var jQuery: any;

@Component({
	selector: 'tds-header',
	templateUrl: 'header.component.html',
})

export class HeaderComponent implements OnInit {

	public userContext: UserContextModel;

	constructor(
		private userContextService: UserContextService,
		private dialogService: UIDialogService) {
		this.getUserContext();
	}

	ngOnInit(): void {
		/**
		 * AdminLTE is main js that handles the layout, this could me removed later when implementing the footer
		 */
		if (jQuery.AdminLTE) {
			jQuery.AdminLTE.layout.fix();
		}
		jQuery('.main-footer').show();
	}

	protected getUserContext(): void {
		this.userContextService.getUserContext().subscribe( (userContext: UserContextModel) => {
			this.userContext = userContext;
		});
	}

	public openPrefModal(): void {
		this.dialogService.open(UserPreferencesComponent, []).catch(result => {
			//
		});
	}

	public openEditPersonModal(): void {
		this.dialogService.open(UserEditPersonComponent, [
			{provide: PersonModel, useValue: {}},
			{provide: PasswordChangeModel, useValue: {}}
		]).catch(result => {
			//
		});
	}

	public openDateTimezoneModal(): void {
		this.dialogService.open(UserDateTimezoneComponent, [], DIALOG_SIZE.LG).catch(result => {
			//
		});
	}
}