/**
 * Created by Jorge Morayta on 12/20/2015.
 */
'use strict';

export default class ProjectView {

    constructor($log, $state) {
        this.log = $log;
        this.state = $state;
        this.module = 'ProjectView';

        this.log.debug('Project View Instanced');

        this.prepareIFrameURL();
    }

    prepareIFrameURL() {
        window.TDSTM.iframeLoader();

        var status = this.state.params.status;

        if (status === 'created') {
            this.msgStatus = 'Project was successfully created';
            this.type = 'info';
        }

        if (status === 'updated') {
            this.msgStatus = 'Project was successfully updated';
            this.type = 'info';
        }

        if (status === 'logodeleted') {
            this.msgStatus = 'Project logo was deleted';
            this.type = 'info';
        }

        if (status === 'logonotfound') {
            this.msgStatus = 'Project logo was not found';
            this.type = 'info';
        }

        this.projectId = this.state.params.id;

        this.iframeUrl = '/tdstm/project/show/' + this.projectId;
    }
}