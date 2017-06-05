import {Component, ViewChild} from '@angular/core';
import {ComponentFixture, TestBed, async} from '@angular/core/testing';
import {By} from '@angular/platform-browser';
import {DebugElement} from '@angular/core';

import {RichTextEditorComponent} from '../shared/modules/rich-text-editor/rich-text-editor.component';

@Component({
	moduleId: module.id,
	selector: 'container',
	template: `<rich-text-editor #htmlTextField [elementId]="'htmlText'" [required]="true" 
    [name]="'htmlText'" [(rawValue)]="model.rawText" [(value)]="model.htmlText"></rich-text-editor>`
})
class ContainerComponent {
	@ViewChild('htmlTextField') htmlText: RichTextEditorComponent;

	model: any = {
		rawText: '<h1>This is a title</h1><br><p>This is a paragraph</p>',
		htmlText: '<h1>This is a title</h1><br><p>This is a paragraph</p>'
	};
}

describe('RichTextEditorComponent', () => {

	let fixture: ComponentFixture<ContainerComponent>;
	let comp: ContainerComponent;
	let de: DebugElement;

	beforeEach(async(() => {
		TestBed.configureTestingModule({
			declarations: [ContainerComponent, RichTextEditorComponent],
		}).compileComponents();
	}));

	beforeEach(() => {
		fixture = TestBed.createComponent(ContainerComponent);
		comp = fixture.componentInstance;
	});

	it('should create component', () => expect(comp).toBeDefined());

	it('should define tinymce', () => {
		fixture.detectChanges();
		expect(comp.htmlText.editor).toBeDefined();
	});

});