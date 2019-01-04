/**
 * Created by Jorge Morayta on 22/07/15.
 */

'use strict';

export default class TaskManagerService {

    constructor($log, RestServiceHandler) {
        this.log = $log;
        this.restService = RestServiceHandler;

        this.log.debug('TaskManagerService Instanced');
    }

    failCall(callback) {
        this.restService.ResourceServiceHandler().getSVG();
    }

    testService(callback) {
        this.restService.TaskServiceHandler().getFeeds((data) => {
            return callback(data);
        });
    }
}

