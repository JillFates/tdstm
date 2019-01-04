/**
 * Created by Jorge Morayta on 1/17/15.
 * When Angular invokes the link function, it is no longer in the context of the class instance, and therefore all this will be undefined
 * This structure will avoid those issues.
 */

'use strict'

// Controller is being used to Inject all Dependencies
class SVGLoaderController {
    constructor($log) {
        this.$log = $log;
    }
}

SVGLoaderController.$inject = ['$log'];

// Directive
class SVGLoader {

    constructor(){
        this.restrict = 'E';
        this.controller = SVGLoaderController;
        this.scope = {
            svgData: '='
        };
    }

    link(scope, element, attrs, ctrl) {
        element.html(scope.svgData);
    }

    static directive() {
        return new SVGLoader();
    }
}


export default SVGLoader.directive;
