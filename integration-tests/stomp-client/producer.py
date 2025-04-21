import json
import stomp
import time
import os

connection = stomp.Connection([(os.environ['STOMP_BROKER_HOSTNAME'], int(os.environ['STOMP_BROKER_PORT']))])
connection.connect(os.environ['STOMP_BROKER_USERNAME'], os.environ['STOMP_BROKER_PASSWORD'], wait=True)


for i in range(int(os.environ["NUM_MESSAGES"])):
    message = {
        "Hello" : "World",
        "Count" : i,
        "info" : {
            "weather" : 24.0,
            "coffee" : "good",
            "status" : 42
        }
    }
    connection.send(body=json.dumps(message), destination='/topic/hello')
    time.sleep(1)