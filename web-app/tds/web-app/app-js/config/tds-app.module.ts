/**
 * App or Root Module
 * it identify how the TDS App is being constructed
 */
import {BrowserModule} from '@angular/platform-browser';
import {NgModule, NgModuleFactoryLoader, SystemJsNgModuleLoader} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {HttpModule, Http} from '@angular/http';

import {NgbModule} from '@ng-bootstrap/ng-bootstrap';

import {TDSAppComponent} from './tds-app.component';
// Providers

// Feature modules
import {SharedModule} from'../shared/shared.module';
import {GamesModule} from '../modules/games/games.module';
import {NoticesManagerModule} from '../modules/noticeManager/notice-manager.module';
// Router Logic
import {UIRouterModule, UIView} from 'ui-router-ng2';
import {TDSRoutingStates, AuthConfig} from './tds-routing.states';

// Decorator that tells to Angular is a module.
@NgModule({
    imports: [
        // Angular Modules
        BrowserModule,
        FormsModule,
        HttpModule,
        //Bootstrap Module
        NgbModule.forRoot(),
        // Feature Modules
        SharedModule,
        NoticesManagerModule,
        GamesModule,
        // Routing Modules using UI Router
        UIRouterModule.forRoot(<UIRouterModule>{
            states: TDSRoutingStates,
            useHash: true,
            config: AuthConfig,
        }),
    ],
    declarations: [
        TDSAppComponent,
    ], // components, directives and pipes ONLY and only ONCE\
    providers: [
        {provide: NgModuleFactoryLoader, useClass: SystemJsNgModuleLoader}
    ],
    bootstrap: [UIView]
})

export class TDSAppModule {
}