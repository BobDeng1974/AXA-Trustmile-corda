
function GETvar(parameterName) {
    var result = null,
    tmp = [];
    location.search
        .substr(1)
        .split("&")
        .forEach(function (item) {
            tmp = item.split("=");
            if (tmp[0] === parameterName) result = decodeURIComponent(tmp[1]);
    });
    return result;
}

var port = GETvar("port");
var local = GETvar("local");
var test = GETvar("test");
MAIN_URL="";
if (port != null) {
    if (local != null) {
        MAIN_URL = "http://localhost:"+port
    } else {
        MAIN_URL = document.location.protocol + "//" + document.location.hostname + ":" + port
    }
   if (test != null) {
        MAIN_URL="//65.52.142.219:"+port;
   }
}

ME_MILEAGE=-1000
ME_PRICE=0
ME_EST_PRICE=0

function X500toO(x500) {
    if (x500 == null || x500 == "") return "";
    var DNs = x500.split(/[,=]/)
    return DNs[1];
}

function estimatedPrice(price, mileage) {
    var estPrice = price - (mileage * 0.5); // 1.- per km
    ME_EST_PRICE = estPrice;
    if(estPrice < 0)
        estPrice = 0;
    $( "#estimatedValue" ).html(estPrice + ".- CHF");
}

function get_vehicle() {
    $( "#car-policy-url" ).attr("href", MAIN_URL+"/api/v1/car-policy");
    $( "#car-event-url" ).attr("href", MAIN_URL+"/api/v1/car-event");

    $.get({
        url: MAIN_URL+"/api/v1/car-event",
        data: {        },
        success: function( result ) {
            //$( "#" ).html(result.);
            $( "#vehicleIdentNumber" ).html(result.vin);
            ME_MILEAGE = result.mileage;
            $( "#mileage" ).html(ME_MILEAGE);
            estimatedPrice(ME_PRICE, ME_MILEAGE);
            $( "#operatingHours" ).html("-");
            if(result.accident)
                $( "#damage" ).html("Nein");
            else
                $( "#damage" ).html("Ja");
        }
    }).fail(function(e) {
      $( "#errorMessage" ).html( "Oh, holy heaven: error reading data from trust store" );
    });

    $.get({
        url: MAIN_URL+"/api/v1/car-policy",
        data: {        },
        success: function( result ) {
            ME_PRICE = result.details.originalPrice;
            $( "#vehicle" ).html(result.car);
            $( "#model" ).html(result.details.model);
            $( "#originalPrice" ).html(ME_PRICE  + ".- CHF");
            estimatedPrice(ME_PRICE, ME_MILEAGE);
            var insurer = X500toO(result.insurer);
            $( "#trustIssuer" ).html(insurer);
            var nofDamages = result.accidentState == "NO" ? "0" : (
                result.accidentState == "ONE" ? "1" : "> 1");
            $( "#numberOfDamages" ).html(nofDamages);
            if (result.state == "FRAUD") {
                $( "#fraudImage" ).html("<img style=\"width:20px\" src=\"images/red.png\"/>");
            } else {
               $( "#fraudImage" ).html("<img style=\"width:20px\" src=\"images/green.png\"/>");
            }
            drawBasic();
        }
    }).fail(function(e) {
      $( "#errorMessage" ).html( "Oh, holy heaven: error reading data from trust store" );
    });
}

function get_me() {
    $.get({
        url: MAIN_URL+"/api/v1/me",
        data: {        },
        success: function( result ) {

             var x500name = result.me.x500Principal.name.split(",");
             var O=x500name[0].split("=")[1];
             var L=x500name[1].split("=")[1];
             var C=x500name[2].split("=")[1];
             var imageName = O.trim().replace(/[ ]/g, '_').replace(/[,\.]/g, '').toLowerCase();
             $( "#party_me" ).html( O+", "+L+" ("+C+")" );
             $( "#image_me" ).html( "<img style=\"width:100%\" src=\"images/node_"+imageName+".jpeg\"/>" );

             drawBasic();
        }
    }).fail(function(e) {
      $( "#errorMessage" ).html( "Oh, holy heaven: error reading data from trust store" );
    });
}



window.addEventListener('resize', function(event){
  // do stuff here
  console.log('resized');
  drawBasic();
});


// Load the Visualization API and the corechart package.
google.charts.load('current', {'packages':['corechart']});

// Set a callback to run when the Google Visualization API is loaded.


google.charts.setOnLoadCallback(drawBasic);


// Callback that creates and populates a data table,
// instantiates the pie chart, passes in the data and
// draws it.
function drawBasic() {
    if (!google.visualization.arrayToDataTable || !google.visualization.LineChart) {
        setTimeout(drawBasic, 1000);
        return;
    };
    var mileage = ME_MILEAGE; //Testing
    var point = 'point { size: 10; shape-type: circle; fill-color: #a52714; }';

    var estPrice = ME_EST_PRICE;

    // Create the data table.
    var data = google.visualization.arrayToDataTable
            ([['X', 'Y', {'type': 'string', 'role': 'style'}],
              [0, ME_PRICE, null],
              [mileage, estPrice, point],
              [200000,  60000, null]
        ]);


    // Set chart options
    var options = {
        legend:{position:'none'},
        pointSize: 1,
        dataOpacity: 1,
        hAxis: {
          title: 'Kilometerstand',
          logscale: true
        },
        vAxis: {
          minValue: 0,
          title: 'Fahrzeugwert',
          logscale: true
        }
      };

    // Instantiate and draw our chart, passing in some options.
    var chart = new google.visualization.LineChart(document.getElementById('curve_chart'));
    chart.draw(data, options);
}



function setWebSocketConnected(connected, running) {
     if (connected && running) {
        $("#image-socket").html("<img id='image-socket-ball' src='images/green.gif'>")
     } else if (connected) {
        $("#image-socket").html("<img id='image-socket-ball' src='images/green.png'>")
     } else {
        $("#image-socket").html("<img id='image-socket-ball' src='images/red.png'>")
     }
}


function connectWebSocket() {
    var socket = new SockJS(MAIN_URL+'/gs-guide-websocket');
    stompClient = Stomp.over(socket);
    stompClient.debug = null;
    stompClient.connect({}, function (frame) {
        setWebSocketConnected(true, false);
        console.log('Connected: ' + frame);
        stompClient.subscribe('/topic/vaultChanged', function (changes) {
            load_data();
            animationOff();
        });
    });
}


function animationOff() {
    setWebSocketConnected(true, false);
}
function animationOn() {
    setWebSocketConnected(true, true);
}

function load_data() {
    connectWebSocket();
    get_me();
    get_vehicle();
}