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
	private readonly baseURL = '/tdstm/ws/architectureGraph';
	private readonly GRAPH_PREFERENCES = `${this.baseURL}/preferences/`;
	private readonly ARCHITECTURE_GRAPH_LEGEND = `${this.baseURL}/legend/`;
	private readonly ARCHITECTURE_GRAPH_DATA = `${this.baseURL}`;

	// Resolve HTTP using the constructor
	constructor(private http: HttpClient) {
	}

	/**
	 * Get events list
	 * @returns {Observable<any>}
	 * TODO: @sam please use the previously already implemented getEvents() from task.service.ts.
	 */
	getArchitectureGraphData(assetId: number, levelsUp: number, levelsDown: number, mode: string): Observable<any> {
		let params;
		params = new HttpParams()
			.set('assetId', String(assetId))
			.set('levelsUp', String(levelsUp))
			.set('levelsDown', String(levelsDown))
			.set('mode', mode);
		return this.http.get(`${this.ARCHITECTURE_GRAPH_DATA}`, {params})
			.map((response: any) => {
				return response;
			})
			.catch((error: any) => error);
	}

	getArchitectureGraphPreferences(): Observable<any> {
		return this.http.get(`${this.GRAPH_PREFERENCES}`)
			.map( (response: any) => {
				return response || [];
			})
			.catch( (error: any) => error);
	}
}
