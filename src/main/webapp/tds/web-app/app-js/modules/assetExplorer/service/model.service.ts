import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {DeviceModel} from '../components/device/model-device/model/device-model.model';

import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';

@Injectable()
export class ModelService {
	private modelUrl = '/tdstm/model';

	constructor(private http: HttpClient) {
	}

	/**
	 * Get the model needed to display the model view
	 * @param {string} id
	 * @returns {Observable<DeviceModel>}
	 */
	getModelAsJSON(id: string): Observable<DeviceModel> {
		const url = `${this.modelUrl}/retrieveModelAsJSON?id=${id}`;

		return this.http.post(url, '')
			.map((response: any) => response)
			.catch((error: any) => error);
	}

	/**
	 * Call the endpoint to set the model power type
	 * @param {string} unit
	 * @returns {Observable<any>}
	 */
	setPower(unit: string): Observable<any> {
		const url = `${this.modelUrl}/../project/setPower?p=${unit}`;

		return this.http.post(url, '', {responseType: 'text'})
		.map((response: any) => response)
		.catch((error: any) => error);
	}

	/**
	 * Call the endpoint in charge of determing if the alias is valid
	 * @param {string} alias
	 * @param {number} id
	 * @param {string} parentName
	 * @returns {Observable<boolean>}
	 */
	isValidAlias(alias: string, id: number, parentName: string): Observable<boolean> {
		const url = `/tdstm/manufacturer/validateAliasForForm?alias=${alias}&id=${id}&parentName=${parentName}` ;

		return this.http.get(url)
		.map((res: any) => res === 'valid')
	}

	/**
	 * Get the manufacturer information searching by id
	 * @param {string} id
	 * @returns {Observable<any>}
	 */
	getDeviceManufacturer(id: string): Observable<any> {
		const url = '/tdstm/manufacturer/retrieveManufacturerAsJSON?id=' + id;
		return this.http.post(url, '')
		.map((res: Response) => res.json())
		.map((res: any) => res && Object.assign({aka: res.aliases || ''}, res.manufacturer) || {})
		.catch((error: any) => error.json());
	}

	/**
	 * Delete a model
	 * @param {string} id Model id to delete
	 * @returns {Observable<any>}
	 */
	deleteModel(id: string): Observable<any> {
		const url = `${this.modelUrl}/../ws/model/${id}`;

		return this.http.delete(url)
		.catch((error: any) => error);
	}

	/**
	 * Update the model information
	 * @param payload
	 * @returns {Observable<any>}
	 */
	updateModel(payload: any): Observable<any> {
		const url = `${this.modelUrl}/../ws/model`;

		return this.http.post(url, payload)
			.catch((error: any) => error);
	}
}