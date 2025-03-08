import json
import stomp
import time

connection = stomp.Connection([('artemis', 61613)])
connection.connect('artemis', 'artemis', wait=True)

message = {
    "Hello" : "World"
}

while True:
    connection.send(body=json.dumps(message), destination='/topic/hello')
    time.sleep(1)