/*
Name:		Trustmile.h
Version:	0.0.1
Author:		Lorenz haenggi
*/

#ifndef _CONFIGURATION_h
#define _CONFIGURATION_h

#include <Arduino.h>

#define TM_L_ALL            0x7
#define TM_L_TRACE          0x6
#define TM_L_DEBUG          0x5
#define TM_L_INFO           0x4
#define TM_L_WARN           0x3
#define TM_L_ERROR          0x2
#define TM_L_FATAL          0x1
#define TM_L_OFF            0x0

#define TM_LOG              TM_L_ALL

extern const char* END_POINT_CAR_GET;
extern const char* END_POINT_CAR_POST;

#define INTERVAL 10000
#define DEVICE_ID "Esp32Device"

//GMT: Sunday, September 8, 2019 8:46:20 PM
#define START_PREPARTION 1567975580;
#define START_HACKATHON 1568183400;

#endif
