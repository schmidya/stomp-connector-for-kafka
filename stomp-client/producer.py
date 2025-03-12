import json
import stomp
import time

connection = stomp.Connection([('artemis', 61613)])
connection.connect('artemis', 'artemis', wait=True)


counter = 0
while True:
    message = {
        "Hello" : "World",
        "Count" : counter,
        "info" : {
            "weather" : 24.0,
            "coffee" : "good",
            "status" : 42
        }
    }
    connection.send(body=json.dumps(message), destination='/topic/hello')
    time.sleep(1)
    counter += 1