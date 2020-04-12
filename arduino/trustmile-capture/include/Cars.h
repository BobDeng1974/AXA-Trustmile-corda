/*
Name:		REST.h
Version:	0.0.1
Author:		Lorenz haenggi
*/

#ifndef _CARS_h
#define _CARS_h

#include <Arduino.h>
#include <sstream>
#include <iterator>
#include <string.h>
#include <Configuration.h>

//#include <TimeNTP.h>

class Car
{
  public:
	Car(unsigned int id, const char* color);
	Car(unsigned int id, String color);
	~Car();
    unsigned int getId() { return _id; }
    String getColor() { return _color; };
    int getMileage() { return _mileage; }
    void setMileage(int mileage) { _mileage = mileage; }
    void setOperatingHoursInS(long sec) { _operatingHoursInS = sec; }
    long getOperatingHoursInS() { return _operatingHoursInS; }
    void setOperatingHours(long hours) { _operatingHoursInS = hours * 60 * 60; }
    long getOperatingHours() { return _operatingHoursInS / 60 / 60; }
    String getJSON();
    void setEndpoint(String endpoint) { _endpoint = endpoint; }
    String getEndpoint() { return _endpoint; }

    void reduceMileage(int diff) { _mileage -= min(diff, _mileage); }
    void addMileage(int diff) { _mileage += diff; }
    void addOperatingHours(long diff) { setOperatingHours(getOperatingHours() + diff); }
    void addOperatingHoursInS(long diff) { setOperatingHoursInS(getOperatingHoursInS() + diff); }
    void sendTrustedData();
    void retrieveConfigurationFromVault();
    void hack(boolean hackit);


  private:
    unsigned int _id;
    String _color;
    int _mileage;
    long _operatingHoursInS;
    String _endpoint;
};

#endif
