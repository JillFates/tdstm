var ProgressTimer = function (defaultValue, preferenceName, refreshCallback) {
	
	// public functions
	var public = {};
	
	// called on the initial definition of a TimerBar object to set up variables
	public.init = function () {
		public.container = '.progress-bar-svg';
		public.timerValue = null;
		public.bindListeners();
		public.getUserPreference();

		if (refreshCallback)
			public.refreshFunction = refreshCallback;
		else
			public.refreshFunction = function () {
				window.location.reload();
			};
	}

	public.isManual = function () {
		return (public.timerValue == 0)
	};

	public.setUpTimerBar = function (response) {
		var responseObject = $.parseJSON(response.responseText);
		var time = parseInt(responseObject.data.preferences[preferenceName]);
		time = isNaN(time) ? defaultValue : time;
		$("#selectTimedBarId").val(time);

		public.setTimerValue(time);
	};

	public.getUserPreference = function () {
		jQuery.ajax({
			dataType: 'json',
			url: tdsCommon.createAppURL('/ws/user/preferences/' + preferenceName),
			data: {},
			type:'GET',
			complete: public.setUpTimerBar
		});
	};

	public.showState = function(state, bar, attachment) {
		// When is completed 1 = 100%
		if(bar.value() == 1){
			public.refreshFunction();
		}
	}

	public.Start = function (sec) {

		if(public.progressTimer) {
			public.progressTimer.destroy();
		}

		public.progressTimer = new ProgressBar.Circle(public.container, {
			strokeWidth: 50,
			duration: sec * 1000,
			color: '#3c8dbc',
			trailColor: '#eee',
			trailWidth: 0,
			svgStyle: null,
			step: public.showState
		});

		public.progressTimer.animate(1.0)
	}

	public.Restart = function () {
		var second = public.timerValue;
		$('#timeBarValueId').val(second);
		public.Start(second);
	}

	public.bindListeners = function () {
		$('#selectTimedBarId').on('change', function (e) {
			var element = e.srcElement;
			if (!element)
				element = e.target;
			public.setTimerValue(element.value);
			public.setUserPreference(element.value);
		});

		$('div[role=dialog]').on('dialogclose', function () {
			public.attemptResume();
		});
	}

	public.setUserPreference = function (newValue) {
		jQuery.ajax({
			dataType: 'json',
			url: tdsCommon.createAppURL('/ws/user/preference'),
			data: {'code':preferenceName,'value':newValue},
			type:'POST'
		});
	}

	public.setTimerValue = function (newTime) {

		public.timerValue = newTime;

		if (newTime != 0) {
			public.Start(newTime);
			$(public.container).parent().show();
		} else {
			$(public.container).parent().hide();
		}
	}

	public.resetTimer = function () {
		if (! public.isManual())
			public.Restart();
	}

	public.init();

	return public;
	
};