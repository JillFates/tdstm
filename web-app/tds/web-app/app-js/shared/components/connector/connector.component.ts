import {Component, Input, OnInit} from '@angular/core';
import { Connector } from './model/connector.model';

@Component({
	selector: 'tds-connector',
	templateUrl: '../tds/web-app/app-js/shared/components/connector/connector.component.html'
})
export class ConnectorComponent implements OnInit {
	@Input('connectors') originalConnectors: Connector[];
	positions: string[];
	types: string[];
	modelTypeSelected: string;
	connectors: Connector[];

	constructor() {
		this.positions = ['Right', 'Left', 'Top', 'Bottom'];
		this.types = ['Ether', 'Serial', 'Power', 'Fiber', 'SCSI', 'USB', 'KVM', 'ILO', 'Management', 'SAS', 'Other'];
		this.modelTypeSelected = null;
	}

	ngOnInit() {
		console.log(this.connectors);
		this.connectors = [...this.originalConnectors];
	}

	onAdd(): void {
		const count = this.connectors.length;
		const connector: Connector = { type: 'Ether', label: `Connector${count + 1}`, labelPosition: 'Right', xPosition: 0, yPosition: 0  };
		this.connectors.push(connector);
	}

	trackByIndex(index: number, obj: any): any {
		return index;
	}

	onDelete(index: number): void {
		console.log('on delete');
		this.connectors.splice(index, 1);
	}
}