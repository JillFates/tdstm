import {HttpClient, HttpHeaders} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {DeviceManufacturer} from '../components/device/manufacturer/model/device-manufacturer.model';

import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';

@Injectable()
export class ManufacturerService {
	private manufacturerUrl = '/tdstm/manufacturer';
	constructor(private http: HttpClient) {}

	isValidAlias(alias: string, id: number, parentName: string): Observable<string> {
		const url = `${this.manufacturerUrl}/validateAliasForForm?alias=${alias}&id=${id}&parentName=${parentName}` ;

		return this.http.get(url, {responseType: 'text'})
			.map((res: any) => res)
	}

	getDeviceManufacturer(id: string): Observable<DeviceManufacturer> {
		const url = `${this.manufacturerUrl}/retrieveManufacturerAsJSON?id=${id}`;
		return this.http.post(url, '')
			.map((response: any) => response)
			.map((res: any) => res && Object.assign({aka: res.aliases || '', akaCollection: res.akaCollection}, res.manufacturer) || {})
			.catch((error: any) => error);
	}

	getManufacturerPayload(manufacturer: DeviceManufacturer, isDelete: boolean): string {
		let body = `id=${manufacturer.id}`;
		body += `&name=${manufacturer.name}`;
		body += `&description=${manufacturer.description || ''}`;
		body += `&corporateName=${manufacturer.corporateName  || ''}`;
		body += `&corporateLocation=${manufacturer.corporateLocation  || ''}`;
		body += `&website=${manufacturer.website || ''}`;
		const {edited, deleted, added} = manufacturer.akaChanges;

		if (edited.length) {
			body += edited
				.map(aka => `&aka_${aka.id}=${aka.name}`)
				.join('');
		}

		if (deleted.length) {
			body += '&deletedAka=';
			body += deleted
				.map(aka => aka.id)
				.join(',')
		}

		if (added.length) {
			body += added
				.map(aka => `&aka=${aka.name}`)
				.join('')
		}

		if (isDelete) {
			body += '&_action_Delete=Delete';
		}

		return body;

	}

	updateManufacturer(manufacturer: DeviceManufacturer, isDelete: boolean): Observable<any> {
		const body = this.getManufacturerPayload(manufacturer, isDelete);

		const headers = new HttpHeaders({'Content-Type': 'application/x-www-form-urlencoded'});

		const url = `${this.manufacturerUrl}/update`;
		return this.http.post(url, body, {headers: headers, responseType: 'text'})
			.map((res: any) => res.ok)
			.catch((error: any) => error);
	}
}
