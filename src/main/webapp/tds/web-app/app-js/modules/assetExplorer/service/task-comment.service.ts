import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {HttpClient} from '@angular/common/http';

import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';

@Injectable()
export class TaskCommentService {

	private assetExplorerUrl = '../assetEntity/listComments';

	constructor(private http: HttpClient) {
	}

	searchComments(assetId: number, commentType: string): Observable<any> {
		return this.http.get(`${this.assetExplorerUrl}/${assetId}?commentType=${commentType}`)
			.map((response: any) => response)
			.catch((error: any) => error);
	}

}