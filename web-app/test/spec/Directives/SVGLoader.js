/**
 * Created by Jorge Morayta on 1/25/2016.
 */

import SVGLoader from '../../../app-js/directives/svg/svgLoader.js';
import AppSVGShapes from '../../../app-js/Assets/AppSVGShapes.js';

describe("SVGLoaderDirective", function () {

    var appShapes,
        $compile,
        $rootScope,
        element,
        directiveInit = '<svg-loader svg-data="appShapes.shape.application.svg" > </svg-loader>';

    // Load the general module first
    beforeEach(angular.mock.module('TDSTM'));

    beforeEach(inject(function (_$compile_, _$rootScope_) {
        $compile = _$compile_;
        $rootScope = _$rootScope_;

        appShapes = new AppSVGShapes();

        // We add the property to the scope, so we can use it along the test
        $rootScope.appShapes = appShapes;
        $rootScope.$digest();

        element = $compile(directiveInit)($rootScope);
    }));

    it("Directive should be ", function () {
        expect(element.html()).not.toContain(directiveInit);
    });

    it("It Should display the SVG Content", function () {
        expect(element.html()).toContain('<svg ');
    });

    it("It Should display the SVG Id correctly", function () {
        expect(element.html()).toContain(appShapes.shape.application.id);
    });


});
