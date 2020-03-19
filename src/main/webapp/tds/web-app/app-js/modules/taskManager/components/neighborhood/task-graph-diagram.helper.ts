import {IDiagramLayoutHelper} from '../../../../shared/utils/diagram-layout.helper';
import {ITdsContextMenuOption} from 'tds-component-library/lib/context-menu/model/tds-context-menu.model';
import {IconModel, IDiagramData} from 'tds-component-library/lib/diagram-layout/model/diagram-data.model';
import {
	Adornment,
	Binding,
	default as go,
	InputEvent,
	Layout,
	Link, Margin,
	Node,
	Panel,
	Placeholder,
	Shape,
	Size,
	TextBlock, TreeLayout
} from 'gojs';
import {ContainerComp, IGraphTask, TASK_OPTION_LABEL} from '../../model/graph-task.model';
import {TaskActionEvents} from '../common/constants/task-action-events.constant';
import {TaskStatus} from '../../model/task-edit-create.model';
import {Permission} from '../../../../shared/model/permission.model';
import {PermissionService} from '../../../../shared/services/permission.service';
import {ASSET_ICONS_PATH, CTX_MENU_ICONS_PATH, STATE_ICONS_PATH} from '../common/constants/task-icon-path';
import {ITaskGraphIcon} from '../../model/task-graph-icon.model';

const MAX_TASK_COUNT = 600;
export class TaskGraphDiagramHelper implements IDiagramLayoutHelper {
	tasksData: any;
	currentUser: any;

	constructor(private permissionService: PermissionService, private props?: any) {
		this.currentUser = (props && props.currentUser) && props.currentUser.fullName;
	}

	contextMenuOptions(): ITdsContextMenuOption {
		return  {
			containerComp: ContainerComp.NEIGHBORHOOD,
			fields: [
				{
					label: TASK_OPTION_LABEL.HOLD,
					event: TaskActionEvents.HOLD,
					icon: CTX_MENU_ICONS_PATH.hold,
					status: TaskStatus.HOLD,
					isAvailable: (n: any) => (n.status && n.status.toLowerCase()) !== TaskStatus.HOLD.toLowerCase(),
					hasPermission: () => this.permissionService.hasPermission(Permission.TaskChangeStatus)
				},
				{
					label: TASK_OPTION_LABEL.START,
					event: TaskActionEvents.START,
					icon: CTX_MENU_ICONS_PATH.start,
					status: TaskStatus.STARTED,
					isAvailable: (n: any) => (n.status && n.status.toLowerCase()) !== TaskStatus.STARTED.toLowerCase(),
					hasPermission: () => this.permissionService.hasPermission(Permission.TaskChangeStatus)
				},
				{
					label: TASK_OPTION_LABEL.DONE,
					event: TaskActionEvents.DONE,
					icon: CTX_MENU_ICONS_PATH.done,
					status: TaskStatus.COMPLETED,
					isAvailable: (n: any) => (n.status && n.status.toLowerCase()) !== TaskStatus.COMPLETED.toLowerCase(),
					hasPermission: () => this.permissionService.hasPermission(Permission.TaskChangeStatus)
				},
				{
					label: TASK_OPTION_LABEL.RESET,
					event: TaskActionEvents.RESET,
					icon: CTX_MENU_ICONS_PATH.reset,
					isAvailable: (n: any) => (n.status && n.status.toLowerCase() === TaskStatus.HOLD.toLowerCase() && n.isAutomatic),
					hasPermission: () => this.permissionService.hasPermission(Permission.TaskChangeStatus)
				},
				{
					label: TASK_OPTION_LABEL.INVOKE,
					event: TaskActionEvents.INVOKE,
					icon: CTX_MENU_ICONS_PATH.invoke,
					isAvailable: (n: any) => (n.status && n.status.toLowerCase() === TaskStatus.STARTED.toLowerCase() && n.isAutomatic),
					hasPermission: () => this.permissionService.hasPermission(Permission.TaskChangeStatus)
				},
				{
					label: TASK_OPTION_LABEL.NEIGHBORHOOD,
					event: TaskActionEvents.NEIGHBORHOOD,
					icon: CTX_MENU_ICONS_PATH.neighborhood,
					status: 'neighborhood',
					isAvailable: (n: any) => n.predecessorIds && n.predecessorIds.length > 0 || n.successors && n.successors.length > 0,
					hasPermission: () => this.permissionService.hasPermission(Permission.TaskChangeStatus)
				},
				{
					label: TASK_OPTION_LABEL.ASSET_DETAILS,
					event: TaskActionEvents.SHOW_ASSET_DETAIL,
					icon: CTX_MENU_ICONS_PATH.assetDetail,
					isAvailable: (n: any) => n.asset && n.asset.id,
					hasPermission: () => this.permissionService.hasPermission(Permission.AssetView)
				},
				{
					label: TASK_OPTION_LABEL.VIEW,
					event: TaskActionEvents.SHOW,
					icon: CTX_MENU_ICONS_PATH.view,
					isAvailable: (n: any) => true,
					hasPermission: () => this.permissionService.hasPermission(Permission.TaskView)
				},
				{
					label: TASK_OPTION_LABEL.EDIT,
					event: TaskActionEvents.EDIT,
					icon: CTX_MENU_ICONS_PATH.edit,
					isAvailable: (n: any) => true,
					hasPermission: () => this.permissionService.hasPermission(Permission.TaskEdit)
				},
				{
					label: TASK_OPTION_LABEL.ASSIGN_TO_ME,
					event: TaskActionEvents.ASSIGN_TO_ME,
					icon: CTX_MENU_ICONS_PATH.assignToMe,
					isAvailable: (n: any) => n.assignedTo && n.assignedTo !== this.currentUser,
					hasPermission: () => this.permissionService.hasPermission(Permission.TaskEdit)
				}
			]
		};
	}

