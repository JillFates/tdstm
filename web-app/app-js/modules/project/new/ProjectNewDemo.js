/**
 * Created by Jorge Morayta on 12/20/2015.
 */
'use strict';

export default class ProjectNewDemo {

    constructor($log, $stateParams) {
        this.log = $log;
        this.module = 'ProjectCreateDemo';

        this.log.debug('Project Create Demo Instanced');

        this.prepareIFrameURL();
    }

    prepareIFrameURL() {
        window.TDSTM.iframeLoader();
        this.iframeUrl = '/tdstm/projectUtil/createDemo';
    }
}