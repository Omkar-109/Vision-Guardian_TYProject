#include <BluetoothSerial.h>

#if !defined(CONFIG_BT_ENABLED) || !defined(CONFIG_BLUEDROID_ENABLED)
#error Bluetooth is not enabled! Please run `make menuconfig` to and enable it
#endif

BluetoothSerial SerialBT;
//mac  FC:B4:67:4E:56:7E

const int trigPin=5;
const int echoPin=18;
long duration;
int distance;
String state="No";
int value=30;

void setup() {
  Serial.begin(115200);
  SerialBT.begin("ESP32Project"); //Bluetooth device name
  Serial.println("The device started, now you can pair it with bluetooth!");
  pinMode(trigPin, OUTPUT); // Sets the trigPin as an Output
  pinMode(echoPin, INPUT); // Sets the echoPin as an Input
}

void loop() {
  
  digitalWrite(trigPin,LOW);
  delayMicroseconds(2);
  digitalWrite(trigPin,HIGH);
  delayMicroseconds(10);
  digitalWrite(trigPin,LOW);
  
  duration=pulseIn(echoPin,HIGH);
  distance=duration*0.034/2;
  Serial.println(distance);
  
  
  if(distance <= 80){

     if(state=="No" || state=="mid")
     {
        value=10;
        state="low";
        //send
        String myString=(String)value;
        myString+="\n";
        
        char charArray[myString.length() + 1]; // +1 for the null terminator
        myString.toCharArray(charArray, sizeof(charArray));
        
        Serial.println(distance);
        Serial.write(charArray);
        SerialBT.print(charArray);  // Send data via Bluetooth 
       
     }
  }
  if(distance > 80 && distance <= 150){
     
      if(state=="No" || state=="low")
     {
        value=20;
        state="mid";
        //send
        String myString=(String)value;
        myString+="\n";
        
        char charArray[myString.length() + 1]; // +1 for the null terminator
        myString.toCharArray(charArray, sizeof(charArray));
        
        Serial.println(distance);
        Serial.write(charArray);
        SerialBT.print(charArray);  // Send data via Bluetooth 
         
     }
  }

  if(distance > 150 && distance <= 400){
    Serial.println("Away");
    if(state=="mid" || state=="low"){
      state="No";
    }
  }

  delay(1000);
}
