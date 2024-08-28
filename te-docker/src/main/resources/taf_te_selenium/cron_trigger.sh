#!/usr/bin/env bash
touch cronCheck.txt
TIMESTAMP=$(date +'%Y-%m-%d %H:%M:%S')
echo "[INFO] Cron has been triggered at: \"${TIMESTAMP}\" " >> cronCheck.txt
dataSelNode=$(docker ps | grep selnode | wc -l)
echo "[INFO] Count of selnode containers: \"${dataSelNode}\" "
          if [[ ${dataSelNode} -gt 1 ]] ;then
              for (( i = 1; i <= ${dataSelNode}; i++ ))
              do
                  echo "[INFO] Working on the container:selnode${i} " >> cronCheck.txt
                  docker cp delete_dir.sh selnode${i}:/root/
                  docker exec selnode${i} /bin/sh -c "./root/delete_dir.sh -P /tmp"
              done
          else
          echo "[INFO] Working on the container:selnode " >> cronCheck.txt
          docker cp delete_dir.sh selnode:/root/
          docker exec selnode /bin/sh -c "./root/delete_dir.sh -P /tmp"
          fi

dataSikuliNode=$(docker ps | grep sikulinode | wc -l)
echo "[INFO] Count of sikulinode containers: \"${dataSikuliNode}\" "
          if [[ ${dataSikuliNode} -gt 1 ]] ;then
              for (( i = 1; i <= ${dataSikuliNode}; i++ ))
              do
                  echo "[INFO] Working on the container:sikulinode${i} " >> cronCheck.txt
                  docker cp delete_dir.sh sikulinode${i}:/root/
                  docker exec sikulinode${i} /bin/sh -c "./root/delete_dir.sh -P /tmp"
              done
          else
          echo "[INFO] Working on the container:sikulinode " >> cronCheck.txt
          docker cp delete_dir.sh sikulinode:/root/
          docker exec sikulinode /bin/sh -c "./root/delete_dir.sh -P /tmp"
          fi

sleep 30
TIMESTAMP_END=$(date '+%Y-%m-%d %H:%M:%S')
echo "==========  Cron check completed at: '$TIMESTAMP_END' ===========" >> cronCheck.txt
