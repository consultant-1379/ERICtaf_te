#!/bin/bash

Script=`basename "$0"`
show_help()
{
echo -e "\nUsage:\n"
echo -e "-a|--add <USERNAME>"
echo -e "-m|--modify <USERNAME>"
echo -e "-d|--delete <USERNAME>"
echo -e "-h|--help"
echo -e "\n"
echo -e "Examples: \n"
echo -e "sh $Script --add user1"
echo -e "sh $Script -a user1"
echo -e "sh $Script --modify user1"
echo -e "./$Script -m user1"
echo -e "./$Script --delete user1"
echo -e "./$Script -d user1"
echo -e "\n"
}



add_user()
{
echo "Adding user to the LDAP...."
cp user_template_add.txt /var/tmp/user.ldif
if [ $? != 0 ];then
   echo "Copying the template of User ldif has failed"
   return 1
fi
if [ -f /var/tmp/userid_temp ]
then
                last_userid=`cat /var/tmp/userid_temp`
                Userid=$(($last_userid+1))
                sed -i "s/UID_NUMBER/$Userid/g" /var/tmp/user.ldif
                sed -i "s/GID_NUMBER/$Userid/g" /var/tmp/user.ldif
                echo $Userid > /var/tmp/userid_temp
else
                Userid=1002
                sed -i "s/UID_NUMBER/$Userid/g" /var/tmp/user.ldif
                sed -i "s/GID_NUMBER/$Userid/g" /var/tmp/user.ldif
                echo $Userid > /var/tmp/userid_temp
fi
sed -i "s/USER_NAME/$1/g" /var/tmp/user.ldif
echo -e "Enter the password for the user you would like to add\n"
printf "Password: "
read -s password1
printf "\n"
printf "Enter the password again to confirm: "
read -s password2
printf "\n"
if [ $password1 == $password2 ]
then
        sed -i "s/USER_PASSWORD/$password1/g" /var/tmp/user.ldif
        sed -i "s/TYPE/add/g" /var/tmp/user.ldif
        echo "Thank you for entering the password"
        echo "Loading the provided User credentials in the LDAP server...."
        ldapadd -h localhost -p 3689 -x -c -D 'cn=admin,dc=agat,dc=enm,dc=org' -w adminpassword -f /var/tmp/user.ldif
        rm -rf /var/tmp/user.ldif
else
        echo "The passwords provided doesn't match. Please try again"
fi
}





delete_user()
{
while true; do
    read -p "Do you really want to delete the user $1 yes/no(Y/N)?" yn
    case $yn in
        [Yy]* ) echo -e "Deleting the User: $1\n";
                ldapdelete -h localhost -p 3689 -x -c -D 'cn=admin,dc=agat,dc=enm,dc=org' -w adminpassword "uid=$1,ou=People,dc=agat,dc=enm,dc=org";
                if [ $? != 0 ];then
                        echo "Deletion of the User: $1 unsuccessful"
                else
                        echo "Deletion of the User: $1 successful"
                fi
                break;;
        [Nn]* ) exit;;
        [yes]* ) echo -e "Deleting the User: $1\n";
                ldapdelete -h localhost -p 3689 -x -c -D 'cn=admin,dc=agat,dc=enm,dc=org' -w adminpassword "uid=$1,ou=People,dc=agat,dc=enm,dc=org";
                if [ $? != 0 ];then
                        echo "Deletion of the User: $1 unsuccessful"
                else
                        echo "Deletion of the User: $1 successful"
                fi
                break;;
        [no]* ) exit;;
        * ) echo "Please answer yes/no(Y/N/y/n)";;
    esac
done
}






modify_user()
{
echo "Resetting password for the LDAP user...."
cp -rf user_template_modify.txt /var/tmp/user.ldif
if [ $? != 0 ];then
   echo "Copying the template of User ldif has failed."
   echo "Please make sure user_template_modify.txt file is present in current path"
   return 1
fi
sed -i "s/USER_NAME/$1/g" /var/tmp/user.ldif
echo -e "Enter new password: \n"
printf "Password: "
read -s password1
printf "\n"
printf "Confirm new password:  "
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



current_user=`whoami`
if [ $current_user != "root" ];then
        echo -e "Permission Denied: Script can be run only by \"root\" user"
        exit 1
fi
while :; do
    case $1 in
        -h|-\?|--help)
            show_help    # Display a usage synopsis.
            exit
            ;;
        -a|--add)       # Takes an option argument; ensure it has been specified.
            if [ "$2" ]; then
                user_name=$2
                add_user $user_name
                shift
            else
                die 'ERROR: "--add" requires a non-empty argument with user name'
            fi
            break
            ;;
        -d|--delete)       # Takes an option argument; ensure it has been specified.
            if [ "$2" ]; then
                user_name=$2
                delete_user $user_name
                shift
            else
                die 'ERROR: "--delete" requires a non-empty argument with user name'
            fi
            break
            ;;
        -m|--modify)       # Takes an option argument; ensure it has been specified.
            if [ "$2" ]; then
                user_name=$2
                modify_user $user_name
                shift
            else
                die 'ERROR: "--modify" requires a non-empty argument with user name'
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
