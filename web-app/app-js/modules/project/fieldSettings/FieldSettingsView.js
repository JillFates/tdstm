/**
 * Created by Jorge Morayta on 3/15/2016.
 */

'use strict';

export default class FieldSettingsView {

    constructor($log, $state) {
        this.log = $log;
        this.module = 'Field Settings Instanced';
        this.state = $state;
        this.prepareIFrameURL();
        this.log.debug('Field Settings Instanced');
    }

    /**
     * Prepare the iframe to map active/completed project
     * depending the status, it also modify the UI to proper show the status
     */
    prepareIFrameURL() {
        window.TDSTM.iframeLoader();

        this.projectId = this.state.params.id;

        $('#fieldSettingsIframe').submit();
    }

}
