import {Injectable, OnInit} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';

import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';

import {PREFERENCES_LIST, PreferenceService} from '../../../shared/services/preference.service';

/**
 * @name InsightService
 */
@Injectable()
export class InsightService {
	private readonly baseURL = '/tdstm';
	private readonly APP_INSIGHT_DATA = `${this.baseURL}/ws/dashboard/insight`;
	private readonly APP_INSIGHT_DATA_FOR_PROVIDERS = `${this.baseURL}/ws/dashboard/insight/provider`;
	private readonly APP_INSIGHT_DATA_FOR_TOP_TAGS = `${this.baseURL}/ws/dashboard/insight/topTags`;
	private readonly APP_INSIGHT_DATA_FOR_APPLICATION_BLAST_RADIOUS = `${this.baseURL}/ws/dashboard/insight/applicationsBlastRadius`;
	private readonly APP_INSIGHT_DATA_ASSETS_BY_OS_AND_ENVIROMENT = `${this.baseURL}/ws/dashboard/insight/assetsByOsAndEnvironment`;
	private readonly APP_INSIGHT_DATA_FOR_DEVICES_BY_EVENT = `${this.baseURL}/ws/dashboard/insight/devicesByEvent`;
	private readonly APP_INSIGHT_DATA_FOR_ASSETS_BY_PROVIDER_AND_ASSET_TYPE = `${this.baseURL}/ws/dashboard/insight/AssetsByProviderAndAssetType`;

	// Resolve HTTP using the constructor
	constructor(private http: HttpClient, private preferenceService: PreferenceService) {
		this.preferenceService.getPreference(PREFERENCES_LIST.CURR_TZ).subscribe();
	}

	/**
	 * Get the data for insight data for dashboard
	 * @param {number} max
	 * @param {number} lowRange Bundle id
	 * @param {number} highRange Bundle id
	 * @returns {Observable<any>} Event insight data
	 */
	getInsightDataForProviders(max = 10): Observable<any> {
		let params = new HttpParams().set('status', status.toString())
				.set('max', max.toString());
		return this.http.get(`${this.APP_INSIGHT_DATA_FOR_PROVIDERS}`, {params})
			.map((response: any) => {
				return response;
			})
			.catch((error: any) => error);
	}

	/**
	 * Get the data for insight data for dashboard
	 * @returns {Observable<any>} Event insight data
	 */
	getInsightDataTopTags(): Observable<any> {
		return this.http.get(`${this.APP_INSIGHT_DATA_FOR_TOP_TAGS}`)
			.map((response: any) => {
				return response;
			})
			.catch((error: any) => error);
	}

	/**
	 * Get the data for insight data for dashboard
	 * @returns {Observable<any>} Event insight data
	 */
	getInsightDataForBlastRadious(): Observable<any> {
		return this.http.get(`${this.APP_INSIGHT_DATA_FOR_APPLICATION_BLAST_RADIOUS}`)
			.map((response: any) => {
				return response;
			})
			.catch((error: any) => error);
	}
	/**
	 * Get the data for insight data for dashboard
	 * @returns {Observable<any>} Event insight data
	 */
	getInsightDataForAssetsByOsAndEnviroment(): Observable<any> {
		return this.http.get(`${this.APP_INSIGHT_DATA_ASSETS_BY_OS_AND_ENVIROMENT}`)
			.map((response: any) => {
				return response;
			})
			.catch((error: any) => error);
	}

	/**
	 * Get the data for insight data for dashboard
	 * @returns {Observable<any>} Event insight data
	 */
	getInsightDataForDevicesByEvent(): Observable<any> {
		return this.http.get(`${this.APP_INSIGHT_DATA_FOR_DEVICES_BY_EVENT}`)
			.map((response: any) => {
				return response;
			})
			.catch((error: any) => error);
	}

	/**
	 * Get the data for insight data for dashboard
	 * @returns {Observable<any>} Event insight data
	 */
	getInsightDataForAssetsByProviderAndAssetType(): Observable<any> {
		return this.http.get(`${this.APP_INSIGHT_DATA_FOR_ASSETS_BY_PROVIDER_AND_ASSET_TYPE}`)
			.map((response: any) => {
				return response;
			})
			.catch((error: any) => error);
	}

}
