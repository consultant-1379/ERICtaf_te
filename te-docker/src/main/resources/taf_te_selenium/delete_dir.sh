#!/usr/bin/env bash

readonly  DIR_CHECK="-0"
readonly  CURRENT_PATH="/root"
cd -- "$CURRENT_PATH"

while getopts P: option
do
   case "${option}"
      in
      P) APPLICATION_PATH=${OPTARG};;
    esac
done
readonly  DIR_PATH="${APPLICATION_PATH}"
touch CronLog.txt
echo "[INFO] CHECK-0 Target Directory is: '$DIR_PATH'" >> CronLog.txt

TIMESTAMP=$(date +'%Y-%m-%d %H:%M:%S')
echo "[INFO] CHECK-0 Scanning Started at: \"${TIMESTAMP}\" " >> CronLog.txt

############## f(ONE): Loops in all files in Target directory ######################################
############## Gets the time diff and deletes the file if required #################################

delete_in_process(){
                  TARGET_DIR=$1
                  touch nyk-2k.txt
                  find $TARGET_DIR -printf '%TY-%Tm-%Td %TT %p\n' | sort -nr > nyk-2k.txt
                  while read -r filey; do
                  pathFile=$(echo $filey | cut -d ' ' -f 3);
                  timey=$(echo $filey | cut -d '.' -f 1);
                  last_mod=$(date -d "$timey" '+%s');
                  today=$(date +'%Y-%m-%d %H:%M:%S');
                  time_now=$(date -d "$today" '+%s');
                  diff_time=$(( ($time_now - $last_mod) / (60*60) ));
                  echo "[INFO] CHECK-1 Time Difference for the file $pathFile in HOURS: $diff_time" >> CronLog.txt
                      if [[ ($diff_time -gt 24) && ( $pathFile != $TARGET_DIR) ]] ;then
                      echo "CHECK-1 Deleting: $pathFile" >> CronLog.txt
                      rm --recursive --force $pathFile;
                      else
                      echo "[INFO] CHECK-LAST Target file has been modified recently !!" >> CronLog.txt
                      fi
                  done < nyk-2k.txt
                  rm --recursive --force nyk-2k.txt
}


############## Fetches in all System created Dir's and calls in f(ONE) on every DIR!! #############

fetch_system_dir_all() {
                while read -r route; do
		        		delete_in_process $route
		        		temp_mem=$(ls -A $route)
				        if [ -z "$temp_mem" ]; then
				        echo "[INFO] CHECK-2 DIR: $route is empty!!" >> CronLog.txt
                rm --recursive --force $route;
                fi
		         		done < nyk.txt
}



##############################################################################################
############# ROOT FUNCTION: def_the_process  ------> f(ROOT)               ##################
############# Cleans up all system created dir's (ending in -0) in a path   ##################
##############################################################################################

def_the_process() {
                touch nyk.txt
                find $DIR_PATH -maxdepth 1 -type d -name \*${DIR_CHECK} > nyk.txt
				        if [ ! -s nyk.txt ]; then
                echo "[INFO] CHECK-0 No System created directories (ending in -0) found!!"  >>  CronLog.txt
                else
			        	fetch_system_dir_all
			        	fi
			        	rm --recursive --force nyk.txt
}


def_the_process


END_TIME=$(date +'%Y-%m-%d %H:%M:%S')
echo "[INFO] CHECK-0 Scanning completed : $END_TIME ==============================" >> CronLog.txt