	data(data?: any): any {
		const nodeDataArr = [];
		const linksPath = [];
		this.tasksData = data;
		const tasksCopy = data.tasks.slice();

		// Add tasks to nodeData constant
		// and create linksPath object from number and successors
		tasksCopy.map((t: IGraphTask | any) => {

			const predecessorIds = t.predecessorIds && t.predecessorIds;

			t.key = t.id;
			t.rootNodeKey = data.rootId;
			nodeDataArr.push(t);

			if (predecessorIds && predecessorIds.length > 0) {
				linksPath.push(...this.getLinksPath({taskId: t.id, predecessorIds}));
			}
		});
		return {
			nodeDataArray: nodeDataArr,
			linkDataArray: linksPath,
		};
	}

	diagramData(params?: any): IDiagramData {
		return {
			nodeDataArray: params.nodeDataArray,
			linkDataArray: params.linkDataArray,
			currentUserId: params.currentUserId,
			ctxMenuOptions: this.contextMenuOptions(),
			nodeTemplate: this.nodeTemplate({ isExpandable: params.extras && params.extras.isExpandable }),
			linkTemplate: this.linkTemplate(),
			lowScaleTemplate: this.lowScaleNodeTemplate(),
			mediumScaleTemplate: this.mediumScaleNodeTemplate(),
			layout: this.layout(),
			rootNode: params.rootNode,
			extras: params && params.extras
		};
	}

	diagramEvents(): any[] {
		return [];
	}

	getLinksPath(link: any): any {
		if (link.predecessorIds && link.predecessorIds.length > 0) {
			return link.predecessorIds
				.filter(f => !!this.tasksData.find(t => t.id === f))
				.map(pre => ({
					from: pre,
					to: link.taskId
				}));
		}
		return [];
	}

	icons(): IconModel {
		return undefined;
	}

	layout(): Layout {
		const treeLayout = new go.TreeLayout();
		treeLayout.treeStyle = TreeLayout.StyleAlternating;
		treeLayout.layerStyle = TreeLayout.LayerUniform;
		treeLayout.angle = 0;
		treeLayout.nodeSpacing = 300;
		treeLayout.layerSpacing = 300;

		return treeLayout;
	}

