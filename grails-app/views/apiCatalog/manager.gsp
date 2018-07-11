<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<meta name="layout" content="topNav"/>

	<title>Api Catalog Manager</title>
	<link rel="stylesheet" href="https://rawgithub.com/yesmeck/jquery-jsonview/master/dist/jquery.jsonview.css"/>
	<style type="text/css">
        div#dictionaryView {
            overflow: scroll;
            height: 600px;
        }
	</style>
</head>
<body>
<tds:subHeader title="Api Catalog Manager" crumbs="['Admin','Api Catalog Manager']"/>

<div class="row">
    <div class="col-md-12">
        Select an Api Catalog to view or edit:
        <select id="apiCatalogs" name="apiCatalogs"></select>
    </div>
</div>
<div class="row">
	<div class="col-md-6">
		<form method="post">
			<fieldset>
				<textarea class="form-control" name="dictionary" id="dictionary" rows="30" style="width: 100%;"></textarea>
				<br>
				<div class="col-md-12">
                    <button class="btn btn-primary" type="button" onclick="viewMergedDictionaryJson()">
                        <i class="fa fa-fw fa-eye"></i>
                        View Merged
                    </button>
                    <button class="btn btn-primary" type="button" onclick="saveDictionaryJson()">
                        <i class="fa fa-fw fa-floppy-o"></i>
                        Save
                    </button>
                    <button class="btn btn-danger" type="button" onclick="deleteSelectedOne()">
                        <i class="fa fa-fw fa-trash-o"></i>
                        Delete
                    </button>
                    <input name="id" id="id" type="hidden" value=""/>
                    <input name="version" id="version" type="hidden" value=""/>
				</div>
                <br>
			</fieldset>
		</form>
	</div>
	<div class="col-md-6">
		<div class="form-control" id="dictionaryView"></div>
	</div>
</div>

