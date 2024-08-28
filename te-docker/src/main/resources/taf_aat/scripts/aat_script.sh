#!/bin/bash

show_help()
{
echo -e "\nUsage:\n"
echo -e "-reg|--register <AAT BASE SERVICE IP> <PORT> <JSON FILE FULL PATH> <PASSWORD>"
echo -e "-ref|--refresh <AAT BASE SERVICE IP> <PORT> <TEST SERVICE ID> <JSON FILE FULL PATH> <PASSWORD>"
echo -e "-del|--delete <AAT BASE SERVICE IP> <PORT> <TEST SERVICE ID> <PASSWORD>"
echo -e "-get|--get <AAT BASE SERVICE IP> <PORT> [TEST SERVICE ID]"
echo -e "\n"
echo -e "Examples: \n"
echo "./aat_script.sh -ref 10.221.168.4 33333 eac8bbcf-c237-42eb-a012-c2dd4e6b5842 /var/tmp/register.json password"
echo "./aat_script.sh --refresh 10.221.168.4 33333 eac8bbcf-c237-42eb-a012-c2dd4e6b5842 /var/tmp/register.json password"
echo "./aat_script.sh -reg 10.221.168.4 33333 /var/tmp/register.json password"
echo "./aat_script.sh -get 10.221.168.4 33333 eac8bbcf-c237-42eb-a012-c2dd4e6b5842"
echo "./aat_script.sh -get 10.221.168.4 33333"
echo "./aat_script.sh -del 10.221.168.4 33333 eac8bbcf-c237-42eb-a012-c2dd4e6b5842 password"
echo "./aat_script.sh --delete 10.221.168.4 33333 eac8bbcf-c237-42eb-a012-c2dd4e6b5842 password"
echo -e "\n"
}

get_service(){
echo "Getting the service Info from the AAT core service"
if [ "$3" ];then
        curl http://$1:$2/hectar/v1/service/$3
        echo -e "\n\n"
else
        curl http://$1:$2/hectar/v1/service
        echo -e "\n\n"
fi
}

delete_service(){
echo "Deleting the serivce info from the AAT core service"
curl -X DELETE http://$1:$2/hectar/v1/service/$3?secret=$4
echo -e "\n"
}

register_service(){
echo "Registering the Test service......"
cp -rf $3 /var/tmp/register_temp.json
sed -i "s/\${SECRET PASSWORD}/$4/g" /var/tmp/register_temp.json
curl http://$1:$2/hectar/v1/service -X POST --header "Content-Type: application/json" -d @/var/tmp/register_temp.json
rm -rf /var/tmp/register_temp.json
echo -e "\n"
}

refresh_service(){
echo "Refreshing the Test service......"
cp -rf $4 /var/tmp/register_temp.json
sed -i "s/\${SECRET PASSWORD}/$5/g" /var/tmp/register_temp.json
curl http://$1:$2/hectar/v1/service/$3 -X POST --header "Content-Type: application/json" -d @/var/tmp/register_temp.json
rm -rf /var/tmp/register_temp.json
echo -e "\n"
}

while :; do
    case $1 in
        -h|-\?|--help)
            show_help    # Display a usage synopsis.
            break
            ;;
        -reg|--register)       # Takes an option argument; ensure it has been specified.
            if [ ! -z "$2" -a ! -z "$3" -a ! -z "$4" -a ! -z "$5" ]; then
                aat_base_service="$2"
		        port="$3"
		        json_file="$4"
		        password="$5"
                register_service $aat_base_service $port $json_file $password
                shift
            else
                echo 'ERROR: --register requires all the parameters mentioned in the help. Use: ./script --help'
            fi
            break
            ;;
	    -ref|--refresh)       # Takes an option argument; ensure it has been specified.
            if [ ! -z "$2" -a ! -z "$3" -a ! -z "$4" -a ! -z "$5" -a ! -z "$6" ]; then
		        aat_base_service="$2"
                port="$3"
		        service_id="$4"
		        json_file="$5"
		        password="$6"
		        refresh_service $aat_base_service $port $service_id $json_file $password
                shift
            else
                echo 'ERROR: --refresh requires all the parameters mentioned in the help. Use: ./script --help'
            fi
            break
            ;;
        -del|--delete)       # Takes an option argument; ensure it has been specified.
            if [ ! -z "$2" -a ! -z "$3" -a ! -z "$4" -a ! -z "$5" ]; then
                aat_base_service="$2"
		        port="$3"
		        service_id="$4"
		        secret_key="$5"
                delete_service $aat_base_service $port $service_id $secret_key
                shift
            else
                echo 'ERROR: --delete requires all the parameters mentioned in the help. Use: ./script --help'
            fi
            break
            ;;
        -get|--get)       # Takes an option argument; ensure it has been specified.
            if [ ! -z "$2" -a ! -z "$3" ] || [ ! -z "$2"  -a ! -z "$3"  -a ! -z "$4" ]; then
		        aat_base_service="$2"
		        port="$3"
		        if [ "$4" ];then
			        service_id="$4"
			        get_service $aat_base_service $port $service_id
		        else
			        get_service $aat_base_service $port
		    fi
                shift
            else
                echo 'ERROR: --get requires all the parameters mentioned in the help. Use: ./script --help'
            fi
            break
            ;;
        --)
            show_help            # End of all options.
            shift
            break
            ;;
        -?*)
            show_help
            printf 'WARN: Unknown option (ignored): %s\n' "$1" >&2
            break
            ;;
         *)
            show_help
            break
            ;;
    esac
    shift
done