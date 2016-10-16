/**
 * Created by Jorge Morayta on 10/07/16.
 */

'use strict';

export default class NoticeManagerService {

    constructor($log, restServiceHandler) {
        this.log = $log;
        this.restService = restServiceHandler;

        this.TYPE = {
            '1': 'Prelogin',
            '2': 'Postlogin',
            '3': 'General'
        };

        this.log.debug('NoticeManagerService Instanced');
    }

    getNoticeList(callback) {
        this.restService.noticeManagerServiceHandler().getNoticeList((data) => {
            var noticeList = [];
            try {
                // Verify the List returns what we expect and we convert it to an Array value
                if(data && data.notices) {
                    noticeList = data.notices;
                    if (noticeList && noticeList.length > 0) {
                        for (var i = 0; i < noticeList.length; i = i + 1) {
                            noticeList[i].type = {
                                id: noticeList[i].typeId,
                                name: this.TYPE[noticeList[i].typeId]
                            };
                            delete noticeList[i].typeId;
                        }
                    }
                }
            } catch(e) {
                this.log.error('Error parsing the Notice List', e);
            }
            return callback(noticeList);
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

