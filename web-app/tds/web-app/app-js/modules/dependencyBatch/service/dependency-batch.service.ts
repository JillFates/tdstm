import {HttpInterceptor} from '../../../shared/providers/http-interceptor.provider';
import {Observable} from 'rxjs/Observable';
import {Injectable} from '@angular/core';
import {DependencyBatchModel} from '../model/dependency-batch.model';

@Injectable()
export class DependencyBatchService {

	private dependencyBatchUrl = '../ws/dependencybatch';

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
				status: 'Processing',
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
				errors: 5,
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
				errors: 5,
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
				status: 'Processing',
				importedDate: new Date(),
				importedBy: 'TDS Admin',
				domain: 'Dependencies',
				provider: 'TransitionManager',
				datascript: 'TM File Import',
				filename: 'batch_load2.xls',
				records: 50,
				errors: 2,
				pending: 10,
				processed: 1,
				ignored: 0
			}
		];
		return Observable.of( {data: mockResult} );
	}
}