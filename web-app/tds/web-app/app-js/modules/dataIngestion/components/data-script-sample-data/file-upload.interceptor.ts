import {Component, Injectable} from '@angular/core';
import {HttpEvent, HttpEventType, HttpHandler, HttpInterceptor, HttpProgressEvent, HttpRequest, HttpResponse} from '@angular/common/http';
import {Observable} from 'rxjs/Observable';
import {DataIngestionService} from '../../service/data-ingestion.service';

@Injectable()
export class FileUploadInterceptor implements HttpInterceptor {

	constructor(private dataIngestionService: DataIngestionService) {}

	intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
		if (req.url === 'saveUrl') {
			console.log(req);
			let events: Observable<HttpEvent<any>>[] = [100].map((x) => Observable.of(<HttpProgressEvent>{
				type: HttpEventType.UploadProgress,
				loaded: x,
				total: 100
			}).delay(1000));

			const uploadFileEvent = this.dataIngestionService.uploadFile('whatever..');
			events.push(uploadFileEvent);
			return Observable.concat(...events);
		}
		// return next.handle(req);
	}
}