 MP3SPI 1.9.1
 JavaZOOM 1999-2004

 Project Homepage :
   http://www.javazoom.net/mp3spi/mp3spi.html 

 JAVA and MP3 online Forums :
   http://www.javazoom.net/services/forums/index.jsp
-----------------------------------------------------

DESCRIPTION :
-----------
MP3SPI is a SPI (Service Provider Interface) that adds MP3 support for JavaSound.
It allows to play MPEG 1/2/2.5 Layer 1/2/3 files thanks to underlying JavaLayer 
and Tritonus libraries. This is a non-commercial project and anyone can add his 
contribution. MP3SPI is licensed under LGPL (see LICENSE.txt).


FAQ : 
---
- How to install MP3SPI ?
  Before running MP3SPI you must set PATH and CLASSPATH for JAVA
  and you must add jl0.4.jar, tritonus_share.jar and mp3spi1.9.1.jar to the CLASSPATH.

- Do I need JMF to run MP3SPI player ?
  No, JMF is not required. You just need a JVM JavaSound 1.0 compliant.
  (i.e. JVM1.3 or higher). However, MP3SPI is not JMF compliant.

- Does MP3SPI support streaming ?
  Yes, it has been successfully tested with ShoutCast streaming server.

- Does MP3SPI support MPEG 2.5 ?
  Yes, MP3SPI includes same features as JavaLayer.

- Does MP3SPI support VBR ?
  Yes, It supports XING VBR header too. 

- How to get ID3v2 tags from MP3SPI API ?
  MP3SPI exposes many audio properties such as ID3v2 frames, VBR, bitrate ...
  See online examples from MP3SPI homepage to learn how to get them.

- How to skip frames to have a seek feature ?
  Call skip(long bytes) on AudioInputStream.

- How to run jUnit tests ?
  See ANT script included (ant test). You need to update test.mp3.properties file
  with the audio properties of the MP3 you want to use for testing. You could generate
  test.mp3.properties by uncommenting //out=System.out in jUnit test sources.

- How much memory/CPU MP3SPI needs to run ?
  Here are our benchmark notes :
    - Heap use range : 1380KB to 1900KB - 370 classes loaded. 
    - Footprint : ~8MB under WinNT4/Win2K + J2SE 1.3 (Hotspot).
                  ~10MB under WinNT4/Win2K + J2SE 1.4.1 (Hotspot).
    - CPU usage : ~12% under PIII 800Mhz/WinNT4+J2SE 1.3 (Hotspot).
                  ~8% under PIII 1Ghz/Win2K+J2SE 1.3.1 (Hotspot).
                  ~12% under PIII 1Ghz/Win2K+J2SE 1.4.1 (Hotspot).

- How to contact MP3SPI developers ?
  Try to post a thread on Java&MP3 online forums at :
  http://www.javazoom.net/services/forums/index.jsp
  You can also contact us at mp3spi@javazoom.net for contributions.
 

KNOWN PROBLEMS :
--------------
99% of MP3 plays well with JavaLayer but some (1%) return an ArrayIndexOutOfBoundsException 
while playing. It's a bug (hard to fix) in the underlying JavaLayer decoder.