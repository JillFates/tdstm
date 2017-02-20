/**
 * Alert Model represents the status message being show across the app
 * depending the type;
 */

export enum AlertType {
    SUCCESS,
    DANGER,
    INFO,
    WARNING
};

export class AlertModel {
    static shows: boolean;
    static status: string;
    static message: string;
    static time: 2000;
}