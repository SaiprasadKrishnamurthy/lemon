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

                     if(document.getElementById("chartContent_"+id) == null)
                    {
                       if(divCount%3==0)
                       {
                           appendNode = document.createElement('div');
                           appendNode.className="row";
                           appendNode.style="width: 98%;margin-left: 1%;";
                           $("div#messages").append(appendNode);
                       }
                        var pieNode = document.createElement('div');
                        pieNode.id="chartContentparent_"+id;
                        pieNode.className="col-sm-4";
                        appendNode.appendChild(pieNode);
                        var piePanelNode = document.createElement('div');
                        piePanelNode.innerHTML = '<div class="panel panel-primary"> <div class="panel-heading"> <h3 class="panel-title">'+id+'</h3> </div> <div class="panel-body" id=chartContent_'+id+'> </div> </div>';
                        pieNode.appendChild(piePanelNode);
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
                               var sub_val = data[i][j];
                               var sub_key =  j;
                               console.log(sub_key);
                               finaldata.push({"label":sub_key,"value":parseInt(sub_val)})
                           }

                    }

                    data = {"title":id,"data":finaldata}
                    drawPie(id,data);

                    document.getElementById("charts_"+id).childNodes[0].style="margin-left:-15%;"
                    var labelId = document.getElementById("charts_"+id).childNodes[0].childNodes[0].id.split("_")[0]
                    $('.'+labelId+'_segmentValue-outer').css('display', 'none');


                }else if(object["type"] == "gauge")
                {
                    var data = object;


                     if(document.getElementById("chartContent_"+id) == null)
                    {
                       if(divCount%3==0)
                       {
                           appendNode = document.createElement('div');
                           appendNode.className="row";
                           appendNode.style="width: 98%;margin-left: 1%;";
                           $("div#messages").append(appendNode);
                       }
                        var gaugeNode = document.createElement('div');
                        gaugeNode.id="chartContentparent_"+id;
                        gaugeNode.className="col-sm-4";
                        appendNode.appendChild(gaugeNode);
                         var  gaugePanelNode = document.createElement('div');
                          gaugePanelNode.innerHTML = '<div class="panel panel-primary"> <div class="panel-heading"> <h3 class="panel-title">'+id+'</h3> </div> <div class="panel-body" id=chartContent_'+id+'> </div> </div>';
                          gaugeNode.appendChild(gaugePanelNode);
                        divCount = divCount+1;



                        	var powerGauge = gauge("#chartContent_"+id, {
                        		size: 300,
                        		clipWidth: 500,
                        		clipHeight: 350,
                        		ringWidth: 60,
                        		maxValue: data["maxValue"],
                        		transitionMs: 4000,
                        	},id);
                        	powerGauge.render();
                        	powerGauge.update(data["selectedValue"]);
                        	window["chartContent_"+id] = powerGauge;
                        	document.getElementById("chartContent_"+id).childNodes[0].style="margin-left:25%;margin-top: 8%;"

                    }else
                    {
                        var powerGauge = window["chartContent_"+id];
                        console.log(powerGauge);
                        console.log(data["selectedValue"]);
                        powerGauge.update(data["selectedValue"]);

                    }




                }


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