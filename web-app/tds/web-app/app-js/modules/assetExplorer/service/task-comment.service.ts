import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Response } from '@angular/http';
import { HttpInterceptor } from '../../../shared/providers/http-interceptor.provider';

import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';

@Injectable()
export class TaskCommentService {

	private assetExplorerUrl = '../assetEntity/listComments';

	constructor(private http: HttpInterceptor) {
	}

	searchComments(assetId: number, commentType: string): Observable<any> {
		return this.http.get(`${this.assetExplorerUrl}/${assetId}?commentType=${commentType}`)
			.map((res: Response) => res.json())
			.catch((error: any) => error.json());
	}

}