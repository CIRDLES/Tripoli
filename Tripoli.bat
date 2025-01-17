echo off
for /f useback^tokens^=* %%i in (`where .:"Tripoli-?.?.?.jar"`) do java -jar %%i

