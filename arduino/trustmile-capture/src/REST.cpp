#include "REST.h"
#include <Configuration.h>
#include <HTTPClient.h>
#include <WiFi.h>
#include <WiFiMulti.h>

/* Class constructor */
RESTClass::RESTClass()
{
}

/* Class destructor */
RESTClass::~RESTClass() {}


String RESTClass::sendGET(const char* url, const char* parameters, const char* authorization) {
    if (TM_LOG >= TM_L_TRACE) Serial.printf("[GET] called %s ... \n", url);

    HTTPClient http;
    std::string query = url;
    if (parameters != nullptr) {
        query.append("?").append(parameters);
    }
    //if (TM_LOG >= TM_L_TRACE) Serial.printf("[GET] begin %s .. \n", query.c_str());
    http.setConnectTimeout(20000);
    http.begin(query.c_str());
    if (authorization != nullptr) {
        http.setAuthorization(authorization);
    }
    http.setTimeout(20000);
    int httpCode = http.GET();
    String payload = "";
    if(httpCode > 0) {
        // HTTP header has been send and Server response header has been handled
        //if (TM_LOG >= TM_L_TRACE) Serial.printf("[GET] ... code: %d\n", httpCode);
        // file found at server
        if(httpCode == HTTP_CODE_OK || httpCode == HTTP_CODE_ACCEPTED || httpCode == HTTP_CODE_CREATED) {
            payload = http.getString();
            //Serial.println(payload);
        }
    } else {
        if (TM_LOG >= TM_L_TRACE) Serial.printf("[GET] ... failed, error: %s\n", http.errorToString(httpCode).c_str());
    }
    http.end();
    return payload;
}


void RESTClass::sendPOST(const char* url, const char* parameters, const char* body, const char* authorization) {
    if (TM_LOG >= TM_L_TRACE) Serial.printf("[POST] called %s ... \n", url);

    HTTPClient http;
    std::string query = url;
    if (parameters != nullptr) {
        query.append("?").append(parameters);
    }
    //if (TM_LOG >= TM_L_TRACE) Serial.printf("[POST] begin %s .. \n", query.c_str());
    http.setConnectTimeout(20000);
    http.begin(query.c_str());
    if (authorization != nullptr) {
        http.setAuthorization(authorization);
    }
    http.addHeader("Content-Type", "application/json");
    http.setTimeout(20000);
    int httpCode = http.POST(body);
    if(httpCode > 0) {
        // HTTP header has been send and Server response header has been handled
        //if (TM_LOG >= TM_L_TRACE) Serial.printf("[POST] GET... code: %d\n", httpCode);
        // file found at server
        if(httpCode == HTTP_CODE_OK || httpCode == HTTP_CODE_ACCEPTED || httpCode == HTTP_CODE_CREATED) {
            String payload = http.getString();
            //Serial.println(payload);
        }
    } else {
        if (TM_LOG >= TM_L_TRACE) Serial.printf("[POST] ... failed, error: %s\n", http.errorToString(httpCode).c_str());
    }
    http.end();
}

RESTClass REST;
