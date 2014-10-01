/************************
 * MODULE: Comments
 ************************/

/**
 * Create namespaces
 */
tds.comments = tds.comments || {};
tds.comments.controller = tds.comments.controller || {};
tds.comments.service = tds.comments.service || {};
tds.comments.util = tds.comments.util || {};
tds.comments.directive = tds.comments.directive || {};

/************************
 * CONTROLLERS
 ************************/

tds.comments.controller.MainController = function(rootScope, scope, modal, window, utils, commentUtils) {

	var activePopups = {};

	scope.controller = this;
	scope.config = {};
	scope.config.table = {};
	scope.bulkEditing = false;
	scope.bulkEdit = false;

	//commentsScope is used after "jqgrid:grid" creates dynamic dom elements to $compile the grid
	rootScope.commentsScope = scope;

	scope.$on('popupClosed', function(evt, popupType) {
		activePopups[popupType] = false;
		updateRefreshTimer();
	});

	scope.$on('newActivePopup', function(evt, popupType) {
		activePopups[popupType] = true;
		updateRefreshTimer();
	});

	scope.$on('commentsList', function(evt, assetTO, commentType) {
		scope.controller.list(assetTO, commentType);
	});

	scope.$on('createComment', function(evt, commentType, assetTO) {
		scope.controller.createComment(commentType, assetTO);
	});

	scope.$on('viewComment', function(evt, commentTO, action) {
		scope.controller.showComment(commentTO, action);
	});

	scope.$on('editComment', function(evt, commentTO) {
		scope.controller.editComment(commentTO);
	});

	scope.$on('showAssetDetails', function(evt, redirectTo, type, value) {
		scope.$broadcast('forceDialogClose', ['crud', 'list']);
		window.getEntityDetails(redirectTo, type, value);
	});

	scope.$on('commentCreated', function(evt, commentId, assetId) {
		dispathCommentChangedEvent(commentId, assetId, 'created');
	});

	scope.$on('commentDeleted', function(evt, commentId, assetId) {
		dispathCommentChangedEvent(commentId, assetId, 'deleted');
	});

	scope.$on('commentUpdated', function(evt, commentId, assetId) {
		dispathCommentChangedEvent(commentId, assetId, 'updated');
	});

	var dispathCommentChangedEvent = function(commentId, assetId, action) {
		scope.$broadcast('commentChanged', commentUtils.commentEventTO(commentId, assetId, action));
	}

	this.listBy = function(assetId, assetType, commentType) {
		this.list(commentUtils.assetTO(assetId, assetType), commentType)
	}

	this.list = function(assetTO, commentType) {
		if (!commentType) {
			commentType = 'issue';
		}
		window.setAssetId(assetTO.assetId);
		var view = (commentType == 'comment') ? '/comment/list' : '/task/list';
		scope.$broadcast('forceDialogClose', ['crud', 'list']);
		modal.open({
			templateUrl: utils.url.applyRootPath(view),
			controller: tds.comments.controller.ListDialogController,
			scope: scope,
			windowClass: 'modal-comment-task-list',
			backdrop : 'static',
			resolve: {
				assetTO: function() {
					return assetTO;
				},
				defaultCommentType: function() {
					return commentType;
				}
			}
		});
	}

	this.createCommentBy = function(commentType, assetId, assetType) {
		this.createComment(commentType, commentUtils.assetTO(assetId, assetType));
	}

	this.createComment = function(commentType, assetTO) {
		scope.$broadcast('forceDialogClose', ['crud']);
		var view = (commentType == 'comment') ? '/comment/editComment' : '/task/editTask';
		modal.open({
			templateUrl: utils.url.applyRootPath(view),
			controller: tds.comments.controller.EditCommentDialogController,
			scope: scope,
			windowClass: ((commentType == 'comment') ? 'modal-comment' : 'modal-task'),
			backdrop : 'static',
			resolve: {
				assetTO: function() {
					return assetTO;
				},
				defaultCommentType: function() {
					return commentType;
				},
				commentTO: function() {
					return null;
				}
			}
		});
	}

	this.showCommentById = function(commentId, commentType) {
		this.showComment(commentUtils.commentTO(commentId, commentType), 'show');
	}

	this.showComment = function(commentTO, action) {
		scope.$broadcast('forceDialogClose', ['crud']);
		var view = (commentTO.commentType == 'comment') ? '/comment/showComment' : '/task/showTask';
		modal.open({
			templateUrl: utils.url.applyRootPath(view),
			controller: tds.comments.controller.ShowCommentDialogController,
			scope: scope,
			windowClass: ((commentTO.commentType == 'comment') ? 'modal-comment' : 'modal-task'),
			backdrop : 'static',
			resolve: {
				commentTO: function() {
					return commentTO;
				},
				action: function() {
					return action;
				}
			}
		});
	}

	this.editCommentById = function(commentId, commentType) {
		this.editComment(commentUtils.commentTO(commentId, commentType));
	}

	this.editComment = function(commentTO) {
		scope.$broadcast('forceDialogClose', ['crud']);
		var view = (commentTO.commentType == 'comment') ? '/comment/editComment' : '/task/editTask';
		modal.open({
			templateUrl: utils.url.applyRootPath(view),
			controller: tds.comments.controller.EditCommentDialogController,
			scope: scope,
			windowClass: ((commentTO.commentType == 'comment') ? 'modal-comment' : 'modal-task'),
			backdrop : 'static',
			resolve: {
				assetTO: function() {
					return null;
				},
				defaultCommentType: function() {
					return null;
				},
				commentTO: function() {
					return commentTO;
				}
			}
		});
	}

	this.bulkEditTasks = function() {
		scope.bulkEdit = true;
	if (scope.bulkEditing) {
			scope.$broadcast('hideActionBars');
		} else {
			scope.$broadcast('showActionBars');
		}
		scope.bulkEditing = !scope.bulkEditing;
		scope.bulkEdit = false;
	}

	var updateRefreshTimer = function() {
		var selectTimed = angular.element('#selectTimedId');
		if (selectTimed.length > 0) {
			if (isPopupOpen()) {
				window.timedUpdate('never');
			} else {
				window.timedUpdate(selectTimed.val());
			}
		}
	}

	var isPopupOpen = function() {
		var result = false;
		for (var popupType in activePopups) {
			result = result || (activePopups[popupType]);
		}
		return result;
	}

};

tds.comments.controller.MainController.$inject = ['$rootScope', '$scope', '$modal', '$window', 'utils', 'commentUtils'];


/*****************************************
 * Controller for comments & assets list dialog
 */
