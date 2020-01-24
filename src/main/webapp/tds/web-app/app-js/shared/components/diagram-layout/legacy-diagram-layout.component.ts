import {
	Component,
	Input,
	Output,
	AfterViewInit,
	OnChanges,
	SimpleChanges,
	EventEmitter,
	ViewChild,
	ElementRef, HostListener, OnDestroy, Renderer2
} from '@angular/core';
import * as go from 'gojs';
import {ASSET_ICONS_PATH, CTX_MENU_ICONS_PATH, STATE_ICONS_PATH} from '../../../modules/taskManager/components/common/constants/task-icon-path';
import {
	Adornment,
	Binding,
	Diagram,
	InputEvent,
	Link,
	GraphObject,
	Overview,
	Panel,
	Placeholder,
	Shape,
	Size,
	Spot,
	TextBlock
} from 'gojs';
import {ITaskGraphIcon} from '../../../modules/taskManager/model/task-graph-icon.model';
import {FA_ICONS} from '../../constants/fontawesome-icons';
import {of, ReplaySubject} from 'rxjs';
import {LegacyDiagramContextMenuComponent} from './context-menu/legacy-diagram-context-menu.component';
import {IDiagramContextMenuModel, IDiagramContextMenuOption} from './model/legacy-diagram-context-menu.model';
import {IGraphTask} from '../../../modules/taskManager/model/graph-task.model';
import {NotifierService} from '../../services/notifier.service';
import {TaskTeam} from '../../../modules/taskManager/components/common/constants/task-team.constant';
import {DiagramEvent, DiagramEventAction} from './model/legacy-diagram-event.constant';
import {DiagramLayoutService} from '../../services/diagram-layout.service';
import {ILinkPath} from './model/legacy-diagram-layout.model';
import {GOJS_LICENSE_KEY} from './gojs-license';

const enum NodeTemplateEnum {
	HIGH_SCALE,
	MEDIUM_SCALE,
	LOW_SCALE
}

@Component({
	selector: 'tds-diagram-layout',
	template: `
		<div class="diagram-layout-container">
			<div
					id="diagram-layout"
					[style.height]="screenHeight"
					#diagramLayout></div>
			<div id="graph-control-btn-group">
				<button class="btn btn-block">
					<fa-icon [icon]="faIcons.faCog" size="lg"></fa-icon>
				</button>
				<button class="btn btn-block" (click)="zoomIn()">
					<fa-icon [icon]="faIcons.faSearchPlus" size="lg"></fa-icon>
				</button>
				<button class="btn btn-block" (click)="zoomOut()">
					<fa-icon [icon]="faIcons.faSearchMinus" size="lg"></fa-icon>
				</button>
			</div>
			<tds-task-context-menu
				[data]="ctxMenuData$ | async"
				#taskCtxMenu></tds-task-context-menu>
			<div
				id="overview-container"
				class="overview-container"
				[class.reset-overview-index]="resetOvIndex"
				#overviewContainer></div>
			<div id="node-tooltip" class="diagram-card"
					 [style.background]="getStatusColor(tooltipData?.status)"
           (mouseleave)="hideToolTip()"
					 #nodeTooltip>
					<div class="diagram-card-header"
							 [style.background]="getStatusColor(tooltipData?.status)">
						<h4
								class="k-align-self-center"
								style="font-weight: bold;"
						    [style.color]="getStatusTextColor(tooltipData?.status)"> {{tooltipData?.name}}</h4>
					</div>

					<div class="diagram-card-content">
						<div class="row">
							<div class="col-md-6 k-align-self-center">Status: </div> <div class="col-md-6 k-align-self-center" style="float: right;">{{tooltipData?.status}}</div>
						</div>
						<div class="row">
							<div class="col-md-6 k-align-self-center">Assigned to: </div> <div class="col-md-6 k-align-self-center" style="float: right;">{{tooltipData?.assignedTo || 'N/A'}}</div>
						</div>
						<div class="row">
							<div class="col-md-6 k-align-self-center">Team: </div> <div class="col-md-6 k-align-self-center" style="float: right;">{{tooltipData?.team || 'N/A'}}</div>
						</div>
						<div class="row">
							<div class="col-md-6 k-align-self-center">Task Number: </div> <div class="col-md-6 k-align-self-center" style="float: right;">{{tooltipData?.number}}</div>
						</div>
					</div>
			</div>
			<button id="show-full-graph" *ngIf="showFullGraphBtn" (click)="showFullGraph()">Back to Full Graph</button>
		</div>`,
})
export class LegacyDiagramLayoutComponent implements AfterViewInit, OnChanges, OnDestroy {
	@Input() nodeData: any = {};
	@Input() nodeTemplateOpts: go.Node;
	@Input() linkTemplateOpts: go.Link;
	@Input() layoutOpts: go.Layout;
	@Input() currentUser: any;
	@Input() contextMenuOptions: IDiagramContextMenuOption;
	@Output() nodeClicked: EventEmitter<any> = new EventEmitter<any>();
	@Output() editClicked: EventEmitter<string | number> = new EventEmitter<string | number>();
	@Output() showTaskDetailsClicked: EventEmitter<string | number> = new EventEmitter<string | number>();
	@Output() backTofullGraph: EventEmitter<void> = new EventEmitter<void>();
	@Output() nodeUpdated: EventEmitter<any> = new EventEmitter<any>();
	@Output() diagramClicked: EventEmitter<void> = new EventEmitter<void>();
	@ViewChild('diagramLayout', {static: false}) diagramLayout: ElementRef;
	@ViewChild('overviewContainer', {static: false}) overviewContainer: ElementRef;
	@ViewChild('taskCtxMenu', {static: false}) taskCtxMenu: LegacyDiagramContextMenuComponent;
	@ViewChild('nodeTooltip', {static: false}) nodeTooltip: ElementRef;
	stateIcons = STATE_ICONS_PATH;
	assetIcons = ASSET_ICONS_PATH;
	ctxMenuIcons = CTX_MENU_ICONS_PATH;
	faIcons = FA_ICONS;
	diagram: go.Diagram;
	myModel: go.GraphLinksModel;
	direction = 0;
	diagramAvailable = false;
	tasks: any[];
	actualNodeTemplate: number;
	diagramOverview: Overview;
	resetOvIndex: boolean;
	ctxMenuData$: ReplaySubject<IDiagramContextMenuModel> = new ReplaySubject();
	screenHeight: any;
	largeArrayRemaining: boolean;
	DATA_CHUNKS_SIZE = 200;
	unsubscribe$: ReplaySubject<void> = new ReplaySubject();
	tooltipData: IGraphTask;
	neighborMove: boolean;
	showFullGraphBtn: boolean;
	remainingData: any;
	remainingLinks: any;

