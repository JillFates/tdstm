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

    /**
     * Create a New Notice passing params
     * @param notice
     * @param callback
     */
    createNotice(notice, callback){
        this.restService.noticeManagerServiceHandler().createNotice(notice, (data) => {
            return callback(data);
        });
    }

    /**
     * Notice should have the ID in order to edit the Notice
     * @param notice
     * @param callback
     */
    editNotice(notice, callback){
        this.restService.noticeManagerServiceHandler().editNotice(notice, (data) => {
            return callback(data);
        });
    }

    /**
     * Notice should have the ID in order to delete the notice
     * @param notice
     * @param callback
     */
    deleteNotice(notice, callback) {
        this.restService.noticeManagerServiceHandler().deleteNotice(notice, (data) => {
            return callback(data);
        });
    }

}

