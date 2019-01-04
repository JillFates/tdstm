/**
 * Alert Model represents the status message being show across the app
 * depending the type;
 */

export enum AlertType {
	EMPTY,
	SUCCESS,
	DANGER,
	INFO,
	WARNING
}
;

export class AlertModel {
	static alertType: AlertType;
	static message: string;
	static time: 2000;
}