	constructor(
		private notifierService: NotifierService,
		private renderer: Renderer2
		) {
		Diagram.licenseKey = GOJS_LICENSE_KEY;
		this.onResize();
	}

	/**
	 * Detect changes to update nodeData and linksPath accordingly
	 **/
	ngOnChanges(simpleChanges: SimpleChanges): void {
		if (simpleChanges) {
			if (simpleChanges.nodeData
				&& (!!simpleChanges.nodeData.currentValue.data && !!simpleChanges.nodeData.currentValue.linksPath)
				&& !(simpleChanges.nodeData.firstChange && simpleChanges.nodeData.firstChange)
			) {
				this.loadAll();
			}
		}
	}

	@HostListener('window:resize', ['$event'])
	onResize(event?: any) {
		this.screenHeight = `${window.innerHeight - 230}px`;
	}

	ngAfterViewInit() {
		if (this.nodeData.data && this.nodeData.linksPath) {
			this.loadAll();
		}
	}

	/**
	 * Load all data related to the diagram
	 **/
	loadAll(): void {
		this.loadModel();
		this.initialiseDiagramContainer();
		this.generateDiagram();
	}

	/**
	 * Load data model used by the diagram
	 **/
	loadModel(): void {
		if (this.nodeData.data.length < 600) {
			this.myModel = new go.GraphLinksModel(this.nodeData.data, this.nodeData.linksPath);
			this.myModel.nodeKeyProperty = 'key';
		} else {
			this.remainingData = this.nodeData.data.slice();
			this.remainingLinks = this.nodeData.linksPath.slice();
			this.myModel = new go.GraphLinksModel(
				this.remainingData.splice(0, this.DATA_CHUNKS_SIZE),
				this.remainingLinks.splice(0, this.DATA_CHUNKS_SIZE));
			this.myModel.nodeKeyProperty = 'key';
			this.largeArrayRemaining = true;
		}
	}

	/**
	 * Handler for large array of nodes and links, this helps improve rendering times
	 */
	handleLargeDataArray(): void {
		this.diagram.removeDiagramListener(DiagramEvent.ANIMATION_FINISHED, null);
		const dataCopy = this.nodeData.data.slice();
		const linksCopy = this.nodeData.linksPath.slice();
		const dataChunks = [];
		const linkChunks = [];

		while (dataCopy.length > this.DATA_CHUNKS_SIZE) {
			dataChunks.push(dataCopy.splice(0, this.DATA_CHUNKS_SIZE));
		}
		dataChunks.push(dataCopy);

		while (linksCopy.length > this.DATA_CHUNKS_SIZE) {
			linkChunks.push(linksCopy.splice(0, this.DATA_CHUNKS_SIZE));
		}
		linkChunks.push(linksCopy);

		of(...dataChunks)
			.subscribe(chunk => this.addNewNodesToDiagram(chunk));

		of(...linkChunks)
			.subscribe(chunk => this.addNewLinksToDiagram(chunk));

		this.largeArrayRemaining = false;

	}

	/**
	 * Add nodes to diagram programmatically
	 * @param c
	 */
	addNewNodesToDiagram(c: any): void {
		this.diagram.commitTransaction('add node data');
		this.myModel.addNodeDataCollection(c);
		this.diagram.commitTransaction('added new node data');
	}

	/**
	 * Add links to diagram programmatically
	 * @param c
	 */
	addNewLinksToDiagram(c: any): void {
		this.diagram.commitTransaction('add link data');
		this.myModel.addLinkDataCollection(c);
		this.diagram.commitTransaction('added new link data');
	}

	/**
	 * set the element used as container to hold the diagram
	 **/
	initialiseDiagramContainer(): void {
		if (!this.diagram) {
			this.diagram = new Diagram(this.diagramLayout.nativeElement);
		}
	}

	/**
	 * Generate Diagram canvas
	 **/
	generateDiagram(): void {
		this.diagram.model.nodeDataArray = [];
		this.diagram.startTransaction('generateDiagram');
		this.diagram.initialAutoScale = Diagram.Uniform;
		this.diagram.initialDocumentSpot = Spot.Center;
		this.diagram.initialViewportSpot = Spot.Center;
		this.diagram.hasHorizontalScrollbar = false;
		this.diagram.hasVerticalScrollbar = false;
		this.diagram.allowZoom = true;
		this.setDiagramNodeTemplate();
		this.setDiagramLinksTemplate();
		this.diagram.allowSelect = true;
		this.diagram.toolManager.hoverDelay = 200;
		this.diagram.click = () => this.diagramClicked.emit();
		this.diagram.commitTransaction('generateDiagram');
		this.setTreeLayout();
		this.diagram.model = this.myModel;
		this.diagramAvailable = true;
		this.overrideMouseWheel();
		this.overviewTemplate();
		this.diagramListeners();
		this.overrideDoubleClick();
		this.diagram.zoomToFit();
	}

