/**
 * Created by Jorge Morayta on 3/24/2016.
 *
 */


'use strict';

export default class ActivationEmailView {

    constructor($log, $state) {
        this.log = $log;
        this.module = 'Activation Email View';
        this.state = $state;
        this.prepareIFrameURL();
        this.log.debug('ActivationEmailView instanced');
    }

    /**
     * Prepare the iframe to map active/completed project
     * depending the status, it also modify the UI to proper show the status
     */
    prepareIFrameURL() {
        window.TDSTM.iframeLoader();

        this.iframeUrl = 'project/userActivationEmailsForm';
    }

}
