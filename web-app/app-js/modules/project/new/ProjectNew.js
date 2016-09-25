/**
 * Created by Jorge Morayta on 12/20/2015.
 */
'use strict';

export default class ProjectNew {

    constructor($log, $state) {
        this.log = $log;
        this.module = 'ProjectCreate';
        this.state = $state;
        this.log.debug('Project Create Instanced');

        this.prepareIFrameURL();
    }

    prepareIFrameURL() {
        window.TDSTM.iframeLoader();

        var status = this.state.params.status;

        if (status === 'invalidsize') {
            this.msgStatus = 'Uploaded file exceeds size limit of 50,000 bytes';
            this.type = 'danger';
        }

        this.iframeUrl = '/tdstm/project/create';
    }
}