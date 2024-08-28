#!/bin/bash
# Log transfer and report generate script
# ssh-keyscan(1) -H seliiuts02214.seli.gic.ericsson.se >> ~/.ssh/known_hosts is required
echo -e "\nLog upload script is running..."

logs_dir=$1
folder=$2
source_dir=$logs_dir/$folder
expected_allure_suite_count=$3
upload_to_remote_server=$4
seconds_to_wait=<%= seconds_to_wait_for_suite_xmls %>
found=0
suite_xml_count=0

echo "Expecting at least $expected_allure_suite_count Allure suite reports to appear in $source_dir in $seconds_to_wait seconds"

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

echo "Source Allure XML folder $source_dir contents:"
ls -l $source_dir

archive="<%= te_logs %>/$folder.tar"

ftp_log_dir="<%= ftp_log_storage %>/$folder.tar"

unzip="tar -xvf <%= ftp_log_storage %>/$folder.tar -C <%= ftp_log_storage %>/"

remove_ftp_archive="rm -f $ftp_log_dir"

# Exit with code 1 if any subsequent command returns erroneous exit code
trap "exit 1" ERR

# Generate Allure report; have to use -i to use root's env settings like JAVA_HOME that's required by Allure CLI
allureGenScript="sudo -i <%= allure_cli_path %>/allure generate -o $source_dir $source_dir"
echo "Allure report generation script '$allureGenScript' will be run now by sudoer $USER as root"
echo "----------------------------------------------"
$allureGenScript
echo "----------------------------------------------"

if [ "${upload_to_remote_server}" == "true" ]
then
    #zip log folder
    echo "Zipping reports..."
    sudo tar -cvf $archive -C $logs_dir $folder &> /dev/null

    #copy to ftp
    echo "Uploading reports to FTP log storage..."
    sudo sshpass -p <%= log_storage_user_pw %> scp $archive <%= log_storage_user %>@<%= ftp_host %>:<%= ftp_log_storage %> &> /dev/null

    #remove local archive
    echo "Cleaning local archive..."
    sudo rm -f $archive

    #unzip on ftp
    echo "Unzipping on FTP..."
    sudo sshpass -p <%= log_storage_user_pw %> ssh <%= log_storage_user %>@<%= ftp_host %> $unzip &> /dev/null

    #remove ftp archive
    echo "Dropping archive on FTP..."
    sudo sshpass -p <%= log_storage_user_pw %> ssh <%= log_storage_user %>@<%= ftp_host %> $remove_ftp_archive &> /dev/null
else
    echo "Bypassing reports upload (upload option is unchecked in TE configuration)"
fi