	/**
	 * Diagram listeners to be used for custom functionality
	 */
	diagramListeners(): void {
			this.diagram.addDiagramListener(DiagramEvent.ANIMATION_FINISHED, () => {

				if (this.neighborMove) {
					this.neighborMove = false;
					this.notifierService.broadcast({name: DiagramEventAction.ANIMATION_FINISHED});
				}

				if (this.largeArrayRemaining) {
					this.diagram.animationManager.isEnabled = false;
					this.handleLargeDataArray();
					setTimeout(() => {
						this.diagram.animationManager.isEnabled = true;
					}, 1000);
				}

			});

			if (!this.largeArrayRemaining) {
				if (this.diagram.linkTemplate.routing !== go.Link.AvoidsNodes) { this.setDiagramLinksTemplate(); }
				if (!this.diagram.animationManager.isEnabled) { this.diagram.animationManager.isEnabled = true; }
			}

			this.diagram.addDiagramListener(DiagramEvent.BACKGROUND_SINGLE_CLICKED, () => {
				this.hideToolTip();
				this.diagram.toolManager.contextMenuTool.hideContextMenu();
			});
			this.diagram.addDiagramListener(DiagramEvent.BACKGROUND_DOUBLE_CLICKED, () => {
				this.hideToolTip();
				this.diagram.toolManager.contextMenuTool.hideContextMenu();
			});
			this.diagram.addDiagramListener(DiagramEvent.BACKGROUND_CONTEXT_CLICKED, () => {
				this.hideToolTip();
				this.diagram.toolManager.contextMenuTool.hideContextMenu();
			});
			this.diagram.addDiagramListener(DiagramEvent.OBJECT_SINGLE_CLICKED, () => {
				this.hideToolTip();
				this.diagram.toolManager.contextMenuTool.hideContextMenu();
			});
			this.diagram.addDiagramListener(DiagramEvent.OBJECT_DOUBLE_CLICKED, () => {
				this.hideToolTip();
				this.diagram.toolManager.contextMenuTool.hideContextMenu();
			});
			this.diagram.addDiagramListener(DiagramEvent.OBJECT_CONTEXT_CLICKED, () => {
				this.hideToolTip();
			});
	}

	/**
	 * Node click handler to set the adornments or any other operation on nodes when clicked
	 **/
	onNodeClick(inputEvent: InputEvent, obj: any): void {
		if (obj && obj.part && obj.part.data) {
			obj.selectionAdornmentTemplate = this.selectionAdornmentTemplate();
			this.nodeClicked.emit(obj.part.data);
			this.neighborMove = true;
		}
	}

	/**
	 * Diagram overview (Minimap)
	 **/
	overviewTemplate() {
		if (!this.diagramOverview) {
			this.diagramOverview = new Overview(this.overviewContainer.nativeElement);
			this.diagramOverview.observed = this.diagram;
			this.diagramOverview.contentAlignment = go.Spot.Center;
		}
	}

	/**
	 * Sets the Layered Digraph Layout to the Diagram
	 * @param {any} opts > optional configuration to use for the layout
	 **/
	setLayeredDigraphLayout(opts?: any): void {
		const ldl = new go.LayeredDigraphLayout();
		ldl.direction = 0;
		ldl.layerSpacing = 25;
		ldl.columnSpacing = 25;
		ldl.cycleRemoveOption = go.LayeredDigraphLayout.CycleDepthFirst;

		this.diagram.commit(d => d.layout = ldl);
	}

	/**
	 * Sets the Tree Layout to the Diagram
	 * @param {any} opts > optional configuration to use for the layout
	 **/
	setTreeLayout(opts?: any): void {
		const treeLayout = new go.TreeLayout();
		treeLayout.treeStyle = go.TreeLayout.StyleLayered;
		treeLayout.layerStyle = go.TreeLayout.LayerIndividual;
		treeLayout.angle = 0;
		treeLayout.nodeSpacing = 20;
		treeLayout.sorting = go.TreeLayout.SortingForwards;
		treeLayout.compaction = go.TreeLayout.CompactionBlock;
		treeLayout.rowSpacing = 25;
		treeLayout.rowIndent = 10;
		treeLayout.layerSpacing = 50;
		this.diagram.commit(d => d.layout = treeLayout);
	}

	/**
	 * Sets the Force Directed Layout to the Diagram
	 * @param {any} opts > optional configuration to use for the layout
	 **/
	setForceDirectedLayout(opts?: any): void {
		const forceDirectedLayout = new go.ForceDirectedLayout();
		forceDirectedLayout.arrangementSpacing = new go.Size(100, 105);
		this.diagram.commit(d => d.layout = forceDirectedLayout);
	}

	/**
	 * Sets the template for each node in the Diagram
	 **/
	setDiagramNodeTemplate(): void {
		if (this.nodeData.data && this.nodeData.data.length >= 600) {
			this.lowScaleNodeTemplate();
		} else if (this.nodeData.data && this.nodeData.data.length >= 300) {
			this.mediumScaleNodeTemplate();
		} else {
			this.diagram.nodeTemplate = this.setNodeTemplate();
		}
	}

	/**
	 * Sets the template for each node link in the Diagram
	 **/
	setDiagramLinksTemplate(): void {
		this.diagram.linkTemplate = this.linkTemplate();
	}

