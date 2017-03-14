import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import {NgbModule} from '@ng-bootstrap/ng-bootstrap';

import { GameComponent } from './components/games/games.component'
import { GameListComponent } from './components/games-list/games-list.component'
import { GameFormComponent } from './components/games-form/games-form.component'
import { GameDetailComponent } from './components/games-detail/games-detail.component'
// Routing Logic
import { UIRouterModule } from 'ui-router-ng2';
import { GAMES_STATES } from './games-routing.states';
import { TDSAppModule } from '../../config/tds-app.module'
import { SharedModule } from '../../shared/shared.module'

@NgModule({
    imports: [
        CommonModule,
        FormsModule,
        NgbModule,
        SharedModule,
        UIRouterModule.forChild({ states: GAMES_STATES }), // Same as { states: [state1, state2 ] }
    ],
    declarations: [GameComponent, GameListComponent, GameFormComponent, GameDetailComponent],
    entryComponents: [GameDetailComponent],
    exports: [GameComponent]
})
export class GamesModule {
}