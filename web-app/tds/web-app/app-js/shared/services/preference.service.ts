import { Injectable } from '@angular/core';
import { Response, RequestOptions, Headers } from '@angular/http';
import { Observable } from 'rxjs';
import { HttpInterceptor } from '../providers/http-interceptor.provider';

import {catchError, map} from 'rxjs/operators';

import {DateUtils} from '../utils/date.utils';
import {GRID_DEFAULT_PAGE_SIZE} from '../model/constants';

// add constants as needed
export const PREFERENCES_LIST = {
	ASSET_JUST_PLANNING: 'assetJustPlanning',
	ASSET_LIST_SIZE : 'assetListSize',
	VIEW_MANAGER_DEFAULT_SORT: 'viewManagerDefaultSort',
	CURRENT_DATE_FORMAT: 'CURR_DT_FORMAT',
	CURR_TZ: 'CURR_TZ',
	DATA_SCRIPT_SIZE: 'DataScriptSize',
	VIEW_UNPUBLISHED: 'viewUnpublished',
	IMPORT_BATCH_LIST_SIZE: 'ImportBatchListSize',
	IMPORT_BATCH_RECORDS_FILTER: 'ImportBatchRecordsFilter'
};

@Injectable()
export class PreferenceService {

	private preferenceUrl = '../ws/user/preferences';
	private preferenceUrlPost = '../ws/user/preference';

	// TODO: Refactor to be an Observable rather than a public map
	public preferences: any = {};

	constructor(private http: HttpInterceptor) {
	}

	// query a set of user preferences passed as arg variables
	getPreferences(...preferencesCodes: string[]): Observable <any> {
		return this.getPreference(preferencesCodes.join(','));
	}

	getPreference(preferenceCode: string): Observable<any> {
		return this.http.get(`${this.preferenceUrl}/${preferenceCode}`)
			.pipe(map((res: Response) => {
				let response = res.json();
				Object.keys(response.data.preferences).forEach((key) => {
					this.preferences[key] = response.data.preferences[key];
				});
				return this.preferences;
			}))
			.pipe(catchError((error: any) => Observable.throw(error.json() || 'Server error')));
	}

	getSinglePreference(preferenceCode: string): Observable<any> {
		return this.http.get(`${this.preferenceUrl}/${preferenceCode}`)
			.pipe(map((res: Response) => {
				let response = res.json();
				return response.data.preferences[preferenceCode];
			}))
			.pipe(catchError((error: any) => Observable.throw(error.json() || 'Server error')));
	}

	setPreference(preferenceCode: string, value: string): Observable<any>  {
		const headers = new Headers();
		headers.append('Content-Type', 'application/x-www-form-urlencoded');
		const requestOptions = new RequestOptions({headers: headers});

		const body = `code=${preferenceCode}&value=${value}`;
		return this.http.post(this.preferenceUrlPost, body, requestOptions);
	}

	/**
	 * Used to retrieve the format to use for Date properies based on user's preference (e.g. MM/dd/YYYY)
	 */
	getUserDateFormat(): string {
		const currentUserDateFormat = this.preferences[PREFERENCES_LIST.CURRENT_DATE_FORMAT];
		if (currentUserDateFormat) {
			return DateUtils.translateTimeZoneFormat(currentUserDateFormat);
		}
		return DateUtils.DEFAULT_FORMAT_DATE;
	}
	getUserDateFormatForMomentJS(): string {
		const currentUserDateFormat = this.preferences[PREFERENCES_LIST.CURRENT_DATE_FORMAT];
		return currentUserDateFormat
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
		return this.preferences[PREFERENCES_LIST.CURR_TZ];
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
		const defaultWidth = 580;
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

	getImportBatchListSizePreference(): Observable<number> {
		return this.getSinglePreference(PREFERENCES_LIST.IMPORT_BATCH_LIST_SIZE)
			.pipe(map(result => {
				if (!result || isNaN(result)) {
					return GRID_DEFAULT_PAGE_SIZE;
				}
				return parseInt(result, 0);
			}));
	}

	/**
	 * Used to retrieve the user preference current date format
	 */
	getUserCurrentDateFormatOrDefault(): string {
		const userDateFormat = this.preferences[PREFERENCES_LIST.CURRENT_DATE_FORMAT];

		return userDateFormat ? userDateFormat : DateUtils.DEFAULT_FORMAT_DATE;
	}
}
