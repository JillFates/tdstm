<%@ page import="com.tdsops.etl.ETLDomain" contentType="text/html;charset=UTF-8" %>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<meta name="layout" content="topNav"/>

	<title>Metric Definitions</title>
	<link rel="stylesheet" href="https://rawgithub.com/yesmeck/jquery-jsonview/master/dist/jquery.jsonview.css"/>
	<style type="text/css">
	#script {
		background: url(http://i.imgur.com/2cOaJ.png);
		background-attachment: local;
		background-repeat: no-repeat;
		padding-left: 35px;
		padding-top: 10px;
		border-color: #ccc;
	}

	samp {
		background: #000;
		border: 3px groove #ccc;
		color: #058907;
		display: block;
		padding: 5px;
	}
	</style>
</head>

<body>

<form method="post">

	<div class="row" class="form-group">
		<div class="col-md-6">
			<fieldset>
				<legend>Metric Defintions</legend>
				<br>
				<textarea class="form-control" name="definitions" id="definitions" rows="20" style="width: 100%;">${definitions}</textarea>
				<br>


				<div class="col-md-12">
					<div class="col-md-6">
						<input style="width:100%;" name="metricCodes" id="metricCodes" type="text" placeholder="Enter comma delimited list of codes to test">
						<input style="width:30%;" class="form-control" type="button" value="Test" onclick="testMetricDefinitions();">
					</div>
					<div class="col-md-6">
						<input name="version" id="version" type="hidden" value="${version}"/>
						<input class="form-control" type="button" value="Save" onclick="saveMetricDefinitions();">
					</div>
				</div>

				<div id='data'></div>
			</fieldset>
		</div>
	</div>
</form>

<script src="https://cdnjs.cloudflare.com/ajax/libs/jquery.textcomplete/1.8.4/jquery.textcomplete.js"></script>
<script type="text/javascript" src="https://rawgithub.com/yesmeck/jquery-jsonview/master/dist/jquery.jsonview.js"></script>
<script>

	// Saves the current script to the specified DataScript id
	function saveMetricDefinitions() {
		var version = parseInt($("#version").val());
		var definitions = JSON.parse($("#definitions").val());
		delete Array.prototype.toJSON;
		var data = {"definitions": definitions};

		$.ajax('/tdstm/reports/saveMetricDefinitions?version=' + version, {
			type       : 'POST',
			contentType: "application/json; charset=utf-8",
			dataType   : "json",
			data       : JSON.stringify(data),
			success    : function (data) {
				if (data.status === "error") {
					alert(data.errors);
					return
				}

				$("#definitions").val(JSON.stringify(JSON.parse(data.definitions), undefined, 4));
				$("#version").val(data.version);
				alert('Saved!');
			},
			error      : function (xhr, status, text) {
				if (xhr.status == '400') {
					var response = JSON.parse(xhr.responseText);
					var result = '';
					for (var key in response) {
						result = result.concat(JSON.stringify(response[key], undefined, 4), '\n');
					}
					alert(result);
				} else {
					var msg = xhr.getResponseHeader('X-TM-Error-Message');
					debugger;
					if (msg === null) {
						alert('Error(' + xhr.status + ') ' + xhr.responseText);
					} else {
						alert(msg);
					}
				}
			}
		});
	}

	//
	function testMetricDefinitions() {
		var metricCodes = $("#metricCodes").val();
		var definitions = JSON.parse($("#definitions").val());
		delete Array.prototype.toJSON;
		var data = {"definitions": definitions, "metricCodes": metricCodes};
		$('#data').empty();

		$.ajax('/tdstm/reports/testMetricDefinitions', {
			type       : 'POST',
			contentType: "application/json; charset=utf-8",
			dataType   : "json",
			data       : JSON.stringify(data),
			success    : function (responseData) {
				if (responseData.status === "error") {
					alert(responseData.errors);
					return
				}


				var response = responseData.data;
				$.each(response, function (i, item) {
					var table = $('#' + item.metricCode);
					var tableDoesNotExists = table.length === 0

					if (tableDoesNotExists) {
						table = $("<table/>");
						table.addClass('table');
						table.addClass('table-condensed');
						table.addClass('table-hover');
						table.prop("id", item.metricCode);
						table.append("<th>Project Id</th><th>Metric Code</th><th>Date</th><th>Label</th><th>Value</th>");
					}

					var $tr = $('<tr>').append(
						$('<td>').text(item.projectId),
						$('<td>').text(item.metricCode),
						$('<td>').text(item.date),
						$('<td>').text(item.label),
						$('<td>').text(item.value)
					);

					table.append($tr);

					if (tableDoesNotExists) {
						$('#data').append('<h3>' + item.metricCode + '<h3/>');
						table.appendTo('#data');
					}
				});

			},
			error      : function (xhr, status, text) {
				if (xhr.status == '400') {
					var response = JSON.parse(xhr.responseText);
					var result = '';
					for (var key in response) {
						result = result.concat(JSON.stringify(response[key], undefined, 4), '\n');
					}
					alert(result);
				} else {
					var msg = xhr.getResponseHeader('X-TM-Error-Message');
					debugger;
					if (msg === null) {
						alert('Error(' + xhr.status + ') ' + xhr.responseText);
					} else {
						alert(msg);
					}
				}
			}
		});
	}
</script>

</body>
</html>
