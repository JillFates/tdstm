// function to list the comments list
      		function listCommentsDialog(e,action) {
      			$("#editCommentDialog").dialog("close")
      			$("#showCommentDialog").dialog("close")
				$("#createCommentDialog").dialog("close")
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
					      var image = document.createElement('img');
					      image.src = "../images/skin/database_edit.png"
					      image.border = 0
					      var link = document.createElement('a');
					      link.href = '#'
					      link.id = 'link_'+commentObj.commentInstance.id
					      link.name = commentObj.commentInstance.id
					      link.onclick = function(){new Ajax.Request('../assetEntity/showComment?id='+this.name,{asynchronous:true,evalScripts:true,onComplete:function(e){showAssetCommentDialog( e, 'edit' );commentChangeEdit('#editResolveDiv','editCommentForm');}})} //;return false
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
					      var verifyText = document.createElement('input')
					      verifyText.id = 'verifyText_'+commentObj.commentInstance.id
					      verifyText.type = 'checkbox'
					      verifyText.disabled = 'disabled'
					     
					      //createTextNode(commentObj.commentInstance.mustVerify);
					      link.appendChild( image )
						  editTd.appendChild( link  )					      
					      commentTd.appendChild( commentText )
					      typeTd.appendChild( typeText )
					      resolveTd.appendChild( resolveVal )
					      verifyTd.appendChild( verifyText )
					      tr.appendChild( editTd )
					      tr.appendChild( commentTd )
					      tr.appendChild( typeTd )	     					      
					      tr.appendChild( resolveTd )	     					      
					      tr.appendChild( verifyTd )
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
      			
      			$("#commentsListDialog").dialog('option', 'width', 600)	      	
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
			      	 $('#mustVerifyEditId').val(assetComments[0].assetComment.mustVerify)
			      	 $('#isResolvedEditId').val(assetComments[0].assetComment.isResolved)
			      	 if(action == 'edit'){
				      	$("#editCommentDialog").dialog('option', 'width', 700)
				      	$("#editCommentDialog").dialog('option', 'position', ['center','top']);
				      	$("#editCommentDialog").dialog("open")
				      	$("#showCommentDialog").dialog("close")
			      	 } else if(action == 'show'){
			      	 	$("#showCommentDialog").dialog('option', 'width', 700)
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
					if(status != 'new'){
						$("#createCommentDialog").dialog("close")
				      	//generate dynamic rows	
				      	  var tr = document.createElement('tr');
				      	  tr.style.background = '#65a342'
				      	  tr.id = "commentTr_"+assetComments.id
					      tr.setAttribute('onmouseover','this.style.backgroundColor="white";');
					      var editTd = document.createElement('td');
						  var commentTd = document.createElement('td');
						  commentTd.id = 'comment_'+assetComments.id
						  commentTd.name = assetComments.id
						  commentTd.onclick = function(){new Ajax.Request('../assetEntity/showComment?id='+this.name,{asynchronous:true,evalScripts:true,onComplete:function(e){showAssetCommentDialog( e , 'show' );commentChangeShow();}})}
						  var typeTd = document.createElement('td');
						  typeTd.id = 'type_'+assetComments.id
						  typeTd.name = assetComments.id
						  typeTd.onclick = function(){new Ajax.Request('../assetEntity/showComment?id='+this.name,{asynchronous:true,evalScripts:true,onComplete:function(e){showAssetCommentDialog( e , 'show' );commentChangeShow();}})}
						  var resolveTd = document.createElement('td');
						  resolveTd.id = 'resolve_'+assetComments.id
						  resolveTd.name = assetComments.id
					      resolveTd.onclick = function(){new Ajax.Request('../assetEntity/showComment?id='+this.name,{asynchronous:true,evalScripts:true,onComplete:function(e){showAssetCommentDialog( e, 'show' );commentChangeShow();}})}					      
					   	  var verifyTd = document.createElement('td');
						  verifyTd.id = 'verify_'+assetComments.id
						  verifyTd.name = assetComments.id
						  verifyTd.onclick = function(){new Ajax.Request('../assetEntity/showComment?id='+this.name,{asynchronous:true,evalScripts:true,onComplete:function(e){showAssetCommentDialog( e , 'show' );commentChangeShow();}})}
						  var image = document.createElement('img');
					      image.src = "../images/skin/database_edit.png"
					      image.border = 0
						  var link = document.createElement('a');
						  link.href = '#'
						  link.id = 'link_'+assetComments.id
						  link.onclick = function(){new Ajax.Request('../assetEntity/showComment?id='+assetComments.id,{asynchronous:true,evalScripts:true,onComplete:function(e){showAssetCommentDialog( e, 'edit' );commentChangeEdit('#editResolveDiv','editCommentForm');}})} //;return false
					      var commentText = document.createTextNode(truncate(assetComments.comment));
					      var typeText = document.createTextNode(assetComments.commentType);
					      var resolveVal
					      if(assetComments.commentType != "issue"){
					      resolveVal = document.createTextNode('');
					      }else{
					      resolveVal = document.createElement('input')
					      resolveVal.id = 'verifyResolved_'+assetComments.id
					      resolveVal.type = 'checkbox'					     
					      resolveVal.disabled = 'disabled'
					     
					      }
					      var verifyText = document.createElement('input')
					      verifyText.id = 'verifyText_'+assetComments.id
					      verifyText.type = 'checkbox'
					      verifyText.disabled = 'disabled'
					     
					      //createTextNode(assetComments.mustVerify);
					      link.appendChild( image )
						  editTd.appendChild( link  )					      
					      commentTd.appendChild( commentText )
					      typeTd.appendChild( typeText )
					      resolveTd.appendChild( resolveVal )
					      verifyTd.appendChild( verifyText )
					      tr.appendChild( editTd )
					      tr.appendChild( commentTd )
					      tr.appendChild( typeTd )
					      tr.appendChild( resolveTd )
					      tr.appendChild( verifyTd )
					      tbody.append( tr )
					      if(assetComments.isResolved == 1){
					      	resolveVal.checked = true;
					      }
					       if(assetComments.mustVerify != 0){
					      	verifyText.checked = true
					      }
					  
				    } else {
						window.location.reload()
					}
	      		} else {
	      				alert("Comment not created")
	      		}
      		}
      		// update comments 
      		function updateCommentsOnList( e ){
      		var assetComments = eval('(' + e.responseText + ')');
      			if (assetComments) {
      				$("#editCommentDialog").dialog("close")
			      	//generate dynamic rows	
			      	  var tr = $('#commentTr_'+assetComments.id);
			      	  tr.css( 'background', '#65a342' );
			      	  if(assetComments.mustVerify != 0){
				      $('#verifyText_'+assetComments.id).attr('checked', true);
				      } else {
				      $('#verifyText_'+assetComments.id).attr('checked', false);
				      }
				      if(assetComments.commentType != "issue"){
				      	  $('#resolve_'+assetComments.id).html("");
				      }else{
					      var checkResolveTd = $('#verifyResolved_'+assetComments.id);
					      if(checkResolveTd){
					      	checkResolveTd.remove();
					      }
				      	  var resolveVal = document.createElement('input')
					      resolveVal.id = 'verifyResolved_'+assetComments.id
					      resolveVal.type = 'checkbox'
					      resolveVal.disabled = 'disabled'
					      $('#resolve_'+assetComments.id).append( resolveVal )
					     
					      if(assetComments.isResolved != 0){
					      	$('#verifyResolved_'+assetComments.id).attr('checked', true);
					      } else {
					      	$('#verifyResolved_'+assetComments.id).attr('checked', false);
					      }
				      }
				      $('#type_'+assetComments.id).html(assetComments.commentType);
				      $('#comment_'+assetComments.id).html(truncate(assetComments.comment));
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
			$(resolveDiv).css('display', 'block');
		}else{
			$(resolveDiv).css('display', 'none');
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

		