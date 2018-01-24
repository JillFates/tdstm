import {HttpInterceptor} from '../../../shared/providers/http-interceptor.provider';
import {Observable} from 'rxjs/Observable';
import {Injectable} from '@angular/core';
import {DependencyBatchModel} from '../model/dependency-batch.model';

@Injectable()
export class DependencyBatchService {

	private dependencyBatchUrl = '../ws/dependencybatch';
	private mockRunningBatchFlag = false;

	constructor(private http: HttpInterceptor) {
	}

	getBatchList(): Observable<any> {
		let mockResult: Array<DependencyBatchModel> = [
			{
				id: 1,
				status: 'Pending',
				importedDate: new Date(),
				importedBy: 'David Ontiveros',
				domain: 'Dependencies',
				provider: 'Service Now',
				datascript: 'TM File Import',
				filename: 'load_file_v1.xls',
				records: 20,
				errors: 0,
				pending: 20,
				processed: 0,
				ignored: 0
			},
			{
				id: 2,
				status: 'Pending',
				importedDate: new Date(),
				importedBy: 'TDS Admin',
				domain: 'Dependencies',
				provider: 'TransitionManager',
				datascript: 'TM File Import',
				filename: 'dep_cmdb.xls',
				records: 20,
				errors: 0,
				pending: 3,
				processed: 13,
				ignored: 0
			},
			{
				id: 3,
				status: 'Queued',
				importedDate: new Date(),
				importedBy: 'Auto',
				domain: 'Dependencies',
				provider: 'TransitionManager',
				datascript: 'TM File Import',
				filename: 'dep_cmdb.xls',
				records: 20,
				errors: 0,
				pending: 20,
				processed: 10,
				ignored: 1
			},
			{
				id: 4,
				status: 'Completed',
				importedDate: new Date(),
				importedBy: 'Auto',
				domain: 'Dependencies',
				provider: 'Service Now',
				datascript: 'TM File Import',
				filename: 'dep_cmdb_v2.xls',
				records: 40,
				errors: 3,
				pending: 8,
				processed: 21,
				ignored: 1
			},
			{
				id: 5,
				status: 'Pending',
				importedDate: new Date(),
				importedBy: 'David Ontiveros',
				domain: 'Dependencies',
				provider: 'Service Now',
				datascript: 'TM File Import',
				filename: 'batch_load.xls',
				records: 20,
				errors: 5,
				pending: 20,
				processed: 3,
				ignored: 10
			},
			{
				id: 6,
				status: 'Pending',
				importedDate: new Date(),
				importedBy: 'TDS Admin',
				domain: 'Dependencies',
				provider: 'TransitionManager',
				datascript: 'TM File Import',
				filename: 'batch_load2.xls',
				records: 50,
				errors: 0,
				pending: 10,
				processed: 1,
				ignored: 0
			}
		];
		return Observable.of( mockResult );
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