import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Rx';
import { FieldSettingsModel } from '../model/field-settings.model';

@Injectable()
export class FieldSettingsService {
	mockData: FieldSettingsModel[] = [
		{
			key: 'assetType',
			label: 'Asset Type',
			help: 'This is a blurb about the property',
			shared: false,
			importance: 'C',
			required: false,
			display: true,
			type: 'String',
			length: 20,
			default: 'Server'
		},
		{
			key: 'custom1',
			label: 'NIC Interface',
			help: 'The primary interface to connect to',
			shared: false,
			importance: 'C',
			required: false,
			display: true,
			type: 'String',
			default: 'Unknown',
			control: 'Select',
			controlOpt: [
				'Unknown',
				'eth0',
				'eth1',
				'eth2',
				'en0',
				'en1',
				'en2',
				'other'
			]
		},
		{
			key: 'custom2',
			label: 'ServiceNow Status',
			help: 'Is this asset in the ServiceNow catalog',
			shared: true,
			importance: 'I',
			required: false,
			display: true,
			type: 'String',
			default: 'Unknown',
			control: 'YesNo'
		},
		{
			key: 'custom3',
			label: 'CPU Count',
			help: 'The number of CPUs in the server',
			shared: false,
			importance: 'I',
			required: false,
			display: true,
			type: 'Number',
			default: 0,
			control: 'Number',
			controlOpt: {
				min: 1,
				max: 64
			}
		}
	];

	getFieldSettings(): Observable<FieldSettingsModel[]> {
		return Observable.from(this.mockData).bufferCount(4);
	}
}