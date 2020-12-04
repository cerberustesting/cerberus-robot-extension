# cerberus-extension-sikuli

This project allow to use Cerberus (https://github.com/cerberustesting/cerberus-source) from version 1.2 with sikuli (http://www.sikuli.org) as a Selenium extension. This extension will extend the selenium server that will be used to run your script.

## To use it, you will need to:

> 1 - Install sikuli on servers where the script will be executed (following instruction [here](http://sikulix-2014.readthedocs.org/en/latest/index.html)   and Installation package [here](https://launchpad.net/sikuli/sikulix)).

> 2 - Download [Selenium Standalone Server](http://www.seleniumhq.org/download).

> 3 - Unzip cerberus-extension-sikuli and put the cerberus-extension-sikuli-x.x.x.jar in the same folder as the Selenium server.

> 4 - Create a .bat/.sh file with the following command

- For a standalone mode (Testing FAT application only)
Windows:

      start /b java -jar cerberus-extension-sikuli-x.x.x.jar -p 5555


Linux/Mac:

      java -jar ./cerberus-extension-sikuli-x.x.x.jar -p 5555


- For hybrid testing (Mixing Selenium Web Testing with FAT Testing features using image recognition)

Windows:

    start /b java -jar selenium-server-standalone-x.xx.x.jar -role hub -port 5555
    start /b java -cp "selenium-server-standalone-x.xx.x.jar;cerberus-extension-sikuli-x.x.x.jar" org.openqa.grid.selenium.GridLauncherV3 -role node -hub http://localhost:5555/grid/register -port 5556 -servlets org.sikuliserver.ExecuteSikuliAction


Linux:

    java -jar selenium-server-standalone-x.xx.x.jar -role hub -port 5555
    java -cp selenium-server-standalone-x.xx.x.jar:cerberus-extension-sikuli-x.x.x.jar org.openqa.grid.selenium.GridLauncherV3 -role node -hub http://localhost:5555/grid/register -port 5556 -servlets org.sikuliserver.ExecuteSikuliAction

## Optional arguments:

    Optional arguments :
    -d (--debug)
    -e (--highlightElement) Integer : NumberOfSeconds
    -p (--port) Integer : Port
    -h (--help)

