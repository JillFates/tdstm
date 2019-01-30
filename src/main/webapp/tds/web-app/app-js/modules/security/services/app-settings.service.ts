/**
 * App Settings is an early service injected at the beginning of the App that initialize the App Object that contains
 * multiple information necessary to run the application, like permissions, license management, etc.
 */
// Angular
import {Injectable} from '@angular/core';
// Model
import {AppSettingsModel, APP_SETTINGS_REQUEST} from '../model/app-settings.model';
// Services
import {UserService} from '../../../shared/services/user.service';
import {PermissionService} from '../../../shared/services/permission.service';
// Others
import {BehaviorSubject, Observable} from 'rxjs';

@Injectable()
export class AppSettingsService {

	private appSettings = new BehaviorSubject(new AppSettingsModel());
	private settings = this.appSettings.asObservable();

	constructor(
		private userService: UserService,
		private permissionService: PermissionService) {
	}

	/**
	 * Get the App Settings
	 */
	public getAppSettings(): Observable<AppSettingsModel> {
		return Observable.from(this.settings);
	}

	/**
	 * Being call in the Bootstrap of the Application
	 */
	public initializeAppSettings() {
		let allSettingsPromises = [];
		allSettingsPromises.push(this.userService.getUserInfo());
		allSettingsPromises.push(this.userService.getLicenseManagerEnabled());
		allSettingsPromises.push(this.permissionService.getPermissions());

		return new Promise((resolve) => {
			Observable.forkJoin(allSettingsPromises)
				.subscribe((settingsResponse: any) => {
					this.appSettings.next({
						userInfo: settingsResponse[APP_SETTINGS_REQUEST.USER_INFO],
						licenseEnabled: settingsResponse[APP_SETTINGS_REQUEST.LICENSE_ENABLED],
						permissions: settingsResponse[APP_SETTINGS_REQUEST.PERMISSIONS]
					});
					resolve(true);
				});
		})
	}
}