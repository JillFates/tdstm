import {ComponentFixture, TestBed, async} from '@angular/core/testing';
import {By} from '@angular/platform-browser';
import {DebugElement} from '@angular/core';

import {UIToastDirective} from '../shared/directives/ui-toast.directive';
import {NotifierService} from '../shared/services/notifier.service';
import {AlertType} from '../shared/model/alert.model';

describe('UIToastDirective:', () => {
	let fixture: ComponentFixture<UIToastDirective>;
	let comp: UIToastDirective;
	let de: DebugElement;
	let notifierService: NotifierService;

	beforeEach(async(() => {
		TestBed.configureTestingModule({
			declarations: [UIToastDirective],
			providers: [NotifierService]
		}).compileComponents();
	}));

	beforeEach(() => {
		fixture = TestBed.createComponent(UIToastDirective);
		comp = fixture.componentInstance;
		notifierService = fixture.debugElement.injector.get(NotifierService);
	});

	it('should create component', () => expect(comp).toBeDefined());

	it('should be invisible when start', () => {
		fixture.detectChanges();
		de = fixture.debugElement.query(By.css('.message-wrapper-container'));
		expect(de).toBeNull();
	});

	it('should show message after broadcast', done => {
		fixture.detectChanges();
		de = fixture.debugElement.query(By.css('.message-wrapper-container'));
		expect(de).toBeNull();

		notifierService.on(AlertType.DANGER, (event) => {
			de = fixture.debugElement.query(By.css('.message-wrapper-container'));
			expect(de).toBeDefined();
			done();
		});

		notifierService.broadcast({
			name: AlertType.DANGER,
			message: 'What a worderful message'
		});
	});

});