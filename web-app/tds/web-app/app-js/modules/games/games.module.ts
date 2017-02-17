import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { GameComponent } from './components/games/games.component'
import { GameListComponent } from './components/games-list/games-list.component'
// Routing Logic
import { UIRouterModule } from 'ui-router-ng2';
import { GAMES_STATES } from './games-routing.states';

@NgModule({
    imports: [
        CommonModule,
        UIRouterModule.forChild({ states: GAMES_STATES }), // Same as { states: [state1, state2 ] }
    ],
    declarations: [GameComponent, GameListComponent],
    exports: [GameComponent]
})
export class GamesModule {
}