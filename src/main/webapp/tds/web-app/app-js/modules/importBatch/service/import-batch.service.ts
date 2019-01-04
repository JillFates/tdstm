import {HttpInterceptor} from '../../../shared/providers/http-interceptor.provider';
import {Observable} from 'rxjs';
import {Injectable} from '@angular/core';
import {Headers, RequestOptions, Response} from '@angular/http';
import {ImportBatchRecordModel} from '../model/import-batch-record.model';
import {ApiResponseModel} from '../../../shared/model/ApiResponseModel';

@Injectable()
export class ImportBatchService {

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
				return res.json();
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
	 * PATCH - Bulk Archive Import Batches.
	 * @param {number} id
	 * @returns {Observable<any>}
	 */
	archiveImportBatches(ids: Array<number>): Observable<ApiResponseModel> {
		const request = {
			action: 'ARCHIVE',
			ids: ids
		};
		return this.http.patch(`${this.importBatchesUrl}`, JSON.stringify(request))
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
	 * PATCH - Bulk Unarchive Import Batches.
	 * @param {number} id
	 * @returns {Observable<any>}
	 */
	unArchiveImportBatches(ids: Array<number>): Observable<ApiResponseModel> {
		const request = {
			action: 'UNARCHIVE',
			ids: ids
		};
		return this.http.patch(`${this.importBatchesUrl}`, JSON.stringify(request))
			.map((res: Response) => {
				return res.json();
			})
			.catch((error: any) => error.json());
	}

	/**
	 * PATCH - Bulk EJECT Import Batches.
	 * @param {number} id
	 * @returns {Observable<any>}
	 */
	ejectImportBatches(ids: Array<number>): Observable<ApiResponseModel> {
		const request = {
			action: 'EJECT',
			ids: ids
		};
		return this.http.patch(`${this.importBatchesUrl}`, JSON.stringify(request))
			.map((res: Response) => {
				return res.json();
			})
			.catch((error: any) => error.json());
	}

	/**
	 * PATCH - Bulk QUEUE Import Batches.
	 * @param {number} id
	 * @returns {Observable<any>}
	 */
	queueImportBatches(ids: Array<number>): Observable<ApiResponseModel> {
		const request = {
			action: 'QUEUE',
			ids: ids
		};
		return this.http.patch(`${this.importBatchesUrl}`, JSON.stringify(request))
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
	getImportBatchProgress(id: number): Observable<ApiResponseModel> {
		return this.http.get(`${this.importBatchUrl}/${id}/progress`)
			.map((res: Response) => {
				return res.json();
			})
			.catch((error: any) => error.json());
	}

	/**
	 * PATCH - Stops current import batch in progress.
	 * @param {number} id
	 * @returns {Observable<any>}
	 */
	stopImportBatch(ids: Array<number>): Observable<ApiResponseModel> {
		const request = {
			action: 'STOP',
			ids: ids
		};
		return this.http.patch(`${this.importBatchesUrl}`, JSON.stringify(request))
			.map((res: Response) => {
				return res.json();
			})
			.catch((error: any) => error.json());
	}

	/**
	 * GET - List of import batch records for an import batch.
	 * @param {number} id
	 * @returns {Observable<any>}
	 */
	getImportBatchRecords(id: number): Observable<ApiResponseModel> {
		return this.http.get(this.importBatchUrl + `/${id}/records`)
			.map( (res: Response) => {
				return res.json();
			})
			.catch((error: any) => error.json());
	}

	/**
	 * NOTE: Since there's no endpoint to retreive light information we need to make this call below to get all records.
	 * GET - Returns a single batch record of an import batch by id.
	 * @param {number} id
	 * @returns {Observable<any>}
	 */
	getImportBatchRecordUpdated(batchId: number, id: number): Observable<{} | ImportBatchRecordModel> {
		return this.http.get(this.importBatchUrl + `/${batchId}/records`)
			.map( (res: Response) => {
				const batchRecords: Array<ImportBatchRecordModel> = res.json().data;
				let match: ImportBatchRecordModel = batchRecords.find( (item: ImportBatchRecordModel) => {
					return item.id === id;
				});
				return match;
			})
			.catch((error: any) => error.json());
	}

	/**
	 * GET - Batch Record Fields details for a batch record.
	 * @param {number} id
	 * @returns {Observable<any>}
	 */
	getImportBatchRecordFieldDetail(batchId: number, id: number): Observable<ApiResponseModel> {
		return this.http.get(this.importBatchUrl + `/${batchId}/record/${id}`)
			.map( (res: Response) => {
				return res.json();
			})
			.catch((error: any) => error.json());
	}

	/**
	 * PATCH - Ignore Import Batch Records.
	 * @param {number} id
	 * @returns {Observable<any>}
	 */
	ignoreBatchRecords(batchId: number, ids: Array<number>): Observable<ApiResponseModel> {
		const request = {
			action: 'IGNORE',
			ids: ids
		};
		return this.http.patch(this.importBatchUrl + `/${batchId}/records`, JSON.stringify(request))
			.map( (res: Response) => {
				return res.json();
			})
			.catch((error: any) => error.json());
	}

	/**
	 * PATCH - Include Import Batch Records.
	 * @param {number} id
	 * @returns {Observable<any>}
	 */
	includeBatchRecords(batchId: number, ids: Array<number>): Observable<ApiResponseModel> {
		const request = {
			action: 'INCLUDE',
			ids: ids
		};
		return this.http.patch(this.importBatchUrl + `/${batchId}/records`, JSON.stringify(request))
			.map( (res: Response) => {
				return res.json();
			})
			.catch((error: any) => error.json());
	}

	/**
	 * PATCH - Process/Revalidate Import Batch Records.
	 * @param {number} id
	 * @returns {Observable<any>}
	 */
	processBatchRecords(batchId: number, ids: Array<number>): Observable<ApiResponseModel> {
		const request = {
			action: 'PROCESS',
			ids: ids
		};
		return this.http.patch(this.importBatchUrl + `/${batchId}/records`, JSON.stringify(request))
			.map( (res: Response) => {
				return res.json();
			})
			.catch((error: any) => error.json());
	}

	/**
	 * PUT - Updates the batch record fields values of a batch record.
	 * @param {number} id
	 * @returns {Observable<any>}
	 */
	updateBatchRecordFieldsValues(batchId: number, id: number, fieldsValues: Array<{fieldName: string, value: string}>): Observable<any> {
		const request = {
			fieldsInfo: fieldsValues
		};
		return this.http.put(this.importBatchUrl + `/${batchId}/record/${id}`, JSON.stringify(request))
			.map((res: Response) => res.json())
			.catch((error: any) => error.json());
	}
}