	linkTemplate(): Link {

		const linkTemplate = new go.Link();
		linkTemplate.routing = this.props.taskCount > MAX_TASK_COUNT ? Link.Normal : go.Link.AvoidsNodes;
		linkTemplate.curve = Link.Bezier;

		const linkShape = new go.Shape();
		linkShape.strokeWidth = 2;
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

	lowScaleNodeTemplate(): Node {
		const node = new go.Node(go.Panel.Horizontal);
		node.fromEndSegmentLength = 20;
		node.toEndSegmentLength = 20;
		node.avoidableMargin = new Margin(6, 6, 6, 6);

		const  shape = new go.Shape();
		shape.figure = 'Rectangle';
		shape.background = 'red';
		shape.desiredSize = new go.Size(25, 35);
		shape.bind(new go.Binding('fill', 'status',
			(status: string) => this.getStatusColor(status)));
		node.add(shape);

		node.selectionAdornmentTemplate = this.selectionAdornmentTemplate();
		return node;
	}

	mediumScaleNodeTemplate(): Node {
		const node = new go.Node(go.Panel.Horizontal);
		node.fromEndSegmentLength = 20;
		node.toEndSegmentLength = 20;
		node.avoidableMargin = new Margin(6, 6, 6, 6);

		node.add(this.iconShape());

		node.add(this.assetIconShape());

		node.selectionAdornmentTemplate = this.selectionAdornmentTemplate();

		return node;
	}

	nodeTemplate(opts?: any): Node {
		const node = new Node(Panel.Horizontal);
		node.selectionAdorned = true;
		node.padding = new Margin(0, 0, 0, 0);
		node.fromEndSegmentLength = 20;
		node.toEndSegmentLength = 20;
		node.avoidableMargin = new Margin(6, 6, 6, 6);
		node.add(this.containerPanel());

		node.selectionAdornmentTemplate = this.selectionAdornmentTemplate();

		return node;
	}

	/**
	 * Node bubble color based on status
	 **/
	getStatusColor(name: string): string {
		if (!STATE_ICONS_PATH[name && name.toLowerCase()]) { return '#ddd'; }
		return STATE_ICONS_PATH[name.toLowerCase()].background;
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
	 * Node outer panel container to hold individual shapes related to the node
	 **/
	containerPanel(): go.Panel {
		const panel = new go.Panel(go.Panel.Auto);
		panel.background = '#fff';
		panel.padding = new go.Margin(0, 0, 0, 0);
		panel.mouseOver = (e, o) => o.cursor = 'pointer';
		panel.mouseLeave = (e, o) => o.cursor = 'none';

		panel.add(this.containerShape());
		panel.add(this.panelBody());

		return panel;
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
		container.mouseOver = (e, o) => o.cursor = 'pointer';
		container.mouseLeave = (e, o) => o.cursor = 'none';

		return container;
	}

	/**
	 * Node panel body holding the node content
	 **/
	panelBody(): go.Panel {
		const panel = new go.Panel(go.Panel.Horizontal);
		panel.padding = new go.Margin(0, 0, 0, 0);
		panel.margin = new go.Margin(0, 0, 0, 0);
		panel.mouseOver = (e, o) => o.cursor = 'pointer';
		panel.mouseLeave = (e, o) => o.cursor = 'none';

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
		iconShape.textAlign = 'center';
		iconShape.verticalAlignment = go.Spot.Center;
		iconShape.margin = new go.Margin(0, 0, 0, 5);
		iconShape.desiredSize = new go.Size(35, 35);
		iconShape.font = '25px FontAwesome';
		iconShape.mouseOver = (e, o) => o.cursor = 'pointer';
		iconShape.mouseLeave = (e, o) => o.cursor = 'none';

		iconShape.bind(new Binding('text', 'status',
			(val: string) => this.getIcon(STATE_ICONS_PATH[val.toLowerCase()])));

		iconShape.bind(new Binding('stroke', 'status',
			(val: string) => this.getIconColor(STATE_ICONS_PATH[val.toLowerCase()])));

		iconShape.bind(new Binding('fill', 'status',
			(val: string) => this.getIconColor(STATE_ICONS_PATH[val.toLowerCase()])));

		iconShape.bind(new Binding('background', 'status',
			(val: string) => this.getBackgroundColor(STATE_ICONS_PATH[val.toLowerCase()])));

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
		assetIconShape.mouseOver = (e, o) => o.cursor = 'pointer';
		assetIconShape.mouseLeave = (e, o) => o.cursor = 'none';
		const removeSpaces = s => s.replace(/ /g, '');

		assetIconShape.bind(new Binding('desiredSize', 'asset',
			(val: any) => {
				if (val) {
					const type = !!val.assetType ? val.assetType.toLowerCase() : val.type && val.type.toLowerCase();
					return type === 'application' ? new go.Size(50, 50) : new go.Size(35, 35);
				}
			}));

		assetIconShape.bind(new Binding('font', 'asset',
			(val: any) => {
				if (val) {
					const type = !!val.assetType ? val.assetType.toLowerCase() : val.type && val.type.toLowerCase();
					return type === 'application' ? '40px FontAwesome' : '25px FontAwesome';
				}
			}));

		assetIconShape.bind(new Binding('text', 'asset',
			(val: any) => {
				if (val) {
					const type = !!val.assetType ? removeSpaces(val.assetType).toLowerCase()
						: val.type && removeSpaces(val.type).toLowerCase();
					return this.getIcon(ASSET_ICONS_PATH[type]);
				} else {
					return ASSET_ICONS_PATH.unknown.iconAlt;
				}
			}));

		assetIconShape.bind(new Binding('stroke', 'asset',
			(val: any) => {
				if (val) {
					const type = !!val.assetType ? removeSpaces(val.assetType).toLowerCase()
						: val.type && removeSpaces(val.type).toLowerCase();
					return this.getIconColor(ASSET_ICONS_PATH[type]);
				} else {
					return ASSET_ICONS_PATH.unknown.color;
				}
			}));

		assetIconShape.bind(new Binding('fill', 'asset',
			(val: any) => {
				if (val) {
					const type = !!val.assetType ? removeSpaces(val.assetType).toLowerCase()
						: val.type && removeSpaces(val.type).toLowerCase();
					return this.getIconColor(ASSET_ICONS_PATH[type]);
				} else {
					return ASSET_ICONS_PATH.unknown.color;
				}
			}));

		assetIconShape.bind(new Binding('background', 'asset',
			(val: any) => {
				if (val) {
					const type = val.assetType ? removeSpaces(val.assetType).toLowerCase()
						: val.type && removeSpaces(val.type).toLowerCase();
					return this.getBackgroundColor(ASSET_ICONS_PATH[type]);
				} else {
					return ASSET_ICONS_PATH.unknown.background;
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
		textBlock.mouseOver = (e, o) => o.cursor = 'pointer';
		textBlock.mouseLeave = (e, o) => o.cursor = 'none';

		textBlock.mouseOver = (e: InputEvent, obj: TextBlock) => {
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
			// this.diagram.currentCursor = 'pointer';
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
			return ASSET_ICONS_PATH.unknown.iconAlt;
		}
		return icon.iconAlt;
	}

	/**
	 * Status background handler to get background color for an icon by status
	 * @param {ITaskGraphIcon} icon > icon from which to get background color
	 **/
	getIconColor(icon: ITaskGraphIcon): string {
		return (icon && icon.color) || ASSET_ICONS_PATH.unknown.color;
	}

	/**
	 * Status background handler to get background color for an icon by status
	 * @param {ITaskGraphIcon} icon > icon from which to get background color
	 **/
	getBackgroundColor(icon: ITaskGraphIcon): string {
		return (icon && icon.background) || ASSET_ICONS_PATH.unknown.background;
	}

}