	/**
	 * Links template configuration
	 * @param {Link} templateOpts > optional configuration to use for the template
	 * @param {Shape} linkShapeOpts > optional shape for links
	 **/
	linkTemplate(templateOpts?: go.Link, linkShapeOpts?: go.Shape): go.Link {

		// If template configuration is provided, return it as the template to use
		if (templateOpts) {
			if (linkShapeOpts) { templateOpts.add(linkShapeOpts); }
			return templateOpts;
		}

		const linkTemplate = new go.Link();
		linkTemplate.routing = this.largeArrayRemaining ? go.Link.Orthogonal : go.Link.AvoidsNodes;
		linkTemplate.curve = Link.Bezier;

		const linkShape = new go.Shape();
		linkShape.strokeWidth = 3;
		linkShape.stroke = '#ddd';
		const arrowHead = new Shape();
		arrowHead.strokeWidth = 2;
		arrowHead.stroke = '#afafaf';
		arrowHead.fill = '#afafaf';
		arrowHead.toArrow = 'Standard';

		linkTemplate.add(linkShape);
		linkTemplate.add(arrowHead);
		linkTemplate.selectionAdornmentTemplate = this.linkSelectionAdornmentTemplate();

		return linkTemplate;
	}

	/**
	 * Links template configuration
	 **/
	setDirection(dir: any): void {
		this.direction = dir.target.value;
	}

	/**
	 * Node template configuration
	 * @param {any} templateOpts > optional configuration to use for the template
	 **/
	setNodeTemplate(templateOpts?: go.Node): go.Node {

		// If template configuration is provided, return it as the template to use
		if (templateOpts) {
			return templateOpts;
		}

		this.actualNodeTemplate = NodeTemplateEnum.HIGH_SCALE;

		const node = new go.Node(go.Panel.Horizontal);
		node.selectionAdorned = true;
		node.padding = new go.Margin(0, 0, 0, 0);
		node.add(this.containerPanel());
		node.contextMenu = this.contextMenu();
		node.toolTip = this.createTooltip();
		node.mouseLeave = () => this.hideToolTip();

		// if onNodeClick function is assigned directly to click handler
		// 'this' loses the binding to the component with onNodeClicked function
		node.click = (i, o) => this.onNodeClick(i, o);

		node.selectionAdornmentTemplate = this.selectionAdornmentTemplate();

		return node;
	}

	/**
	 * Node Adornment template configuration
	 * @param {go.Node} node > optional node to add the adornment to
	 **/
	selectionAdornmentTemplate(node?: go.Node): Adornment {
		const selAdornmentTemplate = new Adornment(Panel.Auto);
		selAdornmentTemplate.selectionAdorned = true;
		if (node) { selAdornmentTemplate.adornedObject = node; }

		const selAdornmentShape = new Shape();
		selAdornmentShape.figure = 'RoundedRectangle';
		selAdornmentShape.fill = null;
		selAdornmentShape.stroke = 'red';
		selAdornmentShape.strokeWidth = 4;

		const placeholder = new Placeholder();

		selAdornmentTemplate.add(selAdornmentShape);
		selAdornmentTemplate.add(placeholder);

		return selAdornmentTemplate;
	}

	/**
	 * Node Adornment template configuration
	 * @param {go.Node} node > optional node to add the adornment to
	 **/
	linkSelectionAdornmentTemplate(node?: go.Node): Adornment {
		const selAdornmentTemplate = new Adornment(Panel.Link);
		selAdornmentTemplate.selectionAdorned = true;
		if (node) { selAdornmentTemplate.adornedObject = node; }

		const linkShape = new go.Shape();
		linkShape.isPanelMain = true;
		linkShape.strokeWidth = 5;
		linkShape.stroke = 'red';
		const arrowHead = new Shape();
		arrowHead.strokeWidth = 4;
		arrowHead.stroke = '#af1102';
		arrowHead.toArrow = 'Standard';

		selAdornmentTemplate.add(linkShape);
		selAdornmentTemplate.add(arrowHead);
		return selAdornmentTemplate;
	}

	/**
	 * Node Adornment template configuration for neighbors
	 **/
	neighborAdornmentTemplate(): Adornment {
		const selAdornmentTemplate = new Adornment(Panel.Auto);
		selAdornmentTemplate.selectionAdorned = true;

		const selAdornmentShape = new Shape();
		selAdornmentShape.figure = 'RoundedRectangle';
		selAdornmentShape.fill = null;
		selAdornmentShape.stroke = 'red';
		selAdornmentShape.strokeWidth = 4;
		selAdornmentShape.strokeDashArray = [3, 2];

		const placeholder = new Placeholder();

		selAdornmentTemplate.add(selAdornmentShape);
		selAdornmentTemplate.add(placeholder);

		return selAdornmentTemplate;
	}

	/**
	 * Node inner container to draw borders to the panel
	 **/
	containerShape(): go.Shape {
		const container = new go.Shape();
		container.figure = 'RoundedRectangle';
		container.strokeWidth = 2;
		container.stroke = '#ddd';
		container.fill = 'white';
		container.margin = new go.Margin(0, 0, 0, 0);

		container.mouseOver = () => {
			this.diagram.currentCursor = 'pointer';
		};
		container.mouseLeave = () => {
			this.diagram.currentCursor = 'none';
		};

		return container;
	}

	/**
	 * Node outer panel container to hold individual shapes related to the node
	 **/
	containerPanel(): go.Panel {
		const panel = new go.Panel(go.Panel.Auto);
		panel.background = '#fff';
		panel.padding = new go.Margin(0, 0, 0, 0);

		panel.mouseOver = () => {
			this.diagram.currentCursor = 'pointer';
		};
		panel.mouseLeave = () => {
			this.diagram.currentCursor = 'none';
		};
		panel.add(this.containerShape());
		panel.add(this.panelBody());

		return panel;
	}

