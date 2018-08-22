export interface WindowSettings {
	left: string | number;
	top: string | number;
	width: string | number;
	height: string | number;
}

export interface DecoratorOptions {
	isFullScreen?: boolean;
	isCentered?: boolean;
	isResizable?: boolean;
	isDraggable?: boolean;
	sizeNamePreference?: string;
}