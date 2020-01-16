import { Component, EventEmitter, Input, OnInit, Output, QueryList, ViewChildren } from '@angular/core';
import {
	EVENT_BEFORE_CALL_TEXT,
	EVENT_DEFAULT_ERROR_SCRIPT,
	EVENT_DEFAULT_ERROR_WEB_API,
	EventReaction,
	EventReactionType
} from '../../model/api-action.model';
import { ActionType } from '../../../../shared/model/data-list-grid.model';
import { CHECK_ACTION } from '../../../../shared/components/check-action/model/check-action.model';
import { takeUntil } from 'rxjs/operators';
import { Observable, ReplaySubject } from 'rxjs';
import { CodeMirrorComponent } from '../../../../shared/modules/code-mirror/code-mirror.component';
import { APIActionService } from '../../service/api-action.service';
import { EventReactions } from '../../model/api-action.model';

@Component({
	selector: 'api-action-view-edit-reactions',
	templateUrl: './api-action-view-edit-reactions.component.html',
	styles: []
})
export class ApiActionViewEditReactionsComponent implements OnInit {
	@Input() eventReactions: Array<EventReaction>;
	@Input() isPolling: boolean;
	@Input() modalType: ActionType;
	@Input() actionType: any;
	@Input() isRemote: boolean;
	@Input() codeMirrorMode: string;
	@Input() invalidScriptSyntax: boolean;
	@Output() invalidScriptSyntaxChange: EventEmitter<boolean> = new EventEmitter<boolean>();
	@ViewChildren('reactionCodeMirror') codeMirrorComponents: QueryList<CodeMirrorComponent>;
	public EventReactions = EventReactions;
	reactionCodeMirror = {
		mode: 'Groovy',
		rows: 10,
		cols: 4
	};
	actionTypes = ActionType;
	unsubscribeOnDestroy$: ReplaySubject<void> = new ReplaySubject(1);
	CHECK_ACTION = CHECK_ACTION;

	constructor(private apiActionService: APIActionService) {
	}

	ngOnInit(): void {
		this.reactionCodeMirror.mode = this.codeMirrorMode;
		setTimeout(() => this.disableEnableCodeMirrors(), 500);
	}

	/**
	 * Returns true if Api Action Before Call reaction should be disabled.
	 */
	disableApiActionBeforeCall(): boolean {
		return (this.eventReactions[7].value.length > 0
			&& this.eventReactions[7].value !== EVENT_BEFORE_CALL_TEXT)
			|| this.modalType === this.actionTypes.VIEW;
	}

	/**
	 * On Custom Event Reaction Checkbox Changes
	 * @param eventReaction
	 */
	onEventReactionSelect(eventReaction: EventReaction): void {
		if (eventReaction.type === EventReactionType.PRE) {
			if (eventReaction.selected && eventReaction.value === '') {
				eventReaction.value = EVENT_BEFORE_CALL_TEXT;
			} else {
				eventReaction.value = '';
			}
		}
	}

	/**
	 * Determine if Check Syntax Section should be disabled.
	 * @param sectionIndex
	 */
	isCheckSyntaxSectionDisabled(sectionIndex: number): boolean {
		const eventReaction: EventReaction = this.eventReactions[sectionIndex];
		return eventReaction.value === '' || eventReaction.state === CHECK_ACTION.VALID;
	}

	/**
	 * Close and Open the Panel for Code Mirror
	 * @param {EventReaction} eventReaction
	 */
	openCloseCodeMirror(eventReaction: EventReaction): void {
		eventReaction.open = !eventReaction.open;
	}

	/**
	 * Show only the Event Label if one Event is selected
	 */
	showsEventLabel(): boolean {
		let events = [EventReactionType.SUCCESS, EventReactionType.DEFAULT, EventReactionType.ERROR, EventReactionType.LAPSED, EventReactionType.STALLED];
		let eventRectionItem = this.eventReactions.find((eventReaction) => {
			let eventItem = events.find((event) => {
				return eventReaction.type === event;
			});
			return eventItem !== undefined && eventReaction.selected;
		});
		return eventRectionItem !== undefined;
	}