	/**
	 * Node panel body holding the node content
	 **/
	panelBody(): go.Panel {
		const panel = new go.Panel(go.Panel.Horizontal);
		panel.padding = new go.Margin(0, 0, 0, 0);
		panel.margin = new go.Margin(0, 0, 0, 0);

		panel.mouseOver = () => {
			this.diagram.currentCursor = 'pointer';
		};
		panel.mouseLeave = () => {
			this.diagram.currentCursor = 'none';
		};
		panel.add(this.iconShape());
		panel.add(this.assetIconShape());
		panel.add(this.textBlockShape());

		return panel;
	}

	/**
	 * Icon shape configuration
	 * @param {any} options > optional configuration to use for the icon shape
	 **/
	iconShape(options?: any): go.TextBlock {
		if (options) { return options; }

		const  iconShape = new go.TextBlock();
		// iconShape.figure = 'RoundedRectangle';
		// iconShape.isGeometryPositioned = true;
		// iconShape.strokeWidth = 2;
		iconShape.textAlign = 'center';
		iconShape.verticalAlignment = go.Spot.Center;
		iconShape.margin = new go.Margin(0, 0, 0, 5);
		iconShape.desiredSize = new go.Size(35, 35);
		iconShape.font = '25px FontAwesome';

		iconShape.mouseOver = () => {
			this.diagram.currentCursor = 'pointer';
		};
		iconShape.mouseLeave = () => {
			this.diagram.currentCursor = 'none';
		};

		iconShape.bind(new Binding('text', 'status',
			(val: string) => this.getIcon(this.stateIcons[val.toLowerCase()])));

		iconShape.bind(new Binding('stroke', 'status',
			(val: string) => this.getIconColor(this.stateIcons[val.toLowerCase()])));

		iconShape.bind(new Binding('fill', 'status',
			(val: string) => this.getIconColor(this.stateIcons[val.toLowerCase()])));

		iconShape.bind(new Binding('background', 'status',
			(val: string) => this.getBackgroundColor(this.stateIcons[val.toLowerCase()])));

		return iconShape;
	}

	/**
	 * Icon shape configuration for 'asset' property
	 * @param {any} options > optional configuration to use for the icon shape
	 **/
	assetIconShape(options?: any): go.TextBlock {
		if (options) { return options; }

		const  assetIconShape = new go.TextBlock();
		assetIconShape.name = 'AssetIconShape';
		assetIconShape.textAlign = 'center';
		assetIconShape.verticalAlignment = go.Spot.Center;
		assetIconShape.margin = new go.Margin(0, 0, 0, 5);
		assetIconShape.desiredSize = new go.Size(35, 35);
		assetIconShape.font = '25px FontAwesome';

		assetIconShape.bind(new Binding('text', 'asset',
			(val: any) => {
				if (val) {
					const type = !!val.assetType ? val.assetType.toLowerCase() : val.type && val.type.toLowerCase();
					return this.getIcon(this.assetIcons[type]);
				} else {
					return this.assetIcons.unknown.iconAlt;
				}
			}));

		assetIconShape.bind(new Binding('stroke', 'asset',
			(val: any) => {
				if (val) {
					const type = !!val.assetType ? val.assetType.toLowerCase() : val.type && val.type.toLowerCase();
					return this.getIconColor(this.assetIcons[type]);
				} else {
					return this.assetIcons.unknown.color;
				}
			}));

		assetIconShape.bind(new Binding('fill', 'asset',
			(val: any) => {
				if (val) {
					const type = !!val.assetType ? val.assetType.toLowerCase() : val.type && val.type.toLowerCase();
					return this.getIconColor(this.assetIcons[type]);
				} else {
					return this.assetIcons.unknown.color;
				}
			}));

		assetIconShape.bind(new Binding('background', 'asset',
			(val: any) => {
				if (val) {
					const type = val.assetType ? val.assetType.toLowerCase() : val.type && val.type.toLowerCase();
					return this.getBackgroundColor(this.assetIcons[type]);
				} else {
					return this.assetIcons.unknown.background;
				}
			}));

		return assetIconShape;
	}

	/**
	 * Text block configuration
	 * @param {any} options > optional configuration to use for the text block
	 **/
	textBlockShape(options?: any): TextBlock {
		if (options) { return options; }

		const textBlock = new TextBlock();
		textBlock.maxSize = new Size(250, 15);
		textBlock.margin = 8;
		textBlock.stroke = 'black';
		textBlock.font = '16px sans-serif';
		textBlock.overflow = TextBlock.OverflowEllipsis;
		textBlock.bind(new Binding('text', 'name'));
		textBlock.bind(new Binding('name', 'assignedTo'));

		textBlock.mouseOver = (e: InputEvent, obj: TextBlock) => {
			this.diagram.currentCursor = 'pointer';
			if ((obj.name && obj.name.length > 0) && obj.name.toLowerCase() === 'automated task') {
				obj.stroke = '#ddd';
				obj.font = 'bold 16px sans-serif';
			} else {
				obj.font = 'bold 16px sans-serif';
			}

			if (obj.text && obj.text.length > 26) {
				obj.toolTip = this.textBlockTooltip(obj.text);
			}
		};

		textBlock.mouseLeave = (e: InputEvent, obj: TextBlock) => {
			obj.stroke = 'black';
			obj.font = '16px sans-serif';
			this.diagram.currentCursor = 'pointer';
		};
		return textBlock;
	}

	textBlockTooltip(tooltipText: string) {
		const $ = go.GraphObject.make;
		return $(Adornment,
			$(TextBlock,
				{
					text: tooltipText,
					background: '#ddd',
					stroke: 'black',
					font: '14px sans-serif'
				}
			)
	); // end Adornment
	}

