angular.module('activitiApp').run(['$templateCache', function($templateCache) {
	$templateCache.put('notes',
	"<div ng-controller=\"notesController\">\n" +
	"	<div id=\"notesContainer\">\n" +
	"\n" +
	"		<div class=\"input-group\" style=\"width: 100%\"\n" +
	"			ng-show=\"!(field.type === 'readonly')\" id=\"notesInput\">\n" +
	"			<textarea class=\"form-control\" rows=\"3\" style=\"resize: none;\"\n" +
	"				ng-model=\"currentNote\" placeholder=\"Write some note ...\"></textarea>\n" +
	"			<span class=\"input-group-addon btn btn-primary\" ng-click=\"addNote()\">\n" +
	"				<span class=\"glyphicon glyphicon-plus\" aria-hidden=\"true\"></span>\n" +
	"			</span>\n" +
	"		</div>\n" +
	"\n" +
	"		<div class=\"list-group\">\n" +
	"\n" +
	"			<div ng-repeat=\"note in field.value | reverse track by $index\"\n" +
	"				class=\"list-group-item list-group-item-action flex-column align-items-start active\">\n" +
	"				<div class=\"d-flex w-100 justify-content-between\">\n" +
	"					<small>{{note.fullName}} - {{note.date}}</small>\n" +
	"				</div>\n" +
	"				<p class=\"mb-1\">{{note.content}}</p>\n" +
	"			</div>\n" +
	"\n" +
	"		</div>\n" +
	"\n" +
	"	</div>\n" +
	"</div>");
	$templateCache.put('signature',
	"<div ng-controller=\"signatureController\" id=\"signatureContainer\">\n" +
	"	<div style=\"float: left\">\n" +
	"		<canvas></canvas>\n" +
	"	</div>\n" +
	"	<div style=\"float: left\" ng-show=\"!(field.type === 'readonly')\">\n" +
	"		<button ng-click=\"clear()\">Clear</button>\n" +
	"	</div>\n" +
	"</div>");
}]);
