import { Injectable } from '@angular/core';
import { Response, RequestOptions, Headers } from '@angular/http';
import { Observable } from 'rxjs/Observable';
import { HttpInterceptor } from '../providers/http-interceptor.provider';

import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';
import {DateUtils} from '../utils/date.utils';
import {GRID_DEFAULT_PAGE_SIZE} from '../model/constants';

// add constants as needed
export const PREFERENCES_LIST = {
	ASSET_JUST_PLANNING: 'assetJustPlanning',
	ASSET_LIST_SIZE : 'assetListSize',
	VIEW_MANAGER_DEFAULT_SORT: 'viewManagerDefaultSort',
	CURRENT_DATE_FORMAT: 'CURR_DT_FORMAT',
	DATA_SCRIPT_SIZE: 'DataScriptSize',
	VIEW_UNPUBLISHED: 'viewUnpublished',
	IMPORT_BATCH_LIST_SIZE: 'ImportBatchListSize'
};

@Injectable()
export class PreferenceService {

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

	getSinglePreference(preferenceCode: string): Observable<any> {
		return this.http.get(`${this.preferenceUrl}/${preferenceCode}`)
			.map((res: Response) => {
				let response = res.json();
				return response.data.preferences[preferenceCode];
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
		const currentUserDateFormat = this.preferences[PREFERENCES_LIST.CURRENT_DATE_FORMAT];
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

	/**
	 * Get the user preference datascript designer size (width/height)
	 * @returns {Observable<{number, number}>}
	 */
	public getDataScriptDesignerSize(): Observable<{width: number, height: number}> {
		const unitSizeSeparator = 'x';
		const defaultWidth = 580;
		const defaultHeight = 680;

		return this.getPreference(PREFERENCES_LIST.DATA_SCRIPT_SIZE)
			.map((preferences: any) => preferences[PREFERENCES_LIST.DATA_SCRIPT_SIZE] || '')
			.map((size: string) => {
				let measure: string[] = (size || '').split(unitSizeSeparator);
				let	width = Number(measure.length &&  measure.shift()) || defaultWidth;
				let height = Number(measure.length &&  measure.shift()) || defaultHeight;
				return { width, height };
			})
			.filter((size: any) =>  size.width !== null && size.height !== null);
	}

	getImportBatchListSizePreference(): Observable<number> {
		return this.getSinglePreference(PREFERENCES_LIST.IMPORT_BATCH_LIST_SIZE).map( result => {
			if (!result || isNaN(result)) {
				return GRID_DEFAULT_PAGE_SIZE;
			}
			return parseInt(result, 0);
		});
	}

}
