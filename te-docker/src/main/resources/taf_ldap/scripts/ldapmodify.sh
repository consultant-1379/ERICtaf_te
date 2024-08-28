#!/bin/bash

Script=`basename "$0"`
show_help()
{
echo "This script is to modify the existing password for an LDAP user"
echo -e "\nUsage:\n"
echo -e "-u|--user <USERNAME>"
echo -e "-h|--help"
echo -e "\n"
echo -e "Examples: \n"
echo -e "sh $Script -u user1"
echo -e "sh $Script --user user2"
echo -e "\n"
}

modify_password() {
echo "Modifying password for the mentioned LDAP user...."
cp -rf user_template_modify.txt /var/tmp/user.ldif
if [ $? != 0 ];then
   echo "Copying the template of User ldif has failed"
   echo "Please make sure user_template_modify.txt file is present in current path"
   return 1
fi

sed -i "s/USER_NAME/$1/g" /var/tmp/user.ldif
printf "Enter the current password of the user $1: "
read -s currentPassword
ldapwhoami -x -w $currentPassword -D uid=$1,ou=People,dc=agat,dc=enm,dc=org -h localhost -p 3689 > /dev/null 2>&1

if [ $? != 0 ]
then
    echo "LDAP username/password are invalid. Please try again with valid username/password"
    exit 1
fi

echo -e "Enter new password for $1: \n"
printf "Password: "
read -s password1
printf "\n"
printf "Confirm new password: "
read -s password2
printf "\n"

if [ $password1 == $password2 ]
then
        sed -i "s/USER_PASSWORD/$password1/g" /var/tmp/user.ldif
        sed -i "s/TYPE/modify/g" /var/tmp/user.ldif
        echo "Thank you for entering the password"
        echo "Loading the provided User credentials in the LDAP server...."
        ldapmodify -h localhost -p 3689 -x -c -D 'cn=admin,dc=agat,dc=enm,dc=org' -w adminpassword -f /var/tmp/user.ldif
        rm -rf /var/tmp/user.ldif
else
        echo "The passwords provided doesn't match. Please try again"
fi
}

while :; do
    case $1 in
        -h|-\?|--help)
            show_help    # Display a usage synopsis.
            exit
            ;;
        -u|--user)       # Takes an option argument; ensure it has been specified.
            if [ "$2" ]; then
                user_name=$2
                modify_password $user_name
                shift
            else
                die 'ERROR: "--user or -u" requires a non-empty argument with user name'
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
            ;;
         *)
            show_help
            break
            ;;
    esac
    shift
done
