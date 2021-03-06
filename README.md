
# PypyDanceVideoBooster

When you play a certain video in PypyDance, this software will help you create a video cache, and then when the video is played again, it will read the cache directly from the hard disk, thereby preventing network problems from causing the video to not be played. 

Some videos have been deleted from some CDN nodes, so you may encounter 404 Not Found because of copyright or other reasons.

__Any player is prohibited from spreading the cache files generated by this software, absolutely not! Otherwise Sadako will knock on your door in the middle of the night.__


## User Guide

In version 1.4-beta, this software has become easier to use, just start it with the console and modify the hosts file. 

Each instance uses an 80 port and allows a maximum of 2 QPS. 

1. Download and install Java from java.com, if you are already installed a Java8, you can skip the step.
2. Append the text "127.0.0.1" to the file "C:\Windows\System32\drivers\etc\hosts", and save the file. 
3. Unzip dist.zip to a folder, where the cache files will be stored in the future.
4. Open a console or PowerShell in this folder, and type "java -jar PypyDanceVideoBooster.jar", and then press Enter. 
5. Go to PypyDance world and you should be able to see the video is playing. 

An example of a successful video playback:

![image](https://user-images.githubusercontent.com/83615308/143261919-04fbec0d-b45b-4257-a2f8-e2415d89d9e9.png)

It usually only takes a few seconds to download a video. If you see the progress bar stagnating, you should change the ip. I suggest you use the Japanese ip to get the fastest download speed, because pypy's server is also in Japan (it seems to be so) 
