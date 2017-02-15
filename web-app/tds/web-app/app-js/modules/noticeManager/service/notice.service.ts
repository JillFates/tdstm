import { Injectable }     from '@angular/core';
import { Http, Response, Headers, RequestOptions } from '@angular/http';
import { NoticeModel }           from '../model/notice.model';
import {Observable} from 'rxjs/Rx';

// Import RxJs required methods
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';

@Injectable()
export class NoticeService {
    // Resolve HTTP using the constructor
    constructor (private http: Http) {}
    // private instance variable to hold base url
    private commentsUrl = 'http://localhost:3000/api/comments';

}