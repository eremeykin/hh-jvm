set -x
recd="600" # recording duration
rdel="10" # recording delay
ddel="120" # dump delay

if [[ $* == *--clear* ]]
	then
		echo "clear only"
		rm ./*.jfr
		rm ./*.plg
		rm ./*.hprof

		mvn clean install
		if [[ $? -eq 0 ]]
			then
				echo "maven install ok"
		else
			echo "maven failed!"
			exit 1
		fi			

		exit 0
fi

lsofline=`lsof -i:8080 | grep java --color=never`
lsof_regex="java\s*(\S*)"
if [[ $lsofline =~ $lsof_regex ]]
	then
		oldjpid="${BASH_REMATCH[1]}"
		echo "oldjpid = $oldjpid"
		kill $oldjpid
		echo "ok, I killed it"	
else
	echo "port 8080 is free"
fi

stage=""
gitline=`git log --oneline --decorate | grep HEAD`
git_regex="tag: ([^,)]*)"
if [[ $gitline =~ $git_regex ]]
	then
		stage="${BASH_REMATCH[1]}"
		echo $stage
else
	echo "cant' determinate stage"
	exit 1
fi

java -XX:+UnlockCommercialFeatures \
	 -XX:+FlightRecorder \
	 -XX:+StartAttachListener \
	 -jar "./target/money-transfer-1.0.0-SNAPSHOT-shaded.jar" &>/tmp/money &
sleep 5
jpid=$!
echo "Java pid = $jpid"
jfrpath="rec_d-${recd}_s-${stage}.jfr"
logpath="log_d-${recd}_s-${stage}.plg"

if [[ $* == *--dump* ]]
	then
		jfrpath="rec_d-${recd}_s-${stage}_dump.jfr"
		logpath="log_d-${recd}_s-${stage}_dump.plg"
fi

echo "JFR path: " `pwd`$jfrpath
echo "start at " `date +%H:%M:%S`
jcmd $jpid JFR.start duration="${recd}s" delay="${rdel}s" filename="$jfrpath" settings="flight-recorder.jfc" &>/tmp/jcmd
python3 "run_tests.py" &>$logpath &
ppid=$!
echo "Python pid = $ppid"

sleep $(expr $rdel + 10)

ps -p $ppid
psres=$?
if [[ psres -eq 0 ]]
	then
		echo "payload ok"
else
	echo "PAYLOAD IS NOT RUNNING!"
	exit 1
fi	


if [[ $* == *--dump* ]]
	then
		count=0
		timepassed=0
		while [ $timepassed -lt  $recd ]
		do 
			count=$(expr $count + 1)
			dumpname="dump_${count}.hprof"
			jmap -dump:format=b,file=$dumpname $jpid
			sleep $ddel
			timepassed=$(expr $timepassed + $ddel)
		done
else
	sleep $recd
fi

sleep 10 # guard interval
kill  $ppid
kill  $jpid
echo "ready"