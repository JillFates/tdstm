import {Component, Injectable} from '@angular/core';
import {HttpEvent, HttpEventType, HttpHandler, HttpInterceptor, HttpProgressEvent, HttpRequest, HttpResponse} from '@angular/common/http';
import {Observable} from 'rxjs/Observable';
import 'rxjs/add/operator/delay';
import 'rxjs/add/observable/concat';
import {DataIngestionService} from '../../modules/dataIngestion/service/data-ingestion.service';
import {FileRestrictions} from '@progress/kendo-angular-upload';
import {
	ASSET_IMPORT_FILE_UPLOAD_TYPE,
	ETL_SCRIPT_FILE_UPLOAD_TYPE, FILE_UPLOAD_REMOVE_URL, FILE_UPLOAD_SAVE_URL,
	FILE_UPLOAD_TYPE_PARAM,
	REMOVE_FILENAME_PARAM, SAVE_FILENAME_PARAM
} from '../model/constants';

/**
 * Mainly used by Kendo Upload Component.
 * @see Kendo Upload Component docs.
 */

@Injectable()
export class KendoFileUploadInterceptor implements HttpInterceptor {

	constructor(private dataIngestionService: DataIngestionService) {}

	intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
		if (req.url === FILE_UPLOAD_SAVE_URL) {
			let events: Observable<HttpEvent<any>>[] = [100].map((x) => Observable.of(<HttpProgressEvent>{
				type: HttpEventType.UploadProgress,
				loaded: x,
				total: 100
			}).delay(1000));
			if (req.body.get(FILE_UPLOAD_TYPE_PARAM) === ETL_SCRIPT_FILE_UPLOAD_TYPE) {
				events.push(this.dataIngestionService.uploadETLScriptFile(req.body));
			} else if (req.body.get(FILE_UPLOAD_TYPE_PARAM) === ASSET_IMPORT_FILE_UPLOAD_TYPE) {
				events.push(this.dataIngestionService.uploadAssetImportFile(req.body));
			} else {
				events.push(this.dataIngestionService.uploadFile(req.body));
			}
			return Observable.concat(...events);
		}
		if (req.url === FILE_UPLOAD_REMOVE_URL) {
			let filename = req.body.get(REMOVE_FILENAME_PARAM);
			return this.dataIngestionService.deleteFile(filename);
		}
	}
}

export class KendoFileUploadBasicConfig {

	uploadRestrictions: FileRestrictions;
	uploadSaveUrl: string;
	uploadDeleteUrl: string;
	autoUpload: boolean;
	saveField: string;
	removeField: string;
	multiple: boolean;
	[x: string]: any; // this enables the model to add any extra property as needed.

	constructor() {
		this.uploadRestrictions = { allowedExtensions: ['csv', 'xml', 'json', 'xlsx', 'xls'] };
		this.uploadSaveUrl = FILE_UPLOAD_SAVE_URL;
		this.uploadDeleteUrl = FILE_UPLOAD_REMOVE_URL;
		this.autoUpload = false;
		this.saveField =  SAVE_FILENAME_PARAM;
		this.removeField = REMOVE_FILENAME_PARAM;
		this.multiple = false;
	}
}