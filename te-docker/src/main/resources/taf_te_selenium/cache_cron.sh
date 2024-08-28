#!/usr/bin/env bash
echo shroot | sudo -S su - root -c "(crontab -l | grep -v $HOME/cache_clean.sh; ) | crontab - "
echo shroot | sudo -S su - root -c "(crontab -l ; echo \"*/10 * * * * $HOME/cache_clean.sh\"; ) | crontab - "
