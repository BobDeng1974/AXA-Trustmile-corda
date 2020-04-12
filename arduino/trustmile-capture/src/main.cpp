/*
 *  THis is our Trustedmile - ESP32 code
 * 
 * tips 
 * - read from EEPROM https://randomnerdtutorials.com/esp32-flash-memory/
 * - run tasks https://techtutorialsx.com/2017/05/06/esp32-arduino-creating-a-task/
 *             http://www.iotsharing.com/2017/06/arduino-esp32-freertos-how-to-use-task-param-task-priority-task-handle.html 
 * - webserver https://github.com/espressif/arduino-esp32/blob/master/libraries/WebServer/examples/HelloServer/HelloServer.ino
 * - wifi client https://github.com/espressif/arduino-esp32/blob/master/libraries/WiFi/examples/WiFiClient/WiFiClient.ino
 * - http client https://github.com/espressif/arduino-esp32/tree/master/libraries/HTTPClient/examples/BasicHttpClient
 * - UTC convert https://www.epochconverter.com/
 
 */
#include <Arduino.h>
#include <HTTPClient.h>
#include <WiFi.h>
#include <WiFiMulti.h>
#include <WebServer.h>
#include <ESPmDNS.h>
//#include <TimeNTP.h>

#include <Configuration.h>
#include <REST.h>
#include <Cars.h>
#include <AnalogState.h>

#include "AzureIotHub.h"
#include "Esp32MQTTClient.h"
#include <list>

WiFiMulti WiFiMulti;

const int buzzerHackitPin = 27;
const int redInputLapPin = 32;
const int blackInputLapPin = 35;

WebServer server(80);

const char* ssid = "Lolo";
const char* password = "JoelUndPascal1";
const int INC_MILEAGE = 1000;
const int INC_MILEAGE_PER_ROUND = 2;

int WIFI_RUNNING = 0;
boolean hackit=false;
unsigned long easyTimer=0;

// init MQTT
/*String containing Hostname, Device Id & Device Key in the format:                         */
/*  "HostName=<host_name>;DeviceId=<device_id>;SharedAccessKey=<device_key>"                */
/*  "HostName=<host_name>;DeviceId=<device_id>;SharedAccessSignature=<device_sas_token>"    */
static const char *connectionString = "HostName=axa-trustmile-iot-hub.azure-devices.net;DeviceId=RaceTrackTransmitter;SharedAccessSignature=SharedAccessSignature sr=axa-trustmile-iot-hub.azure-devices.net%2Fdevices%2FRaceTrackTransmitter&sig=3%2BpUhEZln01%2BMgvCsHGh9vm4QNv%2FUkl3BzgIon4Vrag%3D&se=1599762258";
static bool messageSending = true;

std::list<String> mqttMessages;

void WebServer_start();
void logWebRequest(IPAddress ip, const char* user, HTTPMethod method, String url, int status, long len);
void serverSend(int status, const char* contentType, const String& content);
void WebServer_handleNotFound();
void WebServer_handleRoot();
void WebServer_cars();

void handleSwitches();
void handleCarUpdates();

static void SendConfirmationCallback(IOTHUB_CLIENT_CONFIRMATION_RESULT result);
static void MessageCallback(const char *payLoad, int size);
static void DeviceTwinCallback(DEVICE_TWIN_UPDATE_STATE updateState, const unsigned char *payLoad, int size);
static int DeviceMethodCallback(const char *methodName, const unsigned char *payload, int size, unsigned char **response, int *response_size);
static void initMQTT();
static void sendMqttMessage(Car* car);
void task_sendMQTTMessages(void * parameter);


// method signatures end

Car* carRed = new Car(42, "Red");
Car* carBlack = new Car(43, "Black");

AnalogState* redCarLap = new AnalogState(redInputLapPin, 0, 4095, 1);
AnalogState* blackCarLap = new AnalogState(blackInputLapPin, 0, 4095, 1);

