import {Ng2StateDeclaration} from 'ui-router-ng2';
import { GameListComponent }  from './games-list/games-list.component'

/**
 * This state displays the game module.
 * It also provides a nested ui-view (viewport) for child states to fill in.
 * The games modules are fetched using a resolve.
 */
export const gamesState: Ng2StateDeclaration = <Ng2StateDeclaration>{
    name: 'games',
    url: '/games',
    component: GameListComponent
};

export const GAMES_STATES = [
    gamesState
];