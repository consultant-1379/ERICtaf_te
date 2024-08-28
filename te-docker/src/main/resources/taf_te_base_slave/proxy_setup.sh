#!/bin/sh

if [ -n "$1" ] && [ -n "$2" ]
then
    http_proxy="http://${1}:${2}"
    https_proxy="https://${1}:${2}"

#    echo ${http_proxy} >> /etc/environment
#    echo ${https_proxy} >> /etc/environment

    echo "export http_proxy=${http_proxy}" >> /etc/profile.d/set_proxy.sh
    echo "export https_proxy=${https_proxy}" >> /etc/profile.d/set_proxy.sh
fi

if [ -n "$3" ]
then
    no_proxy_for=$3
#    echo ${no_proxy_for} >> /etc/environment
    echo "export no_proxy=${no_proxy_for}" >> /etc/profile.d/set_proxy.sh
fi