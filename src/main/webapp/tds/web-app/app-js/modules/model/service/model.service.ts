import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {ModelModel} from '../model/model.model';

@Injectable()
export class ModelService {
	private modelUrl = '/tdstm/model';
	private wsModelUrl = '../ws/model'
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

	getModelDetails(modelId: number): Observable<ModelModel> {
		return this.http.get(`${this.modelUrl}/show?id=${modelId}`)
			.map((response: any) => {
				return response;
			})
			.catch((error: any) => error);
	}

	deleteModel(id: number): Observable<string> {
		return this.http.delete(`${this.wsModelUrl}/${id}`)
			.map((response: any) => {
				return response && response.status === 'success' && response.data;
			})
			.catch((error: any) => error);
	}

	saveModel(model: ModelModel): Observable<ModelModel> {
		const postRequest = {
			id: model.id,
			modelName: model.modelName,
			manufacturer: (model.manufacturer['id']) ? {id: model.manufacturer['id']} : {id: model.manufacturer},
			assetType: model.assetType,
			usize: model.usize,
			weight: model.weight,
			productLine: model.productLine,
			endOfLifeDate: model.endOfLifeDate,
			cpuType: model.cpuType,
			cpuCount: model.cpuCount,
			powerNameplate: model.powerNameplate,
			powerDesign: model.powerDesign,
			powerUse: model.powerUse,
			roomObject: model.roomObject,
			height: model.height,
			width: model.width,
			depth: model.depth,
			layoutStyle: model.layoutStyle,
			endOfLifeStatus: model.endOfLifeStatus,
			modelFamily: model.modelFamily,
			memorySize: model.memorySize,
			storageSize: model.storageSize,
			description: model.description,
			sourceTDS: (model.sourceTDS) ? 1 : 0,
			sourceURL: model.sourceURL,
			modelStatus: model.modelStatus,
			modelConnectors: model.modelConnectors,
			removedConnectors: model.removedConnectors,
			connectorCount: model.connectorCount,
			aka: {
				deleted: model.akaChanges.deleted,
				edited: model.akaChanges.edited,
				added: model.akaChanges.added
			},
			useImage: model.useImage,
			connectors: {added: model.modelConnectors, edited: [], deleted: model.removedConnectors}
		};
		if (!model.id) {
			return this.http.post(`${this.wsModelUrl}`, JSON.stringify(postRequest))
				.map((response: any) => {
					return response && response.status === 'success' && response.data;
				})
				.catch((error: any) => error);
		} else {
			return this.http.put(`${this.wsModelUrl}`, JSON.stringify(postRequest))
				.map((response: any) => {
					return response && response.status === 'success' && response.data;
				})
				.catch((error: any) => error);
		}
	}

	getPreData(): Observable<any> {
		return this.http.get(`${this.modelUrl}/create`)
			.map((response: any) => {
				return response && response.status === 'success' && response.data;
			})
			.catch((error: any) => error);
	}
}