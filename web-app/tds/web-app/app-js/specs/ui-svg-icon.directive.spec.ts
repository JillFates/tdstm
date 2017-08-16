/**
 * Created by aaferreira on 13/02/2017.
 */
import {ComponentFixture, TestBed, async} from '@angular/core/testing';
import {By} from '@angular/platform-browser';
import {DebugElement} from '@angular/core';
import {EmptyComponent} from '../testing/empty.component';

import {UISVGIconDirectiveDirective} from '../shared/directives/ui-svg-icon.directive';

describe('UISVGIconDirective:', () => {
	let fixture: ComponentFixture<EmptyComponent>;
	let comp: EmptyComponent;
	let de: DebugElement;

	beforeEach(async(() => {
		TestBed.overrideComponent(EmptyComponent, {
			set: {
				template: '<tds-ui-svg-icon [name]="\'application_menu\'" [height]="16" [width]="16"></tds-ui-svg-icon>'
			}
		});
		TestBed.configureTestingModule({
			declarations: [EmptyComponent, UISVGIconDirectiveDirective]
		}).compileComponents();
	}));

	beforeEach(() => {
		fixture = TestBed.createComponent(EmptyComponent);
		comp = fixture.componentInstance;
		de = fixture.debugElement.query(By.css('tds-ui-svg-icon'));
	});

	it('should create component', () => expect(comp).toBeDefined());

	it('should have a SVG element', () => {
		fixture.detectChanges();
		const svgElement = de.nativeElement;
		expect(svgElement.innerHTML).toMatch(/application_menu/i, 'Should say something about "application_menu"');
	});

});