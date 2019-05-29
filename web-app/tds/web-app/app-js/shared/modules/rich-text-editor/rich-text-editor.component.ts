import {
	Component,
	OnDestroy,
	AfterViewInit,
	EventEmitter,
	Input,
	Output
} from '@angular/core';

declare var tinymce: any;

@Component({
	selector: 'rich-text-editor',
	template: `<textarea name="{{name}}" id="{{elementId}}" [required]="required">{{value}}</textarea>`,
	exportAs: 'richTextEditor'
})
export class RichTextEditorComponent implements AfterViewInit, OnDestroy {

	private isPristine = true;

	@Input() elementId: String;
	@Input() name: String;
	@Input() required: boolean;
	@Input() readonly: boolean;

	@Input() value: any;
	@Output() valueChange = new EventEmitter<any>();

	@Input() rawValue: any;
	@Output() rawValueChange = new EventEmitter<any>();
	@Output() escHandler = new EventEmitter<void>();

	editor;

	ngAfterViewInit() {
		tinymce.init({
			selector: '#' + this.elementId,
			elementpath: false,
			statusbar: false,
			menu: {},
			height: 250,
			content_style: this.readonly ? 'body#tinymce {background-color: #fff; cursor: not-allowed}' : '',
			readonly: this.readonly,
			skin_url: '../../dist/js/vendors/tinymce/lightgray',
			setup: editor => {
				this.editor = editor;
				editor.on('keyup', (keyEvent) => {
					// fix the issue that prevents closing the window pressing esc key if this control has the focus
					if (keyEvent.key === 'Escape') {
						return this.escHandler.emit();
					}

				});
				editor.on('change', () => {
					this.saveContent();
				});
			},
		});
	}

	/**
	 * Updates the model on any change
	 */
	private saveContent(): void {
		this.isPristine = false;
		const content = this.editor.getContent();
		const rawContent = this.editor.getContent({format: 'text'});
		this.valueChange.emit(content);
		this.rawValueChange.emit(rawContent);
	}

	ngOnDestroy() {
		tinymce.remove(this.editor);
	}

	valid(): boolean {
		return this.required ? !!this.value : true;
	}

	invalid(): boolean {
		return !this.valid();
	}

	pristine(): boolean {
		return this.isPristine;
	}

	dirty(): boolean {
		return !this.pristine();
	}

	setPristine(): void {
		this.editor.setContent('');
		this.isPristine = true;
	}
}