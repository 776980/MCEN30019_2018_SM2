char command;
String string;
#define led LED_BUILTIN
void setup() {
  Serial.begin(9600);
  pinMode(led, OUTPUT);
  LEDOn();
}

void loop() {
  while(Serial.available() > 0) {
    command = ((byte)Serial.read());
    string += command;
    Serial.println("String is this: " + string);
    delay(1);
  }
  switch(string[0]) {
   case 'O':
    LEDOn();
    break;
   case 'F':
    LEDOff();
    break;
   case 'A':
   
    break;
   case 'B':
    break;
   case 'C':
    break;
   default :
    break;
}
  string = "";
  command = "";
  //Serial.
}

void LEDOn() {   
  digitalWrite(led, HIGH);
}

void LEDOff() {
  digitalWrite(led, LOW);
}
