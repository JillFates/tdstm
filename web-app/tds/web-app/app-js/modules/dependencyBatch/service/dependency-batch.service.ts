import {HttpInterceptor} from '../../../shared/providers/http-interceptor.provider';
import {Observable} from 'rxjs/Observable';
import {Injectable} from '@angular/core';
import { Response } from '@angular/http';
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
		// return this.http.get(this.importBatchUrl)
		// 	.map( (res: Response) => {
		// 		return res.json();
		// 	})
		// 	.catch((error: any) => error.json());

		// let mockResult: Array<ImportBatchModel> = [
		// 	{
		// 		id: 1,
		// 		status: 'Pending',
		// 		dateCreated: new Date(),
		// 		createdBy: 'David Ontiveros',
		// 		domainClassName: 'Dependencies',
		// 		provider: 'Service Now',
		// 		datascript: 'TM File Import',
		// 		filename: 'load_file_v1.xls',
		// 		records: 20,
		// 		errors: 0,
		// 		pending: 20,
		// 		processed: 0,
		// 		ignored: 0
		// 	},
		// 	{
		// 		id: 2,
		// 		status: 'Pending',
		// 		importedDate: new Date(),
		// 		importedBy: 'TDS Admin',
		// 		domain: 'Dependencies',
		// 		provider: 'TransitionManager',
		// 		datascript: 'TM File Import',
		// 		filename: 'dep_cmdb.xls',
		// 		records: 20,
		// 		errors: 0,
		// 		pending: 3,
		// 		processed: 13,
		// 		ignored: 0
		// 	},
		// 	{
		// 		id: 3,
		// 		status: 'Queued',
		// 		importedDate: new Date(),
		// 		importedBy: 'Auto',
		// 		domain: 'Dependencies',
		// 		provider: 'TransitionManager',
		// 		datascript: 'TM File Import',
		// 		filename: 'dep_cmdb.xls',
		// 		records: 20,
		// 		errors: 0,
		// 		pending: 20,
		// 		processed: 10,
		// 		ignored: 1
		// 	},
		// 	{
		// 		id: 4,
		// 		status: 'Completed',
		// 		importedDate: new Date(),
		// 		importedBy: 'Auto',
		// 		domain: 'Dependencies',
		// 		provider: 'Service Now',
		// 		datascript: 'TM File Import',
		// 		filename: 'dep_cmdb_v2.xls',
		// 		records: 40,
		// 		errors: 3,
		// 		pending: 8,
		// 		processed: 21,
		// 		ignored: 1
		// 	},
		// 	{
		// 		id: 5,
		// 		status: 'Pending',
		// 		importedDate: new Date(),
		// 		importedBy: 'David Ontiveros',
		// 		domain: 'Dependencies',
		// 		provider: 'Service Now',
		// 		datascript: 'TM File Import',
		// 		filename: 'batch_load.xls',
		// 		records: 20,
		// 		errors: 5,
		// 		pending: 20,
		// 		processed: 3,
		// 		ignored: 10
		// 	},
		// 	{
		// 		id: 6,
		// 		status: 'Pending',
		// 		importedDate: new Date(),
		// 		importedBy: 'TDS Admin',
		// 		domain: 'Dependencies',
		// 		provider: 'TransitionManager',
		// 		datascript: 'TM File Import',
		// 		filename: 'batch_load2.xls',
		// 		records: 50,
		// 		errors: 0,
		// 		pending: 10,
		// 		processed: 1,
		// 		ignored: 0
		// 	}
		// ];

		return Observable.of( [] );
	}

	/**
	 * DELETE - Bulk Delete all Import Batches
	 * @returns {Observable<any>}
	 */
	deleteImportBatches(): Observable<any> {
		return this.http.delete(this.importBatchUrl)
			.map( (res: Response) => {
				// response: [status: 'success|error', deleted: true]
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
	 * POST - Archives a single Import Batch.
	 * @param {number} id
	 * @returns {Observable<any>}
	 */
	archiveImportBatch(id: number): Observable<any> {
		return this.http.post(`${this.importBatchUrl}/archive/${id}`, null)
			.map((res: Response) => {
				return res.json();
			})
			.catch((error: any) => error.json());
	}

	/**
	 * POST - Bulk Archive Import Batches.
	 * @param {number} id
	 * @returns {Observable<any>}
	 */
	archiveImportBatches(id: number): Observable<any> {
		return this.http.post(`${this.importBatchUrl}/archive`, null)
			.map((res: Response) => {
				return res.json();
			})
			.catch((error: any) => error.json());
	}

	/**
	 * POST - Un-Archives a single Import Batch.
	 * @param {number} id
	 * @returns {Observable<any>}
	 */
	unArchiveImportBatch(id: number): Observable<any> {
		return this.http.post(`${this.importBatchUrl}/unarchive/${id}`, null)
			.map((res: Response) => {
				return res.json();
			})
			.catch((error: any) => error.json());
	}

	/**
	 * POST - Bulk Unarchive Import Batches.
	 * @param {number} id
	 * @returns {Observable<any>}
	 */
	unArchiveImportBatches(id: number): Observable<any> {
		return this.http.post(`${this.importBatchUrl}/unarchive`, null)
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

	getArchivedBatchList(): Observable<any> {
		// let mockResult: Array<ImportBatchModel> = [
		// 	{
		// 		id: 1,
		// 		status: 'Completed',
		// 		importedDate: new Date(),
		// 		importedBy: 'David Ontiveros',
		// 		domain: 'Dependencies',
		// 		provider: 'Service Now',
		// 		datascript: 'TM File Import',
		// 		filename: 'load_file_v1.xls',
		// 		records: 30,
		// 		errors: 0,
		// 		pending: 0,
		// 		processed: 28,
		// 		ignored: 2
		// 	},
		// 	{
		// 		id: 2,
		// 		status: 'Completed',
		// 		importedDate: new Date(),
		// 		importedBy: 'TDS Admin',
		// 		domain: 'Dependencies',
		// 		provider: 'TransitionManager',
		// 		datascript: 'TM File Import',
		// 		filename: 'dep_cmdb.xls',
		// 		records: 21,
		// 		errors: 0,
		// 		pending: 0,
		// 		processed: 20,
		// 		ignored: 1,
		// 	}
		// ];
		return Observable.of( [] );
	}

	startBatch(batchId: number): Observable<any> {
		// The endpoint /ws/import/process/$ID will be called
		let mockResult = {};
		if ( !this.mockRunningBatchFlag ) {
			mockResult = { status: 'success', data: {} };
			this.mockRunningBatchFlag = true;
		} else {
			mockResult = { status: 'error', error: 'Another batch is already running ...' };
		}
		return Observable.of( mockResult );
	}

	stopBatch(batchId: number): Observable<any> {
		this.mockRunningBatchFlag = false;
		return Observable.of( { status: 'success', data: {}} );
	}
}