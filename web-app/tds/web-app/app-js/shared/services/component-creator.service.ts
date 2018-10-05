/**
 * Component Creator allow you to create component dynamically and/or add to a specificy view.
 */
import {Injectable, Injector, ComponentRef, ComponentFactoryResolver, ReflectiveInjector, ViewContainerRef} from '@angular/core';
import {ComponentFactory} from '@angular/core/src/linker/component_factory';

@Injectable()
export class ComponentCreatorService {
	private resolver: ComponentFactoryResolver;

	constructor(private mainResolver: ComponentFactoryResolver, private parentInjector: Injector) {
	}

	/**
	 * Inject the current ComponentFactoryResolver per Module
	 * @param resolver
	 */
	public setFactoryResolver(resolver: ComponentFactoryResolver): void{
		this.resolver = resolver;
	}

	/**
	 * Create component and already add it to an existing view
	 * @param component ComponentType
	 * @param params properties to be inject in the component creation
	 * @param view view to have component inserted into
	 */
	insert(component: any, params: Array<any>, view: ViewContainerRef): ComponentRef<{}> {
		let factory: ComponentFactory<any> = null;
		let resolvedInputs = ReflectiveInjector.resolve(params);
		let injector = ReflectiveInjector.fromResolvedProviders(resolvedInputs, view.parentInjector);
		// If the Current module does not have a resolver, use the main
		if (!this.resolver) {
			factory = this.mainResolver.resolveComponentFactory(component);
		} else {
			factory = this.resolver.resolveComponentFactory(component);
		}
		return view.createComponent(factory, null, injector);
	}

	/**
	 * Create the component letting you access its methods and variables
	 * @param component ComponentType
	 * @param params properties to be inject in the component creation
	 */
	create(component: any, params: Array<any>): ComponentRef<{}> {
		let resolvedInputs = ReflectiveInjector.resolve(params);
		let injector = ReflectiveInjector.fromResolvedProviders(resolvedInputs, this.parentInjector);
		return this.resolver.resolveComponentFactory(component).create(injector);
	}
}