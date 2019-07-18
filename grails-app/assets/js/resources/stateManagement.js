/**
 * Helper class to update if necessary the state management from a non-angular page
 * @type {{setProject: setProject}}
 */
var stateManagement = function () {

    var stateKey = '@@STATE';
    var state = JSON.parse(localStorage.getItem(stateKey));

    var setProject = function (project) {
        if (state && state.TDSApp && state.TDSApp.userContext) {
            state.TDSApp.userContext.project = project;
            updateState();
        }
    };

    /**
     * Store the new date into the State
     */
    var updateState = function () {
        localStorage.setItem(stateKey, JSON.stringify(state));
    };

    return {
        setProject: setProject,
    }
}()


