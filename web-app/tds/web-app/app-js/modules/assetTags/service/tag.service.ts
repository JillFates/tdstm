import {Injectable} from '@angular/core';
import {Observable} from 'rxjs/Observable';
import {ApiResponseModel} from '../../../shared/model/ApiResponseModel';
import {HttpInterceptor} from '../../../shared/providers/http-interceptor.provider';
import {Headers, RequestOptions, Response} from '@angular/http';
import {TagModel} from '../model/tag.model';

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

	/**
	 * GET - Tag by ID
	 * @param {number} tagId
	 * @returns {Observable<any>}
	 */
	getTag(tagId: number): Observable<any> {
		return this.http.get(`${this.tagURL}/${tagId}`)
			.map((res: Response) => {
				return res.json();
			})
			.catch((error: any) => error.json());
	}

	/**
	 * PUT - Save/Update existing tag.
	 * @param {number} tagId
	 * @returns {Observable<any>}
	 */
	updateTag(tagModel: TagModel): Observable<any> {
		const request: any = {
			name: tagModel.Name,
			description: tagModel.Description,
			color: tagModel.Color
		};
		return this.http.put(`${this.tagURL}/${tagModel.id}`, JSON.stringify(request))
			.map((res: Response) => {
				return res.json();
			})
			.catch((error: any) => error.json());
	}

	/**
	 * POST - Creates a new tag.
	 * @param {number} tagId
	 * @returns {Observable<any>}
	 */
	createTag(tagModel: TagModel): Observable<any> {
		const request: any = {
			name: tagModel.Name,
			description: tagModel.Description,
			color: tagModel.Color
		};
		return this.http.post(this.tagURL, JSON.stringify(request))
			.map((res: Response) => {
				return res.json();
			})
			.catch((error: any) => error.json());
	}

	/**
	 * DELETE - Save/Update existing tag.
	 * @param {number} tagId
	 * @returns {Observable<any>}
	 */
	deleteTag(tagId: number): Observable<any> {
		return this.http.delete(`${this.tagURL}/${tagId}`)
			.map((res: Response) => {
				return res.json();
			})
			.catch((error: any) => error.json());
	}

	/**
	 * PUT - Merge two tags into one.
	 * @param {number} tagIdOne
	 * @param {number} tagIdTwo
	 */
	mergeTags(tagIdOne: number, tagIdTwo: number): Observable<any> {
		return this.http.put(`${this.tagURL}/${tagIdOne}/merge/${tagIdTwo}`, null)
			.map((res: Response) => {
				return res.json();
			})
			.catch((error: any) => error.json());
	}

	/**
	 * TODO: document
	 * @returns {Array<string>}
	 */
	getTagColorList(): Array<any> {
		return [
			{id: 'Brown', css: 'tag-brown'},
			{id: 'Red', css: 'tag-red'},
			{id: 'Orange', css: 'tag-orange'},
			{id: 'Yellow', css: 'tag-yellow'},
			{id: 'Green', css: 'tag-green'},
			{id: 'Cyan', css: 'tag-cyan'},
			{id: 'Blue', css: 'tag-blue'},
			{id: 'Purple', css: 'tag-purple'},
			{id: 'Pink', css: 'tag-pink'}
		];
	}
}