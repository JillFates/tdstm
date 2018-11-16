import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
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
	getTags(): Observable<ApiResponseModel> {
		return this.http.get(this.tagURL)
			.map((res: Response) => {
				let jsonResult = res.json();
				let models: Array<TagModel> = jsonResult && jsonResult.status === ApiResponseModel.API_SUCCESS && jsonResult.data;
				models = models.sort( (item1, item2) => this.sortTagsByColorThenByName(item1, item2));
				models.forEach((model: TagModel) => {
					model.dateCreated = ((model.dateCreated) ? new Date(model.dateCreated) : null);
					model.lastModified = ((model.lastModified) ? new Date(model.lastModified) : null);
				});
				return jsonResult;
			})
			.catch((error: any) => error.json());
	}

	/**
	 * GET - Tag by ID
	 * @param {number} tagId
	 * @returns {Observable<any>}
	 */
	getTag(tagId: number): Observable<ApiResponseModel> {
		return this.http.get(`${this.tagURL}/${tagId}`)
			.map((res: Response) => {
				return res.json();
			})
			.catch((error: any) => error.json());
	}

	/**
	 * GET - Tag by Move Bundle Id
	 * @param {number} moveBundleId
	 * @returns {Observable<any>}
	 */
	getTagByMoveBundleId(moveBundleId: number): Observable<ApiResponseModel> {
		return this.http.get(`${this.tagURL}?moveBundleId=${moveBundleId}`)
			.map((res: Response) => {
				let result = res.json();
				return result && result.status === 'success' && result.data;
			})
			.catch((error: any) => error.json());
	}

	/**
	 * GET - Tag by Move Event Id
	 * @param {number} moveEventId
	 * @returns {Observable<any>}
	 */
	getTagByMoveEventId(moveEventId: number): Observable<ApiResponseModel> {
		return this.http.get(`${this.tagURL}?moveEventId=${moveEventId}`)
			.map((res: Response) => {
				let result = res.json();
				return result && result.status === 'success' && result.data;
			})
			.catch((error: any) => error.json());
	}

	/**
	 * PUT - Save/Update existing tag.
	 * @param {number} tagId
	 * @returns {Observable<any>}
	 */
	updateTag(tagModel: TagModel): Observable<ApiResponseModel> {
		const request: any = {
			name: tagModel.name,
			description: tagModel.description ? tagModel.description : '',
			color: tagModel.color
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
	createTag(tagModel: TagModel): Observable<ApiResponseModel> {
		const request: any = {
			name: tagModel.name,
			description: tagModel.description ? tagModel.description : '',
			color: tagModel.color
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
	deleteTag(tagId: number): Observable<ApiResponseModel> {
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
	mergeTags(tagIdFrom: number, tagIdTo: number): Observable<ApiResponseModel> {
		return this.http.put(`${this.tagURL}/${tagIdTo}/merge/${tagIdFrom}`, null)
			.map((res: Response) => {
				return res.json();
			})
			.catch((error: any) => error.json());
	}

	/**
	 * GET - Get the list of all tags linked/associated to a particular asset.
	 * @returns {Observable<ApiResponseModel>}
	 */
	getAssetTags(assetId: number): Observable<ApiResponseModel> {
		return this.http.get(`${this.tagURL}/asset/${assetId}`)
			.map((res: Response) => {
				let result = res.json();
				if (result.data) {
					let data = result.data.map(item => {
						let tagModel: any = {};
						tagModel.id = item.tagId;
						tagModel.name = item.name;
						tagModel.description = item.description;
						tagModel.color = item.color;
						tagModel.css = item.css;
						tagModel.dateCreated = item.dateCreated;
						tagModel.assetTagId = item.id;
						return tagModel;
					});
					// sort tags by color then by name.
					result.data = data.sort((item1, item2) => this.sortTagsByColorThenByName(item1, item2));
				}
				return result;
			})
			.catch((error: any) => error.json());
	}

	/**
	 * Sort function to sort an array of tags by "css-color" then by "Name"
	 * @returns {number}
	 */
	private sortTagsByColorThenByName(item1: TagModel, item2: TagModel): number {
		if (item1.css < item2.css) { return -1 };
		if (item1.css > item2.css) { return 1 };
		if (item1.name.toUpperCase() < item2.name.toUpperCase()) { return -1 };
		if (item1.name.toUpperCase() > item2.name.toUpperCase()) { return 1 };
	}

	/**
	 * POST, DELETE - Creates and Deletes Assets Tags in a fork join operation.
	 * @returns {Observable<any>}
	 */
	createAndDeleteAssetTags(assetId: number, tagIdsToAdd: Array<number>, idsToDelete: Array<number>): Observable<any> {
		let operations = [];
		if (tagIdsToAdd.length > 0) {
			operations.push(this.createAssetTags(assetId, tagIdsToAdd));
		}
		if (idsToDelete.length > 0) {
			operations.push(this.deleteAssetTags(idsToDelete));
		}
		return Observable.forkJoin(operations);
	}

	/**
	 * POST - Associate tags to a particular asset.
	 * @returns {Observable<ApiResponseModel>}
	 */
	createAssetTags(assetId: number, tagIds: Array<number>): Observable<ApiResponseModel> {
		const request = {
			'tagIds': tagIds,
			'assetId': assetId
		};
		return this.http.post(`${this.tagURL}/asset`, JSON.stringify(request))
			.map((res: Response) => {
				return res.json();
			})
			.catch((error: any) => error.json());
	}

	/**
	 * DELETE - Associate tags to a particular asset.
	 * @returns {Observable<ApiResponseModel>}
	 */
	deleteAssetTags(idsToDelete: Array<number>): Observable<ApiResponseModel> {
		let body = JSON.stringify({
			ids: idsToDelete
		});
		let headers = new Headers({ 'Content-Type': 'application/json' });
		let options = new RequestOptions({
			headers: headers,
			body : body
		});
		return this.http.delete(`${this.tagURL}/asset`, options)
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
			{id: 'Grey', css: 'tag-grey'},
			{id: 'Red', css: 'tag-red'},
			{id: 'Orange', css: 'tag-orange'},
			{id: 'Yellow', css: 'tag-yellow'},
			{id: 'Green', css: 'tag-green'},
			{id: 'Cyan', css: 'tag-cyan'},
			{id: 'Blue', css: 'tag-blue'},
			{id: 'Purple', css: 'tag-purple'},
			{id: 'Pink', css: 'tag-pink'},
			{id: 'White', css: 'tag-white'}
		];
	}
}