/*
 *  This sketch sends a message to a TCP server
 *
 */
#include <Arduino.h>
#include <sstream>
#include <string.h>
#include <HTTPClient.h>
#include <WiFi.h>
#include <WiFiMulti.h>
#include <time.h>

WiFiMulti WiFiMulti;

const char* ssid = "Lolo_Guest";
const char* password = "MogliUndJenny1";

const char* END_POINT_GET = "https://postman-echo.com/get";
const char* END_POINT_POST = "https://postman-echo.com/post";

int WIFI_RUNNING = 0;

int RED = 42;
long RED_OH = 0;
long RED_KM = 1042;

int BLACK = 43;
long BLACK_OH = 0;
long BLACK_KM = 45000;

void sendGET(const char* url, const char* parameters, const char* authorization);
void sendPOST(const char* url, const char* parameters, const char* body, const char* authorization);
std::string getCarBody(int id, int mileage, int operatingHours);

void setup()
{
    Serial.begin(9600);
    delay(10);

    // We start by connecting to a WiFi network
    WiFiMulti.addAP(ssid, password);

    Serial.println();
    Serial.println();
    Serial.printf("Waiting for WiFi %s... ", ssid);

    while(WiFiMulti.run() != WL_CONNECTED) {
        Serial.print(".");
        delay(500);
    }

    Serial.println("");
    Serial.println("WiFi connected");
    Serial.println("IP address: ");
    Serial.println(WiFi.localIP());

    delay(500);
}


void loop()
{
    if (WiFiMulti.run() == WL_CONNECTED) {
        if (WIFI_RUNNING == 0) {
            Serial.print("... connected\n");
            WIFI_RUNNING = 1;
        }
        try {
            Serial.print("send GET ...\n");
            if (rand() % 2 == 0) {
                sendGET(END_POINT_GET, "test=a", "AF42AF");
            } else {
                std::string redCar = getCarBody(RED, 1000, 12);
                std::string blackCar = getCarBody(BLACK, 20000, 66);
                sendPOST(END_POINT_POST, nullptr, redCar.c_str(), "AF42AF");
                sendPOST(END_POINT_POST, nullptr, blackCar.c_str(), "AF42AF");
            }
        } catch(const char* msg) {
            Serial.print("Error");
            Serial.print(msg);
            Serial.print("\n");
        }
        Serial.print("wait 5s\n");
        delay(5000);        
        
    } else {
        Serial.print(".");
        delay(500);        
    }
}



void sendGET(const char* url, const char* parameters, const char* authorization = nullptr) {
    Serial.printf("[GET] called %s ... \n", url);

    HTTPClient http;
    std::string query = url;
    if (parameters != nullptr) {
        query.append("?").append(parameters);
    }
    Serial.printf("[GET] begin %s .. \n", query.c_str());
    http.begin(query.c_str());
    if (authorization != nullptr) {
        http.setAuthorization(authorization);
    }
    int httpCode = http.GET();
    if(httpCode > 0) {
        // HTTP header has been send and Server response header has been handled
        Serial.printf("[GET] ... code: %d\n", httpCode);
        // file found at server
        if(httpCode == HTTP_CODE_OK) {
            String payload = http.getString();
            Serial.println(payload);
        }
    } else {
        Serial.printf("[GET] ... failed, error: %s\n", http.errorToString(httpCode).c_str());
    }
    http.end();
}


void sendPOST(const char* url, const char* parameters, const char* body, const char* authorization = nullptr) {
    Serial.printf("[POST] called %s ... \n", url);

    HTTPClient http;
    std::string query = url;
    if (parameters != nullptr) {
        query.append("?").append(parameters);
    }
    Serial.printf("[POST] begin %s .. \n", query.c_str());
    http.begin(query.c_str());
    if (authorization != nullptr) {
        http.setAuthorization(authorization);
    }
    http.addHeader("Content-Type", "application/json");
    int httpCode = http.POST(body);
    if(httpCode > 0) {
        // HTTP header has been send and Server response header has been handled
        Serial.printf("[POST] GET... code: %d\n", httpCode);
        // file found at server
        if(httpCode == HTTP_CODE_OK) {
            String payload = http.getString();
            Serial.println(payload);
        }
    } else {
        Serial.printf("[POST] ... failed, error: %s\n", http.errorToString(httpCode).c_str());
    }
    http.end();
}

std::string intToString(int i) {
    std::string out_string;
    std::stringstream ss;
    ss << i;
    out_string = ss.str();
    return out_string;
}

std::string getCarBody(int id, int mileage, int operatingHours) {
    time_t timer;
    time(&timer);
    std::string json = "";
    return
        json.append("{\n")
            .append("   \"id\" : ").append(intToString(id)).append(",\n")
            .append("   \"mileage\" : ").append(intToString(mileage)).append(",\n")
            .append("   \"operatingHours\" : ").append(intToString(operatingHours)).append(",\n")
            .append("   \"timestampInS\" : ").append(intToString(timer)).append("\n")
            .append("}");
}