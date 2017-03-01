import { Component } from '@angular/core'
import { Game } from '../../model/game.model'
import { GameService } from '../../service/game.service'

@Component({
    moduleId: module.id,
    selector: 'games-detail',
    templateUrl: '../../tds/web-app/app-js/modules/games/components/games-detail/games-detail.component.html',
    providers: []
})

export class GameDetailComponent {

    constructor(public id: Number, public game: Game, private gameService: GameService) {

    }
}