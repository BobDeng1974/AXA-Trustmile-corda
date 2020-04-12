#include "Cars.h"
#include <string.h>
#include <REST.h>
#include <ArduinoJson.h>

const size_t capacity = JSON_OBJECT_SIZE(0) + JSON_OBJECT_SIZE(6) + 80;

std::string intToString(int i)
{
    std::string out_string;
    std::stringstream ss;
    ss << i;
    out_string = ss.str();
    return out_string;
}
std::string uintToString(unsigned int i)
{
    std::string out_string;
    std::stringstream ss;
    ss << i;
    out_string = ss.str();
    return out_string;
}

std::string longToString(long i)
{
    std::string out_string;
    std::stringstream ss;
    ss << i;
    out_string = ss.str();
    return out_string;
}

std::string uint64ToString(uint64_t i)
{
    std::string out_string;
    std::stringstream ss;
    ss << i;
    out_string = ss.str();
    return out_string;
}

unsigned long timeSinceEpochMillisec()
{
    //return UTC_now();
    return millis() + START_HACKATHON;
}

String UTC_nowString()
{
    return "00:00:00";
}

/* Class constructor */
Car::Car(unsigned int id, const char *color)
{
    _id = id;
    _color = String(color);
    _mileage = random(24000, 100000);
    _operatingHoursInS = 0;
}
Car::Car(unsigned int id, String color)
{
    _id = id;
    _color = color;
    _mileage = random(24000, 100000);
    _operatingHoursInS = 0;
}

/* Class destructor */
Car::~Car() {}


String Car::getJSON()
{
    std::string json = "";
    unsigned long t = timeSinceEpochMillisec();
    return String("{\n")
            + String("   \"vin\" : ")
            + String(getId())
            + String(",\n")
            + String("   \"mileage\" : ")
            + String(getMileage())
            + String(",\n")
            + String("   \"operatingHoursInS\" : \"")
            + String(getOperatingHoursInS())
            + String("\",\n")
            + String("   \"timestamp\" : ")
            + String(t)
            + String(",\n")
            + String("   \"accident\" : ")
            + String("false")
            + String(",\n")
            + String("   \"data\" : ")
            + String("{}")
            + String("\n")
            + String("}");
}

void Car::sendTrustedData()
{
    // for later use
    // String uri = String(END_POINT_CAR_POST)+String("/car")+String(getId());
    //REST.sendPOST(uri.c_str(), "", getJSON().c_str(), "car-key");

    // REST disabled
    // REST.sendPOST(getEndpoint().c_str(), nullptr, getJSON().c_str(), "car-key");
}


void Car::retrieveConfigurationFromVault() {
    String lastEvent = REST.sendGET(getEndpoint().c_str(), nullptr, "car-key");
    if (lastEvent != "") {
        //https://arduinojson.org/v6/assistant/
        DynamicJsonDocument doc(capacity);
        deserializeJson(doc, lastEvent.c_str());
        long tempMileage = doc["mileage"];
        this->setMileage((int)tempMileage);
        Serial.printf("configuration saved: mileage=%i\n", this->getMileage());
    }
}


void Car::hack(boolean hackit)
{
    if (hackit)
    {
        _mileage = _mileage / 2;
    }
    else
    {
        _mileage = _mileage * 2 + 1;
    }
}
