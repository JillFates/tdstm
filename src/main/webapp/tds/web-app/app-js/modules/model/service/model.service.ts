import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {ModelModel} from '../model/model.model';

@Injectable()
export class ModelService {
	private modelUrl = '/tdstm/model';
	constructor(private http: HttpClient) {	}

	getModels(): Observable<ModelModel[]> {
		const url = `${this.modelUrl}/listJson?sord=ASC`;

		return this.http.get(url)
			.map((response: any) => {
				let modelModels = response && response.status === 'success' && response.data.rows;
				modelModels.forEach((r) => {
					r.lastModified = ((r.lastModified) ? new Date(r.lastModified) : '');
				});
				return modelModels;
			})
			.catch((error: any) => error);
	}

	saveModel(model: ModelModel): Observable<ModelModel> {
		const postRequest = {
			name: model.name,
		};
		if (!model.id) {
			return this.http.post(`${this.modelUrl}/save`, JSON.stringify(postRequest))
				.map((response: any) => {
					return response && response.status === 'success' && response.data;
				})
				.catch((error: any) => error);
		} else {
			return this.http.put(`${this.modelUrl}/update/${model.id}`, JSON.stringify(postRequest))
				.map((response: any) => {
					return response && response.status === 'success' && response.data;
				})
				.catch((error: any) => error);
		}
	}
}