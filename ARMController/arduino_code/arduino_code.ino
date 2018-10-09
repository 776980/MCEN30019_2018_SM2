char command;
String string;
#define led LED_BUILTIN
void setup() {
  Serial.begin(9600);
  pinMode(led, OUTPUT);
  LEDOn();
}

void loop() {
  if (Serial.available() > 0) {
    string = "";
  }
  while(Serial.available() > 0)
  {
    command = ((byte)Serial.read());
    Serial.print(string);
    if(command == ':') {
      break;
    }      
    else
    {
      string += command;
    }      
    delay(1);
  }    
  if(string == "O") {
    LEDOn();
  }    
  if (string =="F")
  {
    LEDOff();
  }
}

void LEDOn() {   
  digitalWrite(led, HIGH);
}

void LEDOff() {
  digitalWrite(led, LOW);
}
