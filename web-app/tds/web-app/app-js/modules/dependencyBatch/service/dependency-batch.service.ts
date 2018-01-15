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
				filename: 'dep_cmdb.xls',
				records: 20,
				errors: 5,
				pending: 20,
				processed: 0,
				ignored: 0
			},
			{
				id: 2,
				status: 'Pending',
				importedDate: new Date(),
				importedBy: 'David Ontiveros',
				domain: 'Dependencies',
				provider: 'Service Now',
				datascript: 'TM File Import',
				filename: 'dep_cmdb.xls',
				records: 20,
				errors: 5,
				pending: 20,
				processed: 0,
				ignored: 0
			}
		];
		return Observable.of( {data: mockResult} );
	}
}