	/**
	 * Show only the Customize Label if one Custom is selected
	 */
	showsCustomizeLabel(): boolean {
		let events = [EventReactionType.PRE, EventReactionType.FINAL];
		let eventRectionItem = this.eventReactions.find((eventReaction) => {
			let eventItem = events.find((event) => {
				return eventReaction.type === event;
			});
			return eventItem !== undefined && eventReaction.selected;
		});
		return eventRectionItem !== undefined;
	}

	/**
	 * On Error Reaction Checkbox change, default its script value if its currently empty.
	 * @param value: boolean
	 */
	onErrorReactionCheckboxChangeHandler(eventReaction: EventReaction): void {
		if (eventReaction.selected && !eventReaction.value) {
			const isWebAPI = this.actionType && this.actionType.id === 'WEB_API';
			eventReaction.value = isWebAPI ? EVENT_DEFAULT_ERROR_WEB_API : EVENT_DEFAULT_ERROR_SCRIPT;
		}
	}

	/**
	 * Disables or Enable codemirrors text areas based on the VIEW/EDIT mode.
	 */
	disableEnableCodeMirrors(): void {
		this.codeMirrorComponents.forEach((comp: CodeMirrorComponent) => {
			comp.setDisabled(this.modalType === ActionType.VIEW);
			comp.change.pipe(takeUntil(this.unsubscribeOnDestroy$)).subscribe(change => {
				setTimeout(() => {
					comp.setDisabled(this.modalType === ActionType.VIEW);
				}, 100);
			})
		});
		this.codeMirrorComponents.changes.pipe(takeUntil(this.unsubscribeOnDestroy$))
			.subscribe((comps: QueryList<CodeMirrorComponent>) => {
				comps.forEach((child) => {
					setTimeout(() => {
						child.setDisabled(this.modalType === ActionType.VIEW);
					}, 100);
				});
			});
	}

	/**
	 *  Verify the current Event Reaction input is a valid code
	 * @param {EventReaction} eventReaction
	 */
	verifyCode(eventReaction: EventReaction): void {
		this.validateAllSyntax(eventReaction)
			.pipe(takeUntil(this.unsubscribeOnDestroy$))
			.subscribe();
	}

	/**
	 * Execute the validation and return an Observable
	 * so we can attach this event to different validations
	 * @returns {Observable<any>}
	 */
	validateAllSyntax(singleEventReaction?: EventReaction): Observable<any> {
		return new Observable(observer => {
			let scripts = [];
			// Doing a single Event reaction Validation
			if (singleEventReaction) {
				if (singleEventReaction.value !== '') {
					scripts.push({ code: singleEventReaction.type, script: singleEventReaction.value });
				}
			} else {
				this.eventReactions.forEach((eventReaction: EventReaction) => {
					eventReaction.state = CHECK_ACTION.UNKNOWN;
					eventReaction.error = '';
					if (eventReaction.value !== '') {
						scripts.push({ code: eventReaction.type, script: eventReaction.value });
					}
				});
			}
			this.apiActionService.validateCode(scripts)
				.pipe(takeUntil(this.unsubscribeOnDestroy$))
				.subscribe(
					(result: any) => {
						this.invalidScriptSyntax = false;
						result.forEach((eventResult: any) => {
							let eventReaction = this.eventReactions.find((r: EventReaction) => r.type === eventResult['code']);
							if (!eventResult['validSyntax']) {
								let errorResult = '';
								eventResult.errors.forEach((error: string) => {
									errorResult += error['message'] + '\n';
								});
								eventReaction.error = errorResult;
								eventReaction.state = CHECK_ACTION.INVALID;
								this.invalidScriptSyntax = true;
							} else {
								eventReaction.state = CHECK_ACTION.VALID;
							}
						});
						this.invalidScriptSyntaxChange.emit(this.invalidScriptSyntax);
						observer.next();
					},
					(err) => console.log(err));
		});
	}

	/**
	 * Execute the API to validated every Syntax Value.
	 */
	public onCheckAllSyntax(): void {
		this.validateAllSyntax()
			.pipe(takeUntil(this.unsubscribeOnDestroy$))
			.subscribe();
	}
}
