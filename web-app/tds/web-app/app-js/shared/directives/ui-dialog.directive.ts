import {
    Component, ComponentRef, ComponentFactoryResolver,
    OnInit, OnChanges, OnDestroy, SimpleChange, Input,
    ViewChild, ViewContainerRef, ReflectiveInjector
} from '@angular/core';

import { NotifierService } from "../services/notifier.service";

import { DialogModel } from '../model/dialog.model'

@Component({
    selector: 'tds-ui-dialog',
    template: `
    <div class="modal fade" id="{{config.name}}" tabindex="-1" role="dialog">
        <div class="modal-dialog" role="document">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                    <h4 class="modal-title">Modal title</h4>
                </div>
                <div class="modal-body" #view>
                    
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
                    <button type="button" class="btn btn-primary">Save changes</button>
                </div>
            </div><!-- /.modal-content -->
        </div><!-- /.modal-dialog -->
    </div>`
})
export class UIDialogDirective implements OnInit, OnChanges, OnDestroy {
    @Input('config') config: DialogModel;
    @ViewChild('view', { read: ViewContainerRef }) view: ViewContainerRef;
    cmpRef: ComponentRef<{}>;

    constructor(private notifierService: NotifierService,
        private resolver: ComponentFactoryResolver) {
        this.registerListeners();
    }

    ngOnInit(): void {
        if (!this.config.lazyLoad)
            this.createExistingComponent();
    };

    ngOnChanges(changes: { [propKey: string]: SimpleChange }) {
        if (changes['config'].previousValue.params)
            this.createExistingComponent();
    };

    ngOnDestroy(): void {
        this.cmpRef.destroy();
    }

    private createExistingComponent(): void {
        if (this.cmpRef)
            this.cmpRef.destroy();

        let resolvedInputs = ReflectiveInjector.resolve(this.config.params);
        let injector = ReflectiveInjector.fromResolvedProviders(resolvedInputs, this.view.parentInjector);
        let factory = this.resolver.resolveComponentFactory(this.config.component);

        this.cmpRef = this.view.createComponent(factory, null, injector);
    };

    private registerListeners(): void {
        this.notifierService.on(this.config.name + '.open', event => {
            if (!this.cmpRef)
                this.createExistingComponent();
        });

        this.notifierService.on(this.config.name + '.close', event => {
            if (this.cmpRef)
                this.cmpRef.destroy();
        });
    }

}