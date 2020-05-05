import {HttpClient, HttpParams, HttpResponse} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';

import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';
import {ManufacturerModel} from '../model/manufacturer.model';
import {IManufacturerModelsExportData} from '../model/manufacturer-models-export-data.model';

@Injectable()
export class ManufacturerService {
	private manufacturerUrl = '/tdstm/manufacturer';
	private manufacturerExportUrl = '/tdstm/ws/model/export';
	constructor(private http: HttpClient) {}

	getManufacturerList(): Observable<ManufacturerModel[]> {
		const url = `${this.manufacturerUrl}/listJson?sord=ASC`;
		return this.http.get(url)
			.map((response: any) => response.data.rows)
			.catch((error: any) => error);
	}

	deleteManufacturer(id: number): Observable<string> {
		const body = {
			id
		}
		return this.http.post(`${this.manufacturerUrl}/delete/${id}`, body)
			.map((response: any) => {
				return response && response.status === 'success' && response.data;
			})
			.catch((error: any) => error);
	}

	saveManufacturer(model: ManufacturerModel, aliasAdded: any, aliasDeleted: any, aliasUpdated: any): Observable<ManufacturerModel> {
		const postRequest = {
			name: model.name,
			description: model.description,
			corporateName: model.corporateName,
			corporateLocation: model.corporateLocation,
			website: model.website,
			alias: model.alias,
			aliasAdded,
			aliasDeleted,
			aliasUpdated
		};
		if (!model.id) {
			return this.http.post(`${this.manufacturerUrl}/save`, JSON.stringify(postRequest))
				.map((response: any) => {
					return response && response.status === 'success' && response.data;
				})
				.catch((error: any) => error);
		} else {
			return this.http.put(`${this.manufacturerUrl}/update/${model.id}`, JSON.stringify(postRequest))
				.map((response: any) => {
					return response && response.status === 'success' && response.data;
				})
				.catch((error: any) => error);
		}
	}

	validateUniquenessManufacturerByName(model: ManufacturerModel): Observable<ManufacturerModel> {
		let postRequest = {};

		if (model.id) {
			postRequest['id'] = model.id;
		}

		if (model.name) {
			postRequest['name'] = model.name;
		}

		return this.http.post(`${this.manufacturerUrl}/validateUniqueName`, JSON.stringify(postRequest))
			.map((response: any) => {
				return response && response.status === 'success' && response.data;
			})
			.catch((error: any) => error);
	}

	validateUniquenessManufacturerAliasByName(manufacturerId: number, manufacturerName: string, alias: string): Observable<ManufacturerModel> {
		let postRequest = {
			id: manufacturerId,
			name: manufacturerName,
			alias
		};

		return this.http.post(`${this.manufacturerUrl}/validateUniqueAlias`, JSON.stringify(postRequest))
			.map((response: any) => {
				return response && response.status === 'success' && response.data;
			})
			.catch((error: any) => error);
	}

	getManufacturerModelExportData(tdsModelsOnly: boolean): Observable<HttpResponse<IManufacturerModelsExportData>> {
		const params = new HttpParams().set('onlyTdsModels', `${tdsModelsOnly}`);
		return this.http.get<IManufacturerModelsExportData>(this.manufacturerExportUrl, {params, observe: 'response'});
	}
}
