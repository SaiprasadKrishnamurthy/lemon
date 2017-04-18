var eventBus = null;
var eventBusOpen = false;
var divCount = 0;
var appendNode = "";

function initWs() {
	eventBus = new EventBus('http://localhost:8765/output');
    eventBus.onopen = function () {
    	eventBusOpen = true;
    	regForMessages();
    	// Blocking request once to let the server know that I'm here. Please broadcast to me too.
    	var xmlHttp = new XMLHttpRequest();
        xmlHttp.open( "GET", 'http://localhost:8765/data', true); // false for synchronous request
        xmlHttp.send(null);
    };
    eventBus.onerror = function(err) {
    	eventBusOpen = false;
    };
}

function regForMessages() {
    if (eventBusOpen) {
    	eventBus.registerHandler('OUTPUT', function (error, message) {
            if (message) {

				var msgList = $("div#messages");
                var object = message.body;
                var id = object["id"];
                if(object["type"] == "pie")
                {
                    var data = object["data"];
                    if($.isArray(data))
                    {
                        data = data;
                    }else
                    {
                        data = [data];
                    }

                }

                if(document.getElementById("chartContent_"+id) == null)
                {
                   if(divCount%3==0)
                   {
                       appendNode = document.createElement('div');
                       appendNode.className="row";
                       $("div#messages").append(appendNode);
                   }
                    var pieNode = document.createElement('div');
                    pieNode.id="chartContent_"+id;
                    pieNode.className="col-sm-4";
                    pieNode.style="border: 2px solid powderblue;"
                    appendNode.appendChild(pieNode);
                    divCount = divCount+1;
                }else
                {
                    document.getElementById("chartContent_"+id).innerHTML = "";

                }
                console.log(divCount)
                 var finaldata = [];
                for(var i=0;i<data.length;i++)
                {
                   for(var j in data[i]){

                           var sub_val =  data[i][j];
                           var  sub_key = j;
                           debugger;
                           console.log(sub_key);
                           finaldata.push({"label":sub_key,"value":parseInt(sub_val)})
                       }

                }

                data = {"title":id,"data":finaldata}
                drawPie(id,data);

				//msgList.html(msgList.html() + "<div>" + JSON.stringify(message.body) + "</div>");

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