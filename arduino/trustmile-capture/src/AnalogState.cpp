#include "AnalogState.h"
#include <Configuration.h>

/* Class constructor */
AnalogState::AnalogState(int pin, int minValue, int maxValue)
{
    _pin = pin;
    _pinMinValue = minValue;
    _pinMaxValue = maxValue;
    _precision = 1;
}
AnalogState::AnalogState(int pin, int minValue, int maxValue, int precision)
{
    _pin = pin;
    _pinMinValue = minValue / precision;
    _pinMaxValue = maxValue / precision;
    _precision = precision;
}

/* Class destructor */
AnalogState::~AnalogState() {}

void AnalogState::setState(int state, String name, int rawMin, int rawMax) {
    _states[_nofStates].min = rawMin;
    _states[_nofStates].max = rawMax;
    _states[_nofStates].state = state;
    _states[_nofStates].name = name;
    _nofStates++;
}
int AnalogState::readRawValue() {
  int newRawValue = analogRead(_pin);
  //int newRawValue = digitalRead(_pin);
  int newValue = map(newRawValue, MIN_VALUE_STATE, MAX_VALUE_STATE+1, _pinMinValue, _pinMaxValue) * _precision;
  if (_lastValue != newValue) {
    int index = findStateIndex(_lastRawValue);
    if (index != -1) {
        _lastRawValue = newRawValue;
        _lastValue = newValue;
        _lastStateName = _states[index].name;
        int newState = _states[index].state;
        if (_lastState != newState) {
            _lastState = newState;
            _lastChanged = 1;
            if (TM_LOG >= TM_L_TRACE) Serial.printf("State %i = %s, %i, %i\n", _pin, _lastStateName.c_str(), _lastValue, _lastRawValue);
            return _lastRawValue;
        }
    } else {
        _lastRawValue = newRawValue;
        _lastValue = newValue;
        _lastStateName = "";
        _lastState = -1;
        _lastChanged = 1;
        if (_nofStates == 0) { 
            if (TM_LOG >= TM_L_TRACE) Serial.printf("State %i = %i, %i\n", _pin, _lastValue, _lastRawValue);
        }
        return _lastRawValue;
    }
  }
  _lastChanged = 0;
  return _lastRawValue;
}

int AnalogState::readValue() {
    readRawValue();
    return _lastValue;
}

void AnalogState::readState(state_result_t &state) {
    readRawValue();
    state.rawValue = _lastRawValue;
    state.value = _lastValue;
    state.state = _lastState;
    state.name = _lastStateName;
    state.hasChanged = _lastChanged == 1;

}
int AnalogState::findStateIndex(int _currentRawState) {
    for (int i = _nofStates - 1; i >= 0; i--) {
        if (_states[i].min <= _currentRawState && _currentRawState <= _states[i].max) {
            return i;
        }
    }
    return -1;
}