void setup()
{
    Serial.begin(9600);
    delay(10);

    // We start by connecting to a WiFi network
    WiFi.mode(WIFI_STA);
    //WiFiMulti.addAP(ssid, password);
    WiFiMulti.addAP("WIN-51LTE355379 3886", "k94)8W37");

    if (TM_LOG >= TM_L_INFO) {
        Serial.println();
        Serial.println();
        Serial.printf("Waiting for WiFi ... ");
    }
    pinMode(buzzerHackitPin, INPUT_PULLUP);
    //pinMode(redInputLapPin, INPUT);
    //pinMode(blackInputLapPin, INPUT);

    while(WiFiMulti.run() != WL_CONNECTED) {
    }
    WebServer_start();
    if (TM_LOG >= TM_L_INFO) {
        Serial.println("");
        Serial.printf("WiFi connected %s\n", WiFi.SSID().c_str());
        Serial.printf("IP address: %s\n", WiFi.localIP().toString().c_str());
    }
    redCarLap->setState(0,   "OnTheRun",        0,  1000);
    redCarLap->setState(1,   "Lap",             1001,  4095);
    
    blackCarLap->setState(0,   "OnTheRun",      0,  1000);
    blackCarLap->setState(1,   "Lap",           1001,  4095);
 
    initMQTT();

    carRed->setEndpoint("http://65.52.142.219:10803/api/v1/car-event");
    carBlack->setEndpoint("http://65.52.142.219:10804/api/v1/car-event");
    carRed->retrieveConfigurationFromVault();
    carBlack->retrieveConfigurationFromVault();
}




void loop()
{
    if (WiFiMulti.run() == WL_CONNECTED) {
        if (WIFI_RUNNING == 0) {
            Serial.print("... connected\n");
            WIFI_RUNNING = 1;
        }
        server.handleClient();
        handleSwitches();
        handleCarUpdates();
    } else {
        if (TM_LOG >= TM_L_TRACE) Serial.print(".");
        delay(500);
        WIFI_RUNNING = 0;
    }
    if (millis()-200>easyTimer) {
        easyTimer=millis();
        if(hackit!=digitalRead(buzzerHackitPin)) {
            hackit=!hackit;
            carBlack->hack(hackit);
            carRed->hack(hackit);
            Serial.print("Buzzer, hackit: ");
            Serial.println(hackit);
        }
    }

}


void handleCarUpdates() {
    long sec = millis() / 1000;
    carRed->setOperatingHoursInS(sec);
    carBlack->setOperatingHoursInS(sec);
}


int FRAUD_MILEAGE = 10000;

void handleSwitches() {
    state_result_t redCarLap_data;
    redCarLap->readState(redCarLap_data);
    if (redCarLap_data.hasChanged && redCarLap_data.state == 1) {
        carRed->addMileage(INC_MILEAGE);
        sendMqttMessage(carRed);
        Serial.println("RED: send message");
    }

    state_result_t blackCarLap_data;
    blackCarLap->readState(blackCarLap_data);
    if (blackCarLap_data.hasChanged && blackCarLap_data.state == 1) {
        carBlack->addMileage(INC_MILEAGE);
        sendMqttMessage(carBlack);
        Serial.println("BLACK: send message");
    }
}


void logWebRequest(IPAddress ip, const char* user, HTTPMethod method, String url, int status, unsigned int len) {
    const char* methodString;
    switch (method) {
    case HTTP_GET:
        methodString = "GET";
        break;
    case HTTP_POST:
        methodString = "POST";
        break;        
    case HTTP_PUT:
        methodString = "PUT";
        break;
    case HTTP_DELETE:
        methodString = "DELETE";
        break;
    case HTTP_PATCH:
        methodString = "PATCH";
        break;
    case HTTP_HEAD:
        methodString = "HEAD";
        break;
    case HTTP_OPTIONS:
        methodString = "OPTIONS";
        break;
    default:
        methodString = "NONE";
        break;
    }
    if (TM_LOG >= TM_L_INFO) {
        Serial.printf("%s %s [%s] \"%s %s %s\" %d %u\n", 
            ip.toString().c_str(), 
            user, 
            //UTC_nowString().c_str(), 
            "00:00:00 beer",
            methodString, 
            url.c_str(), 
            "HTTP 1.0",
            status, 
            len);
    }
}

void serverSend(int status, const char* contentType, const String& content) {
    server.send(status, contentType, content);
    logWebRequest(
        server.client().remoteIP(),
        "-",
        server.method(),
        server.uri(),
        status,
        content.length()
    );
}

void WebServer_handleRoot() {
    serverSend(200, "text/plain", "Welcome to Trustmile tracker!");
}

void WebServer_handleNotFound() {
    String message = "File Not Found\n\n";
    message += "URI: ";
    message += server.uri();
    message += "\nMethod: ";
    message += (server.method() == HTTP_GET) ? "GET" : "POST";
    message += "\nArguments: ";
    message += server.args();
    message += "\n";
    for (uint8_t i = 0; i < server.args(); i++) {
        message += " " + server.argName(i) + ": " + server.arg(i) + "\n";
    }
    serverSend(404,"text/plain", message);
}

