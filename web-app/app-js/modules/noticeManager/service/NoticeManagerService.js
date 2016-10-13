/**
 * Created by Jorge Morayta on 10/07/16.
 */

'use strict';

export default class NoticeManagerService {

    constructor($log, restServiceHandler) {
        this.log = $log;
        this.restService = restServiceHandler;

        this.log.debug('NoticeManagerService Instanced');
    }

    getNoticeList(callback) {
        this.restService.noticeManagerServiceHandler().getNoticeList((data) => {
            return callback(data);
        });
    }

    createNotice(notice, callback){
        // Process New License data if necessary (add, remove, etc)
        this.restService.noticeManagerServiceHandler().createNotice(notice, (data) => {
            return callback(data);
        });
    }

    editNotice(notice, callback){
        // Process New License data if necessary (add, remove, etc)
        this.restService.noticeManagerServiceHandler().editNotice(notice, (data) => {
            return callback(data);
        });
    }
}

