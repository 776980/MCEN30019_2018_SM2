#include <Servo.h>

#define PIN_INDEX_MIDDLE 9
#define PIN_RING_PINKY 10
#define PIN_THUMB 11

int baseline = 0;   // emg strength when muscles are relaxed
int threshold = 5;

// Estimate the sampling rate we are getting
double tNow;
double samplingRate;
bool emg = false;
bool isContracted = false;
bool outPulseVal = true;
int numSample = 32; // num of samples to average (EMG avg sample)

//Moving Averagej Filter params
double readings[32];      // the readings from the analog input
int index = 0;            // the index of the current reading
double total = 0;         // the running total
double emgMAF = 0;        // the average, Moving Average Filter ouput
int N = 20;               // No of points for moving average (1<N<16)

//LPF and sample averaging param
float emgLPF, emgHPF;  // variable for low-pass filtered EMG

Servo servo_index_middle;
Servo servo_ring_pinky;
Servo servo_thumb;

char command;
String string;
String value;

#define led LED_BUILTIN
bool isWaving = false;

void setup() {
  Serial.begin(9600);
  servo_index_middle.attach(PIN_INDEX_MIDDLE);
  servo_ring_pinky.attach(PIN_RING_PINKY);
  servo_thumb.attach(PIN_THUMB);
  pinMode(led, OUTPUT);
  LEDOff();
  pinMode(8, OUTPUT);

  // Get the baseline EMG value - keep your hand/arm relaxed until the LED is on
  long sum = 0;
  for (int i = 0; i < 1024; i++)
  {
    sum += analogRead(0);
    delay(5);
  }
  baseline = sum >> 10;
  Serial.println(baseline);
  
  // initialize all the elements of the array "readings" for moving average
  for (int i = 0; i < N; i++) {
    readings[i] = 0;
  }

  tNow = micros();  // current microseconds since the start of Arduino
}

void loop() {
  int flag = 0;
  while(Serial.available() > 0) {
    command = ((byte)Serial.read());
    string += command;
    flag = 1;
    delay(10);
  }
  if(isWaving){
    wave();
  }
  switch(string[0]) {
    case('O'): //led ON
      LEDOn();
      Serial.println("LED ON");
      break;
    case('F'): //led OFF
      LEDOff();
      Serial.println("LED OFF");
      break;
    case('I'): //IndexMiddle
      servo_index_middle.write((atoi(&string[1]) * 180)/99);
      Serial.print("IndexMiddle ");
      Serial.println(atoi(&string[1]));
      delay(15);
      break;
    case('R'): //RingPinky
      servo_ring_pinky.write((atoi(&string[1]) * 180)/99);
      Serial.print("RingPinky ");
      Serial.println(atoi(&string[1]));
      delay(15);
      break;
    case('T'): //Thumb
      servo_thumb.write((atoi(&string[1]) * 180)/99);
      Serial.print("Thumb ");
      Serial.println(atoi(&string[1]));
      delay(15);
      break;
    case ('H'): //hammer
      Serial.println("HammerMode ");
      break;
    case('X'): //expand all
      Serial.println("Expanding all ");
      expand_hand();
        break;
    case('C'): //contract all
      contract_hand();
      Serial.println("Contracting all ");
        break;
    case('W'): //Wave
      if(isWaving){
        isWaving = false;
      }
      else{
        isWaving = true;
      }
        break;
    case('B'): //ball
      Serial.println("BallMode ");
       break;
    case('M'): //marker
      Serial.println("MarkerMode ");
        break;
    case('E'):
      if(string[1] == '1'){
        Serial.println("EMG on ");
        emg = true;
      }
      else{
        Serial.println("EMG off ");
        emg = false;
      }
      break;
  }
  string = "";
  if(emg){
      // get "n" samples to create an average --> this smoothens out noise
    long sum = 0;   // sample accumulator
    int emgBaselineCorrected = 0;
    for (int i = 0; i < numSample; i++)
    {
      int temp = analogRead(0);         // raw EMG
      //emgBaselineCorrected = (temp - baseline);     // baseline shift
      emgBaselineCorrected = abs(temp - baseline);  // baseline shift + rectification
      sum += emgBaselineCorrected * emgBaselineCorrected;
    }
    emgBaselineCorrected = sum >> (int)(log (numSample) / log (2)); // smoothened
    emgBaselineCorrected = sqrt(emgBaselineCorrected);
  
    // OR Applying Moving Average
    total = total - readings[index];  // subtract the last reading:
    readings[index] = emgBaselineCorrected;
    total = total + readings[index];
    index++;  index = index >= N ? 0 : index;
    emgMAF = total / N;
  
    // Pass it through a low-pass filter (~1Hz) cut-off
    emgLPF = 0.9445 * emgLPF + 0.05552 * emgMAF;
  
    // estimate the sample rate and reset the tNow
    samplingRate = 1000000.0 / (micros() - tNow); // Sampling rate achieved
    tNow = micros();  // current microseconds since the start of Arduino
    bool outPulse = emgLPF > threshold ? HIGH : LOW;
    digitalWrite(8, outPulse); // indicate the pulse output with LED
  
    //Printing the EMG data / display data WHEN serial plotter is used
    Serial.print(samplingRate); Serial.print(" ");        // sampling Rate
    Serial.print(outPulse); Serial.print(" ");             // sampling Rate
    Serial.print(emgLPF); Serial.print(" ");
    Serial.println(emgBaselineCorrected);
    if (!outPulse){
      outPulseVal = true;
    }
    if(outPulse && outPulseVal){
      if(!isContracted){
        contract_hand();
        isContracted = true;
      }
      else{
        expand_hand();
        isContracted = false;
      }
      delay(5000);
      outPulseVal=false;
    }
  }
  //A delay to slow down the process
  delay(10);
}

void contract_hand(){
  Serial.println("Contracting hand");
  int pos;
  for (pos = 0; pos <= 180; pos += 1) { // goes from 0 degrees to 180 degrees
    // in steps of 1 degree
    servo_index_middle.write(pos);              // tell servo to go to position in variable 'pos'
    servo_ring_pinky.write(pos);              // tell servo to go to position in variable 'pos'
    delay(15);                       // waits 15ms for the servo to reach the position
  }
  for (pos = 0; pos <= 180; pos++) { // goes from 180 degrees to 0 degrees
    servo_thumb.write(pos);              // tell servo to go to position in variable 'pos'
    delay(15);                       // waits 15ms for the servo to reach the position
  }
}

void expand_hand(){
  Serial.println("expanding hand");
  int pos;
  for (pos = 180; pos >= 0; pos -= 1) { // goes from 180 degrees to 0 degrees
    servo_index_middle.write(pos);              // tell servo to go to position in variable 'pos'
    servo_ring_pinky.write(pos);
    servo_thumb.write(pos);               // tell servo to go to position in variable 'pos'
    delay(15);                       // waits 15ms for the servo to reach the position
  }
}

void LEDOn() {   
  digitalWrite(led, HIGH);
}

void LEDOff() {
  digitalWrite(led, LOW);
}

void wave(){
  static int wave_pos = 1;
  static bool drctn = true;

  servo_index_middle.write(90 - wave_pos);// tell servo to go to position in variable 'pos'
  Serial.print("index middle ");
  Serial.println(90 - wave_pos);
  servo_ring_pinky.write(wave_pos);
  Serial.print("ring pinky ");
  Serial.println(wave_pos);
  if(wave_pos > 89 || wave_pos < 1){
    drctn = !drctn;
  }
  if(drctn){
    wave_pos++;
  }
  else{
    wave_pos--;
  }
}
