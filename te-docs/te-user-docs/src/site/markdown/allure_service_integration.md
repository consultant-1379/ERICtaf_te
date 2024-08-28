<head>
    <title>Allure Service Integration</title>
</head>

# Old Allure solution via CLI
During installation of the TE master slave the Allure CLI is downloaded and installed on the VM.
The final step of the Test_Scheduler job is to generate and upload the allure report.
To do this it calls the Allure CLI generate command, zips up the complete report and uploads it to OSS Logs.
Once uploaded the Test_scheduler job then unzips the report on the FTP server.

# Allure service integration

### When using older TE version

TE Jenkins configuration can be manually edited to use allure-service to generate the report or to host the report or both.

Allure report host: This is the base URL used for reporting.
Allure service backend URL: By specifying this TE will use the allure service specified to generate the report otherwise will use allure CLI.
Upload reports to OSS Logs server: If this is checked the report will be copied via ssh to the allure report host.

### When using dockerized TE

The same configuration can be set as part of the docker-compose file used to spin up the TE containers.
The following values are added to the command option of the master container in the docker-compose file.

local-report-host-base-uri: This is where the report will be hosted after generation, if this is not set it will default to using https://oss-taf-logs.seli.wh.rnd.internal.ericsson.com.
allure_service_url: When this is specified TE will use the allure service to generate reports, if it's not specified it will use allure CLI.
allure_service_backend_url: This is used for healthchecks when allure service is deployed inside the vApp.