tds.comments.controller.ListDialogController = function($scope, $modalInstance, $log, $timeout, alerts, assetTO, commentService, appCommonData, utils, commentUtils, defaultCommentType) {

	$scope.commentsData = [];
	$scope.truncate = utils.string.truncate;
	$scope.formatDueDate = utils.date.formatDueDate;

	$scope.taskStyle = "";
	$scope.commentStyle = "";
	$scope.overStyle = "backgroundColor='white'";

	$modalInstance.opened.then(function(modalReady) {
		$scope.$broadcast("popupOpened");
	});

	$scope.removePopupOpenedListener = $scope.$on('popupOpened', function(evt) {
		$timeout(function() {
			$scope.removePopupOpenedListener();
			$scope.$emit('newActivePopup', 'listComment');
			refreshView();
		}, 50);
	});

	var refreshView = function() {
		commentService.searchComments(assetTO.assetId, defaultCommentType).then(
			function(data) {
				showComments(data)
			},
			function(data) {
				alerts.showGenericMsg();
			}
		);
	};

	$scope.close = function() {
		commentUtils.closePopup($scope, 'listComment');
	};

	$scope.$on('forceDialogClose', function(evt, types) {
		if (types.indexOf('list') > -1) {
			$scope.close();
		}
	});

	$scope.$on('commentChanged', function(evt, eventTO) {
		if ((eventTO != null) && (eventTO.assetId == assetTO.assetId)) {
			refreshView();
		}
	});

	$scope.createTask = function() {
		$scope.$emit("createComment", 'issue', assetTO);
	}

	$scope.createComment = function() {
		$scope.$emit("createComment", 'comment', assetTO);
	}

	$scope.view = function(id, commentType) {
		$scope.$emit("viewComment", commentUtils.commentTO(id, commentType), 'show');
	}

	$scope.edit = function(id, commentType) {
		$scope.$emit("editComment", commentUtils.commentTO(id, commentType));
	}

	var transformComment = function(comment) {
		return {
			commentInstance: {
				id: comment.commentId,
				commentType: comment.commentType,
				comment: comment.comment,
				dueDate: comment.dueDate,
				category: comment.category,
			}
		};
	}

	$scope.rowColor = function(asset) {
		if (asset.commentInstance.commentType == 'issue') {
			return {
				background: asset.cssClass
			};
		} else {
			return "";
		}
	}

	var showComments = function(data) {
		$scope.commentsData = data;
	}

};

/*****************************************
 * Controller that shows a commet
 */
tds.comments.controller.ShowCommentDialogController = function($window, $scope, $modalInstance, $log, $timeout, commentService, alerts, commentTO, action, appCommonData, utils, commentUtils) {

	$scope.action = action;

	$scope.ac = commentUtils.commentTemplate(null);
	$scope.ac.commentId = commentTO.commentId;
	$scope.acData = {};

	$modalInstance.opened.then(function(modalReady) {
		$scope.$broadcast("popupOpened");
	});

	$scope.removePopupOpenedListener = $scope.$on('popupOpened', function(evt) {
		$timeout(function() {
			$scope.removePopupOpenedListener();
			$scope.$emit('newActivePopup', 'showComment');

			refreshView();
		}, 50);
	});

	var refreshView = function() {
		commentService.getComment(commentTO.commentId).then(
			function(data) {
				showAssetCommentDialog(data)
			},
			function(data) {
				alerts.showGenericMsg();
			}
		);
	};

	$scope.close = function() {
		commentUtils.closePopup($scope, 'showComment');
	};

	$scope.$on('forceDialogClose', function(evt, types) {
		if (types.indexOf('crud') > -1) {
			$scope.close();
		}
	});

	$scope.$on('commentChanged', function(evt, eventTO) {
		if ((eventTO != null) && (eventTO.commentId == commentTO.commentId) && (eventTO.action != 'deleted')) {
			refreshView();
		}
	});

	$scope.editComment = function() {
		$scope.close();
		$scope.$emit("editComment", commentTO);
	}

	$scope.deleteComment = function() {
		commentUtils.validateDelete($scope.ac.commentId, $scope.ac.assetEntity, commentService.deleteComment).then(
			function(data) {
				$scope.close();
				$scope.$emit("commentDeleted", $scope.ac.commentId, $scope.ac.assetEntity);
			}
		);
	}

	$scope.getEntityDetails = function() {
		$scope.$emit("showAssetDetails", 'listTask', $scope.ac.assetType, $scope.ac.assetEntity);
	}

	$scope.viewTask = function(taskId) {
		$scope.$emit("viewComment", commentUtils.commentTO(taskId, 'issue'), 'show');
	}

	function showAssetCommentDialog(data) {

		if (data && data[0]) {
			if (data[0].error) {
				alerts.addAlertMsg(data[0].error);
			} else {
				var ac = commentUtils.commentTemplateFromEditResponse(data[0]);
				angular.copy(data[0], $scope.acData);
				angular.copy(ac, $scope.ac);

				if ($scope.ac.comment == null) {
					$scope.ac.comment = "";
				}
				if ($scope.ac.resolution == null) {
					$scope.ac.resolution = "";
				}

				if ($("#selectTimedId").length > 0) {
					timedUpdate('never')
				}
			}
		}
		if (!isIE7OrLesser)
			$("select.assetSelect").select2();
	}

};


/*****************************************
 * Controller use to edit a comment
 */
