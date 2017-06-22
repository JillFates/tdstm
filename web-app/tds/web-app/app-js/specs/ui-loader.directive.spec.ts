import { ComponentFixture, TestBed, async } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { DebugElement } from '@angular/core';

import { UILoaderDirective } from '../shared/directives/ui-loader.directive';
import { NotifierService } from '../shared/services/notifier.service';
import { UILoaderService } from '../shared/services/ui-loader.service';

describe('UILoaderDirective:', () => {
	let fixture: ComponentFixture<UILoaderDirective>;
	let comp: UILoaderDirective;
	let de: DebugElement;
	let notifierService: NotifierService;

	beforeEach(async(() => {
		TestBed.configureTestingModule({
			declarations: [UILoaderDirective],
			providers: [NotifierService, UILoaderService]
		}).compileComponents();
	}));

	beforeEach(() => {
		fixture = TestBed.createComponent(UILoaderDirective);
		comp = fixture.componentInstance;
		notifierService = fixture.debugElement.injector.get(NotifierService);
	});

	it('should create component', () => expect(comp).toBeDefined());

	it('should show loader when a broadcast with specific key has been made', done => {
		notifierService.on('httpRequestInitial', (event) => {
			expect(comp.isShowing()).toBe(true);
			de = fixture.debugElement.query(By.css('#main-loader'));
			expect(de).toBeDefined();
			done();
		});
		fixture.detectChanges();
		notifierService.broadcast({
			name: 'httpRequestInitial'
		});
	});

	it('should dismiss loader when a broadcast with specific key has been made', done => {
		notifierService.on('httpRequestCompleted', (event) => {
			expect(comp.isShowing()).toBe(false);
			de = fixture.debugElement.query(By.css('#main-loader'));
			expect(de).toBeNull();
			done();
		});

		notifierService.on('httpRequestInitial', (event) => {
			expect(comp.isShowing()).toBe(true);
			notifierService.broadcast({
				name: 'httpRequestCompleted'
			});
		});
		notifierService.broadcast({
			name: 'httpRequestInitial'
		});
	});

});