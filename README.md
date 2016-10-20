# cerberus-extension-sikuli

This project allow to use Cerberus (https://github.com/cerberustesting/cerberus-source) from version 1.2 with sikuli (http://www.sikuli.org) as a Selenium extension. This extension will extend the selenium server that will be used to run your script.

To use it, you will need to:

> 1 - Install sikuli on servers where the script will be executed (following instruction here : http://sikulix-2014.readthedocs.org/en/latest/index.html).

> 2 - Download Selenium Standalone Server (http://www.seleniumhq.org/download).

> 3 - Unzip cerberus-extension-sikuli and put the cerberus-extension-sikuli-x.x.x.jar it in the same folder than the selenium server.

> 4 - Create a .bat/.sh file with the following command

>> start /b java -jar selenium-server-standalone-x.xx.x.jar -role hub -port 5555

>> start /b java -cp selenium-server-standalone-x.xx.x.jar;cerberus-extension-sikuli-x.x.x.jar org.openqa.grid.selenium.GridLauncher -role node -hub http://localhost:5555/grid/register -port 5556 -servlets org.sikuliserver.ExecuteSikuliAction
