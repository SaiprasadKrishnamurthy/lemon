var eventBus = null;
var eventBusOpen = false;

function initWs() {
	eventBus = new EventBus('http://localhost:8765/output');
    eventBus.onopen = function () {
    	eventBusOpen = true;
    	regForMessages();
    };
    eventBus.onerror = function(err) {
    	eventBusOpen = false;
    };
}

function regForMessages() {
    if (eventBusOpen) {
    	eventBus.registerHandler('OUTPUT', function (error, message) {
            if (message) {
				console.info('Found message: ' + message);
				var msgList = $("div#messages");
				msgList.html(msgList.html() + "<div>" + message.body + "</div>");

            } else if (error) {
            	console.error(error);
            }
        });
    } else {
        console.error("Cannot register for messages; event bus is not open");
    }
}

$( document ).ready(function() {
	initWs();
});

function unregister() {
	reg().subscribe(null);
}