import { Injectable } from '@angular/core';
import { HttpInterceptor } from '../../../shared/providers/http-interceptor.provider';
import { Observable } from 'rxjs';
import { Response } from '@angular/http';
import { RequestOptions, Headers } from '@angular/http';

@Injectable()
export class DependecyService {
	private assetUrl = '../ws/asset';

	constructor(private http: HttpInterceptor) {

	}

	getDependencies(mainAsset: number, secondaryAsset: number): Observable<any> {
		const headers = new Headers({ 'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8' });
		const options = new RequestOptions({ headers: headers });
		return this.http.post(`${this.assetUrl}/dependencies`, `assetAId=${mainAsset}&assetBId=${secondaryAsset}`, options)
			.map((res: Response) => {
				let result = res.json();
				return result && result.status === 'success' && result.data;
			}).catch((error: any) => error.json());
	}

}