	/**
	 * Icons handler to parse to geometry object to be used for the nodes
	 * @param {ITaskGraphIcon} icon > icon to use as the icon shape
	 **/
	getIcon(icon: ITaskGraphIcon): any {
		if (!icon) {
			return this.assetIcons.unknown.iconAlt;
		}
		return icon.iconAlt;
	}

	/**
	 * Status background handler to get background color for an icon by status
	 * @param {ITaskGraphIcon} icon > icon from which to get background color
	 **/
	getIconColor(icon: ITaskGraphIcon): string {
		return (icon && icon.color) || this.assetIcons.unknown.color;
	}

	/**
	 * Status background handler to get background color for an icon by status
	 * @param {ITaskGraphIcon} icon > icon from which to get background color
	 **/
	getBackgroundColor(icon: ITaskGraphIcon): string {
		return (icon && icon.background) || this.assetIcons.unknown.background;
	}

	/**
	 * Zoom in on the diagram
	 **/
	zoomIn(): void {
		this.diagram.commandHandler.increaseZoom(1.2);
		const input = new go.InputEvent();
		input.control = true;
		this.setNodeTemplateByScale(this.diagram.scale, input);
	}

	/**
	 * Zoom out on the diagram
	 **/
	zoomOut(): void {
		this.diagram.commandHandler.decreaseZoom(0.8);
		const input = new go.InputEvent();
		input.control = true;
		this.setNodeTemplateByScale(this.diagram.scale, input);
	}

	/**
	 * highlight nodes on the diagram based on passed filter
	 **/
	highlightNodes(filter?: (x: go.Node) => boolean, highlightLinks?: boolean): void {
		this.diagram.commit(d => {
			const highlightCollection = d.nodes.filter(filter);
			if (highlightCollection) {
				d.selectCollection(highlightCollection);
				if (highlightLinks) {
					highlightCollection.each(n => n.linksConnected.each(l => {
						l.selectionAdornmentTemplate = this.linkSelectionAdornmentTemplate();
						l.isSelected = true;
					}));
				}
				if (highlightCollection.count > 0 && highlightCollection.first()) {
					d.centerRect(highlightCollection.first().actualBounds);
				} else {
					d.clearSelection();
				}
			}
		})
	}

	/**
	 * highlight all nodes on the diagram
	 **/
	highlightAllNodes(): void {
		this.diagram.commit(d => {
			d.selectCollection(d.nodes);
		});
	}

	/**
	 * highlight nodes by asset type on the diagram
	 **/
	highlightNodesByAssetType(matches: any[]): void {
		this.diagram.commit(d => {
			const highlightCollection = d.nodes.filter(f => !!matches.find(m => m === f.data.asset && f.data.asset.assetType));
			d.selectCollection(highlightCollection);
			if (highlightCollection.count > 0 && highlightCollection.first()) {
				d.centerRect(highlightCollection.first().actualBounds);
			}
		});
	}

	/**
	 * highlight nodes by status type on the diagram
	 **/
	highlightNodesByStatus(matches: any[]): void {
		this.diagram.commit(d => {
			const highlightCollection = d.nodes.filter(f => !!matches.find(m => m === f.data.status));
			d.selectCollection(highlightCollection);
			if (highlightCollection.count > 0 && highlightCollection.first()) {
				d.centerRect(highlightCollection.first().actualBounds);
			}
		});
	}

	/**
	 * highlight nodes by team on the diagram
	 **/
	highlightNodesByTeam(matches: string): void {
		this.diagram.commit(d => {
			let highlightCollection: any;

			if (matches && matches === TaskTeam.ALL_TEAMS) {
				d.clearSelection();
			} else if (matches === TaskTeam.NO_TEAM_ASSIGNMENT) {
				highlightCollection = d.nodes.filter(f => !f.data.team);
			} else {
				highlightCollection = d.nodes.filter(f => matches === f.data.team);
			}

			if (highlightCollection) {
				d.selectCollection(highlightCollection);
				if (highlightCollection.count > 0 && highlightCollection.first()) {
					d.centerRect(highlightCollection.first().actualBounds);
				} else {
					d.clearSelection();
				}
			}
		});
	}

	/**
	 * highlight filter, every node that has a description matching the text received will be highlighted
	 * @param {string} match > criteria to highlight
	 * @param {string} team > optional criteria to filter nodes to highlight
	 **/
	highlightNodesByText(match: string, team?: string): void {
		this.diagram.commit(d => {
			if ((!match || match.length < 1) && (!team || team.length < 1)) { return d.clearSelection(); }
			let highlightCollection: any;

			if ((match && match.length > 0) && (team && team.length > 0)) {
				if (team === TaskTeam.ALL_TEAMS) {
					highlightCollection = d.nodes
						.filter(f => (!!f.data.name.toLowerCase().includes(match.toLowerCase())
							|| (f.data.assignedTo && !!f.data.assignedTo.toLowerCase().includes(match.toLowerCase())))
						);
				} else if (team === TaskTeam.NO_TEAM_ASSIGNMENT) {
					highlightCollection = d.nodes
						.filter(f => (!!f.data.name.toLowerCase().includes(match.toLowerCase())
								|| f.data.assignedTo && !!f.data.assignedTo.toLowerCase().includes(match.toLowerCase()))
							&& !f.data.team);
				} else {
					highlightCollection = d.nodes
						.filter(f => {
							return (!!f.data.name.toLowerCase().includes(match.toLowerCase())
								|| (f.data.assignedTo && !!f.data.assignedTo.toLowerCase().includes(match.toLowerCase())))
							&& f.data.team && !!f.data.team.includes(team)
						});
				}
			} else if ((!match || match.length < 1) && (team && team.length > 0)) {
				highlightCollection = d.nodes.filter(f => team === f.data.team);
			} else {
				highlightCollection = d.nodes.filter(f => f.data.name.toLowerCase().includes(match.toLowerCase()));
			}

			if (highlightCollection) {
				d.selectCollection(highlightCollection);
				if (highlightCollection.count > 0 && highlightCollection.first()) {
					d.centerRect(highlightCollection.first().actualBounds);
				} else {
					d.clearSelection();
				}
			}
		});
	}

