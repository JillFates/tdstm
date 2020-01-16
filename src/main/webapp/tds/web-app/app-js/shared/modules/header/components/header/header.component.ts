// Angular
import {Component} from '@angular/core';
import {FormGroup, FormControl} from '@angular/forms';
// NGXS
import {Store} from '@ngxs/store';
import {Logout} from '../../../../../modules/auth/action/login.actions';
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
import {PageMetadataModel} from '../../model/page-metadata.model';
import {APP_STATE_KEY} from '../../../../providers/localstorage.provider';
import {LIC_MANAGER_GRID_PAGINATION_STORAGE_KEY} from '../../../../../shared/model/constants';
import {ReplaySubject} from 'rxjs';
import {SetUserContextPerson} from '../../../../../modules/auth/action/user-context-person.actions';

declare var jQuery: any;

@Component({
	selector: 'tds-header',
	templateUrl: 'header.component.html',
})
export class HeaderComponent {
	public userContext: UserContextModel;
	public pageMetaData: PageMetadataModel = new PageMetadataModel();
	public searchForm = new FormGroup({
		search: new FormControl(''),
	});
	public fullName: ReplaySubject<string> = new ReplaySubject<string>(1);
	public iconText: ReplaySubject<string> = new ReplaySubject<string>(1);

	constructor(
		private userContextService: UserContextService,
		private dialogService: UIDialogService,
		private notifierService: NotifierService,
		private store: Store
	) {
		this.pageMetaData.hideTopNav = true;
		this.notifierService.on('userDetailsUpdated', () => this.getUserContext());
		this.store.dispatch(new SetUserContextPerson())
			.subscribe(() => this.getUserContext());
		this.headerListeners();
	}

	/**
	 * Create the Lister for any changes made to the Routing that affects the Header Component
	 * Includes breadcrumbs, tiles, and other menu changes
	 */
	private headerListeners(): void {
		this.notifierService.on('notificationRouteChange', event => {
			this.pageMetaData.hideTopNav = event.event.url.indexOf('/auth/') >= 0;
		});
	}

	protected getUserContext(): void {
		this.userContextService
			.getUserContext()
			.subscribe((userContext: UserContextModel) => {
				if (!userContext.user) {
					this.pageMetaData.hideTopNav = true;
				} else if (!userContext.project.logoUrl) {
					userContext.project.logoUrl =
						'/tdstm/tds/web-app/assets/images/transitionLogo.svg';
				}
				this.userContext = userContext;
				const fName = userContext.person && userContext.person.fullName;
				if (fName) {
					this.fullName.next(fName);
					this.iconText.next(this.getUserIconText(fName));
				}
			});
	}

	/**
	 * transform full name to be only the initials
	 * @param fullName
	 */
	public getUserIconText(fullName: string): string {
		return fullName.split(' ').map(x => x.charAt(0)).join('').substring(0, 3).toUpperCase();
	}

	/**
	 * Opens the user preferences modal.
	 */
	public openPrefModal(): void {
		this.dialogService.extra(UserPreferencesComponent, [], true, true).catch(result => {
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
			useValue: false
		}, {
			provide: String,
			useValue: ''
		}]).catch(result => {
			console.log('Dismissed Dialog');
		});
	}

	/**
	 * Destroy the Storage and redirect the user
	 */
	public logOut(): void {
		localStorage.removeItem(APP_STATE_KEY);
		localStorage.removeItem(LIC_MANAGER_GRID_PAGINATION_STORAGE_KEY);
		this.store.dispatch(new Logout());
	}
}
