# cveDetails
This project deals with copying certain columns from www.cvedetails.com as json


It uses Selenium to get the text from UI. 
It doesn't require a browser to be installed, making it cron job friendly. 
It is a gradle project. After cloning/downloading the project.
Run the following command

```
./gradlew build
```

After building the project setup project SDK with a installed JAVA SDK path,
JAVA_HOME needs to be setup as environment variable.

Update **fileName** variable with the desired .json file path

Run the following command

```
COLLECTION_MODE="current" ./gradlew runWithJavaExec
```
Where COLLECTION_MODE can be 

1. **"all"** i.e., it collects all the CVEs from 1999 to till date.

2. **"year"**  Pass any required year, example: "2022" to collect all the CVEs from the year 2022 and copy it to the given json file.

3. **"current"** On passing "current" it will collect the current months CVEs identified so far.

If COLLECTION_MODE is not passed as the argument it is defaulted to **"current"** i.e.,

```
./gradlew runWithJavaExec
```

**runWithJavaExec** is the gradle task.




