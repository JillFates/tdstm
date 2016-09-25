/**
 * Created by Jorge Morayta on 3/15/2016.
 */

'use strict';

export default class ProjectList {

    constructor($log, $state) {
        this.log = $log;
        this.module = 'Project List Instanced';
        this.state = $state;
        this.prepareIFrameURL();
        this.log.debug('Project List Instanced');
    }

    /**
     * Prepare the iframe to map active/completed project
     * depending the status, it also modify the UI to proper show the status
     */
    prepareIFrameURL() {
        window.TDSTM.iframeLoader();

        var page = {},
            status = this.state.params.status,
            projectName = this.state.params.project,
            index = 0;

        // if status doesn't exist, show active by default
        if (!status) {
            status = 'active';
        }

        // Handle the UI for each status type
        if (this.state.current && this.state.current.data) {
            page = this.state.current.data.page;
            index = page.menu.length - 1;

            if (status === 'active') {
                page.instruction = 'ACTIVE';
                page.menu[index] = 'ACTIVE';
            } else if (status === 'completed') {
                page.instruction = 'COMPLETED';
                page.menu[index] = 'COMPLETED';
            } else if (status === 'deleted') {
                page.instruction = 'ACTIVE';
                page.menu[index] = 'ACTIVE';
                status = 'active';
                if (!projectName) {
                    projectName = '';
                }
                this.msgStatus = 'Project ' + projectName + ' deleted';
                this.type = 'info';
            }
        }

        this.iframeUrl = '/tdstm/project/list?active=' + status;
    }

}
