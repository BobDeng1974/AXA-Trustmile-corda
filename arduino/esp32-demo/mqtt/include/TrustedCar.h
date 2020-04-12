/*
Name:		TrustedCar.h
Version:	0.0.1
Author:		Lorenz haenggi
*/

#ifndef _TrustedCar_h
#define _TrustedCar_h

#if defined(ARDUINO) && ARDUINO >= 100
#include "Arduino.h"
#else
#include "WProgram.h"
#endif

#include "Config.h"

#ifdef ESP8266
#define min _min
#define max _max
#endif
#ifdef ESP32
#define min _min
#define max _max
#endif

class TrustedCarClass
{
  public:
	TrustedCarClass();
	~TrustedCarClass();
	/* set id of the car. */
	void setId(unsigned int id);

  private:
	unsigned int id = 0;
	unsigned int mileage = 12042;
	unsigned int opHoursInS = 60;
	void (*mFinishedCallbackFunction)();
};

extern TrustedCarClass TrustedCar;

#endif
