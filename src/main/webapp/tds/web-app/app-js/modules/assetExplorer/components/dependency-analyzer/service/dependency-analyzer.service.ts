import {Injectable, OnInit} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';

import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';

/**
 * @name Dependency Analizer Service
 */
@Injectable()
export class DependencyAnalyzerService {
	private readonly baseURL = '/tdstm/ws/dependencyConsole';
	private readonly GRAPH_PREFERENCES = `${this.baseURL}/preferences/`;
	private readonly DEPENDENCY_ANALYZER_URL = `${this.baseURL}`;

	// Resolve HTTP using the constructor
	constructor(private http: HttpClient) {
	}

	/**
	 * Get events list
	 * @returns {Observable<any>}
	 * TODO: @sam please use the previously already implemented getEvents() from task.service.ts.
	 */
	getDependencyAnalyzerData(): Observable<any> {
		return this.http.get(`${this.DEPENDENCY_ANALYZER_URL}`)
			.map((response: any) => {
				return response;
			})
			.catch((error: any) => error);
	}
}
