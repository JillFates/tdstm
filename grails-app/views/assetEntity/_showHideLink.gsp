<a class="show-hide-link" id="showHide" onClick="toggleDetailList()">
    View All Fields
</a>

<script>
	var showDetails = false;

	function toggleDetailList() {
		// toggle showDetails value
		showDetails = !showDetails;
		
		if (showDetails) {
			$("#showHide").html("Hide Additional Fields");
			$("#details").removeClass("clr-col-6");
			$("#details").addClass("clr-col-12");
			$("#detailsTable").addClass("all-details");
			$("#detailsBody").removeClass("one-column");
			$("#detailsBody").addClass("two-column");
			$("#tab1").html("Details");
		} else {
			$("#showHide").html("View All Fields");
			$("#details").removeClass("clr-col-12");
			$("#details").addClass("clr-col-6");
			$("#detailsTable").removeClass("all-details");
			$("#detailsBody").removeClass("two-column");
			$("#detailsBody").addClass("one-column");
			$("#tab1").html("Summary");
		}
    }
</script>