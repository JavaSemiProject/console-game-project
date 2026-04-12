@echo off
chcp 65001 > nul
pushd "%~dp0."

if not exist out\cmd mkdir out\cmd

javac -encoding UTF-8 -cp lib\mysql-connector-j-9.6.0.jar @sources.txt -d out\cmd
if %errorlevel% neq 0 goto fail

java -Dfile.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8 -Dsun.stderr.encoding=UTF-8 -cp out\cmd;lib\mysql-connector-j-9.6.0.jar Main
goto end

:fail
echo Compilation failed!

:end
popd
pause