tds.comments.controller.EditCommentDialogController = function($scope, $modalInstance, $log, $timeout, commentService, alerts, assetTO, defaultCommentType, commentTO, appCommonData, utils, commentUtils) {

	$scope.comments = [];
	$scope.isEdit = (commentTO != null);

	//Datasource
	$scope.ds = commentUtils.datasourcesTemplate();
	$scope.ds.roleTypes = appCommonData.getRoles();
	$scope.ds.durationScales = appCommonData.getDurationScales();

	$scope.acBackup = {};
	$scope.ac = commentUtils.commentTemplate(assetTO);
	$scope.ac.commentType = defaultCommentType;
	if ($scope.isEdit) {
		$scope.ac.commentId = commentTO.commentId;
	}
	$scope.acData = {};

	$scope.dependencies = commentUtils.createDependencies();

	$scope.cssForCommentStatus = "name";

	$scope.workFlowTransition = "";
	$scope.assignedEditData = "";

	$scope.enableMoveEvent = true;

	$modalInstance.opened.then(function(modalReady) {
		$scope.$broadcast("popupOpened");
	});

	$scope.removePopupOpenedListener = $scope.$on('popupOpened', function(evt) {
		$timeout(function() {
			$scope.removePopupOpenedListener();
			$scope.$emit('newActivePopup', 'editComment');
			if ($scope.isEdit) {
				commentService.getComment(commentTO.commentId).then(
					function(data) {
						editComment(data)
					},
					function(data) {
						alerts.showGenericMsg();
					}
				);
			} else {
				if (defaultCommentType == 'comment') {
					$scope.$broadcast("noPendingRequests");
				}
			}

		}, 50);
	});

	$scope.close = function() {
		commentUtils.closePopup($scope, 'editComment');
	};

	$scope.$on('forceDialogClose', function(evt, types) {
		if (types.indexOf('crud') > -1) {
			$scope.close();
		}
	});

	$scope.deleteComment = function() {
		commentUtils.validateDelete($scope.ac.commentId, $scope.ac.assetEntity, commentService.deleteComment).then(
			function(data) {
				$scope.close();
				$scope.$emit("commentDeleted", $scope.ac.commentId, $scope.ac.assetEntity);
			}
		);
	}

	$scope.checkHardAssigned = function() {
		if ($scope.ac.assignedTo == '') {$scope.ac.hardAssigned='0'};
	}

	$scope.updateEstFinish = function() {
		var startDate = utils.date.createDateTimeFromString($scope.ac.estStart);
		if (startDate.isValid() && $scope.acData.durationTime) {
			var endDate = startDate.add('ms', $scope.acData.durationTime);
			$scope.ac.estFinish = utils.date.formatDateTime(endDate);
		}
	}

	$scope.updateDuration = function() {
		var startDate = utils.date.createDateTimeFromString($scope.ac.estStart);
		var endDate =utils.date.createDateTimeFromString($scope.ac.estFinish);
		if (startDate.isValid() && endDate.isValid()) {
			var diff = startDate.diff(endDate);
			$scope.acData.durationTime = (diff.valueOf() * -1);
			if(!$scope.$$phase) {
				$scope.$apply();
			}
		}
	}

	function editComment(data) {
		if (B2 != '') {
			B2.Pause()
		}

		if (data && data[0]) {
			if (data[0].error) {
				alerts.addAlertMsg(data[0].error);
			} else {
				var ac = commentUtils.commentTemplateFromEditResponse(data[0]);
				angular.copy(data[0], $scope.acData);
				angular.copy(ac, $scope.ac);
				angular.copy(ac, $scope.acBackup);

				if (ac.comment == null) {
					ac.comment = "";
				}
				if (ac.resolution == null) {
					ac.resolution = "";
				}

				updateCommentId = ac.id;

				$scope.dependencies.predecessors = $scope.acData.predecessorList;
				$scope.dependencies.successors = $scope.acData.successorList;

				$scope.enableMoveEvent = ((!$scope.isEdit) ||
					                      ( ($scope.isEdit) &&
					                      	($scope.dependencies.predecessors.length == 0) &&
					                      	($scope.dependencies.successors.length == 0)
					                      )
					                     );

				if ($scope.ac.commentType == 'issue') {
					$scope.statuWarnId = $scope.acData.statusWarn;
				} else {
					$scope.acData.notes = [];
				}

				if ($("#selectTimedId").length > 0) {
					timedUpdate('never')
				}
			}
		}
		//if(!isIE7OrLesser)
		//	$("select.assetSelect").select2();
	}

	//
	// Invoked by createCommentForm and editCommentDialog to make Ajax call to persist changes of new and existing assetComment classes
	//
	$scope.saveComment = function(viewAfterSave) {
		// Bump the list timer if it exists
		if ($("#selectTimedId").length > 0) {
			timedUpdate($("#selectTimedId").val())
		}

		$scope.ac.id = $scope.ac.commentId;

		if (commentService.validDependencies($scope.dependencies)) {
			if ($scope.isEdit) {
				commentService.updateComment($scope.ac, $scope.dependencies).then(
					function(data) {
						if (data.error) {
							alerts.addAlertMsg(data.error);
						} else {
							$scope.close();
							$scope.$emit("commentUpdated", $scope.ac.id, $scope.ac.assetEntity);
							if ($scope.ac.assetEntity != $scope.acBackup.assetEntity) {
								$scope.$emit("commentUpdated", $scope.ac.id, $scope.acBackup.assetEntity);
							}
							if (viewAfterSave) {
								$scope.$emit("viewComment", commentTO, 'show');
							}
						}
					},
					function(data) {
						alerts.showGenericMsg();
					}
				);
			} else {
				commentService.saveComment($scope.ac, $scope.dependencies).then(
					function(data) {
						if (data.error) {
							alerts.addAlertMsg(data.error);
						} else {
							var comment = commentUtils.commentTemplateFromCreateResponse(data, $scope.ac.assetEntity, $scope.ac.assetType);
							$scope.close();
							$scope.$emit("commentCreated", comment.commentId, comment.assetEntity);
							if (open == 'view') {
								$scope.$emit("viewComment", commentUtils.commentTO(comment.commentId, comment.commentType), 'show');
							}
						}
					},
					function(data) {
						alerts.showGenericMsg();
					}
				);
			}
		}
	}

};

/************************
 * SERVICES
 ************************/

/**
 * Factory used to interact with the comments/tasks services
 */
