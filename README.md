# Start app
[TCP Main Class](src/main/java/com/example/tcppush/TcpPushApplication.java)
mvn spring-boot:run

# TCP Server locally
For testing you can open the port locally and start the application
nc -l 8000

# Messages flow
Everything will start by parsing a hard coded route (Or read GPX file if specified in the configuration). 
This GPX file will have 3 points in the same location at the beginning (to allow login and ignition on in the same place and trip start)
This GPX file will have 3 points in the same location at the end (to allow voltage error simulation and restored and ignition off on in the same place and trip start)

# Useful links 
[Visual Map path designer](https://nmeagen.org/)
[Maps to NMEA](https://sendaone.com/google-maps-to-nmea)
[GPRMC decoder not working](https://mapstogpx.com/)
[GPRMC decoder](https://www.aggsoft.com/nmea-decoder.htm)
[Standalone GPRMC decoder](https://github.com/jonahmurphy/Whats-Here-To-GPRMC)
https://gist.github.com/tdenewiler/7b9ee7d9e1655ee40dc9524ca01d6341
https://metacad.io/en/tools/nmea-decoder/


# Routes
[Athlone surroundings](https://maps.app.goo.gl/xpqcanwRkNAC56F28)