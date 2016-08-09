# Laptop Automator
A automator for PCBeta to collect hackintosh laptop infomation

## Features

1. Grab information from PCBeta and filter
1. Double click to open the link
1. Easy to pick out
1. Write to local JSON file
1. Update to PCBeta by one click

## Binary File

File `laptop.jar` is a runnable JAR file, and configuarion files are needed.

## Configuarion

the folder `config` should be placed in the same folder with the file `laptop.jar`.

`fid.txt` used to store the fid, for macOS Sierra, it is 557.

`session.txt` used to store the session to access the threads list. A correct session is needed, or you will get nothing. You may need tools like Wireshark or Charles to help you get the session.

`threads.txt` used to store the threads to write. Every line of this file is `fid,tid,pid,page` illustrating a "floor" of forum. You should be the author of the "floor" due to the permission.

`value.txt` used to tore the regex and the weight for the filter. Every line of this file is `regex=weight`.

`data.txt` is a local stoage of laptop infomation, using the JSON format.