tds.comments.service.CommentService = function(utils, http, q) {

	http.defaults.headers.post["Content-Type"] = "application/x-www-form-urlencoded";

	var getWorkflowTransitions = function(assetId, category, id) {
		var deferred = q.defer();
		http.post(utils.url.applyRootPath('/assetEntity/getWorkflowTransition?format=json&assetId=' + assetId + '&category=' + category + '&assetCommentId=' + id)).
		success(function(data, status, headers, config) {
			deferred.resolve(data);
		}).
		error(function(data, status, headers, config) {
			deferred.reject(data);
		});
		return deferred.promise;
	};

	var getAssignedToList = function(forView, id) {
		var deferred = q.defer();
		http.post(utils.url.applyRootPath('/assetEntity/updateAssignedToSelect?format=json&forView=' + forView + '&id=' + id)).
		success(function(data, status, headers, config) {
			deferred.resolve(data);
		}).
		error(function(data, status, headers, config) {
			deferred.reject(data);
		});
		return deferred.promise;
	};

	var getStatusList = function(commentId) {
		var deferred = q.defer();
		http.post(utils.url.applyRootPath('/assetEntity/updateStatusSelect?format=json&id=' + commentId)).
		success(function(data, status, headers, config) {
			deferred.resolve(data);
		}).
		error(function(data, status, headers, config) {
			deferred.reject(data);
		});
		return deferred.promise;
	};

	var duplicatedDependencies = function(dependencies) {
		var depSet = {};
		var result = true;
		for (var i = 0; i < dependencies.length; i++) {
			if (dependencies[i].taskId) {
				if (depSet[dependencies[i].taskId]) {
					alert('Duplicated dependency.');
					result = false;
					break;
				} else {
					depSet[dependencies[i].taskId] = true;
				}
			}
		}
		return result;
	};

	var checkDependenciesLoops = function(depData) {
		var depSet = {};
		var result = true;
		for (var i = 0; i < depData.predecessors.length; i++) {
			if (depData.predecessors[i].taskId) {
				depSet[depData.predecessors[i].taskId] = true;
			}
		}
		for (var j = 0; j < depData.successors.length; j++) {
			if ((depData.successors[j].taskId) && depSet[depData.successors[j].taskId]) {
				alert("One or more tasks are assigned as both a Predecessor and Successor which is not allowed.");
				result = false;
				break;
			}
		}
		return result;
	};

	var validDependencies = function(depData) {
		return duplicatedDependencies(depData.predecessors) &&
			duplicatedDependencies(depData.successors) &&
			checkDependenciesLoops(depData);
	};

	var createDependenciesArray = function(dependencies) {
		var result = [];
		var noIdCount = -1;
		angular.forEach(dependencies, function(value) {
			if (value.taskId) {
				if (value.id == '') {
					result.push(noIdCount + '_' + value.taskId);
					noIdCount--;
				} else {
					result.push(value.id + '_' + value.taskId);
				}
			}
		});
		return result;
	};

	var createDeletedDependencies = function(dependencies) {
		var result = '';
		angular.forEach(dependencies.deletedPredecessors, function(key, value) {
			if (result.length != 0) {
				result += ','
			};
			result += key;
		});
		angular.forEach(dependencies.deletedSuccessors, function(key, value) {
			if (result.length != 0) {
				result += ','
			};
			result += key;
		});
		return result;
	};

	var createDependenciesParams = function(params, dependencies) {
		params.taskDependency = createDependenciesArray(dependencies.predecessors);
		params.taskSuccessor = createDependenciesArray(dependencies.successors);
		params.deletedPreds = createDeletedDependencies(dependencies);
		params.manageDependency = 1;
	};

	var saveComment = function(params, dependencies) {
		var deferred = q.defer();
		createDependenciesParams(params, dependencies);
		var params = $.param(params);
		http.post(utils.url.applyRootPath('/assetEntity/saveComment'), params).
		success(function(data, status, headers, config) {
			deferred.resolve(data);
		}).
		error(function(data, status, headers, config) {
			deferred.reject(data);
		});
		return deferred.promise;
	};

	var updateComment = function(params, dependencies) {
		var deferred = q.defer();
		createDependenciesParams(params, dependencies);
		var params = $.param(params);
		http.post(utils.url.applyRootPath('/assetEntity/updateComment'), params).
		success(function(data, status, headers, config) {
			deferred.resolve(data);
		}).
		error(function(data, status, headers, config) {
			deferred.reject(data);
		});
		return deferred.promise;
	};

	var searchComments = function(assetId, commentType) {
		var deferred = q.defer();
		http.get(utils.url.applyRootPath('/assetEntity/listComments/' + assetId + '?commentType=' + commentType)).
		success(function(data, status, headers, config) {
			deferred.resolve(data);
		}).
		error(function(data, status, headers, config) {
			deferred.reject(data);
		});
		return deferred.promise;
	};

	var getComment = function(commentId) {
		var deferred = q.defer();
		http.get(utils.url.applyRootPath('/assetEntity/showComment?id=' + commentId)).
		success(function(data, status, headers, config) {
			deferred.resolve(data);
		}).
		error(function(data, status, headers, config) {
			deferred.reject(data);
		});
		return deferred.promise;
	};

	var getActionBarButtons = function(commentId, includeDetails) {
		var params = $.param({
			'id': commentId,
			'includeDetails': includeDetails
		});
		var deferred = q.defer();
		http.post(utils.url.applyRootPath('/task/genActionBarForShowViewJson'), params).
		success(function(data, status, headers, config) {
			deferred.resolve(data);
		}).
		error(function(data, status, headers, config) {
			deferred.reject(data);
		});
		return deferred.promise;
	};

	var predecessorTableHtml = function(commentId) {
		var deferred = q.defer();
		http.get(utils.url.applyRootPath('/assetEntity/predecessorTableHtml?commentId=' + commentId)).
		success(function(data, status, headers, config) {
			deferred.resolve(data);
		}).
		error(function(data, status, headers, config) {
			deferred.reject(data);
		});
		return deferred.promise;
	};

	var successorTableHtml = function(commentId) {
		var deferred = q.defer();
		http.get(utils.url.applyRootPath('/assetEntity/successorTableHtml?commentId=' + commentId)).
		success(function(data, status, headers, config) {
			deferred.resolve(data);
		}).
		error(function(data, status, headers, config) {
			deferred.reject(data);
		});
		return deferred.promise;
	};

	var getDependencies = function(category, commentId, forWhom, moveEvent) {
		var deferred = q.defer();
		http.get(utils.url.applyRootPath('/assetEntity/predecessorSelectHtml?format=json&category=' + category + '&commentId=' + commentId + '&forWhom=' + forWhom + '&moveEvent=' + moveEvent)).
		success(function(data, status, headers, config) {
			deferred.resolve(data);
		}).
		error(function(data, status, headers, config) {
			deferred.reject(data);
		});
		return deferred.promise;
	};

	var deleteComment = function(commentId, assetId) {
		var params = $.param({
			'id': commentId,
			'assetEntity': assetId
		});
		var deferred = q.defer();
		http.post(utils.url.applyRootPath('/assetEntity/deleteComment'), params).
		success(function(data, status, headers, config) {
			deferred.resolve(data);
		}).
		error(function(data, status, headers, config) {
			deferred.reject(data);
		});
		return deferred.promise;
	};

	var changeTaskStatus = function(commentId, newStatus, currentStatus) {
		var params = $.param({
			'id': commentId,
			'status': newStatus,
			'currentStatus': currentStatus
		});
		var deferred = q.defer();
		http.post(utils.url.applyRootPath('/task/update'), params).
		success(function(data, status, headers, config) {
			deferred.resolve(data);
		}).
		error(function(data, status, headers, config) {
			deferred.reject(data);
		});
		return deferred.promise;
	};

	var assignTaskToMe = function(commentId, status) {
		var params = $.param({
			'id': commentId,
			'status': status
		});
		var deferred = q.defer();
		http.post(utils.url.applyRootPath('/task/assignToMe'), params).
		success(function(data, status, headers, config) {
			deferred.resolve(data);
		}).
		error(function(data, status, headers, config) {
			deferred.reject(data);
		});
		return deferred.promise;
	};

	var changeTaskEstTime = function(commentId, day) {
		var params = $.param({
			'commentId': commentId,
			'day': day
		});
		var deferred = q.defer();
		http.post(utils.url.applyRootPath('/task/changeEstTime'), params).
		success(function(data, status, headers, config) {
			deferred.resolve(data);
		}).
		error(function(data, status, headers, config) {
			deferred.reject(data);
		});
		return deferred.promise;
	};

	var getStaffRoles = function() {
		var deferred = q.defer();
		http.get(utils.url.applyRootPath('/task/getStaffRoles')).
		success(function(data, status, headers, config) {
			deferred.resolve(data);
		}).
		error(function(data, status, headers, config) {
			deferred.reject(data);
		});
		return deferred.promise;
	};

	var getAssetsByType = function(assetType) {
		var deferred = q.defer();
		http.get(utils.url.applyRootPath('/assetEntity/getAssetsByType?assetType=' + assetType)).
		success(function(data, status, headers, config) {
			deferred.resolve(data);
		}).
		error(function(data, status, headers, config) {
			deferred.reject(data);
		});
		return deferred.promise;
	};

	var setShowAllPreference = function(selected) {
		var deferred = q.defer();
		http.get(utils.url.applyRootPath('/assetEntity/setShowAllPreference?selected='+selected)).
		success(function(data, status, headers, config) {
			deferred.resolve(data);
		}).
		error(function(data, status, headers, config) {
			deferred.reject(data);
		});
		return deferred.promise;
	};

	return {
		getWorkflowTransitions: getWorkflowTransitions,
		getAssignedToList: getAssignedToList,
		getStatusList: getStatusList,
		saveComment: saveComment,
		updateComment: updateComment,
		searchComments: searchComments,
		getComment: getComment,
		getActionBarButtons: getActionBarButtons,
		predecessorTableHtml: predecessorTableHtml,
		successorTableHtml: successorTableHtml,
		getDependencies: getDependencies,
		deleteComment: deleteComment,
		validDependencies: validDependencies,
		changeTaskStatus: changeTaskStatus,
		assignTaskToMe: assignTaskToMe,
		changeTaskEstTime: changeTaskEstTime,
		getStaffRoles: getStaffRoles,
		getAssetsByType: getAssetsByType,
		setShowAllPreference: setShowAllPreference
	};

};

