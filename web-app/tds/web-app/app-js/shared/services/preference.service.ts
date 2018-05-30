import { Injectable } from '@angular/core';
import { Response, RequestOptions, Headers } from '@angular/http';
import { Observable } from 'rxjs/Rx';
import { HttpInterceptor } from '../providers/http-interceptor.provider';

import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';
import {DateUtils} from '../utils/date.utils';

@Injectable()
export class PreferenceService {

	public static readonly USER_PREFERENCES_DATE_FORMAT = 'CURR_DT_FORMAT';

	private preferenceUrl = '../ws/user/preferences';
	private preferenceUrlPost = '../ws/user/preference';

	preferences: any = {};

	constructor(private http: HttpInterceptor) {
	}

	// query a set of user preferences passed as arg variables
	getPreferences(...preferencesCodes: string[]): Observable <any> {
		return this.getPreference(preferencesCodes.join(','));
	}

	getPreference(preferenceCode: string): Observable<any> {
		return this.http.get(`${this.preferenceUrl}/${preferenceCode}`)
			.map((res: Response) => {
				let response = res.json();
				Object.keys(response.data.preferences).forEach((key) => {
					this.preferences[key] = response.data.preferences[key];
				});
				return this.preferences;
			})
			.catch((error: any) => Observable.throw(error.json() || 'Server error'));
	}

	setPreference(preferenceCode: string, value: string): Observable<any>  {
		const headers = new Headers();
		headers.append('Content-Type', 'application/x-www-form-urlencoded');
		const requestOptions = new RequestOptions({headers: headers});

		const body = `code=${preferenceCode}&value=${value}`;
		return this.http.post(this.preferenceUrlPost, body, requestOptions);
	}

	getUserTimeZone(): string {
		const currentUserDateFormat = this.preferences[PreferenceService.USER_PREFERENCES_DATE_FORMAT];
		if (currentUserDateFormat) {
			return DateUtils.translateTimeZoneFormat(currentUserDateFormat);
		}
		return DateUtils.DEFAULT_TIMEZONE_FORMAT;
	}

	/**
	 * Get the user date format preference and translate it to the corresponding Kendo date format
	 * @returns {Observable<string>}
	 */
	public getUserDatePreferenceAsKendoFormat(): Observable<string> {
		return this.getPreference(PREFERENCES_LIST.CURRENT_DATE_FORMAT)
			.map((preferences: any) => (preferences && preferences[PREFERENCES_LIST.CURRENT_DATE_FORMAT]) || DateUtils.DEFAULT_TIMEZONE_FORMAT )
			.map((dateFormat) => DateUtils.translateDateFormatToKendoFormat(dateFormat))
	}
}

// add constants as needed
export const PREFERENCES_LIST = {
	ASSET_JUST_PLANNING: 'assetJustPlanning',
	ASSET_LIST_SIZE : 'assetListSize',
	VIEW_MANAGER_DEFAULT_SORT: 'viewManagerDefaultSort',
	CURRENT_DATE_FORMAT: 'CURR_DT_FORMAT'
};
