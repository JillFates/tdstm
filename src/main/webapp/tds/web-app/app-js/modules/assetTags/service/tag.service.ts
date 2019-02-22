import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {ApiResponseModel} from '../../../shared/model/ApiResponseModel';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {TagModel} from '../model/tag.model';

@Injectable()
export class TagService {

	private readonly tagURL = '../ws/tag';

	constructor(private http: HttpClient) {}

	/**
	 * GET - List of Tags
	 * @returns {Observable<any>}
	 */
	getTags(): Observable<ApiResponseModel> {
		return this.http.get(this.tagURL)
			.map((response: any) => {
				let models: Array<TagModel> = response && response.status === ApiResponseModel.API_SUCCESS && response.data;
				models = models.sort( (item1, item2) => this.sortTagsByColorThenByName(item1, item2));
				models.forEach((model: TagModel) => {
					model.dateCreated = ((model.dateCreated) ? new Date(model.dateCreated) : null);
					model.lastModified = ((model.lastModified) ? new Date(model.lastModified) : null);
				});
				return response;
			})
			.catch((error: any) => error);
	}

	/**
	 * GET - Tag by ID
	 * @param {number} tagId
	 * @returns {Observable<any>}
	 */
	getTag(tagId: number): Observable<ApiResponseModel> {
		return this.http.get(`${this.tagURL}/${tagId}`)
			.map((response: any) => response)
			.catch((error: any) => error);
	}

	/**
	 * GET - Tag by Move Bundle Id
	 * @param {number} moveBundleId
	 * @returns {Observable<any>}
	 */
	getTagByMoveBundleId(moveBundleId: number): Observable<ApiResponseModel> {
		return this.http.get(`${this.tagURL}?moveBundleId=${moveBundleId}`)
			.map((response: any) => {
				return response && response.status === 'success' && response.data;
			})
			.catch((error: any) => error);
	}

	/**
	 * GET - Tag by Move Event Id
	 * @param {number} moveEventId
	 * @returns {Observable<any>}
	 */
	getTagByMoveEventId(moveEventId: number): Observable<ApiResponseModel> {
		return this.http.get(`${this.tagURL}?moveEventId=${moveEventId}`)
			.map((response: any) => {
				return response && response.status === 'success' && response.data;
			})
			.catch((error: any) => error);
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
			.map((response: any) => response)
			.catch((error: any) => error);
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
			.map((response: any) => response)
			.catch((error: any) => error);
	}

	/**
	 * DELETE - Save/Update existing tag.
	 * @param {number} tagId
	 * @returns {Observable<any>}
	 */
	deleteTag(tagId: number): Observable<ApiResponseModel> {
		return this.http.delete(`${this.tagURL}/${tagId}`)
			.map((response: any) => response)
			.catch((error: any) => error);
	}

	/**
	 * PUT - Merge two tags into one.
	 * @param {number} tagIdOne
	 * @param {number} tagIdTwo
	 */
	mergeTags(tagIdFrom: number, tagIdTo: number): Observable<ApiResponseModel> {
		return this.http.put(`${this.tagURL}/${tagIdTo}/merge/${tagIdFrom}`, null)
			.map((response: any) => response)
			.catch((error: any) => error);
	}

	/**
	 * GET - Get the list of all tags linked/associated to a particular asset.
	 * @returns {Observable<ApiResponseModel>}
	 */
	getAssetTags(assetId: number): Observable<ApiResponseModel> {
		return this.http.get(`${this.tagURL}/asset/${assetId}`)
			.map((response: any) => {
				if (response.data) {
					let data = response.data.map(item => {
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
					response.data = data.sort((item1, item2) => this.sortTagsByColorThenByName(item1, item2));
				}
				return response;
			})
			.catch((error: any) => error);
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
			.map((response: any) => response)
			.catch((error: any) => error);
	}

	/**
	 * DELETE - Associate tags to a particular asset.
	 * @returns {Observable<ApiResponseModel>}
	 */
	deleteAssetTags(idsToDelete: Array<number>): Observable<ApiResponseModel> {
		const httpOptions = {
			headers: new HttpHeaders({'Content-Type': 'application/json'}), body: JSON.stringify({ids: idsToDelete})
		};

		return this.http.delete(`${this.tagURL}/asset`, httpOptions)
			.map((response: any) => response)
			.catch((error: any) => error);
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