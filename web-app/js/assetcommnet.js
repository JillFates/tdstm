// function to list the comments list
      		function listCommentsDialog(e) {
      			$("#editCommentDialog").dialog("close")
      			$("#showCommentDialog").dialog("close")
				$("#createCommentDialog").dialog("close")
      			var assetComments = eval('(' + e.responseText + ')');
      			
      			var listTable = document.getElementById("listCommentsTable");
	      		var tbody = document.getElementById('listCommentsTbodyId')
      			if (assetComments) {
      				if(tbody != null){				
				   		listTable.removeChild(tbody)
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
					   
					      commentTd.onclick = function(){new Ajax.Request('showComment?id='+this.name,{asynchronous:true,evalScripts:true,onComplete:function(e){showAssetCommentDialog( e , 'show' );}})}
					      // = 'comment_'+commentObj.commentInstance.id
					      var typeTd = document.createElement('td');
					      typeTd.id = 'type_'+commentObj.commentInstance.id
					      typeTd.name = commentObj.commentInstance.id
					      typeTd.onclick = function(){new Ajax.Request('showComment?id='+this.name,{asynchronous:true,evalScripts:true,onComplete:function(e){showAssetCommentDialog( e, 'show' );}})}
					      var verifyTd = document.createElement('td');
						  verifyTd.id = 'verify_'+commentObj.commentInstance.id
						  verifyTd.name = commentObj.commentInstance.id
					      verifyTd.onclick = function(){new Ajax.Request('showComment?id='+this.name,{asynchronous:true,evalScripts:true,onComplete:function(e){showAssetCommentDialog( e, 'show' );}})}					      
					      var image = document.createElement('img');
					      image.src = "../images/skin/database_edit.png"
					      image.border = 0
					      var link = document.createElement('a');
					      link.href = '#'
					      link.id = 'link_'+commentObj.commentInstance.id
					      link.name = commentObj.commentInstance.id
					      link.onclick = function(){new Ajax.Request('showComment?id='+this.name,{asynchronous:true,evalScripts:true,onComplete:function(e){showAssetCommentDialog( e, 'edit' );commentChange('editResolveDiv','editCommentForm');}})} //;return false
					      var commentText = document.createTextNode(truncate(commentObj.commentInstance.comment));
					      var typeText = document.createTextNode(commentObj.commentInstance.commentType);
					      var verifyText = document.createElement('input')
					      verifyText.id = 'verifyText_'+commentObj.commentInstance.id
					      verifyText.type = 'checkbox'
					      verifyText.disabled = 'disabled'
					      if(commentObj.commentInstance.mustVerify == 1){
					      	verifyText.checked = true ;
					      }
					      //createTextNode(commentObj.commentInstance.mustVerify);
					      link.appendChild( image )
						  editTd.appendChild( link  )					      
					      commentTd.appendChild( commentText )
					      typeTd.appendChild( typeText )
					      verifyTd.appendChild( verifyText )
					      tr.appendChild( editTd )
					      tr.appendChild( commentTd )
					      tr.appendChild( typeTd )
					      tr.appendChild( verifyTd )
					      listTbody.appendChild( tr )
				      	}
				      listTable.appendChild( listTbody )
      			}
      			
      			$("#commentsListDialog").dialog('option', 'width', 600)	      	
      			$("#commentsListDialog").dialog('option', 'position', ['center','top']);
		      	$("#commentsListDialog").dialog("open")
		      	timedRefresh('never')
      		}
      		function showAssetCommentDialog( e , action){
      			$("#createCommentDialog").dialog("close")
      		var assetComments = eval('(' + e.responseText + ')');
      			if (assetComments) {
      				 document.getElementById("commentId").value = assetComments[0].assetComment.id
			      	 document.getElementById("commentTdId").value = assetComments[0].assetComment.comment
			      	 document.getElementById("commentTypeTdId").innerHTML = assetComments[0].assetComment.commentType
			      	 document.getElementById("mustVerifyEdit").value = assetComments[0].assetComment.mustVerify
			      	 document.getElementById("isResolved").value = assetComments[0].assetComment.isResolved
			      	 if(assetComments[0].assetComment.mustVerify != 0){
			      	 document.getElementById("mustVerifyShowId").checked = true
			      	 document.editCommentForm.mustVerify.checked = true
			      	 } else {
			      	 document.getElementById("mustVerifyShowId").checked = false
			      	 document.editCommentForm.mustVerify.checked = false
			      	 }
			      	 if(assetComments[0].assetComment.isResolved != 0){
			      	 document.getElementById("isResolvedId").checked = true
			      	 document.editCommentForm.isResolved.checked = true
			      	 } else {
			      	 document.getElementById("isResolvedId").checked = false
			      	 document.editCommentForm.isResolved.checked = false
			      	 }
			      	 document.getElementById("dateResolvedId").innerHTML = assetComments[0].assetComment.dateResolved
			      	 document.getElementById("dateResolvedEditId").innerHTML = assetComments[0].assetComment.dateResolved
			      	 document.getElementById("dateCreatedId").innerHTML = assetComments[0].assetComment.dateCreated
			      	 document.getElementById("dateCreatedEditId").innerHTML = assetComments[0].assetComment.dateCreated
			      	 if(assetComments[0].personResolvedObj != null){
			      	 document.getElementById("resolvedById").innerHTML = assetComments[0].personResolvedObj.firstName+" "+assetComments[0].personResolvedObj.lastName
			      	 document.getElementById("resolvedByEditId").innerHTML = assetComments[0].personResolvedObj.firstName+" "+assetComments[0].personResolvedObj.lastName
			      	 
			      	 }
			      	 if(assetComments[0].personCreateObj != null){
			      	 document.getElementById("createdById").innerHTML = assetComments[0].personCreateObj.firstName+" "+assetComments[0].personCreateObj.lastName
			      	 document.getElementById("createdByEditId").innerHTML = assetComments[0].personCreateObj.firstName+" "+assetComments[0].personCreateObj.lastName
			      	 }
			      	 document.getElementById("resolutionId").value = assetComments[0].assetComment.resolution
			      	 document.editCommentForm.resolution.value = assetComments[0].assetComment.resolution
			      	 document.editCommentForm.comment.value = assetComments[0].assetComment.comment
			      	 document.editCommentForm.commentType.value = assetComments[0].assetComment.commentType
			      	 document.editCommentForm.mustVerify.value = assetComments[0].assetComment.mustVerify
			      	 document.editCommentForm.isResolved.value = assetComments[0].assetComment.isResolved
			      	 document.editCommentForm.id.value = assetComments[0].assetComment.id
			      	 if(action == 'edit'){
				      	$("#editCommentDialog").dialog('option', 'width', 700)
				      	$("#editCommentDialog").dialog("open")
				      	$("#showCommentDialog").dialog("close")
			      	 } else if(action == 'show'){
			      	 	$("#showCommentDialog").dialog('option', 'width', 700)
			      	 	$("#showCommentDialog").dialog("open")
			      	 	$("#editCommentDialog").dialog("close")
			      	 }
			      	 
      			}
      		}
      		function addCommentsToList( e ){
      			var status = document.getElementById('statusId').value
	      		$("#editCommentDialog").dialog("close")
			    var assetComments = eval('(' + e.responseText + ')');
		      	var tbody = document.getElementById('listCommentsTbodyId')
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
						  commentTd.onclick = function(){new Ajax.Request('showComment?id='+this.name,{asynchronous:true,evalScripts:true,onComplete:function(e){showAssetCommentDialog( e , 'show' );}})}
						  var typeTd = document.createElement('td');
						  typeTd.id = 'type_'+assetComments.id
						  typeTd.name = assetComments.id
						  typeTd.onclick = function(){new Ajax.Request('showComment?id='+this.name,{asynchronous:true,evalScripts:true,onComplete:function(e){showAssetCommentDialog( e , 'show' );}})}
						  var verifyTd = document.createElement('td');
						  verifyTd.id = 'verify_'+assetComments.id
						  verifyTd.name = assetComments.id
						  verifyTd.onclick = function(){new Ajax.Request('showComment?id='+this.name,{asynchronous:true,evalScripts:true,onComplete:function(e){showAssetCommentDialog( e , 'show' );}})}
						  var image = document.createElement('img');
					      image.src = "../images/skin/database_edit.png"
					      image.border = 0
						  var link = document.createElement('a');
						  link.href = '#'
						  link.id = 'link_'+assetComments.id
						  link.onclick = function(){new Ajax.Request('showComment?id='+assetComments.id,{asynchronous:true,evalScripts:true,onComplete:function(e){showAssetCommentDialog( e, 'edit' );commentChange('editResolveDiv','editCommentForm');}})} //;return false
					      var commentText = document.createTextNode(truncate(assetComments.comment));
					      var typeText = document.createTextNode(assetComments.commentType);
					      var verifyText = document.createElement('input')
					      verifyText.id = 'verifyText_'+assetComments.id
					      verifyText.type = 'checkbox'
					      verifyText.disabled = 'disabled'
					      if(assetComments.mustVerify != 0){
					      	verifyText.checked = true
					      }
					      //createTextNode(assetComments.mustVerify);
					      link.appendChild( image )
						  editTd.appendChild( link  )					      
					      commentTd.appendChild( commentText )
					      typeTd.appendChild( typeText )
					      verifyTd.appendChild( verifyText )
					      tr.appendChild( editTd )
					      tr.appendChild( commentTd )
					      tr.appendChild( typeTd )
					      tr.appendChild( verifyTd )
					      tbody.appendChild( tr )
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
			      	  var tr = document.getElementById('commentTr_'+assetComments.id);
			      	  tr.style.background = '#65a342'
			      	  if(assetComments.mustVerify != 0){
				      document.getElementById('verifyText_'+assetComments.id).checked = true ;
				      } else {
				      document.getElementById('verifyText_'+assetComments.id).checked = false ;
				      }
				      document.getElementById('type_'+assetComments.id).innerHTML = assetComments.commentType;
				      document.getElementById('comment_'+assetComments.id).innerHTML = truncate(assetComments.comment);
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
			document.getElementById(resolveDiv).style.display = 'block' ;
			document.forms[formName].mustVerify.checked = false;
		}else if(type == "instruction"){
			document.forms[formName].mustVerify.checked = true;
			document.getElementById(resolveDiv).style.display = 'none' ;
		}else{
			document.forms[formName].mustVerify.checked = false;
			document.getElementById(resolveDiv).style.display = 'none' ;
		}
	}


		