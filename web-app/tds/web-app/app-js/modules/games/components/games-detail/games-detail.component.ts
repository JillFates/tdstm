import { Component } from '@angular/core';
import { Game } from '../../model/game.model';
import { GameService } from '../../service/game.service';
import { UIActiveDialogService } from '../../../../shared/services/ui-dialog.service';

@Component({
    moduleId: module.id,
    selector: 'games-detail',
    templateUrl: '../../tds/web-app/app-js/modules/games/components/games-detail/games-detail.component.html',
    providers: [{ provide: Game, useValue: new Game('Uncharted') }]
})

export class GameDetailComponent {

    constructor(
        public game: Game,
        private gameService: GameService,
        public activeDialog: UIActiveDialogService) {

    }
}