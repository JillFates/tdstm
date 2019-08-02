// Angular
import {
	Component,
	ElementRef,
	HostListener,
	OnDestroy,
	OnInit,
	Renderer2,
} from '@angular/core';

declare var jQuery: any;

@Component({
	selector: 'tds-asset-export',
	templateUrl: 'export.component.html',
	styles: []
})
export class ExportComponent implements OnInit, OnDestroy {
	protected gridColumns: any[];
	constructor() {
		// comment
	}

	ngOnInit() {
		// comment
	}

	/**
	 * unsubscribe from all subscriptions on destroy hook.
	 * @HostListener decorator ensures the OnDestroy hook is called on events like
	 * Page refresh, Tab close, Browser close, navigation to another view.
	 */
	@HostListener('window:beforeunload')
	ngOnDestroy(): void {
		// comment
	}
}