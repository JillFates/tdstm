function TaskProgressBar(taskId, pingTime, onSuccess, onFailure) {
	this.taskId = taskId;
	this.pingTime = pingTime;
	this.onSuccess = onSuccess;
	this.onFailure = onFailure;
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
	    newcontent.innerHTML = '<div id="globalProgressBar" style="display:none; width:100%; text-align:center; position: absolute; top: 80px; "><div style="width: 400px; height: 60px; margin: auto; background-color: #FFFFFF; border-color: #DDDDDD; border-style: solid; border-width: 1px; border-radius: 4px 4px 4px 4px; -webkit-box-shadow: none; box-shadow: none"><p id="progressStatus" style="color:#777777; font-size: 11px; font-family: verdana; margin-top: 4px;"></p><div class="progress" style="margin: 10px; margin: auto; width: 300px;"><div id="innerGlobalProgressBar" class="progress-bar" role="progressbar" aria-valuenow="0" aria-valuemin="0" aria-valuemax="100" style="width: 0%; font-size: 11px; font-family: verdana;">0%</div></div></div></div>';
        document.body.appendChild(newcontent.firstChild);
	}
}

TaskProgressBar.prototype.finishProgressBar = function() {
	clearInterval(this.interval);
	setTimeout(function() {
		$('#globalProgressBar').hide();
	}, 2000);

}

TaskProgressBar.prototype.showProgressBar = function() {
	$('#globalProgressBar').show();
}

TaskProgressBar.prototype.updateProgress = function() {
	var self = this;
	
	var jqxhr = $.get('/tdstm/ws/progress/' + self.taskId, function(data) {
		if (data) {
			data = data.data;
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
				status.html(data.status);

				if (self.onFailure != undefined) {
					self.onFailure();
				}
				self.finishProgressBar();
			}
		}
	}).fail(function() {
		if (self.onFailure != undefined) {
			self.onFailure();
		}
		self.finishProgressBar();
	});
}