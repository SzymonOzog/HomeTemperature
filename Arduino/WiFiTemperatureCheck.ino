//Library Import
#include <SoftwareSerial.h>                   
#include "DHT.h"
#include "WiFiNINA.h"
DHT dht;
char ssid[] = "YourWiFiName";
char pass[] = "YourWiFiPassword";
int status = WL_IDLE_STATUS;

unsigned int localPort = 2390;      // local port to listen on

char packetBuffer[256]; //buffer to hold incoming packet
char ReplyBuffer[] = "Acknowleged";
String temperatureCheckCode = "GetTemperature";
String lightSwitchCode = "Light";
bool light = false;
WiFiUDP UDP;


//Pin number to whitch we connected the sensor
#define DHT11_PIN 2
// Adres czujnika

float temperature = 0;

void setup() 
{
  pinMode(9, OUTPUT);
  while(!Serial);    //start the transmission with the terminal
   Serial.begin(9600);

  if(WiFi.firmwareVersion() < WIFI_FIRMWARE_LATEST_VERSION)
  {
    Serial.print("Please upgrade your WiFi firmware, up to date version is: ");
    Serial.println(WIFI_FIRMWARE_LATEST_VERSION);
    
  }
  while(status != WL_CONNECTED)
  {
    Serial.print("Attempting to connect to wpa SSID: ");
    Serial.println(ssid);
    status = WiFi.begin(ssid, pass);
    delay(5000);
  }

  Serial.println("You got connected");
  printWiFiData();
  printCurrentNet(); 
  UDP.begin(localPort);
  
  dht.setup(DHT11_PIN);
  dht.getTemperature();
}

void printWiFiData()
{
  // print your board's IP address:
  IPAddress ip = WiFi.localIP();
  Serial.print("IP address : ");
  Serial.println(ip);

  Serial.print("Subnet mask: ");
  Serial.println((IPAddress)WiFi.subnetMask());

  Serial.print("Gateway IP : ");
  Serial.println((IPAddress)WiFi.gatewayIP());

  // print your MAC address:
  byte mac[6];
  WiFi.macAddress(mac);
  Serial.print("MAC address: ");
  printMacAddress(mac);
}

void printCurrentNet() 
{
  // print the SSID of the network you're attached to:
  Serial.print("SSID: ");
  Serial.println(WiFi.SSID());

  // print the MAC address of the router you're attached to:
  byte bssid[6];
  WiFi.BSSID(bssid);
  Serial.print("BSSID: ");
  printMacAddress(bssid);
  // print the received signal strength:
  long rssi = WiFi.RSSI();
  Serial.print("signal strength (RSSI): ");
  Serial.println(rssi);

  // print the encryption type:
  byte encryption = WiFi.encryptionType();
  Serial.print("Encryption Type: ");
  Serial.println(encryption, HEX);
  Serial.println();
}

void printMacAddress(byte mac[]) 
{
  for (int i = 5; i >= 0; i--) {
    if (mac[i] < 16) {
      Serial.print("0");
    }
    Serial.print(mac[i], HEX);
    if (i > 0) {
      Serial.print(":");
    }
  }
  Serial.println();
}

void loop() 
{
    while (dht.getStatusString() != "OK")
    {
      temperature = dht.getTemperature();  
    }
  for(char& c : packetBuffer)
    c = 0;
  int packetSize = UDP.parsePacket();
  if (packetSize) {
    Serial.print("Received packet of size ");
    Serial.println(packetSize);
    Serial.print("From ");
    IPAddress remoteIp = UDP.remoteIP();
    Serial.print(remoteIp);
    Serial.print(", port ");
    Serial.println(UDP.remotePort());

    // read the packet into packetBufffer
    int len = UDP.read(packetBuffer, 255);
//    if (len > 0) {
//      packetBuffer[len] = 0;
//    }
    Serial.println("Contents:");
    Serial.println(packetBuffer);
        Serial.println("Contentss:");
    Serial.println(temperatureCheckCode);
//    Serial.println(packetBuffer==temperatureCheckCode);
    String recievedMessage = packetBuffer;
    if(recievedMessage==temperatureCheckCode)
      dtostrf(temperature, 6, 2, ReplyBuffer); //puts the temperature in the ReplyBuffer
    if(recievedMessage==lightSwitchCode) // change the light settings
    {
      light = !light;
      Serial.println(light);
      Serial.println((PinStatus)light);
    }
    digitalWrite(9, (PinStatus) light); //turn light on/off
    // send a reply, to the IP address and port that sent us the packet we received
    UDP.beginPacket(UDP.remoteIP(), UDP.remotePort());
    UDP.write(ReplyBuffer);
    UDP.endPacket();
  }
}  
