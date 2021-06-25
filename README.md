
# PypyDanceVideoBooster
When you play a video, it will create a cache of this video locally. 
It can save network overhead and boost video loading.

The full version of this software will be released within two weeks, and the beta version will be released within one week. 

Maybe some people will ask me: "PypyDance already has a great CDN network, why do you want to make this program?" 

And my answer to this question is: Although PypyDance provides a great CDN network, but some people's network is too bad. The slow network speed and a large number of Avatar loading behaviors will cause the network to be completely blocked, and therefore lose the opportunity to load the video and get a black screen. I think that some means should be used to ensure that the video must be loaded, so I made this program. 



![upload1](https://user-images.githubusercontent.com/83615308/122561692-298b9600-d010-11eb-8591-4a8f5ba731f7.png)Its default running address is "localhost:12345" 

You can use some methods to introduce the socket to "storage-cdn.llss.io" into this program, and then it will automatically manage your cache. 
It is worth mentioning that only when a file is fully loaded will it be considered as a valid cache. This is to avoid the "206 partial content loop" when the network is poor. 

![set](https://user-images.githubusercontent.com/83615308/123447321-b651c900-d5a7-11eb-8e66-c80b07925a05.png)

![set2](https://user-images.githubusercontent.com/83615308/123447325-b782f600-d5a7-11eb-94e8-b110bbb4bebb.png)

![U1](https://user-images.githubusercontent.com/83615308/123447327-b81b8c80-d5a7-11eb-8040-8718c58421e9.png)
