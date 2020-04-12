/*
Name:		REST.h
Version:	0.0.1
Author:		Lorenz haenggi
*/

#ifndef _REST_h
#define _REST_h

#include <Arduino.h>
#include <HTTPClient.h>
#include <WiFi.h>
#include <WiFiMulti.h>

class RESTClass
{
  public:
	RESTClass();
	~RESTClass();
    String sendGET(const char* url, const char* parameters, const char* authorization);
    void sendPOST(const char* url, const char* parameters, const char* body, const char* authorization);

  private:
};

extern RESTClass REST;

#endif
