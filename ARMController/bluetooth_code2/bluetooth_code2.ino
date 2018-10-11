#include <Servo.h>

#define PIN_INDEX_MIDDLE 9
#define PIN_RING_PINKY 10
#define PIN_THUMB 11

Servo servo_index_middle;
Servo servo_ring_pinky;
Servo servo_thumb;

char command;
String string;
String value;

#define led LED_BUILTIN

void setup() {
  Serial.begin(9600);
  servo_index_middle.attach(PIN_INDEX_MIDDLE);
  servo_ring_pinky.attach(PIN_RING_PINKY);
  servo_thumb.attach(PIN_THUMB);
  pinMode(led, OUTPUT);
  LEDOn();
}

void loop() {
  while(Serial.available() > 0) {
    command = ((byte)Serial.read());      
    string += command;
    delay(10);
  }
  
  char val[2];
  val[0]=string[1];
  val[1]=string[2];
  
  switch(string[0]) {
  case('O'):
    Serial.println("String is this: " + string);
    LEDOn();
    break;
  case('F'):
    Serial.println("String is this: " + string);
    LEDOff();
    break;
  case('A'):
    Serial.println("String is this: " + string + " atoi is " + (atoi(val) * 180)/99);
    servo_index_middle.write((atoi(val) * 180)/99);
    delay(15);
    break;
  case('B'):
    Serial.println("String is this: " + string + " atoi is " + (atoi(val) * 180)/99);
    servo_ring_pinky.write((atoi(val) * 180)/99);
    delay(15);
    break;
  case('C'):
    Serial.println("String is this: " + string + " atoi is " + (atoi(val) * 180)/99);
    servo_thumb.write((atoi(val) * 180)/99);
    delay(15);
    break;
  }
  string = "";
  command = "";
}

void LEDOn() {   
  digitalWrite(led, HIGH);
}

void LEDOff() {
  digitalWrite(led, LOW);
}
