/*-----------------------------------------------
   EMG Acquisition and Control
   By: Mukesh Soni | Ver: 02 (18-Sep-2018)

   Acquires the EMG signal from forehand muscle using Grove EMG Detector
   feeding the signal to A0 of the Arduino.
   Moving Avg Filter Added, followed by Low Pass Filter. Thresholding is done
   to generate output pulse.
  ------------------------------------------------*/

int baseline = 0;   // emg strength when muscles are relaxed
int threshold = 5;

// Estimate the sampling rate we are getting
double tNow;
double samplingRate;

int numSample = 32; // num of samples to average (EMG avg sample)

//Moving Averagej Filter params
double readings[32];      // the readings from the analog input
int index = 0;            // the index of the current reading
double total = 0;         // the running total
double emgMAF = 0;        // the average, Moving Average Filter ouput
int N = 20;               // No of points for moving average (1<N<16)

//LPF and sample averaging param
float emgLPF, emgHPF;  // variable for low-pass filtered EMG

void setup()
{
  //Starting the serial monitor
  Serial.begin(115200);
  pinMode(8, OUTPUT);

  // Get the baseline EMG value - keep your hand/arm relaxed until the LED is on
  digitalWrite(8, HIGH); // turn on the LED on pin 8
  long sum = 0;
  for (int i = 0; i < 1024; i++)
  {
    sum += analogRead(0);
    delay(5);
  }
  baseline = sum >> 10;
  Serial.println(baseline);
  digitalWrite(8, LOW); // turn off the LED, baseline recorded

  // initialize all the elements of the array "readings" for moving average
  for (int i = 0; i < N; i++) {
    readings[i] = 0;
  }

  tNow = micros();  // current microseconds since the start of Arduino
}


void loop()
{
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
  //Serial.print(samplingRate); Serial.print(" ");        // sampling Rate
  Serial.print(outPulse); Serial.print(" ");             // sampling Rate
  Serial.print(emgLPF); Serial.print(" ");
  Serial.println(emgBaselineCorrected);

  //A delay to slow down the process
  delay(1);
}
