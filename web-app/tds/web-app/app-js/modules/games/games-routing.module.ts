import { NgModule }            from '@angular/core';
import { RouterModule }        from '@angular/router';

import { GameListComponent }  from './games-list/games-list.component'

@NgModule({
    imports: [RouterModule.forChild([
        { path: 'games', component: GameListComponent }
    ])],
    exports: [RouterModule]
})
export class GamesRoutingModule {}
