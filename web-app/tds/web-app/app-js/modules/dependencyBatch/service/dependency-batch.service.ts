import {HttpInterceptor} from '../../../shared/providers/http-interceptor.provider';
import {Observable} from 'rxjs/Observable';
import {Injectable} from '@angular/core';
import {Headers, RequestOptions, Response} from '@angular/http';
import {ImportBatchModel} from '../model/import-batch.model';
import {ImportBatchRecordModel} from '../model/import-batch-record.model';
import {ApiReponseModel} from '../../../shared/model/ApiReponseModel';

@Injectable()
export class DependencyBatchService {

	private readonly importBatchesUrl = '../ws/import/batches';
	private readonly importBatchUrl = '../ws/import/batch';
	private readonly batchProgressUrl = '../ws/progress';

	constructor(private http: HttpInterceptor) {
	}

	/**
	 * GET - List of all Import Batches
	 * @returns {Observable<any>}
	 */
	getImportBatches(): Observable<any> {
		return this.http.get(this.importBatchesUrl)
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
		return this.http.delete(this.importBatchesUrl, options)
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
		return this.http.delete(`${this.importBatchesUrl}/${id}`)
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
		return this.http.put(`${this.importBatchesUrl}/archive/${id}`, null)
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
		return this.http.put(`${this.importBatchesUrl}/archive`, JSON.stringify(request))
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
		return this.http.put(`${this.importBatchesUrl}/unarchive/${id}`, null)
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
		return this.http.put(`${this.importBatchesUrl}/unarchive`, JSON.stringify(request))
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

	// private readonly mockBatchRecords: Array<ImportBatchRecordModel> = [
	// 	{
	// 		id: 1, status: 'Pending', errorCount: 1, operation: 'Update', sourceRowId: 1,
	// 		currentValues: {
	// 			'Name (P)': 'pwebwp01',
	// 			'Type (P)': 'VM',
	// 			'Dep Type (P)': 'VM Runs On',
	// 			'Name (D)': 'VMClusterPCI01',
	// 			'Type (D)': 'Application'
	// 		}
	// 	},
	// 	{
	// 		id: 2, status: 'Pending', errorCount: 2, operation: 'Add', sourceRowId: 2,
	// 		currentValues: {
	// 			'Name (P)': 'Batch Reporting',
	// 			'Type (P)': 'Application',
	// 			'Dep Type (P)': 'File',
	// 			'Name (D)': 'Ecommerce',
	// 			'Type (D)': 'Application'
	// 		}
	// 	},
	// 	{
	// 		id: 3, status: 'Pending', errorCount: 1, operation: 'Undetermined', sourceRowId: 3,
	// 		currentValues: {
	// 			'Name (P)': 'Batch Reporting',
	// 			'Type (P)': 'Application',
	// 			'Dep Type (P)': 'DB',
	// 			'Name (D)': null,
	// 			'Type (D)': null
	// 		}
	// 	},
	// 	{
	// 		id: 4, status: 'Ignored', errorCount: 2, operation: 'Add', sourceRowId: 4,
	// 		currentValues: {
	// 			'Name (P)': 'Online Banking',
	// 			'Type (P)': 'Application',
	// 			'Dep Type (P)': 'Web Service',
	// 			'Name (D)': 'RSA SecureID SaaS',
	// 			'Type (D)': 'Application'
	// 		}
	// 	}
	// 	// {id: 5, status: 'Completed', errorCount: 0, operation: 'Add', sourceRow: 5, name: 'Azure ADSync', type: 'Application', depType: 'Runs On', nameD: 'usmd1nis015', typeD: 'VM'}
	// ];

	/**
	 * TODO: document.
	 * @param {number} id
	 * @returns {Observable<any>}
	 */
	getImportBatchRecords(id: number): Observable<ApiReponseModel> {
		return this.http.get(this.importBatchUrl + `/${id}/records`)
			.map( (res: Response) => {
				return res.json();
			})
			.catch((error: any) => error.json());
	}

	/**
	 * TODO: document.
	 * @param {number} id
	 * @returns {Observable<any>}
	 */
	getImportBatchRecordFieldDetail(batchId: number, id: number): Observable<ApiReponseModel> {
		return this.http.get(this.importBatchUrl + `/${batchId}/record/${id}`)
			.map( (res: Response) => {
				return res.json();
			})
			.catch((error: any) => error.json());
		// const matchedRecord = this.mockBatchRecords.find( record => record.id === id);
		// const mockResult: Array<any> = [];
		// Object.keys(matchedRecord.currentValues).forEach(key => {
		// 	mockResult.push({name: key, currentValue: '', importValue: matchedRecord.fields[key], error: ''});
		// });
		// return Observable.of(mockResult);
	}
}