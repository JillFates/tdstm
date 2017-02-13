import { NgModule }      from '@angular/core';
import { CommonModule }       from '@angular/common';

import { GameListComponent }  from './games-list/games-list.component'
// Routing Logic
import { UIRouterModule } from 'ui-router-ng2';
import { GAMES_STATES } from './games-routing.states';

@NgModule({
    imports: [
        CommonModule,
        UIRouterModule.forChild({states: GAMES_STATES}), // Same as { states: [state1, state2 ] }
    ],
    declarations: [GameListComponent],
    exports: [GameListComponent]
})
export class GamesModule {
}