function TaskProgressBar(taskId, pingTime, onSuccess, onFailure, progressTitle) {
	this.taskId = taskId;
	this.pingTime = pingTime;
	this.onSuccess = onSuccess;
	this.onFailure = onFailure;
	if (progressTitle == undefined) {
		progressTitle = "Working";
	}
	this.progressTitle = progressTitle;
	var self = this;
	
	setTimeout(function() {
		self.initUI();
	}, 10);
	
	this.interval = setInterval(function() {
		self.updateProgress();
	}, self.pingTime);
}

TaskProgressBar.prototype.initUI = function() {
	var initialized = $('#progressBar').length != 0;
	if (!initialized) {
		var newcontent = document.createElement('div');
	    newcontent.innerHTML = '<div class="modal fade" id="globalProgressBar" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">  <div class="modal-dialog">    <div class="modal-content">      <div class="modal-header">        <h4 id="progressTitle" class="modal-title" id="myModalLabel">Modal title</h4>      </div>      <div class="modal-body">	<p id="progressStatus" style="color:#777777; font-size: 11px; font-family: verdana; margin-top: 4px;"></p>        <div id="innerGlobalProgressBar" class="progress-bar" role="progressbar" aria-valuenow="0" aria-valuemin="0" aria-valuemax="100" style="width: 0%; font-size: 11px; font-family: verdana;">0%</div>	<p id="progressStatus" style="color:#777777; font-size: 11px; font-family: verdana; margin-top: 4px;"></p>      </div>      <div class="modal-footer">	<button id="progressClose" type="button" class="btn btn-default" data-dismiss="modal" style="display:none;">Close</button>        <button id="progressCancel" type="button" class="btn btn-primary" style="display:none;">Cancel</button>      </div>    </div>  </div></div>';
	    document.body.appendChild(newcontent.firstChild);
	}
}

TaskProgressBar.prototype.finishProgressBar = function() {
	clearInterval(this.interval);
	setTimeout(function() {
		$('#globalProgressBar').modal('hide');
	}, 2000);

}

TaskProgressBar.prototype.showProgressBar = function() {
	$('#globalProgressBar').modal('show');
}

TaskProgressBar.prototype.updateProgress = function() {
	var self = this;
	
	var jqxhr = $.ajax( 
		{ 	type:'GET',
			cache: false,
			//dataType: 'json',
			// data: 
			url: tdsCommon.createAppURL('/ws/progress/' + self.taskId + '?rand=' + tdsCommon.randomString(16)), 
			success: function(data) {
				if (data) {
					data = data.data;
			        $('#progressTitle').html(self.progressTitle);

					if (data.status == "In progress") {
						var inner = $('#innerGlobalProgressBar');
						var status = $('#progressStatus');
						var value = data.percentComp;
						inner.attr('aria-valuenow', value);
						inner.css('width', value + '%');
						inner.html(value + '%');
						self.showProgressBar();
						status.html(data.status);
						
					} else if (data.status == "Completed")  {
						var inner = $('#innerGlobalProgressBar');
						var status = $('#progressStatus');
						var value = 100;
						inner.attr('aria-valuenow', value);
						inner.css('width', value + '%');
						inner.html(value + '%');
						status.html(data.status);

						if (self.onSuccess != undefined) {
							self.onSuccess();
						}
						self.finishProgressBar();
					} else {
						var status = $('#progressStatus');
						status.html(data.status + (data.detail ? ": " + data.detail : ""));
						
						$('#progressClose').show();
						$('#progressClose').click(function() {
							if (self.onFailure != undefined) {
								self.onFailure();
							}
							self.finishProgressBar();
						});

					}
				}
			},
			error: function() {
				if (self.onFailure != undefined) {
					self.onFailure();
				}
				self.finishProgressBar();
			}
		});
}