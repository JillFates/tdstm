import { Injectable }     from '@angular/core';
import { Http, Response, Headers, RequestOptions } from '@angular/http';
import { NoticeModel }           from '../model/notice.model';
import {Observable} from 'rxjs/Rx';

// Import RxJs required methods
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';

@Injectable()
export class NoticeService {

    // private instance variable to hold base url
    private noticeListUrl = '../../ws/notices';

    // Resolve HTTP using the constructor
    constructor (private http: Http) {}

    getNoticesList() : Observable<NoticeModel[]> {

        // ...using get request
        return this.http.get(this.noticeListUrl)
            // ...and calling .json() on the response to return data
            .map((res:Response) => res.json())
            //...errors if any
            .catch((error:any) => Observable.throw(error.json().error || 'Server error'));

    }

}