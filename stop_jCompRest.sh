#!/bin/sh
kill $(cat ./bin/shutdown.pid)
ps -ef | grep JComp