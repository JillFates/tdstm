import 'core-js/es6';
import 'core-js/es7/reflect';
import 'zone.js/dist/zone';
import 'zone.js/dist/long-stack-trace-zone';
import 'zone.js/dist/proxy';
import 'zone.js/dist/sync-test';
import 'zone.js/dist/jasmine-patch';
import 'zone.js/dist/async-test';
import 'zone.js/dist/fake-async-test';

import { TestBed } from '@angular/core/testing';
import { BrowserDynamicTestingModule, platformBrowserDynamicTesting } from '@angular/platform-browser-dynamic/testing';

import './specs/highlight.directive.spec';
import './specs/notifier.service.spec';
import './specs/tds-app.component.spec';
import './specs/ui-boolean.pipe.spec';
import './specs/translate.pipe.spec';
import './specs/ui-loader.directive.spec';
import './specs/ui-toast.directive.spec';
import './specs/ui-svg-icon.directive.spec';
import './specs/component-creator.service.spec';
import './specs/permission.service.spec';
import './modules/fieldSettings/specs/field-settings-imp.component.spec';
import './modules/fieldSettings/specs/field-settings-list.component.spec';
import './modules/fieldSettings/specs/selectlist-configuration-popup.component.spec';
import './modules/assetExplorer/specs/asset-explorer-report-selector.component.spec';

TestBed.initTestEnvironment(BrowserDynamicTestingModule, platformBrowserDynamicTesting());
