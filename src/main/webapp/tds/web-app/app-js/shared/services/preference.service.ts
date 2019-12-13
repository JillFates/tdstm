import { Injectable } from '@angular/core';
import {BehaviorSubject, Observable} from 'rxjs';
import {HttpClient, HttpHeaders} from '@angular/common/http';

import {catchError, map} from 'rxjs/operators';

import {DateUtils} from '../utils/date.utils';
import {Store} from '@ngxs/store';
import {UserContextModel} from '../../modules/auth/model/user-context.model';
import {UserContextState} from '../../modules/auth/state/user-context.state';

// add constants as needed
export const PREFERENCES_LIST = {
	ASSET_JUST_PLANNING: 'ASSET_JUST_PLANNING',
	ASSET_LIST_SIZE: 'ASSET_LIST_SIZE',
	VIEW_MANAGER_DEFAULT_SORT: 'VIEW_MANAGER_DEFAULT_SORT',
	CURRENT_DATE_FORMAT: 'CURR_DT_FORMAT',
	CURR_TZ: 'CURR_TZ',
	DATA_SCRIPT_SIZE: 'DATA_SCRIPT_SIZE',
	VIEW_UNPUBLISHED: 'VIEW_UNPUBLISHED',
	IMPORT_BATCH_PREFERENCES: 'IMPORT_BATCH_PREFERENCES',
	CURR_DT_FORMAT: 'CURR_DT_FORMAT',
	CURRENT_MOVE_BUNDLE_ID: 'MOVE_BUNDLE',
	EVENTDB_REFRESH: 'EVENTDB_REFRESH',
	MOVE_EVENT: 'MOVE_EVENT',
	CURR_PROJ: 'CURR_PROJ',
	CURRENT_EVENT_ID: 'MOVE_EVENT',
	TASK_MANAGER_REFRESH_TIMER: 'TASKMGR_REFRESH',
	TASK_MANAGER_LIST_SIZE: 'TASK_LIST_SIZE',
	JUST_REMAINING: 'JUST_REMAINING',
	WRAP_TAGS_COLUMN: 'WRAP_TAGS_COLUMN'
};

export const IMPORT_BATCH_PREFERENCES = {
	LIST_SIZE: 'LIST_SIZE',
	TWISTIE_COLLAPSED: 'TWISTIE_COLLAPSED',
	RECORDS_FILTER: 'RECORDS_FILTER'
}

@Injectable()
export class PreferenceService {

	private preferenceUrl = '../ws/user/preferences';
	private preferenceUrlPost = '../ws/user/preference';

	// TODO: No one should have access to the the preferences outside
	public preferences: any = {};

	/**
	 * This is going to be the new structure, if the Preference exist it will return the value
	 * if not it will got it from the endpoint and persist the value for next request
	 */
	private preferencesList = new BehaviorSubject([]);

	constructor(
		private http: HttpClient,
		private store: Store) {
	}

	// query a set of user preferences passed as arg variables
	getPreferences(...preferencesCodes: string[]): Observable <any> {
		return this.getPreference(preferencesCodes.join(','));
	}

	getPreference(preferenceCode: string): Observable<any> {
		return this.http.get(`${this.preferenceUrl}/${preferenceCode}`)
			.pipe(map((response: any) => {
				Object.keys(response.data.preferences).forEach((key) => {
					this.preferences[key] = response.data.preferences[key];
				});
				return this.preferences;
			}))
			.pipe(catchError((error: any) => Observable.throw(error || 'Server error')));
	}

	getSinglePreference(preferenceCode: string): Observable<any> {
		return this.http.get(`${this.preferenceUrl}/${preferenceCode}`)
			.pipe(map((response: any) => {
				return response.data.preferences[preferenceCode];
			}))
			.pipe(catchError((error: any) => Observable.throw(error || 'Server error')));
	}

