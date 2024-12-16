#!/bin/sh
nohup java -Dlog4j2.formatMsgNoLookups=true -jar JobComp-Rest.jar 1>/dev/null 2>&1 &