import { ComponentFixture, TestBed, async } from '@angular/core/testing'
import { By } from '@angular/platform-browser'
import { DebugElement } from '@angular/core'

import { TDSAppComponent } from '../config/tds-app.component'
import { UserService } from '../shared/services/user.service'
import { HttpServiceProvider } from '../shared/providers/http-interceptor.provider';
import { NotifierService } from '../shared/services/notifier.service';
import { UILoaderDirective } from '../shared/directives/ui-loader.directive';
import { UIToastDirective } from '../shared/directives/ui-toast.directive';

describe('TDSAppComponent:', () => {
    let fixture: ComponentFixture<TDSAppComponent>;
    let comp: TDSAppComponent;
    let de: DebugElement;
    let userStub: UserService = {
        userName: 'Bruce Wayne'
    };

    beforeEach(async(() => {
        TestBed.configureTestingModule({
            declarations: [TDSAppComponent, UILoaderDirective, UIToastDirective],
            providers: [NotifierService, { provide: UserService, useValue: userStub },
                HttpServiceProvider
            ]
        }).compileComponents();
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(TDSAppComponent);
        comp = fixture.componentInstance;
        de = fixture.debugElement.query(By.css('h1'));
    });

    it('should create component', () => expect(comp).toBeDefined());

});