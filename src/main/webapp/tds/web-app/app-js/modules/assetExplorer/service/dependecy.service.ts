import {Injectable} from '@angular/core';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {Observable} from 'rxjs';

@Injectable()
export class DependecyService {
	private assetUrl = '../ws/asset';

	constructor(private http: HttpClient) {

	}

	/**
	 * Get the dependencies for the main and the secondary asset
	 * @param {number} mainAsset asset id
	 * @param {number} secondaryAsset Secondary asset id
	 * @return {Observable<any>)
	 */
	getDependencies(mainAsset: number, secondaryAsset: number): Observable<any> {
		const headers = new HttpHeaders({ 'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8' });
		return this.http.post(`${this.assetUrl}/dependencies`, `assetAId=${mainAsset}&assetBId=${secondaryAsset}`, { headers: headers })
			.map((response: any) => {
				return response && response.status === 'success' && response.data;
			}).catch((error: any) => error);
	}

	/**
	 * Update an asset dependency
	 * @param {any} asset Asset to be updated
	 * @return {Observable<any>)
	 */
	updateDependency(asset: any): Observable<any> {
		const headers = new HttpHeaders({ 'Content-Type': 'application/json; charset=UTF-8' });

		return this.http.put(`${this.assetUrl}/dependencies`, JSON.stringify(asset), { headers: headers })
			.map((response: any) => {
				return (response && response.status === 'success');
			}).catch((error: any) => error);
	}

	/**
	 * Delete an asset dependency
	 * @param {any} asset Asset to be deleted
	 * @return {Observable<any>)
	 */
	deleteDependency(asset: any): Observable<any> {
		const httpOptions = {
			headers: new HttpHeaders({ 'Content-Type': 'application/json' }), body: JSON.stringify(asset)
		};

		return this.http.delete(`${this.assetUrl}/dependencies`, httpOptions)
			.map((response: any) => {
				return (response && response.status === 'success');
			}).catch((error: any) => error);
	}
}