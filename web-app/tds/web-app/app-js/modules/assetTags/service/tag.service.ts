import {Injectable} from '@angular/core';
import {Observable} from 'rxjs/Observable';
import {ApiResponseModel} from '../../../shared/model/ApiResponseModel';
import {HttpInterceptor} from '../../../shared/providers/http-interceptor.provider';
import {Headers, RequestOptions, Response} from '@angular/http';

@Injectable()
export class TagService {

	private readonly tagURL = '../ws/tag';

	constructor(private http: HttpInterceptor) {}

	/**
	 * GET - List of Tags
	 * @returns {Observable<any>}
	 */
	getTags(): Observable<any> {
		return this.http.get(this.tagURL)
			.map((res: Response) => {
				return res.json();
			})
			.catch((error: any) => error.json());
	}

	getTag(tagId: number): Observable<any> {
		return this.http.get(`${this.tagURL}/${tagId}`)
			.map((res: Response) => {
				return res.json();
			})
			.catch((error: any) => error.json());
	}
}