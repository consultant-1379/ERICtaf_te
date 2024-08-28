#!flask/bin/python
from flask import Flask, jsonify, abort, make_response, request
from multiprocessing import Process
import os
import re
import time
import subprocess
import sys
import datetime

app = Flask(__name__)

@app.errorhandler(404)
def not_found(error):
    return make_response(jsonify({'error': 'Not found'}), 404)

@app.route('/<string:version>/test_execution/<string:jobid>', methods=['POST'])
def create_task(version,jobid):
    print(datetime.datetime.now(), ": POST Request with JobID-", jobid)
    ip_regex = '''^(25[0-5]|2[0-4][0-9]|[0-1]?[0-9][0-9]?)\.(
                25[0-5]|2[0-4][0-9]|[0-1]?[0-9][0-9]?)\.(
                25[0-5]|2[0-4][0-9]|[0-1]?[0-9][0-9]?)\.(
                25[0-5]|2[0-4][0-9]|[0-1]?[0-9][0-9]?)'''
    try:
	    requests_content = request.get_json()
	    if not os.path.exists("/var/tmp/aat_execution/" + str(jobid)):
		    os.makedirs("/var/tmp/aat_execution/" + str(jobid))
	    configuration = request.json.get('configurations', '')
	    test_configuration = configuration.get('Testing', '')
	    taf_trigger_full_path = '/root/agat/'
	    taf_trigger_jar = test_configuration.get('taf_trigger_jar', '')
	    taf_version = test_configuration.get('taf_version', '')
	    taf_te_address = test_configuration.get('taf_te_address', '')
	    taf_test_prop = test_configuration.get('taf_test_properties', '')
	    taf_host_prop = test_configuration.get('taf_host_properties', '')
	    taf_schedule = test_configuration.get('taf_schedule', '')
	    taf_te_user = test_configuration.get('taf_te_user', '')
	    taf_te_pass = test_configuration.get('taf_te_password', '')
	    try:
	        os.chdir(taf_trigger_full_path)
	    except:
	        print(datetime.datetime.now(),"Error 409: Invalid TAF trigger JAR path")
	        return make_response(jsonify({'error': 'Invalid TAF trigger JAR path. Please check whether /root/agat folder exists' }), 409)
	    if not os.path.isfile(taf_trigger_full_path + taf_trigger_jar):
	        print(datetime.datetime.now(),"Error 409: Invalid TAF trigger JAR")
	        return make_response(jsonify({'error': 'TAF trigger jar file is not valid or not present in /root/agat/ folder' }), 409)
	    if not os.path.isfile(taf_trigger_full_path + taf_test_prop ):
	        print(datetime.datetime.now(),"Error 409: Invalid TAF test properties")
	        return make_response(jsonify({'error': 'TAF Test Properties file is not valid or not present in /root/agat/ folder' }), 409)
	    if not os.path.isfile(taf_trigger_full_path + taf_schedule):
	        print(datetime.datetime.now(),"Error 409: Invalid TAF Schedule XML")
	        return make_response(jsonify({'error': 'TAF Schedule XML file is not valid or not present in /root/agat/ folder' }), 409)
	    if not re.search(ip_regex, taf_te_address):
	        print(datetime.datetime.now(),"Error 409: Invalid IP address")
	        return make_response(jsonify({'error': 'Invalid TE IP Address' }), 409)
	    LdapAuthCommand = "ldapwhoami -x -w " + taf_te_pass + " -D uid=" + taf_te_user + ",ou=People,dc=agat,dc=enm,dc=org -h " + taf_te_address + " -p 3689"
	    returned_value = subprocess.call(LdapAuthCommand, shell=True)
	    if returned_value!=0:
	        print(datetime.datetime.now(),"Error 409: Invalid LDAP User/Password or Cannot contact LDAP server. Please check the TE Host IP address")
	        return make_response(jsonify({'error': 'Invalid LDAP LDAP User/Password or Cannot contact LDAP server. Please check the TE Host IP address' }), 409)
	    os.chdir(taf_trigger_full_path)
	    filename = 'test.log'
	    exec_command = ["java -jar ", taf_trigger_jar, " taf.version=" + taf_version, " taf.te.address=" + taf_te_address, " taf.test.properties=" + taf_test_prop, " taf.host.properties=" + taf_host_prop, " taf.schedule=" + taf_schedule, " taf.te.user=" + taf_te_user, " taf.te.password=" + taf_te_pass]
	    exec_command_log = ["java -jar ", taf_trigger_jar, " taf.version=" + taf_version, " taf.te.address=" + taf_te_address, " taf.test.properties=" + taf_test_prop, " taf.host.properties=" + taf_host_prop, " taf.schedule=" + taf_schedule, " taf.te.user=" + taf_te_user, " taf.te.password=****"]
	    procs = []
	    exec_command_full = ''.join(exec_command)
	    exec_command_full_log = ''.join(exec_command_log)
	    print("Test execution command is:  ", exec_command_full_log)
	    proc = Process(target=start_execution, args=(exec_command_full,jobid,))
	    proc.start()
	    for proc in procs:
		    proc.join()
	    return "ok"
    except Exception as e:
	    return make_response(jsonify({'Error': str(e)}), 409)
	    print(datetime.datetime.now(),"Error 409: - ", st(e))



