#!/bin/bash
mvn package -q

files=($(find ./integration-tests/ -type f -name "*.yaml"))
for file in "${files[@]}"; do
    filename=${file##*/}
    touch ./integration-tests/logs/${filename}.logs
    docker compose -f $file up --exit-code-from test-service &> ./integration-tests/logs/${filename}.logs
    exitcode=$?
    if [ $exitcode -ne 0 ]; then
        echo -e "Test \033[31m${filename}\033[0m failed! Integration test service exited with code $exitcode. See ./integration-tests/logs/${filename}.logs for details."
    else
        echo -e "Test \033[32m${filename}\033[0m passed!"
    fi
    docker compose -f $file down --volumes &> /dev/null
done



