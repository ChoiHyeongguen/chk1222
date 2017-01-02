#include <Adafruit_VC0706.h>
#include <SoftwareSerial.h>
#include <SPI.h>
#include <Ethernet.h>
#include <Twitter.h>

/* ******** Ethernet Card Settings ******** */
// Set this to your Ethernet Card Mac Address
byte mac[] = { 0xDE, 0xAD, 0xBE, 0xEF, 0xFE, 0xED };
IPAddress server(172, 30, 35, 157); 

// Your Token to Tweet (get it from http://arduino-tweet.appspot.com/)
Twitter twitter("768731806971547648-56Mqkb7zasi09mJ6SGCojd1MNSUU4RG");
char msg[] = "안전문제 발생 : 6206 정보과학관 (글로컬 IT 학과 테스트 진행중--! ) ";

const int gasPin = A0 ;
const int ledPin = 8;
boolean flag = false;

SoftwareSerial cameraconnection = SoftwareSerial(2, 3);
Adafruit_VC0706 cam = Adafruit_VC0706(&cameraconnection);
EthernetClient client;

void setup() {
  cameraconnection.begin(38400);
  Serial.begin(9600);

  pinMode(ledPin, OUTPUT);
  if (Ethernet.begin(mac) == 0) {
    Serial.println("Ethernet Connection Fail");
    while(true);
  }
  
  delay(1000);
}

void sendingTwitter(char msg[]){
  if (twitter.post(msg)) {
    // Specify &Serial to output received response to Serial.
    // If no output is required, you can just omit the argument, e.g.
    // int status = twitter.wait();
    int status = twitter.wait(&Serial);
    if (status == 200) {
      Serial.println("Sending Twitter Success.");
    } else {
      Serial.print("Sending Twitter Fail : code ");
      Serial.println(status);
    }
  } else {
    Serial.println("Twitter Connection Failed.");
  }  
}

void takeingPicture() {
  flag = true;
  
  Serial.println("VC0706 Camera snapshot test"); // Try to locate the camera
  if (cam.begin()) {
    Serial.println("Camera Found:");
  } else {
    Serial.println("No camera found?");
    return;
  }
  
  // Print out the camera version information (optional)
  char *reply = cam.getVersion();
  if (reply == 0) {
    Serial.print("Failed to get version");
  } else {
    Serial.println("-----------------");
    Serial.print(reply);
    Serial.println("-----------------");
  }

  // Set the picture size - you can choose one of 640x480, 320x240 or 160x120 
  // Remember that bigger pictures take longer to transmit!
  
//  cam.setImageSize(VC0706_640x480);        // biggest
  cam.setImageSize(VC0706_320x240);        // medium
//  cam.setImageSize(VC0706_160x120);        // small

  // You can read the size back from the camera (optional, but maybe useful?)
  uint8_t imgsize = cam.getImageSize();
  Serial.print("Image size: ");
  if (imgsize == VC0706_640x480) Serial.println("640x480");
  if (imgsize == VC0706_320x240) Serial.println("320x240");
  if (imgsize == VC0706_160x120) Serial.println("160x120");
  
  Serial.println("Snap in 3 secs...");
  delay(3000);  
  if (! cam.takePicture()) {
    Serial.println("Failed to snap!");
  } else { 
    Serial.println("Picture taken!");
  }
    
  Serial.println("Server connecting...");   
   
  if (client.connect(server, 8080)) {
    Serial.println("Start uploading...");
    
    // Get the size of the image (frame) taken  
    uint16_t jpglen = cam.frameLength();
    Serial.print("Storing ");
    Serial.print(jpglen, DEC);
    Serial.print(" byte image.");
    uint32_t len = jpglen + 177; // 177 is the content without the image data  
  
    client.println(F("POST /ArduinoServerProject/upload HTTP/1.1"));
    client.println(F("Host: www.shku.ac.kr"));
    client.println(F("Content-type: multipart/form-data, boundary=UH3xBZZtzewr09oPP"));
    client.print(F("Content-Length: "));
    client.println(len);
    client.println();
    client.println(F("--UH3xBZZtzewr09oPP"));
    client.print(F("Content-disposition: form-data; name=\"uploadFile\"; filename=\"cam.jpg\""));
    client.println(F("Content-Type: image/jpeg"));
    client.println(F("Content-Transfer-Encoding: binary"));
    client.println();
  
    int32_t time = millis();
    
    // Read all the data up to # bytes!
    byte wCount = 0; // For counting # of writes
    while (jpglen > 0) {
      // read 32 bytes at a time;
      uint8_t *buffer;
      uint8_t bytesToRead = min(32, jpglen); // change 32 to 64 for a speedup but may not work with all setups!
      buffer = cam.readPicture(bytesToRead);
    
      client.write(buffer, bytesToRead);
    

      if(++wCount >= 64) { // Every 2K, give a little feedback so it doesn't appear locked up
        Serial.print('.');
        wCount = 0;
       }
    
       jpglen -= bytesToRead;
    }
    client.println();
    client.println(F("--UH3xBZZtzewr09oPP--"));

    time = millis() - time;
    Serial.println("done!");
    Serial.print(time); Serial.println(" ms elapsed");
  } 
  else {
    // kf you didn't get a connection to the server:
    Serial.println("connection failed");
    client.stop();
  }      
}

// This is where all the magic happens...
void loop() {   
  if (flag == true) {
    //Preso da DnsWebClient
    if (client.available()) {
      char c = client.read();
      if (c == '\0') {
        flag = false;
      }
      Serial.print(c);
    }
  
    // if the server's disconnected, stop the client:
    if (!client.connected()) {
      Serial.println();
      Serial.println("Client : disconnecting.");
  
      flag = false;
    }  
  } else {
    Serial.print("Gas Testing : ");
    Serial.println(analogRead(gasPin)); //센서값을 시리얼모니터로 전송
  
    if (analogRead(gasPin) > 250) {  // 가스 검출 시(자신의 센서 감도에 알맞게 조절필요) 
       digitalWrite(ledPin, HIGH);
       sendingTwitter(msg);
       takeingPicture();
       digitalWrite(ledPin, LOW);
    }  
    delay(1000);
  }
  client.stop();
}
