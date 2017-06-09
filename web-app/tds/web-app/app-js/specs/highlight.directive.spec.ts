/**
 * Created by aaferreira on 13/02/2017.
 */
import {ComponentFixture, TestBed, async} from '@angular/core/testing';
import {By} from '@angular/platform-browser';
import {DebugElement} from '@angular/core';
import {EmptyComponent} from '../testing/empty.component';

import {HighlightDirective} from '../shared/directives/highlight.directive';

describe('HighlightDirective:', () => {
	let fixture: ComponentFixture<EmptyComponent>;
	let comp: EmptyComponent;
	let de: DebugElement;

	beforeEach(async(() => {
		TestBed.overrideComponent(EmptyComponent, {
			set: {
				template: '<h1 tds-highlight elementHighlight="purple">Highlight when mouse over</h1>'
			}
		});
		TestBed.configureTestingModule({
			declarations: [EmptyComponent, HighlightDirective]
		}).compileComponents();
	}));

	beforeEach(() => {
		fixture = TestBed.createComponent(EmptyComponent);
		comp = fixture.componentInstance;
		de = fixture.debugElement.query(By.css('h1'));

	});

	it('should create component', () => expect(comp).toBeDefined());

	it('should have a h1 element', () => {
		fixture.detectChanges();
		const h1 = de.nativeElement;
		expect(h1.innerText).toMatch(/highlight/i, 'Should say sometingh about "highlight"');
	});

	it('should turn backgroung color purple when mouse over', () => {
		fixture.detectChanges();
		de.triggerEventHandler('mouseenter', null);
		fixture.detectChanges();
		expect(de.nativeElement.style.backgroundColor).toBe('purple');
	});

	it('should turn backgroung color to empty when mouse out', () => {
		fixture.detectChanges();
		de.triggerEventHandler('mouseenter', null);
		fixture.detectChanges();
		expect(de.nativeElement.style.backgroundColor).toBe('purple');
		de.triggerEventHandler('mouseleave', null);
		fixture.detectChanges();
		expect(de.nativeElement.style.backgroundColor).toBe('');
	});
});