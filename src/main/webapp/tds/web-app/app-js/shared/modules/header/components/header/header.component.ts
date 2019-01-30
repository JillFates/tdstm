// Angular
import {Component} from '@angular/core';
// Service
import {AppSettingsService} from '../../../../../modules/security/services/app-settings.service';
import {AppSettingsModel} from '../../../../../modules/security/model/app-settings.model';

@Component({
	selector: 'tds-header',
	templateUrl: '../tds/web-app/app-js/shared/modules/header/components/header/header.component.html',
})

export class HeaderComponent {

	protected appSettings: AppSettingsModel;

	constructor(private appSettingsService: AppSettingsService) {
		this.getAppSettings();
	}

	protected getAppSettings(): void {
		this.appSettingsService.getAppSettings().subscribe( (appSettings: AppSettingsModel) => {
			this.appSettings = appSettings;
		});
	}
}