import {Injectable, OnInit} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';

import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';
import {DependencyAnalyzerDataModel} from '../model/dependency-analyzer-data.model';

/**
 * @name Dependency Analizer Service
 */
@Injectable()
export class DependencyAnalyzerService {
	private readonly baseURL = '/tdstm/ws/dependencyConsole';
	private readonly DEPENDENCY_BUNDLE_DETAILS = `${this.baseURL}/dependencyBundleDetails/`;
	private readonly DEPENDENCY_ANALYZER_URL = `${this.baseURL}`;

	// Resolve HTTP using the constructor
	constructor(private http: HttpClient) {
	}

	/**
	 * Get DA Data
	 * @returns {Observable<any>}
	 */
	getDependencyAnalyzerData(): Observable<DependencyAnalyzerDataModel> {
		return this.http.get(`${this.DEPENDENCY_ANALYZER_URL}`)
			.map((response: any) => {
				return response;
			})
			.catch((error: any) => error);
	}

	/**
	 * Get filtered data with bundles and tags
	 * @returns {Observable<any>}
	 */
	getFilteredData(data): Observable<DependencyAnalyzerDataModel> {
		return this.http.post(`${this.DEPENDENCY_BUNDLE_DETAILS}`, data)
			.map((response: any) => {
				return response;
			})
			.catch((error: any) => error);
	}
}
