/**
 * Helper class to update if necessary the state management from a non-angular page
 * @type {{setProject: setProject}}
 */
var stateManagement = function () {

    var stateKey = '@@STATE-' + window.location.hostname;
    var state = JSON.parse(localStorage.getItem(stateKey));

    /**
     * Update the project, it also set to null the event and bundle
     * @param id
     * @param name
     * @param logoUrl
     */
    var setProject = function (id, name, logoUrl) {
        if (state && state.TDSApp && state.TDSApp.userContext) {
            state.TDSApp.userContext.project = { id: id, name: name, logoUrl: logoUrl};
            state.TDSApp.userContext.event = null;
            state.TDSApp.userContext.bundle = null;
            updateState();
        }
    };

    /**
     * Update the event, it also set to null the bundle
     * @param id
     * @param name
     */
    var setEvent = function (id, name) {
        if (state && state.TDSApp && state.TDSApp.userContext) {
            state.TDSApp.userContext.event = { id: id, name: name};
            state.TDSApp.userContext.bundle = null;
            updateState();
        }
    };

    /**
     * Update the bundle
     * @param id
     * @param name
     */
    var setBundle = function (id, name) {
        if (state && state.TDSApp && state.TDSApp.userContext) {
            state.TDSApp.userContext.bundle = {id: id, name: name};
            updateState();
        }
    };

    /**
     * To destroy the Local on Logout
     */
    var destroyState = function () {
        localStorage.removeItem(stateKey);
    };

    /**
     * Store the new date into the State
     */
    var updateState = function () {
        localStorage.setItem(stateKey, JSON.stringify(state));
    };

    return {
        stateKey: stateKey,
        destroyState: destroyState,
        setProject: setProject,
        setEvent: setEvent,
        setBundle: setBundle
    }
}();
