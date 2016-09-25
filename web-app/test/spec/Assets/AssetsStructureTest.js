/**
 * Created by Jorge Morayta on 1/18/2016.
 * It Test the general structure of each one of the Asset/foo.js generated
 * Since I can't read folders, is necessary to add it manually
 */

import AppSVGShapes from '../../../app-js/Assets/AppSVGShapes.js';

describe("AssetStructureTest", function () {

    var assetClasses = [];

    // Inject each one of the dependencies
    beforeEach(function () {
        assetClasses.push(new AppSVGShapes());
    });

    // To exist, the Shape class should have at least one item, does not make sense to be empty.
    it("Each class should have at least one svg item", function () {
        for(var i = 0; i < assetClasses.length; i++){
            var propertyList = Object.keys(assetClasses[i].shape);
            expect(propertyList.length).toBeGreaterThan(0);
        }
    });

    it("Each SVG Content must be readable and not empty", function () {
        for(var i = 0; i < assetClasses.length; i++){
            var propertyList = Object.keys(assetClasses[i].shape);
            for(var n = 0; n < propertyList.length; n++){
                expect(assetClasses[i].shape[propertyList[n]].svg).toBeDefined();
                expect(assetClasses[i].shape[propertyList[n]].svg).toContain('svg');
            }
        }
    });

    // i.e. property 'application' should be applicationShapeId and must be defined on the svg content
    it("Each Id param must match the id of the property", function () {
        for(var i = 0; i < assetClasses.length; i++){
            var propertyList = Object.keys(assetClasses[i].shape);
            for(var n = 0; n < propertyList.length; n++){
                expect(assetClasses[i].shape[propertyList[n]].id).toBeDefined();
                expect(assetClasses[i].shape[propertyList[n]].id).toContain(propertyList[n]+'ShapeId');
                expect(assetClasses[i].shape[propertyList[n]].svg).toContain(propertyList[n]+'ShapeId');
            }
        }
    });

    it("GetId must return a valid id", function () {
        for(var i = 0; i < assetClasses.length; i++){
            var propertyList = Object.keys(assetClasses[i].shape);
            var svgId = assetClasses[i].getId(propertyList[0]);
            expect(svgId).toBeDefined();
        }
    });

});