/**
 * Created by Jorge Morayta on 12/2/2015.
 * Header Controller manage the view available on the state.data
 * ----------------------
 * Header Controller
 * Page title                      Home -> Layout - Sub Layout
 *
 * Module Controller
 * Content
 * --------------------
 *
 */
'use strict';

export default class HeaderController {

    constructor($log, $state) {
        this.log = $log
        this.state = $state;

        this.pageMetaData = {
            title: '',
            instruction: '',
            menu: []
        };

        this.prepareHeader();
        this.log.debug('Header Controller Instanced');
    }

    /**
     * Verify if we have a menu to show to made it available to the View
     */
    prepareHeader() {
        if (this.state && this.state.$current && this.state.$current.data) {
            this.pageMetaData = this.state.$current.data.page;
            document.title = this.pageMetaData.title;
        }
    }

}