	/**
	 * Save value of the preference
	 * @param preferenceCode
	 * @param value
	 */
	setPreference(preferenceCode: string, value: string): Observable<any>  {
		const httpOptions = {
			headers: new HttpHeaders({'Content-Type': 'application/json'})
		};
		const body = JSON.stringify({code: preferenceCode, value});

		return this.http.post(this.preferenceUrlPost, body, httpOptions);
	}

	/**
	 * Used to retrieve the format to use for Date properties based on user's preference (e.g. MM/dd/YYYY)
	 */
	getUserDateFormat(): string {
		const dateFormat  = this.store.selectSnapshot(UserContextState.getDateFormat);
		if (dateFormat) {
			return dateFormat;
		}
		return DateUtils.DEFAULT_FORMAT_DATE;
	}

	// TODO: I don't see this returning the Date with any "special MomentJS" format?
	getUserDateFormatForMomentJS(): string {
		const currentUserDateFormat = this.preferences[PREFERENCES_LIST.CURRENT_DATE_FORMAT];
		if (currentUserDateFormat) {
			return currentUserDateFormat;
		}
		return DateUtils.PREFERENCE_MIDDLE_ENDIAN;
	}

	getUserDateFormatForKendo(): string {
		const currentUserDateFormat = this.preferences[PREFERENCES_LIST.CURRENT_DATE_FORMAT];
		return DateUtils.translateDateFormatToKendoFormat(currentUserDateFormat);
	}

	/**
	 * Used to retrieve the user's preferred TimeZone that which is used to display date times
	 * based on user's preference in TM instead of the TimeZone of their computer.
	 */
	getUserTimeZone(): string {
		const timeZone  = this.store.selectSnapshot(UserContextState.getTimezone);
		if (timeZone) {
			return timeZone;
		}
		return DateUtils.TIMEZONE_GMT;
	}

	/**
	 * Used to retrieve the format to use for Date Time properties (e.g. MM/dd/YYYY hh:mm a)
	 */
	getUserDateTimeFormat(): string {
		return this.getUserDateFormat() + ' ' + DateUtils.DEFAULT_FORMAT_TIME;
	}

	/**
	 * Get the user date format preference and translate it to the corresponding Kendo date format
	 * @returns {Observable<string>}
	 */
	public getUserDatePreferenceAsKendoFormat(): Observable<string> {
		return this.getPreference(PREFERENCES_LIST.CURRENT_DATE_FORMAT)
			.pipe(map((preferences: any) => (preferences && preferences[PREFERENCES_LIST.CURRENT_DATE_FORMAT]) || DateUtils.DEFAULT_FORMAT_DATE ))
			.pipe(map((dateFormat) => DateUtils.translateDateFormatToKendoFormat(dateFormat)));
	}

	/**
	 * Get the default date format in the the corresponding Kendo date format
	 * @returns {string}
	 */
	public getDefaultDateFormatAsKendoFormat(): string {
		return DateUtils.translateDateFormatToKendoFormat(DateUtils.DEFAULT_FORMAT_DATE);
	}

	/**
	 * Get the user preference datascript designer size (width/height)
	 * @returns {Observable<{number, number}>}
	 */
	public getDataScriptDesignerSize(): Observable<{width: number, height: number}> {
		const unitSizeSeparator = 'x';
		const defaultWidth = 850;
		const defaultHeight = 680;

		return this.getPreference(PREFERENCES_LIST.DATA_SCRIPT_SIZE)
			.pipe(map((preferences: any) => preferences[PREFERENCES_LIST.DATA_SCRIPT_SIZE] || ''))
			.pipe(map((size: string) => {
				let measure: string[] = (size || '').split(unitSizeSeparator);
				let	width = Number(measure.length &&  measure.shift()) || defaultWidth;
				let height = Number(measure.length &&  measure.shift()) || defaultHeight;
				return { width, height };
			}))
			.filter((size: any) =>  size.width !== null && size.height !== null);
	}
}
