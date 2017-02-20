import {Injectable}     from '@angular/core';
import {Response, Headers, RequestOptions} from '@angular/http';
import {HttpInterceptor} from '../../../shared/providers/http-interceptor.provider'
import {NoticeModel}           from '../model/notice.model';
import {Observable} from 'rxjs/Rx';

// Import RxJs required methods
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';

@Injectable()
export class NoticeService {

    // private instance variable to hold base url
    private noticeListUrl = '../../ws/notices';

    // Resolve HTTP using the constructor
    constructor(private http: HttpInterceptor) {
    }

    getNoticesList(): Observable<NoticeModel[]> {
        return this.http.get(this.noticeListUrl)
            .map((res: Response) => res.json())
            .catch((error: any) => Observable.throw(error.json().error || 'Server error'));

    }

}