def start_execution(my_command,jobid):
	with open('/var/tmp/aat_execution/'+ str(jobid) + '/test.log', 'wb') as f:
		process = subprocess.Popen(my_command, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, shell=True)
		for line in process.stdout:
			sys.stdout.buffer.write(line)
			f.write(line)
	return "The TE execution triggered"


@app.route('/<string:version>/test_execution/<string:jobid>', methods=['GET'])
def get_result(version, jobid):
	print(datetime.datetime.now(), ": GET Request with JobID-", jobid)
	finished_pattern=re.compile("Secure Allure Logs", re.IGNORECASE)
	host_properties_error1 = re.compile(".*json / Data source name is not valid", re.IGNORECASE)
	host_properties_error2 = re.compile(".*Invalid TDM DataSource ID/Invalid Host properties*", re.IGNORECASE)
	host_properties_error3 = re.compile(".*Problem in fetching json from the given data source name*", re.IGNORECASE)
	test_properties_error1 = re.compile(".*Specified file or url for %s is not valid*", re.IGNORECASE)
	test_properties_error2 = re.compile(".*Specified file or url for taf.host.properties is not valid or Specified %s is not present in test_properties*", re.IGNORECASE)
	tafteout_error = re.compile(".*Failed to write log location to file*", re.IGNORECASE)
	triggering_error = re.compile(".*Failed to send the REST call to trigger the build*", re.IGNORECASE)
	execution_status = ""
	failure_pattern = re.compile(".*FAILURE*", re.IGNORECASE)
	for line in open('/var/tmp/aat_execution/'+ str(jobid) + '/test.log'):
		if finished_pattern.search(line):
			execution_status = "finished"
			allure_report_line = line.split(':')
			allure_report = allure_report_line[1] + ":" + allure_report_line[2] + ":" + allure_report_line[3]
			break;
	if execution_status=='finished':
		for line in open('/var/tmp/aat_execution/'+ str(jobid) + '/test.log'):
			if failure_pattern.search(line):
				print(datetime.datetime.now(),"GET response failed: Allure report -", allure_report)
				return jsonify({"status": "failed", "links":[{"name": "Allure report", "value": allure_report}]})
		print(datetime.datetime.now(),"GET response passed: Allure report -", allure_report)
		return jsonify({"status": "passed", "links":[{"name":"Allure report", "value": allure_report}]})
	else:
		for line in open('/var/tmp/aat_execution/' + str(jobid) + '/test.log'):
		    if host_properties_error1.search(line) or host_properties_error2.search(line) or \
		       host_properties_error3.search(line) or test_properties_error1.search(line) or \
		       test_properties_error2.search(line) or tafteout_error.search(line) or \
		       triggering_error.search(line) :
		       print(datetime.datetime.now(),"Error: ", line)
		       return make_response(jsonify({'Internal error in TE': line}), 409)
		print(datetime.datetime.now(),"GET Response is ongoing")
		return jsonify({"status": "ongoing"})


if __name__ == '__main__':
	import logging
	logging.basicConfig(filename='/var/log/aat_debug.log', level=logging.DEBUG)
	app.run(host='0.0.0.0')
