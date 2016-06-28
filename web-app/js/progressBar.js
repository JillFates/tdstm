/**
  * Creates a progress bar popup to show task batch progress
  */
tds.ui.progressBar = function(taskId, pingTime, onSuccess, onFailure, progressTitleValue, uiContainerId) {

	var containerTemplate = 
	'<div class="modal fade" id="globalProgressBar" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">' +
		'<div class="modal-dialog">' +
			'<div class="modal-content">' +
				'<div class="modal-header">' +
					'<h4 id="progressTitle" class="modal-title" id="myModalLabel">Modal title</h4>' +
				'</div>' +
				'<div class="modal-body" style="max-height:20em; overflow-y:auto">' +
					'<p id="progressStatus" style="color:#777777; font-size: 12px; font-family: verdana; margin-top: 4px;"></p>' +
					'<div id="innerGlobalProgressBar" class="progress-bar" role="progressbar" aria-valuenow="0" aria-valuemin="0" aria-valuemax="100" style="width: 0%; font-size: 11px; font-family: verdana;">0%</div>' +
					'<p id="progressStatus" style="color:#777777; font-size: 11px; font-family: verdana; margin-top: 4px;"></p>' +
				'</div>' +
				'<div class="modal-footer">' +
					'<button id="progressClose" type="button" class="btn btn-default" data-dismiss="modal" style="display:none;">Close</button>' +
					'<button id="progressCancel" type="button" class="btn btn-primary" style="display:none;"><span class="glyphicon glyphicon-ban-circle" aria-hidden="true"></span> Cancel</button>' +
				'</div>' +
			'</div>' + 
		'</div>' +
	'</div>';

	var lastUpdated = 0;
	var noProgressCount = 0; 	// Used to keep track of # of times called service and got same lastUpdated 

	if (progressTitleValue == undefined) {
		progressTitleValue = "";
	}
	var progressTitle = progressTitleValue;

	if (!uiContainerId) {
		uiContainerId = "progressBar"
	}
	var refUiContainerId = '#' + uiContainerId;

	var FAILED='Failed';
	var COMPLETED='Completed';
	var PENDING='Pending';
	var STARTED='In progress';
	var PAUSED='Paused';
	var CANCELLED='Cancelled';

	var STALLED_RETRIES=10;
	
	var destroy = function() {
	};

	var initContainer = function() {
		var initialized = $(refUiContainerId).length != 0;
		if (!initialized) {
			$(document.body).prepend('<div id="' + uiContainerId + '"></div>');
		}
	}

	var initUI = function() {
		initContainer();
		var container = $(refUiContainerId);
		container.html(containerTemplate)

		$('#progressTitle').html(progressTitle);
		var bar = $('#innerGlobalProgressBar');
		var status = $('#progressStatus');
		var value = 0;
		bar.attr('aria-valuenow', value);
		bar.css('width', value + '%');
		bar.html(value + '%');
		showProgressBar();
		status.html("Initializing...");
	}

	var finishProgressBar = function(onFinishCallback) {
		setTimeout(function() {
			hideModal();
			$(refUiContainerId).html('');
			if (onFinishCallback) {
				onFinishCallback();
			}
		}, 1000);

	}

	var hideModal = function() {
		$('#globalProgressBar').modal('hide');
		$('body').removeClass('modal-open');
		$('.modal-backdrop').remove();
	}

	var showProgressBar = function() {
		//@tavo_luna: prevent dialog to close if clicked outside or after presing [ESC]
		$('#globalProgressBar').modal({
			backdrop: 'static',
 			keyboard: false
		});
	}

	var updateProgress = function() {
		var jqxhr = $.ajax( 
			{ 	type:'GET',
				cache: false,
				//dataType: 'json',
				url: tdsCommon.createAppURL('/ws/progress/' + taskId), 
				success: function(response) {
					var bar = $('#innerGlobalProgressBar');
					var status = $('#progressStatus');
					var closeButton = $('#progressClose');

					if (!response) {
						status.html('Unable to determine the progress.');
					} else {
						var failed=false;
						var showDetail=false;
						var showClose=false;
						var showBar=true;
						var percentComp=100;

						data = response.data;

						// Double-check to makes sure that the progress has been moving forward
						if (data.lastUpdated == lastUpdated) {
							noProgressCount++;
							if (noProgressCount > STALLED_RETRIES) {
								if (! confirm('It appears that the process has stopped work. Press Okay to continue waiting otherwise press Cancel to abort.')) {
									data.status=FAILED;
									data.detail=null;
								} else {
									noProgressCount = 0;
									setTimeout( function() {
										updateProgress();
									}, pingTime);
									return;
								}
							}
						} else {
							lastUpdated = data.lastUpdated;
							noProgressCount=0;
						}

				        $('#progressTitle').html(progressTitle);

						switch (data.status) {
							case STARTED:
								status.html(data.status);
								percentComp=data.percentComp;
								setTimeout(function() {
									updateProgress();
								}, pingTime);
								break;

							case COMPLETED:
								if (data.detail) {
									showDetail=true;
									showClose=true;
									showBar=false;
								} else {
									status.html(data.status);
									finishProgressBar(onSuccess);
								}
								break;

							case CANCELLED:
							case FAILED:
								if (data.detail) {
									showDetail=true;
									showClose=true;
								} else {
									status.html(data.status);
									finishProgressBar(onFailure);
								}
								failed=true;

							default:
								// Paused/Pending
								status.html(data.status + (data.detail ? "<br>" + data.detail : ""));
								percentComp=data.percentComp;
								showBar=true;
						} // switch

						if (showBar) {
							var value = data.percentComp;
							bar.show();
							bar.attr('aria-valuenow', value);
							bar.css('width', value + '%');
							bar.html(value + '%');
							showProgressBar();							
						} else {
							bar.hide();
						}

						if (showDetail) {
							status.html(data.detail);
						}

						if (showClose) {
							closeButton.show();

							closeButton.click(function() {
								closeButton.hide();
								if (failed) {
									finishProgressBar(onFailure);
								} else {
									finishProgressBar(onSuccess);
								}
							});							
						} else if (failed) {
							finishProgressBar(onFailure);
						}



					} // if (data)
				},
				error: function() {
					finishProgressBar(onFailure);
				}
			});
	}
	
	setTimeout(function() {
		initUI();
	}, 10);
	
	setTimeout(function() {
		updateProgress();
	}, pingTime);

	return {
		destroy: destroy
	};
};
