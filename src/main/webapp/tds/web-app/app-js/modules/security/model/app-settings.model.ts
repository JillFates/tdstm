export class AppSettingsModel {
	userInfo: Object;
	licenseEnabled: boolean;
	permissions: any[]
}

export enum APP_SETTINGS_REQUEST {
	USER_INFO = 0,
	LICENSE_ENABLED = 1,
	PERMISSIONS = 2
}