import { Component } from '@angular/core';
import { GameService } from '../../service/game.service';
import { DialogModel } from '../../../../shared/model/dialog.model'
import { GameDetailComponent } from '../games-detail/games-detail.component'
import { Game } from '../../model/game.model'

@Component({
    moduleId: module.id,
    selector: 'games',
    templateUrl: '../../tds/web-app/app-js/modules/games/components/games/games.component.html',
    styleUrls: ['../../tds/web-app/app-js/modules/games/components/games/games.component.css'],
    providers: [GameService]
})

export class GameComponent {
    title = "Games you should play";

    config: DialogModel = {
        name: "dialog",
        component: GameDetailComponent,
        lazyLoad: true,
        params: [
            { provide: Number, useValue: 12 },
            { provide: Game, useValue: new Game("Dark Souls") }
        ]
    };

}