/**
 * Factory used to interact with the asset entity services
 */
tds.comments.util.CommentUtils = function(q, interval, appCommonData) {
	console.log("heyo buckaroo");
	console.log(appCommonData);
	var closePopup = function(scope, type) {
		if (!scope.closed) {
			scope.$close('close');
			scope.$emit('popupClosed', type);
			scope.closed = true;
		}
	};

	var validateDelete = function(commentId, assetId, deleteService) {
		var deferred = q.defer();
		if (confirm('Are you sure?')) {
			deleteService(commentId, assetId).then(
				function(data) {
					deferred.resolve(data);
				},
				function(data) {
					alerts.showGenericMsg();
					deferred.reject();
				}
			);

		} else {
			interval(function() {
				deferred.reject()
			}, 100, 1);
		}
		return deferred.promise;
	}

	var commentTemplate = function(assetData) {
		if (assetData == null) {
			assetData = assetTO('', 'Server');
		}
		return {
			comment: '',
			forWhom: '',
			assetEntity: assetData.assetId?assetData.assetId.toString():'',
			category: 'general',
			predCount: '-1',
			deletePredId: '',
			commentFromId: '',
			prevAsset: '',

			isResolved: '0',
			resolution: '',

			commentType: 'comment',
			role: "",
			moveEvent: "",
			assetType: assetData.assetType,
			duration: "",
			durationScale: "M",
			priority: "3",
			predecessorCategory: "",

			mustVerify: 0,
			commentId: "",
			workflowTransition: '',

			dueDate: '',
			estStart: '',
			estFinish: '',
			hardAssigned: '0',
			override: '0',

			taskDependency: "",
			taskSuccessor: "",

			manageDependency: 1,

			assignedTo: appCommonData.getLoginPerson().id.toString(),
			status: 'Ready'
		};
	};

	var commentTemplateFromEditResponse = function(response) {
		return commentTemplateFromCreateResponse(response, response.assetId, response.assetType);
	}

	var commentTemplateFromCreateResponse = function(response, assetId, assetType) {
		var temp = commentTemplate(assetTO(assetId, assetType));
		var ac = response.assetComment;
		temp.comment = ac.comment;
		temp.category = ac.category;
		temp.isResolved = ac.isResolved ? ac.isResolved.toString() : '0';
		temp.resolution = ac.resolution;
		temp.commentType = ac.commentType;
		temp.role = ac.role ? ac.role.toString() : '';
		temp.moveEvent = ac.moveEvent ? ac.moveEvent.id.toString() : '';
		temp.duration = ac.duration;
		temp.durationScale = ac.durationScale?ac.durationScale.name:'M';
		temp.priority = ac.priority ? ac.priority.toString() : '3';
		temp.mustVerify = ac.mustVerify;
		temp.commentId = ac.id;
		temp.workflowTransition = ac.workflowTransition ? ac.workflowTransition.id.toString() : '';
		temp.dueDate = response.dueDate;
		temp.estStart = response.etStart;
		temp.estFinish = response.etFinish;
		temp.hardAssigned = ac.hardAssigned ? ac.hardAssigned.toString() : '0';
		temp.override = ac.workflowOverride ? ac.workflowOverride.toString() : '0';
		temp.assignedTo = ac.assignedTo ? ac.assignedTo.id.toString() : '';
		temp.status = ac.status;
		temp.taskNumber = ac.taskNumber;

		return temp;
	};

	var datasourcesTemplate = function() {
		return {
			commentTypes: [],
			roleTypes: [],
			moveEvents: [],
			categories: [],
			assetTypes: [],
			durationScales: [],
			priorities: [],
			predecessorCategories: [],
			assetSelectValues: [],
			workflows: []
		};
	};

	var commentTO = function(commentId, commentType) {
		return {
			commentId: commentId,
			commentType: commentType
		};
	};

	var getRealAssetType = function(assetType) {
		var tempAssetType = null;
		if (assetType == 'Application' || assetType == 'Database') {
			tempAssetType = assetType
		} else if (assetType == 'Files') {
			tempAssetType = 'Storage'
		} else if (assetType == 'Server' || assetType == 'VM' || assetType == 'Blade') {
			tempAssetType = 'Server'
		} else {
			tempAssetType = 'Other'
		}
		return tempAssetType;
	};

	var assetTO = function(assetId, assetType) {
		var tempAssetType = getRealAssetType(assetType);
		return {
			assetId: assetId,
			assetType: tempAssetType
		};
	};

	var commentEventTO = function(commentId, assetId, action) {
		return {
			commentId: commentId,
			assetId: assetId,
			action: action
		};
	};

	var createDependencies = function() {
		return {
			predecessors: [],
			deletedPredecessors: {},
			successors: [],
			deletedSuccessors: {}
		};
	};

	return {
		commentTemplate: commentTemplate,
		datasourcesTemplate: datasourcesTemplate,
		commentTemplateFromEditResponse: commentTemplateFromEditResponse,
		commentTemplateFromCreateResponse: commentTemplateFromCreateResponse,
		closePopup: closePopup,
		validateDelete: validateDelete,
		commentTO: commentTO,
		assetTO: assetTO,
		commentEventTO: commentEventTO,
		createDependencies: createDependencies,
		getRealAssetType: getRealAssetType
	};

};

/*****************************************
 * Directive AssignedToSelect
 */
