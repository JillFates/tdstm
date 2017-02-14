import {ComponentFixture, TestBed, async} from '@angular/core/testing'
import {By} from '@angular/platform-browser'
import {DebugElement} from '@angular/core'

import {TDSAppComponent} from '../config/tds-app.component'
import {UserService} from '../shared/services/user.service'

describe('TDSAppComponent:', () => {
    let fixture: ComponentFixture<TDSAppComponent>;
    let comp: TDSAppComponent;
    let de: DebugElement;
    let userStub: UserService = {
        userName: 'Bruce Wayne'
    };

    beforeEach(async(() => {
        TestBed.configureTestingModule({
            declarations: [TDSAppComponent],
            providers: [{ provide: UserService, useValue: userStub }]
        }).compileComponents();
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(TDSAppComponent);
        comp = fixture.componentInstance;
        de = fixture.debugElement.query(By.css('h1'));
    });

    it('should create component', () => expect(comp).toBeDefined());

    it('should have a h1 element', () => {
        fixture.detectChanges();
        const h1 = de.nativeElement;
        expect(h1.innerText).toBeDefined()
    });

    it('should say something about bruce wayne', () => {
        fixture.detectChanges();
        const h1 = de.nativeElement;
        expect(h1.innerText).toMatch(/bruce wayne/i, 'Should say something about "Bruce Wayne"');
    });
});