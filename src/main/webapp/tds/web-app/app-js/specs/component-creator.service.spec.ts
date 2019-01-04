import {Component, ViewChild, ViewContainerRef, NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ComponentFixture, TestBed, async} from '@angular/core/testing';
import {By} from '@angular/platform-browser';
import {DebugElement} from '@angular/core';
import {EmptyComponent} from '../testing/empty.component';

import {ComponentCreatorService} from '../shared/services/component-creator.service';

@NgModule({
	imports: [CommonModule],
	declarations: [EmptyComponent],
	exports: [EmptyComponent],
	entryComponents: [EmptyComponent]
})
class ContainerModule {
}

@Component({
	selector: 'container-component',
	template: '<div #container></div>',
})
class ContainerComponent {
	@ViewChild('container', {read: ViewContainerRef}) view: ViewContainerRef;

	constructor(compCreatorService: ComponentCreatorService) {
		let emptyBlock = true;
	}
}

describe('ComponentCreatorService:', () => {

	let fixture: ComponentFixture<ContainerComponent>;
	let comp: ContainerComponent;
	let de: DebugElement;
	let compCreatorService: ComponentCreatorService;

	beforeEach(() => {
		TestBed.overrideComponent(EmptyComponent, {
			set: {
				template: '<p>This is a paragraph</p>'
			}
		});
	});

	beforeEach(async(() => {
		TestBed.configureTestingModule({
			imports: [ContainerModule],
			declarations: [ContainerComponent],
			providers: [ComponentCreatorService],
		}).compileComponents();
	}));

	beforeEach(() => {
		fixture = TestBed.createComponent(ContainerComponent);
		comp = fixture.componentInstance;
		compCreatorService = fixture.debugElement.injector.get(ComponentCreatorService);
	});

	it('should create and insert a component dynamically', () => {
		fixture.detectChanges();
		compCreatorService.insert(EmptyComponent, [], comp.view);
		fixture.detectChanges();
		de = fixture.debugElement.query(By.css('p'));
		expect(de).toBeDefined();
		expect(de.nativeElement.innerText).toMatch(/paragraph/i, 'Should say sometingh about "paragraph"');
	});

});