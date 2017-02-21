import {
    ComponentFixture,// Core class of a component: has control over methods, digest cycle, elements, injection and so on
    TestBed, //Acts like a NgModule to wrap the component as an application
    async, //Any component who has external resources(eg. html,css) or any asynchronous call should import this module
    fakeAsync, //Not needed, another way to handle async, but in the same thread
    tick //Not needed, waits to all async call get resolved or simullates passage of time
} from '@angular/core/testing'

import {
    By // Used to query html elements
} from '@angular/platform-browser'

import {
    DebugElement // Hold the instance to an HTML element you want to test
} from '@angular/core'

//after all these imports you should import what actually is gonna be tested
import { GameListComponent } from '../components/games-list/games-list.component'

import { GameService } from '../service/game.service'
import { Game } from '../model/game.model'

//you should not use/manipulate real data in your test, right?
describe('GameListComponent - Learning: async and spy', () => {

    let mockData: Array<Game>

    let fixture: ComponentFixture<GameListComponent>;
    let comp: GameListComponent;
    let de: DebugElement;
    let gameService: GameService;
    let spyQuery: jasmine.Spy;
    let spySave: jasmine.Spy;

    beforeEach(async(() => {
        TestBed.configureTestingModule({
            declarations: [GameListComponent],
            providers: [GameService]
        }).compileComponents();
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(GameListComponent);
        comp = fixture.componentInstance;
        mockData = [
            new Game('Super Mario'),
            new Game('The legend of zelda'),
            new Game('Metroid')
        ];
        // GameService actually injected into the component
        gameService = fixture.debugElement.injector.get(GameService);

        //return mockData when query get called
        spyQuery = spyOn(gameService, 'query')
            .and.returnValue(Promise.resolve(mockData));

        //save on the mockdata 
        spySave = spyOn(gameService, 'save').and.callFake((game: Game) => {
            return new Promise((resolve, reject) => {
                mockData.push(game);
                resolve(mockData.length);
            });
        });
    });

    it('should not call query() before OnInit', () => {
        expect(spyQuery.calls.any()).toBe(false, 'query() not yet called');
    });

    it('should call query() method', () => {
        fixture.detectChanges();
        expect(spyQuery.calls.any()).toBe(true, 'query() called');
    });

    //first way on how handle async calls, seems better to me
    it('should have 3 games listed (async)', async(() => {
        fixture.detectChanges();
        fixture.whenStable().then(() => {
            fixture.detectChanges();
            de = fixture.debugElement.query(By.css('tr'));
            expect(de.childNodes.length).toBe(3);
        });
    }));

    it('should call query when refresh is clicked', async(() => {
        fixture.detectChanges();
        expect(spyQuery.calls.count()).toBe(1);
        fixture.whenStable().then(() => {
            fixture.detectChanges();
            de = fixture.debugElement.query(By.css('#refresh'));
            de.triggerEventHandler("click", null);
            fixture.detectChanges();
            expect(spyQuery.calls.count()).toBe(2);
        });
    }));

    it('should update games list when refresh is clicked', async(() => {
        fixture.detectChanges();
        fixture.whenStable().then(() => {
            fixture.detectChanges();
            expect(comp.games.length).toBe(3);
            
            gameService.save(new Game("Donkey Kong"));

            de = fixture.debugElement.query(By.css('#refresh'));
            de.triggerEventHandler("click", null);

            fixture.detectChanges();

            fixture.whenStable().then(() => {
                fixture.detectChanges();
                expect(comp.games.length).toBe(4);
            });
        });
    }));

});

describe('GameListComponent - Learning: fakeAsync and tick', () => {
    let mockData: Array<Game> = [
        new Game('Super Mario'),
        new Game('The legend of zelda'),
        new Game('Metroid')
    ];

    let fixture: ComponentFixture<GameListComponent>;
    let comp: GameListComponent;
    let de: DebugElement;
    let gameService: GameService;
    let spy: jasmine.Spy;

    beforeEach(async(() => {
        TestBed.configureTestingModule({
            declarations: [GameListComponent],
            providers: [GameService]
        }).compileComponents();
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(GameListComponent);
        comp = fixture.componentInstance;


        // GameService actually injected into the component
        gameService = fixture.debugElement.injector.get(GameService);

        //return mockData when query get called
        spy = spyOn(gameService, 'query')
            .and.returnValue(Promise.resolve(mockData));
    });

    //another way on how handle async calls
    it('should have 3 games listed (fakeAsync)', fakeAsync(() => {
        fixture.detectChanges();
        tick();//simulates passage of time or in this case wait until all async calls get resolved
        fixture.detectChanges();
        de = fixture.debugElement.query(By.css('tr'));
        expect(de.childNodes.length).toBe(3);
    }));

});
