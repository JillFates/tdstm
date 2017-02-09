import {
    ComponentFixture,// Core class of a component: has control over methods, digest cycle, elements, injection and so on
    TestBed, //Acts like a NgModule to wrap the component as an application
    async, //Any component who has external resources(eg. html,css) or any asynchronous call should import this module
    ComponentFixtureAutoDetect,//Not needed, only used to remove the need of fixture.detectChanges at start of the test
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
import { GameListComponent } from '../games-list/games-list.component'
import { GameService } from '../shared/game.service'
import { Game } from '../shared/game.model'

describe('Warming up: Instance of the component', () => {
    let fixture: ComponentFixture<GameListComponent>;
    let comp: GameListComponent;

    //we need to use async here because this component requires external html and css
    beforeEach(async(() => {
        //similar to what you do when creating a NgModule
        TestBed.configureTestingModule({
            declarations: [GameListComponent],
            providers: [{ provide: Game, useValue: {} }]//ignore this for now
        }).compileComponents();//this methods returns a promise
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(GameListComponent);
        comp = fixture.componentInstance;
    });

    //there is a way to handle this async behavior with only one beforeEach call, but this seems a litte more tricky
    // beforeEach(async(() => {
    //     TestBed.configureTestingModule({
    //         declarations: [GameListComponent],
    //            providers: [{provide:Game,useValue:{}}]//ignore this for now
    //     }).compileComponents().then(result => { //Its a Promise right??
    //         fixture = TestBed.createComponent(GameListComponent);
    //         comp = fixture.componentInstance;
    //     })
    // }));


    it('should create component', () => expect(comp).toBeDefined());

});

describe("Getting Hot: DOM Access", () => {
    let fixture: ComponentFixture<GameListComponent>;
    let comp: GameListComponent;
    let de: DebugElement; // this will hold our html element


    beforeEach(async(() => {
        TestBed.configureTestingModule({
            declarations: [GameListComponent],
            providers: [{ provide: Game, useValue: {} }]//ignore this for now
        }).compileComponents();
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(GameListComponent);
        comp = fixture.componentInstance;
        de = fixture.debugElement.query(By.css('h1'));
    });

    it('should have an empty title at start', () => {
        const h1 = de.nativeElement;
        expect(h1.innerText).toBe('');
    });
    it('should have an an title after detecting the changes', () => {
        fixture.detectChanges();
        const h1 = de.nativeElement;
        expect(h1.innerText).toMatch(/games/i, 'Should say sometingh about "games"');
    });

    it('should reflect the changes made by the component', () => {
        comp.title = "Food you must eat";
        fixture.detectChanges();
        const h1 = de.nativeElement;
        expect(h1.innerText).toMatch(/food/i, 'Should say sometingh about "food"');
    });
});

describe('Litte detour: Fixture.detectChanges', () => {
    let fixture: ComponentFixture<GameListComponent>;
    let comp: GameListComponent;
    let de: DebugElement; // this will hold our html element

    beforeEach(async(() => {
        TestBed.configureTestingModule({
            declarations: [GameListComponent],
            providers: [{ provide: Game, useValue: {} },//ignore this for now
                { provide: ComponentFixtureAutoDetect, useValue: true }]//this enable automatic detectChanges at test startup
        }).compileComponents();
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(GameListComponent);
        comp = fixture.componentInstance;
        de = fixture.debugElement.query(By.css('h1'));
    });

    it('should already has a title defined at startup', () => {
        const h1 = de.nativeElement;
        expect(h1.innerText).toMatch(/games/i, 'Should say sometingh about "games"');
        //remember: if you do any changes to propertys inside the component you still need to call fixture.detectChanges
    })
});

//if your component is receiving some object at its contructor you can use your own objects to test
describe('Hotter: Stub values', () => {
    let fixture: ComponentFixture<GameListComponent>;
    let comp: GameListComponent;
    let gameStub: Game = {
        Name: "Zelda"
    };

    beforeEach(async(() => {
        TestBed.configureTestingModule({
            declarations: [GameListComponent],
            providers: [{ provide: Game, useValue: gameStub }]//passing your value
        }).compileComponents();
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(GameListComponent);
        comp = fixture.componentInstance;
    });

    it('should mark newgame as false with your data', () => {
        fixture.detectChanges();
        expect(comp.newgame).toBe(false)
    });
});

//you should not use/manipulate real data in your test, right?
describe('Welcome to hell: async and spy', () => {

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
            providers: [{ provide: Game, useValue: {} }]
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

    it('should not call query() before OnInit', () => {
        expect(spy.calls.any()).toBe(false, 'query() not yet called');
    });

    it('should call query() method', () => {
        fixture.detectChanges();
        expect(spy.calls.any()).toBe(true, 'query() called');
    });

    //first way on how handle async calls, seems nice to me
    it('should have 3 games listed (async)', async(() => {
        fixture.detectChanges();
        fixture.whenStable().then(() => {
            fixture.detectChanges();
            de = fixture.debugElement.query(By.css('tr'));
            expect(de.childNodes.length).toBe(3);
        });
    }));

    //first way on how handle async calls
    it('should have 3 games listed (fakeAsync)', fakeAsync(() => {
        fixture.detectChanges();
        tick();//simulates passage of time or in this case wait until all async calls get resolved
        fixture.detectChanges();
        de = fixture.debugElement.query(By.css('tr'));
        expect(de.childNodes.length).toBe(3);
    }));
});
