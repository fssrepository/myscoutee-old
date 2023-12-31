@echo off

set srcDir="c:\projects\DT\HAL-core"
set dstDir="c:\projects\hal\swagger_parse\src\main\resources\public"
set conf="c:\projects\hal\impact\conf.txt"

IF exist %dstDir% (
	RD /S /Q %dstDir%
)

set currDir=%cd%

cd %srcDir% 

for /F "tokens=1 delims= " %%A in ('type %conf%') do (
    git checkout sprint/%%A
    Xcopy /E /I %srcDir%\public %dstDir%\%%A
    echo %%A
)

cd %currDir%