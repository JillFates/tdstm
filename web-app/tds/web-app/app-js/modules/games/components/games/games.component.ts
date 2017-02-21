import { Component } from '@angular/core';
import { GameService } from '../../service/game.service'

@Component({
    moduleId: module.id,
    selector: 'games',
    templateUrl: '../../tds/web-app/app-js/modules/games/components/games/games.component.html',
    styleUrls: ['../../tds/web-app/app-js/modules/games/components/games/games.component.css'],
    providers: [GameService]
})

export class GameComponent {
     title = "Games you should play";
}