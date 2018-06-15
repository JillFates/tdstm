import {Component, Injectable} from '@angular/core';
import {HttpEvent, HttpEventType, HttpHandler, HttpInterceptor, HttpProgressEvent, HttpRequest, HttpResponse} from '@angular/common/http';
import {Observable} from 'rxjs/Observable';
import 'rxjs/add/operator/delay';
import 'rxjs/add/observable/concat';
import {DataIngestionService} from '../../modules/dataIngestion/service/data-ingestion.service';
import {FileRestrictions} from '@progress/kendo-angular-upload';

/**
 * Mainly used by Kendo Upload Component.
 * @see Kendo Upload Component docs.
 */

@Injectable()
export class KendoFileUploadInterceptor implements HttpInterceptor {

	public static readonly SAVE_URL = 'saveUrl';
	public static readonly REMOVE_URL = 'removeUrl';

	constructor(private dataIngestionService: DataIngestionService) {}

	intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
		if (req.url === KendoFileUploadInterceptor.SAVE_URL) {
			let events: Observable<HttpEvent<any>>[] = [100].map((x) => Observable.of(<HttpProgressEvent>{
				type: HttpEventType.UploadProgress,
				loaded: x,
				total: 100
			}).delay(1000));
			events.push(this.dataIngestionService.uploadFile(req.body));
			return Observable.concat(...events);
		}
		if (req.url === KendoFileUploadInterceptor.REMOVE_URL) {
			let filename = req.body.get(KendoFileUploadBasicConfig.REMOVE_FIELD);
			return this.dataIngestionService.deleteFile(filename);
		}
	}
}

export class KendoFileUploadBasicConfig {

	public static readonly REMOVE_FIELD = 'filename';

	uploadRestrictions: FileRestrictions;
	uploadSaveUrl: string;
	uploadDeleteUrl: string;
	autoUpload: boolean;
	saveField: string;
	removeField: string;
	multiple: boolean;
	[x: string]: any; // this enables the model to add any extra property as needed.

	constructor() {
		this.uploadRestrictions = { allowedExtensions: ['csv', 'txt', 'xml', 'json', 'xlsx', 'xls'] };
		this.uploadSaveUrl = KendoFileUploadInterceptor.SAVE_URL;
		this.uploadDeleteUrl = KendoFileUploadInterceptor.REMOVE_URL;
		this.autoUpload = false;
		this.saveField = 'file';
		this.removeField = KendoFileUploadBasicConfig.REMOVE_FIELD;
		this.multiple = false;
	}
}