<script src="https://cdnjs.cloudflare.com/ajax/libs/jquery.textcomplete/1.8.4/jquery.textcomplete.js"></script>
<script type="text/javascript" src="https://rawgithub.com/yesmeck/jquery-jsonview/master/dist/jquery.jsonview.js"></script>
<script>

    function viewMergedDictionaryJson() {
        try {
            //$('div#dictionaryView').JSONView($('textarea#dictionary').val(), {collapsed: true});
            var payload = {
                id: null,
                version: 0,
                dictionary: $('textarea#dictionary').val()
            };

            $.ajax('/tdstm/apiCatalog/viewMerged', {
                type       : 'POST',
                contentType: 'application/json; charset=utf-8',
                dataType   : 'json',
                data       : JSON.stringify(payload),
                success    : function (data) {
                    if (data.status === 'error') {
                        $('div#dictionaryView').html(data.errors);
                        return
                    }

                    $('div#dictionaryView').JSONView(data.dictionary, {collapsed: true});
                },
                error      : function (xhr, status, text) {

                    if (xhr.status == '400') {
                        var response = JSON.parse(xhr.responseText);
                        var result = '';
                        for (var key in response) {
                            result = result.concat(JSON.stringify(response[key], undefined, 4), '\n');
                        }
                        $('div#dictionaryView').html(result)
                    } else {
                        var msg = xhr.getResponseHeader('X-TM-Error-Message');
                        debugger;
                        if (msg === null) {
                            alert('Error(' + xhr.status + ') ' + xhr.responseText);
                        } else {
                            $('div#dictionaryView').html(msg)
                        }
                    }
                }
            });
        } catch (e) {
            $('div#dictionaryView').html(e)
        }
    }

    function saveDictionaryJson() {
        try {
            var payload = {
                id: $('input#id').val() === '' ? null : $('input#id').val(),
                version: $('input#version').val() === '' ? 0 : $('input#version').val(),
                dictionary: $('textarea#dictionary').val()
            };

            $.ajax('/tdstm/apiCatalog/save', {
                type       : 'POST',
                contentType: 'application/json; charset=utf-8',
                dataType   : 'json',
                data       : JSON.stringify(payload),
                success    : function (data) {
                    if (data.status === 'error') {
                        $('div#dictionaryView').html(data.errors);
                        return
                    }

                    $('div#dictionaryView').JSONView(data.model.dictionary, {collapsed: true});
                    refreshCatalogsDropDown()
                },
                error      : function (xhr, status, text) {

                    if (xhr.status == '400') {
                        var response = JSON.parse(xhr.responseText);
                        var result = '';
                        for (var key in response) {
                            result = result.concat(JSON.stringify(response[key], undefined, 4), '\n');
                        }
                        $('div#dictionaryView').html(result)
                    } else {
                        var msg = xhr.getResponseHeader('X-TM-Error-Message');
                        debugger;
                        if (msg === null) {
                            alert('Error(' + xhr.status + ') ' + xhr.responseText);
                        } else {
                            $('div#dictionaryView').html(msg)
                        }
                    }
                }
            });
        } catch (e) {
            $('div#dictionaryView').html(e)
        }
    }

    function viewPretty(id) {
        if (id === '-1') {
            $('textarea#dictionary').val('');
            $('div#dictionaryView').html('');
            $('input#id').val('');
            $('input#version').val('');
            return;
        }
        try {
            $.ajax('/tdstm/apiCatalog/viewPretty/' + id, {
                type       : 'GET',
                accept     : 'application/json; charset=utf-8',
                success    : function (data) {
                    if (data.status === 'error') {
                        $('div#dictionaryView').html(data.errors);
                        $('input#id').val('');
                        $('input#version').val('');

                        return
                    }

                    $('textarea#dictionary').val(data.dictionary);
                    $('div#dictionaryView').JSONView(data.jsonDictionaryTransformed, {collapsed: true});
                    $('input#id').val(data.id);
                    $('input#version').val(data.version);

                },
                error      : function (xhr, status, text) {

                    $('input#id').val('');
                    $('input#version').val('');

                    if (xhr.status == '400') {
                        var response = JSON.parse(xhr.responseText);
                        var result = '';
                        for (var key in response) {
                            result = result.concat(JSON.stringify(response[key], undefined, 4), '\n');
                        }
                        $('div#dictionaryView').html(result)
                    } else {
                        var msg = xhr.getResponseHeader('X-TM-Error-Message');
                        debugger;
                        if (msg === null) {
                            alert('Error(' + xhr.status + ') ' + xhr.responseText);
                        } else {
                            $('div#dictionaryView').html(msg)
                        }
                    }
                }
            });
        } catch (e) {
            $('div#dictionaryView').html(e)
            $('input#id').val('');
            $('input#version').val('');
        }
    }

    function deleteSelectedOne() {
        var id = $('input#id').val();
        if (id === '-1' || id === '') {
            $('textarea#dictionary').val('');
            $('div#dictionaryView').html('');
            $('input#id').val('');
            $('input#version').val('');
            return;
        }

        if (!confirm('Are you sure to delete this Api Catalog?')) {
            return;
        }

        try {
            $.ajax('/tdstm/apiCatalog/delete/' + id, {
                type       : 'DELETE',
                accept     : 'application/json; charset=utf-8',
                success    : function (data) {
                    if (data.status === 'error') {
                        $('div#dictionaryView').html(data.errors);
                        $('textarea#dictionary').val('');
                        $('input#id').val('');
                        $('input#version').val('');

                        return
                    }

                    $('textarea#dictionary').val('');
                    $('div#dictionaryView').html('');
                    $('input#id').val('');
                    $('input#version').val('');
                    refreshCatalogsDropDown()

                },
                error      : function (xhr, status, text) {

                    $('textarea#dictionary').val('');
                    $('div#dictionaryView').html('');
                    $('input#id').val('');
                    $('input#version').val('');

                    if (xhr.status == '400') {
                        var response = JSON.parse(xhr.responseText);
                        var result = '';
                        for (var key in response) {
                            result = result.concat(JSON.stringify(response[key], undefined, 4), '\n');
                        }
                        $('div#dictionaryView').html(result)
                    } else {
                        var msg = xhr.getResponseHeader('X-TM-Error-Message');
                        debugger;
                        if (msg === null) {
                            alert('Error(' + xhr.status + ') ' + xhr.responseText);
                        } else {
                            $('div#dictionaryView').html(msg)
                        }
                    }
                }
            });
        } catch (e) {
            $('textarea#dictionary').val('');
            $('div#dictionaryView').html('');
            $('input#id').val('');
            $('input#version').val('');
            alert(e)
        }
    }

    function refreshCatalogsDropDown() {
        try {
            $.ajax('/tdstm/apiCatalog/list', {
                type       : 'GET',
                accept     : 'application/json; charset=utf-8',
                success    : function (data) {
                    if (data.status === 'error') {
                        alert(data.errors)
                        return
                    }

                    var options = $('select#apiCatalogs');
                    options.empty();
                    options.append(new Option('--- Choose an Api Catalog for viewing or editing ---', '-1'));
                    $.each(data.model, function(key, value) {
                        console.log('key=', key, 'value=',value);
                        options.append(new Option(value.provider.name + ' - ' + value.name, value.id));
                    });

                },
                error      : function (xhr, status, text) {

                    if (xhr.status == '400') {
                        var response = JSON.parse(xhr.responseText);
                        var result = '';
                        for (var key in response) {
                            result = result.concat(JSON.stringify(response[key], undefined, 4), '\n');
                        }
                        alert(result)
                    } else {
                        var msg = xhr.getResponseHeader('X-TM-Error-Message');
                        debugger;
                        if (msg === null) {
                            alert('Error(' + xhr.status + ') ' + xhr.responseText);
                        } else {
                            alert(msg)
                        }
                    }
                }
            });
        } catch (e) {
            alert(e)
        }
    }

    $(document).ready(function() {
        $('select#apiCatalogs').on('change', function() {
            viewPretty($(this).val());
        });
        refreshCatalogsDropDown();
    });

</script>

</body>
</html>
