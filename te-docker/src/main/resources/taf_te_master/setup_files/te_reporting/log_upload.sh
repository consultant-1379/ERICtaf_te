#!/bin/bash
# Log transfer and report generate script
# ssh-keyscan(1) -H seliiuts02214.seli.gic.ericsson.se >> ~/.ssh/known_hosts is required
echo -e "\nLog upload script is running..."

logs_dir=$1
folder=$2
source_dir=$logs_dir/$folder
expected_allure_suite_count=$3
upload_to_remote_server=$4
has_allure_service=$5
seconds_to_wait=15
found=0
suite_xml_count=0
te_logs=/var/log/te_logs
ftp_log_storage=${LOGSTORE_DIR}
log_storage_user=${LOGSTORE_USER}
log_storage_user_pw=${LOGSTORE_PW}
ftp_host=${LOGSTORE_FTP}
allure_cli_path=/opt/allure-cli/bin/allure


#
# Use script to generate report. TODO: Delete this block once Allure Service adopted!
#
if [ "${has_allure_service}" == "false" ]
then
    echo "Expecting at least $expected_allure_suite_count Allure suite reports in $source_dir"
    echo "Wait $seconds_to_wait seconds for Allure Suite reports"

    for i in `seq $seconds_to_wait`
    do
        suite_xml_count=`ls -l $source_dir | grep "testsuite.xml" | wc -l`
        if [ "$suite_xml_count" -ge "$expected_allure_suite_count" ]
        then
            found=1
            break
        fi
        sleep 1
    done

    if [ "$found" -eq "0" ]
    then
        echo "Waited for $seconds_to_wait seconds for $expected_allure_suite_count test suites but found $suite_xml_count"
    else
        echo "Found $suite_xml_count Allure suite reports"
    fi

    # Generate Allure report; have to use -i to use root's env settings like JAVA_HOME that's required by Allure CLI
    allureGenScript="${allure_cli_path} generate -o $source_dir $source_dir"
    echo "Allure generation script '$allureGenScript' will be run now by $USER"
    echo "----------------------------------------------"
    $allureGenScript
    echo "----------------------------------------------"
else
    echo "Bypassing the report generation via CLI tool, as Allure service is used"
fi

echo "Source Allure XML folder $source_dir contents:"
ls -l $source_dir

echo "Create archive ${ftp_log_storage}/$folder.tar"
unzip="tar -xvf ${ftp_log_storage}/$folder.tar -C ${ftp_log_storage}/"
FailureLogKey=$(grep -o 'UploadOnlyFailedLogs\\=[^\n]*' $source_dir/tp-environment.properties | awk -F "=" '{print $2}')
if [ "$FailureLogKey" == "Yes" ]
then
        echo "Enabling only Failure Logs"
        SuccessLogs=$(grep -nlri "result 'SUCCESS'" $source_dir/te-console-logs || :)
        if [[ ! -z "$SuccessLogs" ]]
        then
            echo $SuccessLogs | while read SuccessLog
            do
            rm -f $SuccessLog
            done
        fi
fi

# Exit with code 1 if any subsequent command returns erroneous exit code
trap "exit 1" ERR

if [ "${upload_to_remote_server}" == "true" ]
then
    #zip log folder
    echo "Archiving logs..."
    archive="${te_logs}/$folder.tar"
    tar -cvf $archive -C $logs_dir $folder &> /dev/null

    #copy to ftp
    echo "Uploading logs to FTP log storage..."
    sshpass -p ${log_storage_user_pw} scp $archive ${log_storage_user}@${ftp_host}:${ftp_log_storage} &> /dev/null

    #remove local archive
    echo "Cleaning local archive..."
    rm -f $archive

    #unzip on ftp
    echo "Unarchiving on FTP..."
    sshpass -p ${log_storage_user_pw} ssh ${log_storage_user}@${ftp_host} $unzip &> /dev/null

    #remove ftp archive
    echo "Dropping archive on FTP..."
    remove_ftp_archive="rm -f ${ftp_log_storage}/$folder.tar"
    sshpass -p ${log_storage_user_pw} ssh ${log_storage_user}@${ftp_host} $remove_ftp_archive &> /dev/null
else
    echo "Bypassing reports upload (upload option is unchecked in TE configuration)"
fi
