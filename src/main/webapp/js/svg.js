var appSVGShapes = (function ($) {
    var public = {};
    public.shape = {
        application: {
            id: 'applicationShapeId',
            svg: '<svg xmlns="http://www.w3.org/2000/svg"> <g id="applicationShapeId" transform="translate(-15, -15) scale(0.28)">  <g id="svg_1"> <path id="svg_3" d="m53.4,4.7c27.6,0 50,22.4 50,50c0,27.6 -22.4,50 -50,50c-27.6,0 -50,-22.4 -50,-50c0,-27.6 22.4,-50 50,-50l0,0z" fill="#ffffff" />  <path id="svg_2" d="m53.4,4.7c27.6,0 50,22.4 50,50c0,27.6 -22.4,50 -50,50c-27.6,0 -50,-22.4 -50,-50c0,-27.6 22.4,-50 50,-50l0,0zm14.9,73.4c0.4,0.9 0.8,2 1.4,3.2c0.5,1.2 1,2.1 1.5,2.7c0.5,0.6 1.1,1.1 1.7,1.4c0.7,0.4 1.5,0.5 2.5,0.5c1.6,0 3.1,-0.6 4.2,-1.7c1.2,-1.2 1.7,-2.5 1.7,-3.9c0,-1.4 -0.6,-3.5 -1.9,-6.5l-15.8,-39c-0.7,-1.9 -1.4,-3.5 -1.8,-4.7c-0.5,-1.2 -1.1,-2.3 -1.8,-3.3c-0.7,-1 -1.6,-1.8 -2.8,-2.5c-1.1,-0.6 -2.6,-1 -4.3,-1c-1.7,0 -3.1,0.3 -4.2,1c-1.1,0.6 -2.1,1.5 -2.8,2.5c-0.7,1 -1.4,2.4 -2,3.9c-0.6,1.6 -1.1,2.9 -1.6,4.1l-15.2,39.4c-0.6,1.6 -1.1,2.8 -1.4,3.7c-0.3,0.9 -0.4,1.7 -0.4,2.6c0,1.4 0.6,2.7 1.8,3.8c1.2,1.1 2.5,1.7 4.1,1.7c1.8,0 3.1,-0.5 3.9,-1.6c0.8,-1.1 1.8,-3.1 2.9,-6.1l2.9,-7.7l24.6,0l2.8,7.5l0,0zm-24.2,-16.8l18.1,0l-9.1,-24.9l-9,24.9z" clip-rule="evenodd" fill-rule="evenodd"/> </g> </g></svg>'
        },
        database: {
            id: 'databaseShapeId',
            svg: '<svg xmlns="http://www.w3.org/2000/svg"><g id="databaseShapeId" transform="translate(-15, -15) scale(0.28)"><path id="svg_3" fill="#FFFFFF" stroke="#FFFFFF" d="M15.4,23h82.1v30H15.4V23z"/><path id="svg_1" d="M15.4,23c0-8.6,18.4-15.6,41-15.6s41,7,41,15.6S79,38.6,56.4,38.6c-2.8,0-2.8,9.7,0,9.7c11.8,0,22.3-1.5,30.2-4.4c4.4-1.7,8.3-4.3,10.9-6.8c0,56.9,0,38.7,0,54.5c0,8.6-18.4,15.6-41,15.6s-41-7-41-15.6C15.4,68.8,15.4,45.9,15.4,23z"/></g></svg>'
        },
        networkLogical: {
            id: 'networkLogicalShapeId',
            svg: '<svg xmlns="http://www.w3.org/2000/svg"><g id="networkLogicalShapeId" transform="translate(-15, -15) scale(0.28)" id="svg_1"><polygon fill-rule="evenodd" fill="#FFFFFF" stroke="#FFFFFF" clip-rule="evenodd" points="26.4,68.8 20.8,66.6 5.4,51.2 5.4,9.5 9.3,5.5 87.7,5.5 103.2,21 105.4,26.5 105.4,68.8" id="svg_7"/><polygon fill-rule="evenodd" clip-rule="evenodd" points="9.3,5.5 87.7,5.5 103.2,21 26.4,21 24.8,21" id="svg_2"/><polygon id="svg_3" points="20.8,66.6 5.4,51.2 5.4,9.4 20.8,24.9 20.8,26.5" clip-rule="evenodd" fill-rule="evenodd"/><g id="svg_4"><path d="m26.4,26.5l0,42.3l59.6,0c-1.8,-2.3 -2.8,-5.2 -2.8,-8.3c0,-3.8 1.6,-7.3 4.1,-9.8l0,0l0,0c2.5,-2.5 6,-4.1 9.8,-4.1c3.1,0 6,1 8.3,2.8l0,-22.9l-79,0l0,0l0,0l0,0l0,0l0,0l0,0l0,0zm14.4,25.1c-2.2,0 -3.9,-1.8 -3.9,-3.9c0,-2.2 1.8,-3.9 3.9,-3.9c2.2,0 3.9,1.8 3.9,3.9c0,2.1 -1.8,3.9 -3.9,3.9zm18.4,0c-2.2,0 -3.9,-1.8 -3.9,-3.9c0,-2.2 1.8,-3.9 3.9,-3.9c2.2,0 3.9,1.8 3.9,3.9c0.1,2.1 -1.7,3.9 -3.9,3.9zm18.5,0c-2.2,0 -3.9,-1.8 -3.9,-3.9c0,-2.2 1.8,-3.9 3.9,-3.9c2.2,0 3.9,1.8 3.9,3.9c0,2.1 -1.7,3.9 -3.9,3.9z" id="svg_5"/><path d="m99,58.5c-0.5,-0.5 -1.2,-0.8 -2,-0.8c-0.8,0 -1.5,0.3 -2,0.8l0,0c-0.5,0.5 -0.8,1.2 -0.8,2c0,0.8 0.3,1.5 0.8,2l0,0c0.5,0.5 1.2,0.8 2,0.8c0.8,0 1.5,-0.3 2,-0.8l0,0c0.5,-0.5 0.8,-1.2 0.8,-2c0,-0.8 -0.3,-1.5 -0.8,-2l0,0z" id="svg_6"/></g><path fill-rule="evenodd" fill="#00FF00" clip-rule="evenodd" d="m97.1,68.8c4.6,0 8.3,-3.7 8.3,-8.3c0,-4.6 -3.7,-8.3 -8.3,-8.3c-4.6,0 -8.3,3.7 -8.3,8.3c-0.1,4.6 3.7,8.3 8.3,8.3z" id="svg_7"/></g></svg>'
        },
        networkPhysical: {
            id: 'networkPhysicalShapeId',
            svg: '<svg xmlns="http://www.w3.org/2000/svg"> <g id="networkPhysicalShapeId" transform="translate(-15, -15) scale(0.28)"> <g id="svg_1"><path fill-rule="evenodd" clip-rule="evenodd" d="m39.2,7.7l33.3,0l0,33.3c-44.9,0 11.6,0 -33.3,0l0,-33.3z" id="svg_2"/><path fill-rule="evenodd" clip-rule="evenodd" d="m58.7,41l0,13.9l33.4,0c0,6.6 -0.1,13.1 -0.1,19.7c7.9,1.3 13.9,8.2 13.9,16.4c0,9.2 -7.5,16.7 -16.7,16.7s-16.6,-7.5 -16.6,-16.7c0,-8.3 6,-15.1 13.9,-16.4l0,-14.1l-61.2,0l0,14.1c7.9,1.3 13.9,8.2 13.9,16.4c0,9.2 -7.5,16.7 -16.7,16.7c-9.2,0 -16.7,-7.5 -16.7,-16.7c0,-8.3 6,-15.1 13.9,-16.4c0,-6.6 -0.1,-13.1 -0.1,-19.7l33.4,0l0,-13.9l5.7,0l0,0z" id="svg_3"/></g></g></svg>'
        },
        other: {
            id: 'otherShapeId',
            svg: '<svg xmlns="http://www.w3.org/2000/svg"> <g id="otherShapeId" transform="translate(-15, -15) scale(0.28)"> <g id="svg_1"><polygon id="svg_7" points="28.4,69.3 22.8,67.1 7.4,51.7 7.4,10 11.3,6 89.7,6 105.2,21.5 107.4,27 107.4,69.3" clip-rule="evenodd" stroke="#FFFFFF" fill="#FFFFFF" fill-rule="evenodd"/> <polygon id="svg_2" points="11.3,6 89.7,6 105.2,21.5 28.4,21.5 26.8,21.5" clip-rule="evenodd" fill-rule="evenodd"/> <path id="svg_3" d="m107.4,27l0,22.9c-2.3,-1.8 -5.2,-2.8 -8.3,-2.8c-3.8,0 -7.3,1.6 -9.8,4.1l0,0l0,0c-2.5,2.5 -4.1,6 -4.1,9.8c0,3.1 1,6 2.8,8.3l-59.6,0l0,-42.3l79,0l0,0zm-6.4,32c-0.5,-0.5 -1.2,-0.8 -2,-0.8c-0.8,0 -1.5,0.3 -2,0.8l0,0c-0.5,0.5 -0.8,1.2 -0.8,2c0,0.8 0.3,1.5 0.8,2l0,0c0.5,0.5 1.2,0.8 2,0.8c0.8,0 1.5,-0.3 2,-0.8l0,0c0.5,-0.5 0.8,-1.2 0.8,-2c0,-0.8 -0.3,-1.5 -0.8,-2l0,0z" clip-rule="evenodd" fill-rule="evenodd"/><path id="svg_4" d="m99.1,69.4c4.6,0 8.3,-3.7 8.3,-8.3c0,-4.6 -3.7,-8.3 -8.3,-8.3c-4.6,0 -8.3,3.7 -8.3,8.3c-0.1,4.5 3.7,8.3 8.3,8.3z" fill="#00FF00" clip-rule="evenodd" fill-rule="evenodd"/><polygon id="svg_5" points="22.8,67.1 7.4,51.7 7.4,10 22.8,25.4 22.8,27" clip-rule="evenodd" fill-rule="evenodd"/></g></g></svg>'
        },
        serverPhysical: {
            id: 'serverPhysicalShapeId',
            svg: '<svg xmlns="http://www.w3.org/2000/svg" ><g id="serverPhysicalShapeId" transform="translate(-14, -14.3) scale(0.26)"><g id="svg_1"><polygon id="svg_7" fill="#FFFFFF" stroke="#FFFFFF" points="86,104.1 26.4,104.1 20.8,101.8 5.4,86.4 5.4,8 9.3,4.1 87.7,4.1 103.2,19.5 105.4,25.1 105.4,84.7 105.4,104.1"/><polygon id="svg_2" points="10.8,4 88,4.2 103.2,19.6 27.6,19.4 26,19.4 "/><path id="svg_3" d="M105.4,26.3V85c-2.3-1.8-5.1-2.8-8.1-2.8c-3.7,0-7.1,1.6-9.6,4l0,0l0,0c-2.4,2.5-4,5.9-4,9.7c0,3.1,1,5.9,2.7,8.2H28V26.3L105.4,26.3C105.4,26.3,105.4,26.3,105.4,26.3z M99.1,93.9c-0.5-0.5-1.2-0.8-2-0.8s-1.5,0.3-2,0.8l0,0c-0.5,0.5-0.8,1.2-0.8,2s0.3,1.5,0.8,2l0,0c0.5,0.5,1.2,0.8,2,0.8s1.5-0.3,2-0.8l0,0c0.5-0.5,0.8-1.2,0.8-2S99.6,94.3,99.1,93.9L99.1,93.9z"/><path id="svg_4" fill="#00FF00" d="M97.1,104.1c4.6,0,8.3-3.7,8.3-8.3c0-4.6-3.7-8.3-8.3-8.3c-4.6,0-8.3,3.7-8.3,8.3C88.7,100.3,92.5,104.1,97.1,104.1z"/><polygon id="svg_5" points="20.8,101.8 5.4,86.7 5.4,9.6 20.8,24.7 20.8,26.4"/></g></g></svg>'
        },
        serverVirtual: {
            id: 'serverVirtualShapeId',
            svg: '<svg xmlns="http://www.w3.org/2000/svg"><g id="serverVirtualShapeId" transform="translate(-15, -15) scale(0.28)">	<g id="svg_1"><path id="svg_3" fill="#FFFFFF" stroke="#FFFFFF" d="M20.5,19.6v72.2h71.9V19.6C92.3,19.6,20.5,19.6,20.5,19.6z"/><path id="svg_2" d="M20.5,19.6h71.9v54.2c-1.7-1.3-5.5-1.7-7.7-1.7c-1.4,0-5.3,0.1-8.1,2.8c-4.4,4.4-3.9,8.6-3.9,9.7c0,3,1.2,5.6,2.5,7.3H20.5L20.5,19.6C20.5,19.6,20.5,19.6,20.5,19.6z M87.7,84.4c-0.4-0.4-0.9-0.6-1.4-0.6s-1.1,0.2-1.4,0.6l0,0c-0.4,0.4-0.6,0.9-0.6,1.4c0,0.6,0.2,1.1,0.6,1.4l0,0c0.4,0.4,0.9,0.6,1.4,0.6s1.1-0.2,1.4-0.6l0,0c0.4-0.4,0.6-0.9,0.6-1.4C88.3,85.2,88.1,84.7,87.7,84.4L87.7,84.4z"/><path id="svg_3_1_" fill="#00FF00" d="M84.6,92.3c4.3,0,7.7-3.4,7.7-7.7c0-4.3-3.4-7.7-7.7-7.7s-7.7,3.4-7.7,7.7C76.8,88.8,80.4,92.3,84.6,92.3z"/></g></g></svg>'
        },
        storageLogical: {
            id: 'storageLogicalShapeId',
            svg: '<svg xmlns="http://www.w3.org/2000/svg"> <g id="storageLogicalShapeId" transform="translate(-16, -15) scale(0.28)"><g id="svg_1"><path id="svg_3" d="m64.3,20.7l30,0l0,30l-85.5,0l0,-30z" clip-rule="evenodd" stroke="#FFFFFF" fill="#FFFFFF" fill-rule="evenodd"/> <path id="svg_2" fill="#696A6D" d="m55.2,20.7l9.1,0l-0.4,0.5l0,0l-0.3,0.4l0,0l-3.7,4.8l-45.8,0c-1.9,0 -3.6,0.5 -5.2,1.3l0,-14c0,-2.3 1.9,-4.2 4.2,-4.2l30.3,0c3.5,0 5,2.4 6.4,4.2l5.4,7z" clip-rule="evenodd" fill-rule="evenodd"/><path id="svg_3" d="m62.6,32l-48.5,0c-2.8,0 -5.2,2.3 -5.2,5.2l0,51.1c0,2.8 2.3,5.2 5.2,5.2l89.7,0c2.8,0 5.2,-2.3 5.2,-5.2c0,-23.2 0,-40.1 0,-63.3c0,-2.3 -1.9,-4.2 -4.2,-4.2l-30.4,0c-3.5,0 -5,2.4 -6.4,4.2l-5.4,7z" clip-rule="evenodd" fill-rule="evenodd"/></g></g></svg>'
        },
        storagePhysical: {
            id: 'storagePhysicalShapeId',
            svg: '<svg xmlns="http://www.w3.org/2000/svg"><g id="storagePhysicalShapeId" transform="translate(-15, -15) scale(0.28)"><g id="svg_1"><path id="svg_8" fill="#FFFFFF" stroke="#FFFFFF" d="M7.9,21h99.9v77l-21,5.3L7.9,91.6L7.9,21z"/><path id="svg_2" d="M57.9,37.3c27.6,0,50-7,50-15.6S85.5,6,57.9,6s-50,7-50,15.6S30.3,37.3,57.9,37.3L57.9,37.3z"/><path id="svg_3" d="M7.9,67.5c0,8.6,22.4,15.6,50,15.6s50-7,50-15.6v-11c-3.2,2.6-7.6,4.6-13,6.3c-9.6,3-22.7,6-37,6c-14.4,0-27.8-2.5-37.4-5.4c-5.4-1.7-9.4-5.4-12.6-7.9L7.9,67.5C7.9,67.5,7.9,67.5,7.9,67.5z"/><path id="svg_4" d="M94.9,41.9c-9.6,3-22.7,4.8-37,4.8c-14.4,0-27.4-1.9-37-4.8c-5.4-1.7-9.8-3.9-13-6.4v12.9c8.9,6.4,22.4,12.3,50,12.3s38.8-4.8,50-12.3V35.5C104.7,38.1,100.3,40.2,94.9,41.9z"/><path id="svg_5" d="M7.9,90.4L7.9,90.4V77.5c3.2,2.6,7.4,5.9,12.8,7.6c9.6,3,22.9,5.5,37.2,5.5c14.4,0,27.8-3.1,37.4-6c5.4-1.7,9.4-4.6,12.6-7.1v9.2c-2.3-1.8-5.2-2.8-8.3-2.8c-3.8,0-7.3,1.6-9.8,4.1l0,0c-2.5,2.5-4.1,6-4.1,9.8c0,1.9,0.4,3.8,1.1,5.5c-8.2,1.8-18.1,2.9-28.9,2.9C30.3,106,7.9,99,7.9,90.4L7.9,90.4C7.9,90.4,7.9,90.4,7.9,90.4z"/><path id="svg_6" d="M97.7,99.8c1.7-0.7,3.3-1.5,4.6-2.3c0-0.7-0.3-1.4-0.8-1.8c-0.5-0.5-1.2-0.8-2-0.8s-1.5,0.3-2,0.8s-0.8,1.2-0.8,2s0.3,1.5,0.8,2C97.6,99.7,97.7,99.8,97.7,99.8z"/><path id="svg_7" fill="#00FF00" d="M99.6,106c4.6,0,8.3-3.7,8.3-8.3c0-4.6-3.7-8.3-8.3-8.3c-4.6,0-8.3,3.7-8.3,8.3C91.2,102.3,95,106,99.6,106z"/></g></g></svg>'
        }
    };

    /**
     * For D3 and other libraries that require the whole string, this method will return the entire svg app objects concatenated.
     * @returns {string}
     */
    public.getAll = function () {
        var all = '';
        for (var i = 0; i < Object.keys(public.shape).size(); ++i) {
            all += public.shape[Object.keys(public.shape)[i]].svg;
        }
        return all;
    };

    /**
     * Get the SVG Id based on the its name
     * @param svgName
     * @returns {string}
     */
    public.getId = function (svgName) {
        if (svgName && public.shape[svgName])
            return public.shape[svgName].id;
        return null;
    };

    /**
     * Get the SVG content based on the its name
     * @param svgName
     * @returns {string}
     */
    public.getSVG = function (svgName) {
        if (svgName && public.shape[svgName])
            return public.shape[svgName].svg;
        return null;
    };

    return public;
})(jQuery);