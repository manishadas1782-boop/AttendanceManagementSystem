@echo off
setlocal
echo Resetting data and keeping only the permanent admin...
"C:\Users\mulla\tools\apache-maven-3.9.9\bin\mvn.cmd" -q exec:java -Dexec.mainClass=com.ams.tools.AdminMaintenance
echo Done.
pause
endlocal
