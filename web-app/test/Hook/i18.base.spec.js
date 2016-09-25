// Global jasmine hook to prevent errors when attempt to
// download a localization .json file in test environment.
// Here we are resetting the translate provider to a
// safe state.
// If a test needs to assert for localized texts,
// the test author is free to setup the translate provider
// on isolation by overriding its settings.
// This wont affect other tests because once the test execution
// exits, the translate provider is reset once again to
// a safe state.
// The error we get if we do not add this global hook is:

// Error: Unexpected request: GET /locale.en.json
// No more request expected

// https://github.com/PascalPrecht/angular-translate/issues/42

beforeEach(function() {
    var DEFAULT_LANG = 'en_US';

    var DEFAULT_TRANSLATIONS = {};
    var MODULE_NAME = 'TDSTM'; // UPDATE ACCORDINGLY

    angular.mock.module(MODULE_NAME, function config($translateProvider) {
        $translateProvider.translations(DEFAULT_LANG, DEFAULT_TRANSLATIONS);
        $translateProvider.preferredLanguage(DEFAULT_LANG);
    });
});