	/**
	 * highlight nodes by cycles
	 * @param {number[]} cycles > array of ids on that are in a cycle
	 **/
	highlightNodesByCycle(cycles: number[]): void {
		this.diagram.commit(d => {
			const highlightCollection = d.nodes.filter(f => !!cycles.find(m => m === f.data.id));
			if (highlightCollection.count > 0 && highlightCollection.first()) {
				d.selectCollection(highlightCollection);
				highlightCollection.each(n => n.linksConnected.each(l => {
					l.selectionAdornmentTemplate = this.linkSelectionAdornmentTemplate();
					l.isSelected = true;
				}));
				d.centerRect(highlightCollection.first().actualBounds);
			}
		});
	}

	/**
	 * highlight nodes by custom field and value
	 * @param {string} field - name of the field
	 * @param {any} value - value to match against
	 **/
	highlightBy(field: string, value: any): void {
		this.diagram.commit(d => {
			const highlightCollection = d.nodes && d.nodes.filter(n => n.data[field] && n.data[field] === value);
			if (highlightCollection && highlightCollection.count > 0) {
				d.selectCollection(highlightCollection);
				d.centerRect(highlightCollection.first().actualBounds);
			}
		})
	}

	/**
	 * Clear highlighted nodes
	 **/
	clearHighlights(): void {
		this.diagram.commit(d => d.clearSelection())
	}

	/**
	 * Override mousewheel handler to add the zooming scales
	 **/
	overrideMouseWheel(): void {
		const tool = this.diagram.currentTool;
		tool.standardMouseWheel = () => {
			go.Tool.prototype.standardMouseWheel.call(tool);
			this.setNodeTemplateByScale(this.diagram.scale, this.diagram.lastInput);
		};
	}

	/**
	 * Override double click handler to add new behaviour
	 **/
	overrideDoubleClick(): void {
		this.diagram.doubleClick = e => {
			const diagramNodes = e.diagram && e.diagram.nodes;
			let rootNode;
			if (diagramNodes && diagramNodes.first().data.rootNodeKey) {
				rootNode = e.diagram.nodes.filter(n => n.data.key === n.data.rootNodeKey).first();
			} else {
				rootNode = e.diagram.findTreeRoots().first();
			}
			if (rootNode) {
				e.diagram.centerRect(rootNode.actualBounds);
			}
		}
	}

	/**
	 * update node templates depending on the actual scale
	 * @param {number} scale > actual zooming scale
	 * @param {InputEvent} inputEvent > triggered event object
	 * @param {number} initialScale > initial scale when diagram is generated
	 **/
	setNodeTemplateByScale(scale?: number, inputEvent?: go.InputEvent, initialScale?: number): void {
		if ((inputEvent && inputEvent.control) || initialScale) {
			if ((scale || initialScale) >= 0.6446089162177968
					&& this.actualNodeTemplate !== NodeTemplateEnum.HIGH_SCALE) {
				this.actualNodeTemplate = NodeTemplateEnum.HIGH_SCALE;
				this.highScaleNodeTemplate();
			}
			if ((scale || initialScale) < 0.6446089162177968 && scale > 0.4581115219913999
					&& this.actualNodeTemplate !== NodeTemplateEnum.MEDIUM_SCALE) {
				this.actualNodeTemplate = NodeTemplateEnum.MEDIUM_SCALE;
				this.mediumScaleNodeTemplate();
			}
			if ((scale || initialScale) <= 0.4581115219913999
					&& this.actualNodeTemplate !== NodeTemplateEnum.LOW_SCALE) {
				this.actualNodeTemplate = NodeTemplateEnum.LOW_SCALE;
				this.lowScaleNodeTemplate();
			}
		}
	}

	/**
	 * High scale node template, this is where nodes are most visible and provide more visual feedback
	 **/
	highScaleNodeTemplate(): void {
		this.diagram.commit(() => this.diagram.nodeTemplate = this.setNodeTemplate());
	}

	/**
	 * Medium scale node template, this is where nodes are visible but don't provide a lot of visual feedback
	 **/
	mediumScaleNodeTemplate(): void {
		const node = new go.Node(go.Panel.Horizontal);

		node.add(this.iconShape());

		node.add(this.assetIconShape());
		node.toolTip = this.createTooltip();
		node.mouseLeave = () => this.hideToolTip();
		node.contextMenu = this.contextMenu();

		node.selectionAdornmentTemplate = this.selectionAdornmentTemplate();

		// if onNodeClick function is assigned directly to click handler
		// 'this' loses the binding to the component with onNodeClicked function
		node.click = (i, o) => this.onNodeClick(i, o);

		this.diagram.commit(() => this.diagram.nodeTemplate = node);
	}

