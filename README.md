
# PypyDanceVideoBooster
When you play a video, it will create a cache of this video locally. 
It can save network overhead and boost video loading.

***The Software is updateing, all releases has already has been removed.**

***The new version will enable compression to reduce hard drive usage.**

The full version of this software will be released within two weeks, and the beta version will be released within one week. 

Maybe some people will ask me: "PypyDance already has a great CDN network, why do you want to make this program?" 

And my answer to this question is: Although PypyDance provides a great CDN network, but some people's network is too bad. The slow network speed and a large number of Avatar loading behaviors will cause the network to be completely blocked, and therefore lose the opportunity to load the video and get a black screen. I think that some means should be used to ensure that the video must be loaded, so I made this program. 

## About PypyDance updated at 2021-06-27
Because the api was changed to https, this caused the main function to fail. Although it returned to normal after some repairs, it was a little troublesome to use. 

  1. Install Java from https://www.oracle.com/java/technologies/javase/javase-jdk8-downloads.html
  2. Run cmd and execute command "keytool -genkey -keyalg rsa", no matter what it asks you, answer "jd.pypy.moe".
  3. You will got a ".keystore" file in your user home directory, then execute command "keytool -export -keystore .keystore -file publickey.cer".
  4. You will get an HTTPS certificate, but it is not legal yet, so you need to force the operating system to trust this certificate and install it to the trusted root certification authority directory with your mmc console. You can find related tutorials on Google. 
  5. Run cmd and execute command "java -jar PypyDanceVideoBooster.jar -port=12345 -dev=true", you can see "HttpsServer launched".
  6. Install Proxifier and open RuleList.ppx file, the file can be download from main page.
  7. Open the privacy window of your browser, then enter the link "https://jd.pypy.moe/api/v1/videos/GNxgQK50KWE.mp4", when browser says you are at risk, click Advanced and click Continue.
  8. Now is the time to witness the miracle, if you do nothing wrong, Marshell will appear on your screen. 

Finally, this is a hasty development. The old CDN network is still being used. In future versions, it will be switched to the new CDN network in a more friendly way, and network bandwidth consumption will be further improved. 

You can use some methods(such is Proxifer) to introduce the socket to "storage-cdn.llss.io" into this program, and then it will automatically manage your cache. 
It is worth mentioning that only when a file is fully loaded will it be considered as a valid cache. This is to avoid the "206 partial content loop" when the network is poor. 

![set](https://user-images.githubusercontent.com/83615308/123447321-b651c900-d5a7-11eb-8e66-c80b07925a05.png)

![set2](https://user-images.githubusercontent.com/83615308/123447325-b782f600-d5a7-11eb-94e8-b110bbb4bebb.png)

![U1](https://user-images.githubusercontent.com/83615308/123447327-b81b8c80-d5a7-11eb-8040-8718c58421e9.png)
