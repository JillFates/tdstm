// Angular
import {Component} from '@angular/core';
// NGXS
import {Store} from '@ngxs/store';
// Component
import {UserPreferencesComponent} from '../preferences/user-preferences.component';
import {UserEditPersonComponent} from '../edit-person/user-edit-person.component';
import {UserDateTimezoneComponent} from '../date-timezone/user-date-timezone.component';
// Service
import {UserContextService} from '../../../../../modules/auth/service/user-context.service';
import {UIDialogService} from '../../../../services/ui-dialog.service';
import {NotifierService} from '../../../../services/notifier.service';
// Model
import {UserContextModel} from '../../../../../modules/auth/model/user-context.model';
import {PersonModel} from '../../../../components/add-person/model/person.model';
import {PasswordChangeModel} from '../../model/password-change.model';
import {DIALOG_SIZE} from '../../../../model/constants';
import {PageMetadataModel} from '../../model/page-metadata.model';
import {Logout} from '../../../../../modules/auth/action/login.actions';

declare var jQuery: any;

@Component({
	selector: 'tds-header',
	templateUrl: 'header.component.html',
})

export class HeaderComponent {

	public userContext: UserContextModel;
	public pageMetaData: PageMetadataModel = new PageMetadataModel();

	constructor(
		private userContextService: UserContextService,
		private dialogService: UIDialogService,
		private notifierService: NotifierService,
		private store: Store) {
		this.getUserContext();
		this.headerListeners();
	}

	/**
	 * Create the Lister for any changes made to the Routing that affects the Header Component
	 * Includes breadcrumbs, tiles, and other menu changes
	 */
	private headerListeners(): void {
		this.notifierService.on('notificationRouteChange', event => {
			if (event.event.url.indexOf('/auth/') >= 0) {
				this.pageMetaData.hideTopNav = true;
				jQuery('div.content-wrapper').addClass('content-login-wrapper');
			} else {
				this.pageMetaData.hideTopNav = false;
				jQuery('div.content-wrapper').removeClass('content-login-wrapper');
			}
		});
	}

	protected getUserContext(): void {
		this.userContextService.getUserContext().subscribe( (userContext: UserContextModel) => {
			if (!userContext.user) {
				this.pageMetaData.hideTopNav = true;
			}
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

		this.dialogService.extra(UserDateTimezoneComponent, [{
			provide: Boolean,
			useValue: true
		}]).catch(result => {
			console.log('Dismissed Dialog');
		});
	}

	/**
	 * Destroy the Storage and redirect the user
	 */
	public logOut(): void {
		localStorage.removeItem('@@STATE');
		this.store.dispatch(new Logout());
	}
}
