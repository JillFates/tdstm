import {HttpInterceptor} from '../../../shared/providers/http-interceptor.provider';
import {Observable} from 'rxjs/Observable';
import {Injectable} from '@angular/core';
import {Headers, RequestOptions, Response} from '@angular/http';
import {ImportBatchModel} from '../model/import-batch.model';

@Injectable()
export class DependencyBatchService {

	private readonly importBatchUrl = '../ws/import/batch';
	private readonly batchProgressUrl = '../ws/progress';
	private mockRunningBatchFlag = false;

	constructor(private http: HttpInterceptor) {
	}

	/**
	 * GET - List of all Import Batches
	 * @returns {Observable<any>}
	 */
	getImportBatches(): Observable<any> {
		return this.http.get(this.importBatchUrl)
			.map( (res: Response) => {
				return res.json();
			})
			.catch((error: any) => error.json());
	}

	/**
	 * DELETE - Bulk Delete all Import Batches
	 * @returns {Observable<any>}
	 */
	deleteImportBatches(ids: Array<number>): Observable<any> {
		let body = JSON.stringify({ids: ids} );
		const headers = new Headers({ 'Content-Type': 'application/json' });
		let options = new RequestOptions({
			headers: headers,
			body : body
		});
		return this.http.delete(this.importBatchUrl, options)
			.map( (res: Response) => {
				return res.json();
			})
			.catch((error: any) => error.json());
	}

	/**
	 * GET - Find a single import batch by id.
	 * @param {number} id
	 * @returns {Observable<any>}
	 */
	getImportBatch(id: number): Observable<any> {
		return this.http.get(`${this.importBatchUrl}/${id}`)
			.map( (res: Response) => {
				return res;
			})
			.catch((error: any) => error.json());
	}

	/**
	 * DELETE - Delete a single import batch by id.
 	 * @param {number} id
	 * @returns {Observable<any>}
	 */
	deleteImportBatch(id: number): Observable<any> {
		return this.http.delete(`${this.importBatchUrl}/${id}`)
			.map( (res: Response) => {
				return res.json();
			})
			.catch((error: any) => error.json());
	}

	/**
	 * PUT - Archives a single Import Batch.
	 * @param {number} id
	 * @returns {Observable<any>}
	 */
	archiveImportBatch(id: number): Observable<any> {
		return this.http.put(`${this.importBatchUrl}/archive/${id}`, null)
			.map((res: Response) => {
				return res.json();
			})
			.catch((error: any) => error.json());
	}

	/**
	 * PUT - Bulk Archive Import Batches.
	 * @param {number} id
	 * @returns {Observable<any>}
	 */
	archiveImportBatches(ids: Array<number>): Observable<any> {
		const request = {
			ids: ids
		};
		return this.http.put(`${this.importBatchUrl}/archive`, JSON.stringify(request))
			.map((res: Response) => {
				return res.json();
			})
			.catch((error: any) => error.json());
	}

	/**
	 * PUT - Un-Archives a single Import Batch.
	 * @param {number} id
	 * @returns {Observable<any>}
	 */
	unArchiveImportBatch(id: number): Observable<any> {
		return this.http.put(`${this.importBatchUrl}/unarchive/${id}`, null)
			.map((res: Response) => {
				return res.json();
			})
			.catch((error: any) => error.json());
	}

	/**
	 * PUT - Bulk Unarchive Import Batches.
	 * @param {number} id
	 * @returns {Observable<any>}
	 */
	unArchiveImportBatches(ids: Array<number>): Observable<any> {
		const request = {
			ids: ids
		};
		return this.http.put(`${this.importBatchUrl}/unarchive`, JSON.stringify(request))
			.map((res: Response) => {
				return res.json();
			})
			.catch((error: any) => error.json());
	}

	/**
	 * GET - Get current import batch progress.
	 * @param {number} id
	 * @returns {Observable<any>}
	 */
	getImportBatchProgress(id: number): Observable<any> {
		return this.http.get(`${this.batchProgressUrl}/${id}`)
			.map((res: Response) => {
				return res.json();
			})
			.catch((error: any) => error.json());
	}

	/**
	 * DELETE - Stops current import batch in progress.
	 * @param {number} id
	 * @returns {Observable<any>}
	 */
	stopImportBatch(id: number): Observable<any> {
		return this.http.delete(`${this.batchProgressUrl}/${id}`)
			.map((res: Response) => {
				return res.json();
			})
			.catch((error: any) => error.json());
	}
}