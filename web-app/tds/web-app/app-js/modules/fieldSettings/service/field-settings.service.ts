import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Rx';
import { DomainModel } from '../model/domain.model';
import { FieldSettingsData } from './field-settings-mock.data';
@Injectable()
export class FieldSettingsService {

	getAssetClassFieldSetting(): Observable<DomainModel[]> {
		return Observable.from(FieldSettingsData).bufferCount(FieldSettingsData.length);
	}
}