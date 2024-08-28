##Report retention Times

###Testware Files
 Testware runtime files are wiped from TEST_SCHEDULER every 12 hours.
 The node clean up time is specified in te-node-parent/te-node/src/main/resources/settings.properties file. 
 
 *Note: 12 hours was chosen as higher values were causing Maintrack to experience disk storage issues.
        
###Allure Report
####Local Storage
Allure reports will be stored locally in the /var/log/te_logs directory.
If upload to remote storage is set to true local file system will be cleaned, otherwise the files will remain on the 
local system*. 

*Please see ERICtaf_te\te-docker\src\main\resources\taf_te_master\setup_files\te_reporting\log_upload.sh.

####Allure-Service
 Once an allure report has been generated the testware runtime files are stored on Allure Service for a total of 1 hour. 
 Testware Files are stored on Allure Service for 24 hours in cases where a request to generate the report has not yet been made.
  
####OSS Logs
If the the allure report is stored on OSS Logs it will be available for 120 days.