tds.comments.directive.AssignedToSelect = function(commentService, alerts, utils) {

	return {
		restrict: 'E',
		scope: {
			commentId: '=commentId',
			ngModel: '=ngModel'
		},
		templateUrl: utils.url.applyRootPath('/components/comment/assigned-to-select-template.html'),
		link: function(scope, element, attrs) {
			var validateModel = function() {
				var exist = false;
				var id = scope.ngModel;
				var list = scope.roles;
				if (list.length > 0) {
					for (var i=0; i < list.length; i++) {
						if (list[i].id.toString() == id) {
							exist = true;
							break;
						}
					}
				}
				if (!exist) {
					scope.ngModel = '';
				}
			};
			commentService.getAssignedToList('', scope.commentId).then(
				function(data) {
					var unassigned =  {"id" : "0", "nameRole" : "Unassigned"};
					var auto =  {"id" : "AUTO", "nameRole" : "Automatic"};
					var roles = data.data;
					
					roles.push(auto);
					
					roles.sort(function(a, b) {
					    if (a.nameRole < b.nameRole) return -1;
					    if (a.nameRole > b.nameRole) return 1;
					    return 0;
					});

					roles.unshift(unassigned);

					scope.roles = roles;
					if (!scope.$parent.ac.assignedTo || scope.$parent.ac.assignedTo == "") {
						scope.$parent.ac.assignedTo = "0";
					}
					validateModel();
				},
				function(data) {
					alerts.showGenericMsg();
				}
			);
			scope.$watch('ngModel', function(nVal) { 
				var expression = attrs['ngChange'];
				if (expression) {
					scope.$parent.$eval(expression);
				}
			});  
		}
	};
};

/*****************************************
 * Directive workflowTransitionSelect
 */
tds.comments.directive.WorkflowTransitionSelect = function(commentService, alerts, utils) {

	return {
		restrict: 'E',
		scope: {
			workflows: '=workflows',
			commentId: '=commentId',
			assetId: '=assetId',
			category: '=category',
			ngModel: '=ngModel'
		},
		templateUrl: utils.url.applyRootPath('/components/comment/workflow-transition-select-template.html'),

		link: function(scope, element, attrs) {
			var refresh = function() {
				commentService.getWorkflowTransitions(scope.assetId, scope.category, scope.commentId).then(
					function(data) {
						scope.workflows = data.data;
					},
					function(data) {
						alerts.showGenericMsg();
					}
				);
			};
			scope.$watch('category', function(nVal, oVal) {
				refresh();
			});
			refresh();
		}
	};
};

/*****************************************
 * Directive updateStatusSelect
 */
tds.comments.directive.StatusSelect = function(commentService, alerts, utils) {

	return {
		restrict: 'E',
		scope: {
			commentId: '=commentId',
			ngModel: '=ngModel'
		},
		templateUrl: utils.url.applyRootPath('/components/comment/status-select-template.html'),
		link: function(scope, element, attrs) {
			commentService.getStatusList(scope.commentId).then(
				function(data) {
					scope.statusList = data.data;
				},
				function(data) {
					alerts.showGenericMsg();
				}
			);
		}
	};
};

/*****************************************
 * Directive staffRoles
 */
tds.comments.directive.StaffRoles = function(commentService, alerts, utils) {

	return {
		restrict: 'E',
		scope: {
			ngModel: '=ngModel'
		},
		templateUrl: utils.url.applyRootPath('/components/comment/staff-roles-template.html'),
		link: function(scope, element, attrs) {
			var validateModel = function() {
				var exist = false;
				var id = scope.ngModel;
				var list = scope.roleTypes;
				if (list.length > 0) {
					for (var i=0; i < list.length; i++) {
						if (list[i].id.toString() == id) {
							exist = true;
							break;
						}
					}
				}
				if (!exist) {
					scope.ngModel = '';
				}
			};
			commentService.getStaffRoles().then(
				function(data) {
					var unassigned =  {"id" : "0", "description" : "Unassigned"};
					var auto =  {"id" : "AUTO", "description" : "Automatic"};
					var roles = data.data;
										
					roles.push(auto);
										
					roles.sort(function(a, b) {
					    if (a.description < b.description) return -1;
					    if (a.description > b.description) return 1;
					    return 0;
					});		
					
					roles.unshift(unassigned);
					scope.roleTypes = roles;
					if (!scope.$parent.ac.role || scope.$parent.ac.role == "") {
						scope.$parent.ac.role = "0";
					}
					validateModel();
				},
				function(data) {
					alerts.showGenericMsg();
				}
			);
		}
	};
};

/*****************************************
 * Directive AssetsByType
 */
tds.comments.directive.AssetsByType = function(appCommonData, commentService, alerts, utils) {

	return {
		restrict: 'E',
		scope: {
			assetType: '=assetType',
			ngModel: '=ngModel',
			isRequired: '=isRequired'
		},
		templateUrl: utils.url.applyRootPath('/components/comment/assets-by-type-template.html'),
		link: function(scope, element, attrs) {
			var checkExist = function () {
				var exist = false;
				var id = scope.ngModel;
				var list = scope.assets;
				if (list.length > 0) {
					for (var i=0; i < list.length; i++) {
						if (list[i].id.toString() == id) {
							exist = true;
							break;
						}
					}
					if (!exist) {
						scope.ngModel = '';
					}
				}
			}
			var refresh = function() {
				if (scope.assetType) {
					commentService.getAssetsByType(scope.assetType).then(
						function(data) {
							var aType = data.data.type;
							if (aType == 'Network') {
								aType = 'Other';
							}
							appCommonData.setAssetTypeList(aType, data.data.list);
							if (scope.assetType == aType) {
								scope.assets = appCommonData.getAssetTypeList(scope.assetType);
								checkExist();
							}
						},
						function(data) {
							alerts.showGenericMsg();
						}
					);
				}
			};
			scope.$watch('assetType', function(nVal, oVal) {
				refresh();
			});
		}
	};
};

/*****************************************
 * Directive taskDependencies
 */
tds.comments.directive.TaskDependencies = function(commentService, alerts, utils) {

	return {
		restrict: 'E',
		scope: {
			commentId: '=commentId',
			ngModel: '=ngModel',
			deleted: '=deleted',
			moveEvent: '=moveEvent',
			eventName: '@eventName'
		},
		templateUrl: utils.url.applyRootPath('/components/comment/task-dependencies-template.html'),
		link: function(scope, element, attrs) {
			var depByCategory = {};
			scope.categories = tds.core.service.commonDataService.getCategories();
			var checkTaskIdExist = function (dependency) {
				var exist = false;
				for (var i=0; i < dependency.list.length; i++) {
					//dependency.list[i].id = dependency.list[i].id.toString();
					if (dependency.list[i].id == dependency.taskId) {
						exist = true;
						break;
					}
				}
				if (!exist) {
					dependency.taskId = '';
				}
			}
			scope.initDependencyList = function(dependency) {
				scope.updateDependencyList(dependency);
			};
			scope.updateDependencyList = function(dependency) {
				moveEvent = (scope.moveEvent==null?'':scope.moveEvent);
				if (!depByCategory[dependency.category]) {
					depByCategory[dependency.category] = {};
				}
				if (depByCategory[dependency.category][moveEvent]) {
					dependency.list = depByCategory[dependency.category][moveEvent];
					checkTaskIdExist(dependency);
				} else {
					commentService.getDependencies(dependency.category, '', '', moveEvent).then(
						function(data) {
							depByCategory[dependency.category][moveEvent] = data.data;
							dependency.list = depByCategory[dependency.category][moveEvent];
							checkTaskIdExist(dependency);
						},
						function(data) {
							alerts.showGenericMsg();
						}
					);
				}
			};
			scope.deleteRow = function(index) {
				if (scope.ngModel[index].id) {
					scope.deleted[scope.ngModel[index].id] = scope.ngModel[index].id;
				}
				scope.ngModel.splice(index, 1);
			};
			scope.$watch('moveEvent', function(nValue, oValue) {
				angular.forEach(scope.ngModel, function(dependency) {
					scope.updateDependencyList(dependency);
				});
			});
			scope.internalControl = scope.control || {};
			scope.$on('addDependency', function(evt, evtName) {
				if (evtName == scope.eventName) {
					scope.ngModel.push({
						category: scope.categories[1].id,
						list: [],
						id: '',
						taskId: ''
					});
				}
			});

		}
	};
};

