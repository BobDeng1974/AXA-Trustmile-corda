/*
Name:		AnalogState.h
Version:	0.0.1
Author:		Lorenz haenggi
*/

#ifndef _ANALOGSTATE_h
#define _ANALOGSTATE_h

#include <Arduino.h>

#define MIN_VALUE_STATE 0
#define MAX_VALUE_STATE 4095

typedef struct {
    int min;
    int max;
    int state;
    String name;
} state_t;
typedef struct {
    int rawValue;
    int value;
    int state;
    bool hasChanged;
    String name;
} state_result_t;

class AnalogState
{
  public:
	AnalogState(int pin, int minValue, int maxValue);
	AnalogState(int pin, int minValue, int maxValue, int precision);
	~AnalogState();
    void setState(int state, String name, int rawMin, int rawMax);
    int readRawValue();
    int readValue();
    void readState(state_result_t& state);

  private:
    int findStateIndex(int _currentRawState);

    int _pin = 36;
    int _pinMinValue = MIN_VALUE_STATE;
    int _pinMaxValue = MAX_VALUE_STATE;
    int _precision = 1;

    int _lastRawValue = -1;
    int _lastValue = -1;
    int _lastState = -1;
    int _lastChanged = 0;
    String _lastStateName = "";

    int _nofStates = 0;
    state_t _states[8];
};
#endif
