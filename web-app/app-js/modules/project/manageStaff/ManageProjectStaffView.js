/**
 * Created by Jorge Morayta on 3/24/2016.
 *
 */


'use strict';

export default class ManageProjectStaffView {

    constructor($log, $state) {
        this.log = $log;
        this.module = 'Manage Project Staff View';
        this.state = $state;
        this.prepareIFrameURL();
        this.log.debug('ManageProjectStaffView instanced');
    }

    /**
     * Prepare the iframe to map active/completed project
     * depending the status, it also modify the UI to proper show the status
     */
    prepareIFrameURL() {
        window.TDSTM.iframeLoader();

        this.iframeUrl = 'person/manageProjectStaff';
    }

}
