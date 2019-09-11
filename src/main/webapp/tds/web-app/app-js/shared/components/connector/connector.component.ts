import {Component, Input, Output, OnInit, EventEmitter} from '@angular/core';
import { Connector } from './model/connector.model';

@Component({
	selector: 'tds-connector',
	template: `
        <div class="tds-connector-component">
            <tds-button-add id="btnAddConnector" class="add-connector btn-primary"
                            [tabindex]="tabindex"
                            [title]="'CONNECTORS.ADD' | translate"
                            (click)="onAdd()">
            </tds-button-add>
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
								[tabindex]="tabindex"
                                class="select"
                                name="modelType"
                                (selectionChange)="reportChanges()"
                                [data]="types"
                                [(ngModel)]="connectors[index].type">
                        </kendo-dropdownlist>
                    </div>
                    <div class="component-label">
                        <input type="text"
                               [tabindex]="tabindex"
							   (blur)="reportChanges()"
                               [(ngModel)]="connectors[index].label"
                               name="labelValue">
                    </div>
                    <div class="component-label-position">
                        <kendo-dropdownlist
                                [tabindex]="tabindex"
                                class="select"
								(selectionChange)="reportChanges()"
                                name="modelPosition"
                                [data]="positions"
                                [(ngModel)]="connectors[index].labelPosition">
                        </kendo-dropdownlist>
                    </div>
                    <div class="component-position-x">
                        <input type="number"
                               [tabindex]="tabindex"
                               (blur)="reportChanges()"
							   [(ngModel)]="connectors[index].connectorPosX" name="connectorPosX">
                    </div>
                    <div class="component-position-y">
                        <input type="number"
                               [tabindex]="tabindex"
                               (blur)="reportChanges()"
							   [(ngModel)]="connectors[index].connectorPosY" name="connectorPosY">
                    </div>
                    <div class="delete-command">
						<span class="glyphicon glyphicon-remove delete-connector" [tabindex]="tabindex" (click)="onDelete(index)" title="Delete connector"></span>
					</div>
                </div>
            </div>
        </div>
	`
})
export class ConnectorComponent implements OnInit {
	@Input('tabindex') tabindex: string;
	@Input('connectors') originalConnectors: Connector[];
	@Output('modelChange') modelChange = new EventEmitter<any>();
	index = 0;
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

	/**
	 * Add a new empty aka element
	 */
	onAdd(): void {
		const count = this.connectors.length;
		const connector: Connector = { id: null, type: 'Ether', label: `Connector${count + 1}`, labelPosition: 'Right', connectorPosX: 0, connectorPosY: 0};
		this.connectors.push(connector);
		this.reportChanges();
	}

	/**
	 * Used by ngFor in order to improve it
	 * @param {number} index
	 * @param obj
	 * @returns {any}
	 */

	trackByIndex(index: number, obj: any): any {
		return index;
	}

	/**
	 * Delete an aka and report the changes to the host component
	 * @param {number} index
	 */
	onDelete(index: number): void {
		this.connectors.splice(index, 1);
		this.reportChanges();
	}

	/**
	 * Report about aka changes to the host component
	 */
	reportChanges(): void {
		const added = this.connectors.filter((item) => item.id === null);

		const edited = this.connectors.filter((item) => {
			return this.originalConnectors.find((original) => original.id === item.id);
		});

		const deleted = this.originalConnectors.filter((item) => {
			return !this.connectors.find((original) => original.id === item.id);
		});

		const changes = {
			added: this.addConnectorOrder(added),
			edited: this.addConnectorOrder(edited),
			deleted: this.addConnectorOrder(deleted)
		};

		this.modelChange.emit(changes);
	}

	/**
	 * Add to each connector the order
	 * @param {any[]} items collection items
	 * @returns {any}
	 */
	addConnectorOrder(items: any[]): any {
		return  items.map((item: any) => {
			this.index = this.index + 1;
			return {
				id: item.id,
				type: item.type,
				label: item.label,
				labelPosition: item.labelPosition,
				xPosition: item.connectorPosX,
				yPosition: item.connectorPosY,
				connector: item.id || this.index
			};
		});
	}
}