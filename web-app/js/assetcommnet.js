// function to list the comments list
      		function listCommentsDialog(e,action) {
      			var role = $("#role").val();
      			$("#editCommentDialog").dialog("close");
      			$("#showCommentDialog").dialog("close");
				$("#createCommentDialog").dialog("close");
				$('#showDialog').dialog('close');
      			$('#editDialog').dialog('close');
      			$('#createDialog').dialog('close');
      			$('#changeStatusDialog').dialog('close');
      			$('#filterDialog').dialog('close');
      			var assetComments = eval('(' + e.responseText + ')');
      			
      			var listTable = $('#listCommentsTable');
	      		var tbody = $('#listCommentsTbodyId');
      			if (assetComments) {
      				if(tbody != null){				
				   		tbody.remove();
				    }
				    var listTbody = document.createElement('tbody');
				    listTbody.id = 'listCommentsTbodyId'
      				var length = assetComments.length
				      	for (var i=0; i < length; i++) {
				      	//generate dynamic rows	
				      	  var commentObj = assetComments[i]
				      	  var tr = document.createElement('tr');
				      	  tr.id = "commentTr_"+commentObj.commentInstance.id
				      	  tr.setAttribute('onmouseover','this.style.backgroundColor="white";');
					      var editTd = document.createElement('td');
					      var commentTd = document.createElement('td');
					      commentTd.id = 'comment_'+commentObj.commentInstance.id
					      commentTd.name = commentObj.commentInstance.id
					   	  
					      commentTd.onclick = function(){new Ajax.Request('../assetEntity/showComment?id='+this.name,{asynchronous:true,evalScripts:true,onComplete:function(e){showAssetCommentDialog( e , 'show' );commentChangeShow();}})}
					      // = 'comment_'+commentObj.commentInstance.id
					      var typeTd = document.createElement('td');
					      typeTd.id = 'type_'+commentObj.commentInstance.id
					      typeTd.name = commentObj.commentInstance.id
					      typeTd.onclick = function(){new Ajax.Request('../assetEntity/showComment?id='+this.name,{asynchronous:true,evalScripts:true,onComplete:function(e){showAssetCommentDialog( e, 'show' );commentChangeShow();}})}
					      var resolveTd = document.createElement('td');
						  resolveTd.id = 'resolve_'+commentObj.commentInstance.id
						  resolveTd.name = commentObj.commentInstance.id
					      resolveTd.onclick = function(){new Ajax.Request('../assetEntity/showComment?id='+this.name,{asynchronous:true,evalScripts:true,onComplete:function(e){showAssetCommentDialog( e, 'show' );commentChangeShow();}})}					      
					      var verifyTd = document.createElement('td');
						  verifyTd.id = 'verify_'+commentObj.commentInstance.id
						  verifyTd.name = commentObj.commentInstance.id
					      verifyTd.onclick = function(){new Ajax.Request('../assetEntity/showComment?id='+this.name,{asynchronous:true,evalScripts:true,onComplete:function(e){showAssetCommentDialog( e, 'show' );commentChangeShow();}})}
					      
					      var categoryTd = document.createElement('td');
					      categoryTd.id = 'category_'+commentObj.commentInstance.id
					      categoryTd.name = commentObj.commentInstance.id					   	  
					      categoryTd.onclick = function(){new Ajax.Request('../assetEntity/showComment?id='+this.name,{asynchronous:true,evalScripts:true,onComplete:function(e){showAssetCommentDialog( e , 'show' );commentChangeShow();}})}
					      
					      var commentCodeTd = document.createElement('td');
					      commentCodeTd.id = 'commentCode_'+commentObj.commentInstance.id
					      commentCodeTd.name = commentObj.commentInstance.id					   	  
					      commentCodeTd.onclick = function(){new Ajax.Request('../assetEntity/showComment?id='+this.name,{asynchronous:true,evalScripts:true,onComplete:function(e){showAssetCommentDialog( e , 'show' );commentChangeShow();}})}
					      					      
					      var image = document.createElement('img');
					      image.src = "../images/skin/database_edit.png"
					      image.border = 0
					      var link = document.createElement('a');
					      //link.href = '#'
					      link.id = 'link_'+commentObj.commentInstance.id
					      link.name = commentObj.commentInstance.id
					      link.onclick = function(){new Ajax.Request('../assetEntity/showComment?id='+this.name,{asynchronous:true,evalScripts:true,onComplete:function(e){showAssetCommentDialog( e, 'edit' );commentChangeEdit('editResolveDiv','editCommentForm');}})} //;return false
					      var commentText = document.createTextNode(truncate(commentObj.commentInstance.comment));
					      var typeText = document.createTextNode(commentObj.commentInstance.commentType);
					      var resolveVal
					      if(commentObj.commentInstance.commentType != "issue"){
					      resolveVal = document.createTextNode('');
					      }else{
					      resolveVal = document.createElement('input')
					      resolveVal.id = 'verifyResolved_'+commentObj.commentInstance.id
					      resolveVal.type = 'checkbox'
					      resolveVal.disabled = 'disabled'
					     
					      }
					      var categoryText = document.createTextNode(truncate(commentObj.commentInstance.category));
					      var commentCodeText = document.createTextNode(truncate(commentObj.commentInstance.commentCode));
					      
					      var verifyText = document.createElement('input')
					      verifyText.id = 'verifyText_'+commentObj.commentInstance.id
					      verifyText.type = 'checkbox'
					      verifyText.disabled = 'disabled'
					     
					      //createTextNode(commentObj.commentInstance.mustVerify);
					      link.appendChild( image )
						  editTd.appendChild( link )	
					      commentTd.appendChild( commentText )
					      typeTd.appendChild( typeText )
					      resolveTd.appendChild( resolveVal )
					      verifyTd.appendChild( verifyText )
					      categoryTd.appendChild( categoryText )
					      commentCodeTd.appendChild( commentCodeText )
					      if ( role ) {
					      	tr.appendChild( editTd )
					      }
					      tr.appendChild( commentTd )
					      tr.appendChild( typeTd )	     					      
					      tr.appendChild( resolveTd )	     					      
					      tr.appendChild( verifyTd )
					      tr.appendChild( categoryTd )
					      tr.appendChild( commentCodeTd )
					      listTbody.appendChild( tr )
					      if(commentObj.commentInstance.isResolved == 1){
					      	resolveVal.checked = true ;
					      }
				       if(commentObj.commentInstance.mustVerify == 1){
					      	verifyText.checked = true ;
					      }
				      	}
				      listTable.append( listTbody )
      			}
      			
      			$("#commentsListDialog").dialog('option', 'width', 800)	      	
      			$("#commentsListDialog").dialog('option', 'position', ['center','top']);
		      	$("#commentsListDialog").dialog("open")
		      	if(action == 'never'){
		      		timedRefresh('never')
		      	}
      		}
      		function showAssetCommentDialog( e , action){
      			$("#createCommentDialog").dialog("close")
      			var assetComments = eval('(' + e.responseText + ')');
      			if (assetComments) {
      			if(assetComments[0].assetComment.comment == null){
      			assetComments[0].assetComment.comment = "";
      			}
      			if(assetComments[0].assetComment.resolution == null){
      			assetComments[0].assetComment.resolution = "";
      			}
      				 $('#commentId').val(assetComments[0].assetComment.id)
      				 $('#updateCommentId').val(assetComments[0].assetComment.id)
			      	 $('#commentTdId').val(assetComments[0].assetComment.comment)
			      	 $('#commentTypeTdId').html(assetComments[0].assetComment.commentType)
			      	 $('#mustVerifyShowId').val(assetComments[0].assetComment.mustVerify)
			      	 $('#isResolvedId').val(assetComments[0].assetComment.isResolved)
			      	 $('#categoryTdId').html(assetComments[0].assetComment.category)
			      	 $('#commentCodeTdId').html(assetComments[0].assetComment.commentCode)
			      	 if(assetComments[0].assetComment.mustVerify != 0){
			      	 $('#mustVerifyShowId').attr('checked', true);
			      	 $('#mustVerifyEditId').attr('checked', true);
			      	 } else {
			      	 $('#mustVerifyShowId').attr('checked', false);
			      	 $('#mustVerifyEditId').attr('checked', false);
			      	 }
			      	 if(assetComments[0].assetComment.isResolved != 0){
			      	 $('#isResolvedId').attr('checked', true);
			      	 $('#isResolvedEditId').attr('checked', true);
			      	 } else {
			      	 $('#isResolvedId').attr('checked', false);
			      	 $('#isResolvedEditId').attr('checked', false);
			      	 }
			      	 $('#dateResolvedId').html(assetComments[0].dtResolved)
			      	 $('#dateResolvedEditId').html(assetComments[0].dtResolved)
			      	 $('#dateCreatedId').html(assetComments[0].dtCreated)
			      	 $('#dateCreatedEditId').html(assetComments[0].dtCreated)
			      	 if(assetComments[0].personResolvedObj != null){
				      	 $('#resolvedById').html(assetComments[0].personResolvedObj.firstName+" "+assetComments[0].personResolvedObj.lastName)
				      	 $('#resolvedByEditId').html(assetComments[0].personResolvedObj.firstName+" "+assetComments[0].personResolvedObj.lastName)
			      	 }else{
				      	 $('#resolvedById').html("")
				      	 $('#resolvedByEditId').html("")
			      	 }
			      	
			      	 $('#createdById').html(assetComments[0].personCreateObj.firstName+" "+assetComments[0].personCreateObj.lastName)
			      	 $('#createdByEditId').html(assetComments[0].personCreateObj.firstName+" "+assetComments[0].personCreateObj.lastName)
			      	 $('#resolutionId').val(assetComments[0].assetComment.resolution)
			      	 $('#resolutionEditId').val(assetComments[0].assetComment.resolution)
			      	 $('#commentEditId').val(assetComments[0].assetComment.comment)
			      	 $('#commentTypeEditId').val(assetComments[0].assetComment.commentType)
			      	 $('#categoryEditId').html(assetComments[0].assetComment.category)
			      	 $('#commentCodeEditId').html(assetComments[0].assetComment.commentCode)
			      	 $('#mustVerifyEditId').val(assetComments[0].assetComment.mustVerify)
			      	 $('#isResolvedEditId').val(assetComments[0].assetComment.isResolved)
			      	 if(action == 'edit'){
				      	$("#editCommentDialog").dialog('option', 'width', 'auto')
				      	$("#editCommentDialog").dialog('option', 'position', ['center','top']);
				      	$("#editCommentDialog").dialog("open")
				      	$("#showCommentDialog").dialog("close")
			      	 } else if(action == 'show'){
			      	 	$("#showCommentDialog").dialog('option', 'width', 'auto')
			      	 	$("#showCommentDialog").dialog('option', 'position', ['center','top']);
			      	 	$("#showCommentDialog").dialog("open")
			      	 	$("#editCommentDialog").dialog("close")
			      	 }
			      	 
      			}
      		}
      		function addCommentsToList( e ){
      			var status = $('#statusId').val();
      			
	      		$("#editCommentDialog").dialog("close")
			    var assetComments = eval('(' + e.responseText + ')');
		      	var tbody = $('#listCommentsTbodyId')
				if (assetComments != "") {
						$("#createCommentDialog").dialog("close")
				      	//generate dynamic rows	
				      	  var tr = document.createElement('tr');
				      	  tr.style.background = '#65a342'
				      	  tr.id = "commentTr_"+assetComments[0].assetComment.id
					      tr.setAttribute('onmouseover','this.style.backgroundColor="white";');
					      var editTd = document.createElement('td');
						  var commentTd = document.createElement('td');
						  commentTd.id = 'comment_'+assetComments[0].assetComment.id
						  commentTd.name = assetComments[0].assetComment.id
						  commentTd.onclick = function(){new Ajax.Request('../assetEntity/showComment?id='+this.name,{asynchronous:true,evalScripts:true,onComplete:function(e){showAssetCommentDialog( e , 'show' );commentChangeShow();}})}
						  var typeTd = document.createElement('td');
						  typeTd.id = 'type_'+assetComments[0].assetComment.id
						  typeTd.name = assetComments[0].assetComment.id
						  typeTd.onclick = function(){new Ajax.Request('../assetEntity/showComment?id='+this.name,{asynchronous:true,evalScripts:true,onComplete:function(e){showAssetCommentDialog( e , 'show' );commentChangeShow();}})}
						  
						  var categoryTd = document.createElement('td');
						  categoryTd.id = 'category_'+assetComments[0].assetComment.id
						  categoryTd.name = assetComments[0].assetComment.id
						  categoryTd.onclick = function(){new Ajax.Request('../assetEntity/showComment?id='+this.name,{asynchronous:true,evalScripts:true,onComplete:function(e){showAssetCommentDialog( e , 'show' );commentChangeShow();}})}
						  
						  var commentCodeTd = document.createElement('td');
						  commentCodeTd.id = 'commentCode_'+assetComments[0].assetComment.id
						  commentCodeTd.name = assetComments[0].assetComment.id
						  commentCodeTd.onclick = function(){new Ajax.Request('../assetEntity/showComment?id='+this.name,{asynchronous:true,evalScripts:true,onComplete:function(e){showAssetCommentDialog( e , 'show' );commentChangeShow();}})}
						  
						  var resolveTd = document.createElement('td');
						  resolveTd.id = 'resolve_'+assetComments[0].assetComment.id
						  resolveTd.name = assetComments[0].assetComment.id
					      resolveTd.onclick = function(){new Ajax.Request('../assetEntity/showComment?id='+this.name,{asynchronous:true,evalScripts:true,onComplete:function(e){showAssetCommentDialog( e, 'show' );commentChangeShow();}})}					      
					   	  var verifyTd = document.createElement('td');
						  verifyTd.id = 'verify_'+assetComments[0].assetComment.id
						  verifyTd.name = assetComments[0].assetComment.id
						  verifyTd.onclick = function(){new Ajax.Request('../assetEntity/showComment?id='+this.name,{asynchronous:true,evalScripts:true,onComplete:function(e){showAssetCommentDialog( e , 'show' );commentChangeShow();}})}
						  var image = document.createElement('img');
					      image.src = "../images/skin/database_edit.png"
					      image.border = 0
						  var link = document.createElement('a');
						  link.href = '#'
						  link.id = 'link_'+assetComments[0].assetComment.id
						  link.onclick = function(){new Ajax.Request('../assetEntity/showComment?id='+assetComments[0].assetComment.id,{asynchronous:true,evalScripts:true,onComplete:function(e){showAssetCommentDialog( e, 'edit' );commentChangeEdit('editResolveDiv','editCommentForm');}})} //;return false
					      var commentText = document.createTextNode(truncate(assetComments[0].assetComment.comment));
					      var typeText = document.createTextNode(assetComments[0].assetComment.commentType);
					      
					      var categoryText = document.createTextNode(assetComments[0].assetComment.category);
					      var commentCodeText = document.createTextNode(assetComments[0].assetComment.commentCode);
					      
					      var resolveVal
					      if(assetComments[0].assetComment.commentType != "issue"){
					      resolveVal = document.createTextNode('');
					      }else{
					      resolveVal = document.createElement('input')
					      resolveVal.id = 'verifyResolved_'+assetComments[0].assetComment.id
					      resolveVal.type = 'checkbox'					     
					      resolveVal.disabled = 'disabled'
					     
					      }
					      var verifyText = document.createElement('input')
					      verifyText.id = 'verifyText_'+assetComments[0].assetComment.id
					      verifyText.type = 'checkbox'
					      verifyText.disabled = 'disabled'
					     
					      //createTextNode(assetComments.mustVerify);
					      link.appendChild( image )
						  editTd.appendChild( link  )					      
					      commentTd.appendChild( commentText )
					      typeTd.appendChild( typeText )
					      resolveTd.appendChild( resolveVal )
					      verifyTd.appendChild( verifyText )
					      categoryTd.appendChild( categoryText )
					      commentCodeTd.appendChild( commentCodeText )
					      tr.appendChild( editTd )
					      tr.appendChild( commentTd )
					      tr.appendChild( typeTd )
					      tr.appendChild( resolveTd )
					      tr.appendChild( verifyTd )
					      tr.appendChild( categoryTd )
					      tr.appendChild( commentCodeTd )
					      tbody.append( tr )
					      if(assetComments[0].assetComment.isResolved == 1){
					      	resolveVal.checked = true;
					      }
					       if(assetComments[0].assetComment.mustVerify != 0){
					      	verifyText.checked = true
					      }
					  	updateAssetCommentIcon( assetComments[0] );
	      		} else {
	      				alert("Comment not created")
	      		}
      		}
      		// update comments 
      		function updateCommentsOnList( e ){
      		var assetComments = eval('(' + e.responseText + ')');
      			if (assetComments != "") {
      				$("#editCommentDialog").dialog("close")
			      	//generate dynamic rows	
			      	  var tr = $('#commentTr_'+assetComments[0].assetComment.id);
			      	  tr.css( 'background', '#65a342' );
			      	  if(assetComments[0].assetComment.mustVerify != 0){
				      $('#verifyText_'+assetComments[0].assetComment.id).attr('checked', true);
				      } else {
				      $('#verifyText_'+assetComments[0].assetComment.id).attr('checked', false);
				      }
				      if(assetComments[0].assetComment.commentType != "issue"){
				      	  $('#resolve_'+assetComments[0].assetComment.id).html("");
				      }else{
					      var checkResolveTd = $('#verifyResolved_'+assetComments[0].assetComment.id);
					      if(checkResolveTd){
					      	checkResolveTd.remove();
					      }
				      	  var resolveVal = document.createElement('input')
					      resolveVal.id = 'verifyResolved_'+assetComments[0].assetComment.id
					      resolveVal.type = 'checkbox'
					      resolveVal.disabled = 'disabled'
					      $('#resolve_'+assetComments[0].assetComment.id).append( resolveVal )
					     
					      if(assetComments[0].assetComment.isResolved != 0){
					      	$('#verifyResolved_'+assetComments[0].assetComment.id).attr('checked', true);
					      } else {
					      	$('#verifyResolved_'+assetComments[0].assetComment.id).attr('checked', false);
					      }
				      }
				      $('#type_'+assetComments[0].assetComment.id).html(assetComments[0].assetComment.commentType);
				      $('#comment_'+assetComments[0].assetComment.id).html(truncate(assetComments[0].assetComment.comment));
				      updateAssetCommentIcon( assetComments[0] )
      			}
      		}
      		// Truncate the text 
      		function truncate( text ){
      			var trunc = text
      			if(text){
      				if(text.length > 30){
      					trunc = trunc.substring(0, 30);
      					trunc += '...'
      				}
      			}
      			return trunc;
      		}
      		
  	function commentChange(resolveDiv,formName) {
		var type = 	document.forms[formName].commentType.value;
		if(type == "issue"){
			$(resolveDiv).css('display', 'block');
			document.forms[formName].mustVerify.checked = false;
			document.forms[formName].mustVerify.value = 0;
			document.forms[formName].isResolved.checked = false;
			document.forms[formName].isResolved.value = 0;
		}else if(type == "instruction"){
			document.forms[formName].mustVerify.checked = true;
			document.forms[formName].mustVerify.value = 1;
			$(resolveDiv).css('display', 'none');
		}else{
			document.forms[formName].mustVerify.checked = false;
			document.forms[formName].mustVerify.value = 0;
			$(resolveDiv).css('display', 'none');
		}
	}
	
  function commentChangeEdit(resolveDiv,formName) {
		var type = 	document.forms[formName].commentType.value;
		if(type == "issue"){
			$("#"+resolveDiv).css('display', 'block');
		}else{
			$("#"+resolveDiv).css('display', 'none');
		}
	}
	
	function commentChangeShow() {
			var type = 	$('#commentTypeTdId').html();
			if(type == "issue"){
				$('#showResolveDiv').css('display', 'block');
			}else{
				$('#showResolveDiv').css('display', 'none');
			}
	}
		
	/*UPDATE THE ASSET COMMENT ICON*/
	function updateAssetCommentIcon( assetComments ){
		var link = document.createElement('a');
		link.href = '#'
		link.onclick = function(){setAssetId(assetComments.assetComment.assetEntity.id);new Ajax.Request('../assetEntity/listComments?id='+assetComments.assetComment.assetEntity,{asynchronous:true,evalScripts:true,onComplete:function(e){listCommentsDialog(e,'never');}})} //;return false
		if( assetComments.status ){
			link.innerHTML = "<img src=\"../i/db_table_red.png\" border=\"0px\">"
		}else{
			link.innerHTML = "<img src=\"../i/db_table_bold.png\" border=\"0px\">"
		}
		var iconObj = $('#icon_'+assetComments.assetComment.assetEntity.id);
		iconObj.html(link)
	}
	
	function resolveValidate(formName,idVal){
		var type = 	document.forms[formName].commentType.value;
		if(type != "issue"){
			document.forms[formName].isResolved.value = 0;
		}
		var resolveBoo = document.forms[formName].isResolved.checked;
		var resolveVal = document.forms[formName].resolution.value;
		var assetId = $("#"+idVal).val()
		if(type == ""){
			alert('Please select comment type');
			return false;
		}else if(resolveBoo){
			if(resolveVal != ""){
			if(formName == "createCommentForm"){
				new Ajax.Request('../assetEntity/saveComment?assetEntity.id='+assetId+'&comment='+document.forms[formName].comment.value+'&isResolved='+document.forms[formName].isResolved.value+'&resolution='+document.forms[formName].resolution.value+'&commentType='+document.forms[formName].commentType.value+'&mustVerify='+document.forms[formName].mustVerify.value+'&category='+document.forms[formName].category.value,{asynchronous:true,evalScripts:true,onComplete:function(e){addCommentsToList(e);}})
			}else{
				new Ajax.Request('../assetEntity/updateComment?id='+assetId+'&comment='+document.forms[formName].comment.value+'&isResolved='+document.forms[formName].isResolved.value+'&resolution='+document.forms[formName].resolution.value+'&commentType='+document.forms[formName].commentType.value+'&mustVerify='+document.forms[formName].mustVerify.value,{asynchronous:true,evalScripts:true,onComplete:function(e){updateCommentsOnList(e);}})
			}
			}else{
				alert('Please enter resolution');
				return false;
			}
		}else{
			if(formName == "createCommentForm"){
				new Ajax.Request('../assetEntity/saveComment?assetEntity.id='+assetId+'&comment='+document.forms[formName].comment.value+'&isResolved='+document.forms[formName].isResolved.value+'&resolution='+document.forms[formName].resolution.value+'&commentType='+document.forms[formName].commentType.value+'&mustVerify='+document.forms[formName].mustVerify.value+'&category='+document.forms[formName].category.value,{asynchronous:true,evalScripts:true,onComplete:function(e){addCommentsToList(e);}})
			}else{
				new Ajax.Request('../assetEntity/updateComment?id='+assetId+'&comment='+document.forms[formName].comment.value+'&isResolved='+document.forms[formName].isResolved.value+'&resolution='+document.forms[formName].resolution.value+'&commentType='+document.forms[formName].commentType.value+'&mustVerify='+document.forms[formName].mustVerify.value,{asynchronous:true,evalScripts:true,onComplete:function(e){updateCommentsOnList(e);}})
			}
		}
	}


		