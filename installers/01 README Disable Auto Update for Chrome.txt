Link for full instructions:

https://stackoverflow.com/questions/18483087/how-to-disable-google-chrome-auto-update

https://www.webnots.com/7-ways-to-disable-automatic-chrome-update-in-windows-and-mac/


Steps:

1. Uninstall the Google Chrome from the machine
2. Install ChromeStandaloneX64(75.0.3770.80).exe
3. After installation, open the Task Manager
4. End All task that has Google on it. (e.g. Google Crash Handler, etc...)
5. Open the "Run" command by pressing the "Win +R" on the Windows search box, and select the run command.
6. Type "msconfig" and click ok or hit "Enter" to open the system configuration panel.
7. On the system configuration window, select the "Services" tab. At the bottom of the window, uncheck the box on the left of Hide all Microsoft services".
8. Un-check any services that has "Google Update" on it.
9. Click the "Apply" and then "OK" button to save the changes.
10. Open the "Run" command by pressing the "Win +R" on the Windows search box, and select the run command.
11. Type "services.msc" and click "Ok". This will open services manager window.
12. Look for any "Google Update" services.
13. On the Startup type, choose Disabled.
14. Open the CCleaner app. Make sure Google does not have any task or schedulers that would run.
15. Open Windows Defender Firewall
16. Click Advanced settings
17. Hit Outbound Rules
18. Create New Rule
19. Choose Program then Next
20. Hit browse for the specific application to block
21. Go to C:\Program Files (x86)\Google\Update\GoogleUpdate.exe then Next
22. Choose Block the connection then Next
23. Tick all checkboxes then Next
24. Name the rule accordingly to quickly identify it.
25. Restart PC.

NOTE: When you hit the About section of Google Chrome, a pop-op may appear asking for permission, make sure you hit No.