@echo off

set srcDir="c:\projects\hal\swagger_parse\src\main\resources\public"
set dstDir="c:\projects\hal\hal-mt-core"
set conf="c:\projects\hal\impact\conf.txt"

cd %dstDir%

for /F "tokens=1 delims= " %%A in ('type %conf%') do (
        IF exist %dstDir%\public (
		RD /S /Q %dstDir%\public
        )

	Xcopy /E /I %srcDir%\%%A %dstDir%\public

        gradle -q seqRename %dstDir%\public\sequenceDiagrams

	git checkout -b sprint/%%A
	git push origin sprint/%%A
	git add public
	git commit -a -m "[SPRINT:%%A] sprint %%A"
	git checkout master
	git merge sprint/%%A
	git push origin master

)