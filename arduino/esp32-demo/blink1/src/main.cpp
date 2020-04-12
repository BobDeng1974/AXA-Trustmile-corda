#include <Arduino.h>

#include <sstream>
#include <iterator>
#include <string.h>

// ledPin refers to ESP32 GPIO 23
const int ledPin = 23;
const int ledPinRTC = 22;
// unsigned int frequency = 500;
// unsigned int duration = 50;

// analog pint ESP32 GPIO 36
const int potPin = 36;
const int switchPin = 34;
long potValue = 0;
long mappedValue = 0;
long switchValue = 0;

void blink(int nof, uint32_t timeInMs);


void done() {
	Serial.println("Done!");
  //EasyBuzzer.stopBeep();
}

// the setup function runs once when you press reset or power the board
void setup() {
    Serial.begin(9600);
    while (!Serial) ; // Needed for Leonardo only

    delay(10);
    // initialize digital pin ledPin as an output.
    Serial.println("to initialize 23");
    pinMode(ledPin, OUTPUT);
    pinMode(ledPinRTC, OUTPUT);
    Serial.println("initialized 23, 22");
    //pinMode(switchPin, INPUT);

    //EasyBuzzer.setPin(T0);
    //EasyBuzzer.singleBeep(
    //  frequency, 	// Frequency in hertz(HZ).  
    //  duration, 	// Duration of the beep in milliseconds(ms). 
    //  done		// [Optional] Function to call when done.
    //);
}

// the loop function runs over and over again forever
void loop() {
  blink(3,10);
  blink(3,10);

  long newPotValue = analogRead(potPin);
  long newMappedValue = map(newPotValue, 0, 4095, 5, 20) * 100;
  if (potValue != newPotValue && mappedValue != newMappedValue) {
    potValue = newPotValue;
    mappedValue = newMappedValue;
    Serial.print("IN=");Serial.print(potValue);Serial.print(" OUT=");Serial.println(mappedValue);
  }
  long newSwitchValue = analogRead(switchPin);
  if (switchValue != newSwitchValue) {
    switchValue = newSwitchValue;
    Serial.print("SWITCH=");Serial.println(switchValue);
  }

  //EasyBuzzer.update();
  delay(10);
}

void blink(int nof, uint32_t timeInMs) {
  //Serial.println("blink");
  for (int i = nof - 1; i >= 0; i--)
  {
    digitalWrite(ledPin, HIGH);   // turn the LED on (HIGH is the voltage level)
    delay(timeInMs);                  // wait for a second
    digitalWrite(ledPin, LOW);    // turn the LED off by making the voltage LOW
    delay(timeInMs);                  // wait for a second
  }
}
