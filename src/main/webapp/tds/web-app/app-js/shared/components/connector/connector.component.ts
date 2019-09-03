import {Component, Input, Output, OnInit, EventEmitter} from '@angular/core';
import { Connector } from './model/connector.model';

@Component({
	selector: 'tds-connector',
	template: `
        <div class="tds-connector-component">
            <button class="add-connector" (click)="onAdd()">Add Connector</button>
            <div>
                <div class="header">
                    <div class="component-type">Type</div>
                    <div class="component-label">Label</div>
                    <div class="component-label-position">Label Position</div>
                    <div class="component-position-x">Conn Pos X</div>
                    <div class="component-position-y">Conn Pos Y</div>
                    <div class="delete-command">&nbsp;</div>
                </div>
            </div>
            <div class="connections">
                <div *ngFor="let connector of connectors;let index = index;trackBy:trackByIndex;" class="data-row">
                    <div class="component-type">
                        <kendo-dropdownlist
                                class="select"
                                name="modelType"
                                (selectionChange)="reportChanges()"
                                [data]="types"
                                [(ngModel)]="connectors[index].type">
                        </kendo-dropdownlist>
                    </div>
                    <div class="component-label">
                        <input type="text"
							   (blur)="reportChanges()"
                               [(ngModel)]="connectors[index].label"
                               name="labelValue">
                    </div>
                    <div class="component-label-position">
                        <kendo-dropdownlist
                                class="select"
								(selectionChange)="reportChanges()"
                                name="modelPosition"
                                [data]="positions"
                                [(ngModel)]="connectors[index].labelPosition">
                        </kendo-dropdownlist>
                    </div>
                    <div class="component-position-x">
                        <input type="number"
                               (blur)="reportChanges()"
							   [(ngModel)]="connectors[index].xPosition" name="xPosition">
                    </div>
                    <div class="component-position-y">
                        <input type="number"
                               (blur)="reportChanges()"
							   [(ngModel)]="connectors[index].yPosition" name="yPosition">
                    </div>
                    <div class="delete-command"><span class="glyphicon glyphicon-remove delete-connector" (click)="onDelete(index)" title="Delete connector"></span></div>
                </div>
            </div>
        </div>
	`
})
export class ConnectorComponent implements OnInit {
	@Input('connectors') originalConnectors: Connector[];
	@Output('modelChange') modelChange = new EventEmitter<any>();
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
		this.connectors = [...this.originalConnectors];
	}

	onAdd(): void {
		const count = this.connectors.length;
		const connector: Connector = { id: null, type: 'Ether', label: `Connector${count + 1}`, labelPosition: 'Right', xPosition: 0, yPosition: 0  };
		this.connectors.push(connector);
		this.reportChanges();
	}

	trackByIndex(index: number, obj: any): any {
		return index;
	}

	onDelete(index: number): void {
		this.connectors.splice(index, 1);
		this.reportChanges();
	}

	reportChanges(): void {
		const added = this.connectors.filter((item) => item.id === null);

		const edited = this.connectors.filter((item) => {
			return this.originalConnectors.find((original) => original.id === item.id);
		});

		const deleted = this.originalConnectors.filter((item) => {
			return !this.connectors.find((original) => original.id === item.id);
		});

		console.log({added, edited, deleted});
		this.modelChange.emit({added, edited, deleted});
	}

}