	/**
	 * Low scale node template, this is where nodes are least visible and provide only color visual feedback
	 **/
	lowScaleNodeTemplate(): void {
		const node = new go.Node(go.Panel.Horizontal);

		const  shape = new go.Shape();
		shape.figure = 'Rectangle';
		shape.background = 'red';
		shape.desiredSize = new go.Size(25, 35);
		shape.bind(new go.Binding('fill', 'status',
			(status: string) => this.getStatusColor(status)));
		node.toolTip = this.createTooltip();
		node.mouseLeave = () => this.hideToolTip();
		node.add(shape);
		node.contextMenu = this.contextMenu();

		node.selectionAdornmentTemplate = this.selectionAdornmentTemplate();

		// if onNodeClick function is assigned directly to click handler
		// 'this' loses the binding to the component with onNodeClicked function
		node.click = (i, o) => this.onNodeClick(i, o);

		this.diagram.commit(() => this.diagram.nodeTemplate = node);
	}

	/**
	 * Node bubble color based on status
	 **/
	getStatusColor(name: string): string {
		if (!this.stateIcons[name && name.toLowerCase()]) { return '#ddd'; }
		return this.stateIcons[name.toLowerCase()].background;
	}

	/**
	 * Node bubble color based on status
	 **/
	getStatusTextColor(name: string): string {
		if (!this.stateIcons[name && name.toLowerCase()]) { return '#000'; }
		return this.stateIcons[name.toLowerCase()].color;
	}

	/**
	 * Node Context menu containing a variety of operations like 'edit task', 'show task detail', 'start', 'hold', etc
	 **/
	contextMenu(): any {
		const $ = go.GraphObject.make;
		return $(go.HTMLInfo,  // HTML element to contain the context menu
		{
								show: (obj: go.GraphObject, diagram: go.Diagram, tool: go.Tool) => this.showCtxMenu(obj, diagram, tool),
								mainElement: this.taskCtxMenu.ctxMenu.nativeElement
							}
					); // end Adornment
	}

	/**
	 * handler to show context menu passing relevant data to context menu component
	 * @param {GraphObject} obj
	 * @param {Diagram} diagram
	 * @param {Tool} tool
	 **/
	showCtxMenu(obj: go.GraphObject, diagram: go.Diagram, tool: go.Tool): void {
		if (this.taskCtxMenu) {
			const mousePt = diagram.lastInput.viewPoint;
			this.ctxMenuData$.next({
				selectedNode: obj.part.data,
				currentUser: this.currentUser,
				mousePt: {x: `${mousePt.x}px`, y: `${mousePt.y}px`},
				options: this.contextMenuOptions
			});
		}
	}

	/**
	 * handle to reset the overview index value to 0 so that it's not on top of all elements
	 **/
	resetOverviewIndex(): void {
		this.resetOvIndex = true;
	}

	/**
	 * handle to restore the overview index value so that it's on top of all elements
	 **/
	restoreOverviewIndex(): void {
		this.resetOvIndex = false;
	}

	/**
	 * update node on graph
	 **/
	updateNode(data: IGraphTask): void {
		if (data && data.id) {
			this.diagram.commit(d => {
				const update = Object.assign({}, data);
				if (!update.key) { update.key = data.id; }
				const node = d.nodes.filter(n => n.data.key === update.key || n.data.id === update.id).first();
				node.data = update;
				this.nodeUpdated.
				emit({
					data: d.model.nodeDataArray,
					linksPath: this.extractLinks(d.links)
				});
			});
		}
	}

	/**
	 * Extracts links with specified format
	 * @param links
	 */
	extractLinks(links: any): ILinkPath[] {
		const linksPath = [];
		links.each((l: go.Link) => linksPath.push({from: l.data.from, to: l.data.to}));
		return linksPath;
	}

	/**
	 * Cleanup diagram canvas
	 */
	cleanUpDiagram(): void {
		this.unsubscribe$.next();
		this.unsubscribe$.complete();
		this.diagram.clear();
	}

	/**
	 * Node tooltip with feedback about itself when in low or medium scale
	 **/
	createTooltip(): any {
		const $ = go.GraphObject.make;
		return $(go.HTMLInfo,  // HTML element to contain the context menu
			{
				show: (obj: go.GraphObject, diagram: go.Diagram, tool: go.Tool) => this.showTooltip(obj, diagram, tool),
				mainElement: this.nodeTooltip.nativeElement
			}
		); // end Adornment
	}

	/**
	 * handler to show the tooltip on low or medium scale nodes
	 * @param {GraphObject} obj
	 * @param {Diagram} diagram
	 * @param {go.Tool} tool
	 **/
	showTooltip(obj: go.GraphObject, diagram: go.Diagram, tool: go.Tool): void {
		if (this.nodeTooltip.nativeElement) {
			const mousePt = diagram.lastInput.viewPoint;
			this.renderer.setStyle(this.nodeTooltip.nativeElement, 'display', 'block');
			this.renderer.setStyle(this.nodeTooltip.nativeElement, 'left', `${mousePt.x + 10}px`);
			this.renderer.setStyle(this.nodeTooltip.nativeElement, 'top', `${mousePt.y}px`);
			this.tooltipData = obj.part.data;
		}
	}

	/**
	 * Remove the tooltip from DOM
	 */
	hideToolTip(): void {
		this.renderer.setStyle(this.nodeTooltip.nativeElement, 'display', 'none')
	}

	/**
	 * Set new adornment for given node
	 * @param {any} data
	 */
	setNeighborAdornment(data: any): void {
		this.diagram.commit(d => {
			const node = d.findNodeForKey(data.key);
			node.selectionAdornmentTemplate = this.neighborAdornmentTemplate();
			node.updateAdornments();
			d.select(node.part);
			d.centerRect(node.actualBounds);
		});
	}

	/**
	 * Returns to view with the full graph displayed
	 */
	showFullGraph(): void {
		this.backTofullGraph.emit();
	}

	@HostListener('window:beforeunload', ['$event'])
	ngOnDestroy(): void {
		this.unsubscribe$.next();
		this.unsubscribe$.complete();
	}

}