/*****************************************
 * Directive action bar
 */
tds.comments.directive.ActionBar = function(commentService, alerts, utils, commentUtils, window) {

	return {
		restrict: 'E',
		scope: {
			comment: '=comment',
			showDetailsButton: '@showDetails',
			updateTable: '@updateTable',
			status: '=status'
		},
		templateUrl: utils.url.applyRootPath('/components/comment/action-bar-template.html'),
		link: function(scope, element, attrs) {
			var updateActionBar = function() {
				updateStatus(true);
				commentService.getActionBarButtons(scope.comment.commentId, scope.showDetailsButton).then(
					function(data) {
						if (data.status == 'success') {
							scope.buttons = data.data;
						} else {
							alerts.showGenericMsg();
						}
						updateStatus(false);
					},
					function(data) {
						updateStatus(false);
						alerts.showGenericMsg();
					}
				);
			};

			scope.doAction = function(button) {
				if (button.disabled) {
					return;
				}
				var action = null;
				switch (button.actionType) {
					case "changeStatus":
						action = scope.changeStatus;
						break;
					case "assignTask":
						action = scope.assignTask;
						break;
					case "changeEstTime":
						action = scope.changeEstTime;
						break;
					case "showDetails":
						action = scope.showDetails;
						break;
					case "showNeighborhood":
						action = scope.showNeighborhood;
						break;
				}
				if (action != null) {
					action(button);
				}
			};

			scope.changeStatus = function(button) {
				updateStatus(true);
				commentService.changeTaskStatus(scope.comment.commentId, button.newStatus, scope.comment.status).then(
					function(data) {
						postAction(button, data);
					},
					function(data) {
						updateStatus(false);
						alerts.showGenericMsg();
					}
				);
			};

			scope.assignTask = function(button) {
				updateStatus(true);
				commentService.assignTaskToMe(scope.comment.commentId, scope.comment.status).then(
					function(data) {
						postAction(button, data);
					},
					function(data) {
						updateStatus(false);
						alerts.showGenericMsg();
					}
				);
			};

			scope.changeEstTime = function(button) {
				updateStatus(true);
				commentService.changeTaskEstTime(scope.comment.commentId, button.delay).then(
					function(data) {
						postAction(button, data);
					},
					function(data) {
						updateStatus(false);
						alerts.showGenericMsg();
					}
				);

			};

			scope.showDetails = function(button) {
				scope.$emit("viewComment", commentUtils.commentTO(scope.comment.commentId, 'issue'), 'show');
			};

			scope.showNeighborhood = function(button) {
				window.open(utils.url.applyRootPath('/task/taskGraph?neighborhoodTaskId=' + scope.comment.commentId), '_blank');
			};

			var postAction = function(button, data) {
				button.disabled = true;
				updateStatus(false);
				updateActionBar();
				scope.$emit("commentUpdated", scope.comment.commentId, scope.comment.assetEntity);
				if (scope.updateTable) {
					var id = scope.comment.commentId;
					if (data.error) {
						alerts.addAlertMsg(data.error);
					} else {
						switch (button.actionType) {
							case "changeStatus":
								var cell = angular.element('#status_'+id);
								if (cell.length == 0) {
									cell = angular.element('#statusTd_'+id);
								}
								if (cell.length > 0) {
									cell.html(data.assetComment.status)
									cell.parent().removeAttr('class').addClass(data.statusCss)
									cell.removeAttr('class').addClass(data.statusCss).addClass('cellWithoutBackground')
								}
								updateColumn('assignedTo', id, data.assignedToName);
								break;
							case "assignTask":
								updateColumn('assignedTo', id, data.assignedToName);
								break;
							case "changeEstTime":
								updateColumn('estStart', id, data.estStart);
								updateColumn('estFinish', id, data.estFinish);
								break;

						}
					}
				}
			};
			var updateColumn = function(colPrefixId, id, data) {
				var element = angular.element('#' + colPrefixId + '_' + id);
				if (element.length > 0) {
					element.html(data)
				}
			}
			var updateStatus = function(isActive) {
				if (!angular.isUndefined(scope.status)) {
					scope.status.active = isActive;
				}
			}
			updateActionBar();
		}
	};
};

/*****************************************
 * Directive action bar cell
 */
tds.comments.directive.ActionBarCell = function(commentService, alerts, utils, templateCache, http, compile, windowTimedUpdate) {

	return {
		restrict: 'A',
		scope: {
			configTable: '=configTable',
			assetId: '@assetId',
			commentId: '@commentId',
			status: '@status',
			master: '@master',
			idPrefix: '@idPrefix',
			tableColSpan: '@tableColSpan'
		},
		link: function(scope, element, attrs) {
			var templateUrl = utils.url.applyRootPath('/components/comment/action-bar-row-template.html');
			scope.comment = {};
			scope.comment.assetEntity = scope.assetId;
			scope.comment.commentId = scope.commentId;
			scope.comment.status = scope.status;
			scope.configTable[scope.commentId] = null;
			scope.actionBarStatus = {
				active: false
			};
			if (scope.idPrefix == null || (typeof scope.idPrefix === 'undefined')) {
				scope.rowPrefix = '';
			} else {
				scope.rowPrefix = scope.idPrefix;
			}
			if (scope.tableColSpan == null || (typeof scope.tableColSpan === 'undefined')) {
				scope.rowColSpan = 13;
			} else {
				scope.rowColSpan = scope.tableColSpan;
			}

			element.bind('click', function() {
				if (scope.configTable[scope.commentId]) {
					hideContent();
				} else {
					loadContent();	
				}
			});
			var isActiveActionBar = function() {
				var active = false;
				var rowsMap = scope.configTable;
				for(var key in rowsMap) {
					if (rowsMap[key] != null) {
						active = true;
						break;
					}
				}
				return active;
			}
			var hideContent = function() {
				if (scope.configTable[scope.commentId]) {
					scope.configTable[scope.commentId].row.remove();
					scope.configTable[scope.commentId] = null;
				}
				if (!isActiveActionBar()) {
					windowTimedUpdate.resume();
				}
			}
			var loadContent = function() {
				if (scope.configTable[scope.commentId] == null) {
					scope.loading = true;
					var bulkEdit = false;
					var outerBody = angular.element('#outerBodyId');
					if (outerBody.length > 0) {
						bulkEdit = outerBody.scope().bulkEdit;
					}
					if(!(bulkEdit && (scope.comment.status != 'Started' && scope.comment.status != 'Ready'))){
						var content = templateCache.get(templateUrl);
						if (content) {
							showContent(content[1]);
						} else {
							http.get(templateUrl, {cache:templateCache}).then(
								function(data) {
									var content = templateCache.get(templateUrl);
									showContent(content[1]);
								},
								function(data) {
									scope.loading = false;
									alerts.showGenericMsg();
								}
							);
						}
					}
				}
			}
			var showContent = function(content) {
				windowTimedUpdate.pause();
				var row = angular.element('#' + scope.rowPrefix + scope.commentId);
				var newRow = compile(content)(scope);
				row.after(newRow);
				scope.configTable[scope.commentId] = {
					"id": scope.commentId,
					"row": newRow
				}
				scope.loading = false;
			}
			if (scope.master) {
				scope.$on('showActionBars', function(evt) {
					var currentStatus = angular.element('#status_' + scope.commentId).html();
					if ((currentStatus == 'Ready') || (currentStatus == 'Started')) {
						loadContent();	
					}
				});
				scope.$on('hideActionBars', function(evt) {
					hideContent();
				});
			}
		}
	};
};

