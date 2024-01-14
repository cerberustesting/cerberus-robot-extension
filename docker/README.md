# Dokerfile for Cerberus nodes

You can use those Dockerfiles in order to build your own image.

## Parameters

They all support those 2 build parameters in order to specify the VNC Password to use and the version of the robot extension to use 

* VNC_PASSWORD=XXXXXXXX
* CERBERUS_ROBOT_EXTENSION_VERSION=1.7.1

## List of available images 

| image | Description |
| --- | --- |
| nodeChrome | **Chrome** Selenium node. Based on **Selenium 3**. Will build against **latest** available version of Chrome. |
| nodeFirefox | **Firefox** Selenium node. Based on **Selenium 3**. Will build against **latest** available version of Chrome. |

## Instruction on how to build your own image:

    docker build -t my-chrome-node:latest \
    --build-arg="VNC_PASSWORD=XXXXXXX" --build-arg="CERBERUS_ROBOT_EXTENSION_VERSION=XXXXXXXXX" \
    PATH_TO_DOCKERFILE_FOLDER

Examples :

Chrome image with VNC debug password to 1OA1GjLv3PCTou48 and Cerberus Robot Extension to latest SNAPSHOT

    docker build -t my-chrome-node:latest \
    --build-arg="VNC_PASSWORD=1OA1GjLv3PCTou48" --build-arg="CERBERUS_ROBOT_EXTENSION_VERSION=SNAPSHOT" \
    PATH_TO_DOCKERFILE_FOLDER

Firefox image with VNC debug password to 1OA1GjLv3PCTou48 and Cerberus Robot Extension to latest 1.7.1

    docker build -t my-chrome-node:latest \
    --build-arg="VNC_PASSWORD=1OA1GjLv3PCTou48" --build-arg="CERBERUS_ROBOT_EXTENSION_VERSION=1.7.1" \ 
    PATH_TO_DOCKERFILE_FOLDER

## Known issues

### session not created: DevToolsActivePort file doesn't exist

Since latest version of Chrome, you may have the error :

    The test case failed to be executed. 
    More lickely due to an error in the test or in Cerberus configuration. 
    The test case failed to be executed. 
    Could not start Robot Server. org.openqa.selenium.SessionNotCreatedException: session not created: 
    Chrome failed to start: exited normally. (session not created: DevToolsActivePort file doesn't exist) 
    (The process started from chrome location /opt/google/chrome/chrome is no longer running, so ChromeDriver is assuming that Chrome has crashed.) 
    
Solution is to add the following argument to Robot Extra Argument

    --no-sandbox
