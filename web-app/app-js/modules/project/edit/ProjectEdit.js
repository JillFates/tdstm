/**
 * Created by Jorge Morayta on 03/21/2016.
 */
'use strict';

export default class ProjectEdit {

    constructor($log, $state) {
        this.log = $log;
        this.state = $state;
        this.module = 'ProjectEdit';

        this.log.debug('Project Edit Instanced');

        this.prepareIFrameURL();
    }

    prepareIFrameURL() {
        this.iframeUrl = 'project/index';
        window.TDSTM.iframeLoader();

        var status = this.state.params.status;

        if (status === 'invalidsize') {
            this.msgStatus = 'Uploaded file exceeds size limit of 50,000 bytes';
            this.type = 'danger';
        }

        this.projectId = this.state.params.id;

        $('#editProjectForm').submit();
    }
}