/*****************************************
 * Directive comment inner list
 */
tds.comments.directive.CommentInnerList = function(commentService, alerts, utils, commentUtils) {

	return {
		restrict: 'E',
		scope: {
			assetId: '@assetId',
			prefValue: '@prefValue'
		},
		templateUrl: utils.url.applyRootPath('/components/comment/comments-inner-list-template.html'),
		link: function(scope, element, attrs) {
			scope.showAll = (scope.prefValue == 'TRUE')?'1':'0';
			scope.comments = [];
			scope.applyRootPath = utils.url.applyRootPath;
			var refreshView = function() {
				commentService.searchComments(scope.assetId, '').then(
					function(data) {
						scope.comments = data;
					},
					function(data) {
						alerts.showGenericMsg();
					}
				);
			};
			scope.updatePreference = function(comment) {
				scope.comments = scope.comments;
				commentService.setShowAllPreference(scope.showAll);
			}
			scope.onlyCompleted = function(comment) {
				if (scope.showAll == '1') {
					return true;
				} else {
					return (
						((comment.commentInstance.commentType == 'issue') && (comment.commentInstance.status != 'Completed'))
						|| ((comment.commentInstance.commentType == 'comment') && (!comment.commentInstance.isResolved))
					);
				}
			}
			scope.$on('commentChanged', function(evt, eventTO) {
				if ((eventTO != null) && (eventTO.assetId == scope.assetId)) {
					refreshView();
				}
			});
			scope.editComment = function(commentId, commentType) {
				scope.$emit("editComment", commentUtils.commentTO(commentId, commentType));
			}
			scope.showComment = function(commentId, commentType) {
				scope.$emit("viewComment", commentUtils.commentTO(commentId, commentType), 'show');
			}
			refreshView();
		}
	};
};

/*****************************************
 * Directive gridButtons
 */
tds.comments.directive.GridButtons = function(utils, commentUtils) {

	return {
		restrict: 'E',
		replace: true,
		scope: {
			assetId: '@assetId', //=rowId
			assetType: '@assetType',
			tasks: '@tasks',
			comments: '@comments'
		},
		templateUrl: utils.url.applyRootPath('/components/comment/grid-buttons-template.html'),
		link: function(scope, element, attrs) {
			scope.applyRootPath = utils.url.applyRootPath;

			scope.listBy = function(commentType) {
				scope.$emit('commentsList', commentUtils.assetTO(scope.assetId, scope.assetType), commentType);
			}

			scope.createCommentBy = function(commentType) {
				scope.$emit('createComment', commentType, commentUtils.assetTO(scope.assetId, scope.assetType));
			}
		}
	};
};

/*****************************************
 * Comments module configuration
 */
tds.comments.module = angular.module('tdsComments', ['tdsCore']);

tds.comments.module.factory('commentService', ['utils', '$http', '$q', tds.comments.service.CommentService]);
tds.comments.module.factory('commentUtils', ['$q', '$interval', 'appCommonData', tds.comments.util.CommentUtils]);

tds.comments.module.directive('assignedToSelect', ['commentService', 'alerts', 'utils', tds.comments.directive.AssignedToSelect]);
tds.comments.module.directive('workflowTransitionSelect', ['commentService', 'alerts', 'utils', tds.comments.directive.WorkflowTransitionSelect]);
tds.comments.module.directive('statusSelect', ['commentService', 'alerts', 'utils', tds.comments.directive.StatusSelect]);
tds.comments.module.directive('taskDependencies', ['commentService', 'alerts', 'utils', tds.comments.directive.TaskDependencies]);
tds.comments.module.directive('actionBar', ['commentService', 'alerts', 'utils','commentUtils', '$window', tds.comments.directive.ActionBar]);
tds.comments.module.directive('actionBarCell', ['commentService', 'alerts', 'utils', '$templateCache','$http','$compile', 'windowTimedUpdate', tds.comments.directive.ActionBarCell]);
tds.comments.module.directive('staffRoles', ['commentService', 'alerts', 'utils', tds.comments.directive.StaffRoles]);
tds.comments.module.directive('assetsByType', ['appCommonData', 'commentService', 'alerts', 'utils', tds.comments.directive.AssetsByType]);
tds.comments.module.directive('commentInnerList', ['commentService', 'alerts', 'utils', 'commentUtils', tds.comments.directive.CommentInnerList]);
tds.comments.module.directive('gridButtons', ['utils', 'commentUtils', tds.comments.directive.GridButtons]);


/***************************
 * COMPATIBILITY FUNCTIONS
 ***************************/

/**
 * Function used to recompile the dynamic code generated by the jqgrid plugin
 */
function recompileDOM(gridElementId) {
	var objDom = $('[ng-app]');
	var injector = angular.element(objDom).injector();
	injector.invoke(function($rootScope, $compile) {
		var gridElement = $('#' + gridElementId);
		$compile(gridElement)($rootScope.commentsScope);
	});
}

function hideCommentDialogs() {
	var objDom = $('[ng-app]');
	var injector = angular.element(objDom).injector();
	injector.invoke(function($rootScope) {
		$rootScope.$broadcast('forceDialogClose', ['crud', 'list']);
	});
}

/*
function addLoadingIndicator(gridElementId) {
	var gridElement = angular.element('.ui-row-ltr:first');
	gridElement.attr('loading-indicator', true);
}
*/