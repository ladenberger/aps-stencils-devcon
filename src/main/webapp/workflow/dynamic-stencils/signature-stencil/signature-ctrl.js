function signatureController($rootScope, $scope, $http) {

	// Register this controller to listen to the form extensions methods
	$scope.registerCustomFieldListener(this);

	// Deregister on form destroy1
	$scope.$on("$destroy", function handleDestroyEvent() {
		$scope.removeCustomFieldListener(this);
	});

	// Will be triggered before the task is saved
	this.taskBeforeSaved = function(taskId, form, data, scope) {
		data.values.pleasesign = angular.toJson($scope.field.value);
	};

	// Will be triggered before the form is completed
	this.formBeforeComplete = function(form, outcome, scope) {
		$scope.field.value = angular.toJson($scope.field.value);
	};

	var canvas = document.querySelector("canvas");
	var context = canvas.getContext("2d");
	canvas.height = 200;
	canvas.width = 600;
	var signaturePad = new SignaturePad(canvas, {
		// It's Necessary to use an opaque color when saving image as JPEG;
		// this option can be omitted if only saving as PNG or SVG
		backgroundColor : 'rgb(255, 255, 255)',
		onEnd : function() {
			$scope.field.value = signaturePad.toData();
		}
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

	signaturePad.fromData($scope.field.value);

	context.strokeStyle = "#000000";
	context.lineWidth = 1;
	context.strokeRect(0, 0, 600, 200);

	$scope.clear = function() {
		signaturePad.clear();
		context.strokeStyle = "#000000";
		context.lineWidth = 1;
		context.strokeRect(0, 0, 600, 200);
	};

};

angular.module('activitiApp').controller('signatureController',
		signatureController);