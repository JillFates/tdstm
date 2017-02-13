/**
 * App or Root Module
 * it identify how the TDS App is being constructed
 */
import { BrowserModule } from '@angular/platform-browser';
import { NgModule, NgModuleFactoryLoader, SystemJsNgModuleLoader } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpModule } from '@angular/http';

import { TDSAppComponent }  from './tds-app.component';
// Shared Services
import { UserService } from '../shared/services/user.service'
// Feature modules
import { GamesModule } from '../modules/games/games.module'
import { NoticesManagerModule } from '../modules/noticeManager/notice-manager.module'
// Router Logic
import { UIRouterModule, UIView } from 'ui-router-ng2';
import { TDSRoutingStates } from './tds-routing.states';


// Decorator that tells to Angular is a module.
@NgModule({
    imports: [
        // Angular Modules
        BrowserModule,
        FormsModule,
        HttpModule,
        //Feature Modules
        NoticesManagerModule,
        GamesModule,
        // Routing Modules using UI Router
        UIRouterModule.forRoot(<UIRouterModule>{
            states: TDSRoutingStates,
            useHash: true
        }),
    ],
    declarations: [TDSAppComponent], // components, directives and pipes ONLY and only ONCE\
    providers: [UserService, {provide: NgModuleFactoryLoader, useClass: SystemJsNgModuleLoader}],
    bootstrap: [UIView]
})

export class TDSAppModule {
}
