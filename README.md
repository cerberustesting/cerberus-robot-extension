# cerberus-robot-extension

This project allow to use Cerberus (https://github.com/cerberustesting/cerberus-core) from version 1.2 with sikuli (http://www.sikuli.org) as a Selenium extension. This extension will extend the Selenium server that will be used to run your script.

## To use it, you will need to:

* Download [Selenium Standalone Server](http://www.seleniumhq.org/download).
* Unzip cerberus-robot-extension and put the cerberus-robot-extension-x.x.jar in the same folder as the Selenium server.
* Create a .bat/.sh file with the following command depending on your need.

### *For a standalone mode (Testing FAT application only)*

_Windows:_

      start /b java -jar cerberus-robot-extension-x.x.jar -p 5555


_Linux/Mac:_

      java -jar ./cerberus-robot-extension-x.x.jar -p 5555


### *For hybrid testing (Mixing Selenium Web Testing with FAT Testing features using image recognition).*

_Windows:_

    start /b java -jar selenium-server-standalone-x.xx.x.jar -role hub -port 5555
    start /b java -cp "selenium-server-standalone-x.xx.x.jar;cerberus-robot-extension-x.x.jar" org.openqa.grid.selenium.GridLauncherV3 -role node -hub http://localhost:5555/grid/register -port 5556 -servlets org.cerberus.robot.extension.sikuli.ExecuteSikuliAction -servlets org.cerberus.robot.extension.filemanagement.ExecuteFilemanagementAction -servlets org.cerberus.robot.extension..management.ExecuteManagementAction


_Linux:_

    java -jar selenium-server-standalone-x.xx.x.jar -role hub -port 5555
    java -cp selenium-server-standalone-x.xx.x.jar:cerberus-extension-sikuli-x.x.x.jar org.openqa.grid.selenium.GridLauncherV3 -role node -hub http://localhost:5555/grid/register -port 5556 -servlets org.cerberus.robot.extension.sikuli.ExecuteSikuliAction -servlets org.cerberus.robot.extension.filemanagement.ExecuteFilemanagementAction -servlets org.cerberus.robot.extension..management.ExecuteManagementAction

NOTE : The execution needs to be sent to the node port (5556 in the example). Pure Web executions can still be sent to the hub.

## Optional arguments:

    Optional arguments :
    -p (--port) Integer : Port
    -e (--highlightElement) Integer : NumberOfSeconds
    -a (--authorisedFolderScope) String : Path where Extension will be allowed to upload and download files
    -d (--debug)
    -h (--help)