void WebServer_car() {
    String carColor = server.pathArg(0);
    if (carRed->getColor() == carColor || String(carRed->getId()) == carColor ) {
        serverSend(200, "application/json", carRed->getJSON().c_str());
    }
    if (carBlack->getColor() == carColor || String(carBlack->getId()) == carColor) {
        serverSend(200, "application/json", carBlack->getJSON().c_str());
    }
    serverSend(404,"text/plain", "car not found");
}
void WebServer_cars() {
    std::string json = "[ ";
    json = json.append(carRed->getJSON().c_str()).append(",").append(carBlack->getJSON().c_str()).append(" ]");
    serverSend(200, "application/json", json.c_str());
}

void WebServer_start() {
    if (MDNS.begin("esp32")) {
        Serial.println("MDNS responder started");
    }
    server.on("/", HTTP_GET, WebServer_handleRoot);
    server.on("/cars", HTTP_GET, WebServer_cars);
    server.on("/cars/", HTTP_GET, WebServer_cars);
    server.on("/cars/{}", HTTP_GET, WebServer_car);
    server.on("/cars/{}/", HTTP_GET, WebServer_car);

    server.onNotFound(WebServer_handleNotFound);
    server.begin();
    Serial.println("HTTP server started");    
}

// MQTT features
static void SendConfirmationCallback(IOTHUB_CLIENT_CONFIRMATION_RESULT result)
{
  if (result == IOTHUB_CLIENT_CONFIRMATION_OK)
  {
    //Serial.println("Send Confirmation Callback finished.");
  }
}


static void MessageCallback(const char *payLoad, int size)
{
  Serial.println("Message callback:");
  Serial.println(payLoad);
}

static void DeviceTwinCallback(DEVICE_TWIN_UPDATE_STATE updateState, const unsigned char *payLoad, int size)
{
  char *temp = (char *)malloc(size + 1);
  if (temp == NULL)
  {
    return;
  }
  memcpy(temp, payLoad, size);
  temp[size] = '\0';
  // Display Twin message.
  Serial.println(temp);
  free(temp);
}

static int DeviceMethodCallback(const char *methodName, const unsigned char *payload, int size, unsigned char **response, int *response_size)
{
  LogInfo("Try to invoke method %s", methodName);
  const char *responseMessage = "\"Successfully invoke device method\"";
  int result = 200;

  if (strcmp(methodName, "start") == 0)
  {
    LogInfo("Start sending temperature and humidity data");
    messageSending = true;
  }
  else if (strcmp(methodName, "stop") == 0)
  {
    LogInfo("Stop sending temperature and humidity data");
    messageSending = false;
  }
  else
  {
    LogInfo("No method %s found", methodName);
    responseMessage = "\"No method found\"";
    result = 404;
  }

  *response_size = strlen(responseMessage) + 1;
  *response = (unsigned char *)strdup(responseMessage);

  return result;
}


static void initMQTT(){
  Serial.println(" > IoT Hub");
  randomSeed(analogRead(0));
  Esp32MQTTClient_SetOption(OPTION_MINI_SOLUTION_NAME, "GetStarted");
  Esp32MQTTClient_Init((const uint8_t *)connectionString, true);

  Esp32MQTTClient_SetSendConfirmationCallback(SendConfirmationCallback);
  Esp32MQTTClient_SetMessageCallback(MessageCallback);
  Esp32MQTTClient_SetDeviceTwinCallback(DeviceTwinCallback);
  Esp32MQTTClient_SetDeviceMethodCallback(DeviceMethodCallback);

}

bool messagesSending = false;

static void sendMqttMessage(Car* car) {
    String bodyMessage = car->getJSON();
    mqttMessages.push_back(bodyMessage);
    Serial.printf("add MQTT message to queue\n");
    int cpu = xPortGetCoreID();
    cpu = (cpu+1) % 2;
    if (!messagesSending) {
        messagesSending = true;
        xTaskCreatePinnedToCore(
                    task_sendMQTTMessages,          /* Task function. */
                    "send MQTT messages",        /* String with name of task. */
                    10000,            /* Stack size in bytes. */
                    NULL,             /* Parameter passed as input of the task */
                    2,                /* Priority of the task. */
                    NULL,           /* Task handle. */
                    cpu);              /* other CPU. */
    }

}

void task_sendMQTTMessages(void * parameter) {
    while (!mqttMessages.empty()) {
        // from time to time the queue connect is dead and the main thread would be stopped
        Serial.printf("remove MQTT message to queue\n");
        String bodyMessage = mqttMessages.front();
        mqttMessages.pop_front();
        const char* bodyMessage_char = bodyMessage.c_str();
        EVENT_INSTANCE *message = Esp32MQTTClient_Event_Generate(bodyMessage_char, MESSAGE);
        Esp32MQTTClient_SendEventInstance(message);
        delay(2000);
    }
    messagesSending = false;
    vTaskDelete ( NULL );
}