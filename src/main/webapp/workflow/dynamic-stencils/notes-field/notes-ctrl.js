function notesController($rootScope, $scope, $http, $filter) {

	var getDatetime = function() {
		return (new Date).toLocaleFormat("%A, %B %e, %Y");
	};

	// Register this controller to listen to the form extensions methods
	$scope.registerCustomFieldListener(this);

	// Deregister on form destroy1
	$scope.$on("$destroy", function handleDestroyEvent() {
		$scope.removeCustomFieldListener(this);
	});

	if (!$scope.field.value) {
		$scope.field.value = [];
	} else if (typeof $scope.field.value === "string") {
		try {
			$scope.field.value = JSON.parse($scope.field.value);
		} catch (error) {
			$scope.field.value = [];
			console.error(error);
		}
	}

	// Will be triggered before the task is saved
	this.taskBeforeSaved = function(taskId, form, data, scope) {
		// If a note has been entered however the user did not click on the "+"
		// button, save the note anyway
		$scope.addNote();
		// Save the content of the form field as JSON
		data.values.notes = angular.toJson($scope.field.value);
	};

	// Will be triggered before the form is completed
	this.formBeforeComplete = function(form, outcome, scope) {
		// If a note has been entered however the user did not click on the "+"
		// button, save the note anyway
		$scope.addNote();
		// Save the content of the form field as JSON
		$scope.field.value = angular.toJson($scope.field.value);
	};

	// Scope function for adding a new note to the scope
	$scope.addNote = function() {

		// Add the note only if it is not undefined and not empty
		if ($scope.currentNote) {

			// Create a new note object
			var newNote = {
				"userID" : $scope.$root.account.id,
				"fullName" : $scope.$root.account.fullname,
				"date" : $filter("date")(new Date(), "dd.MM.yyyy HH:mm"),
				"content" : $scope.currentNote
			};

			// Add the new note to the list of notes
			$scope.field.value.push(newNote);

			// Clear the text field
			$scope.currentNote = "";

		}

	}

};

// Filter that is used to reverse the order of the notes (force that the
// newest note is at the top of the list)
function ReverseFilter() {
	return function(items) {
		if (items) {
			return items.slice().reverse();
		} else {
			return items;
		}
	};
}

angular.module('activitiApp').filter('reverse', ReverseFilter).controller(
		'notesController', notesController);