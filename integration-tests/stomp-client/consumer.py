import json
import stomp
import time
import sys
import os

class TestListener(stomp.ConnectionListener):

    def __init__(self, connection):
        self.connection = connection
        self.connect()
        self.subscribe()
        self.counter = 0

    def connect(self):
        connection.connect(os.environ['STOMP_BROKER_USERNAME'], os.environ['STOMP_BROKER_PASSWORD'], wait=True)

    def subscribe(self):
        connection.subscribe("/topic/world", "test-sub")

    def on_message(self, frame):
        # data = json.loads(frame.body)
        print(frame.body)
        self.counter += 1

    def on_disconnected(self):
        self.connect()
        self.subscribe()

connection = stomp.Connection([(os.environ['STOMP_BROKER_HOSTNAME'], int(os.environ['STOMP_BROKER_PORT']))])
listener = TestListener(connection)
connection.set_listener('test', listener)

time.sleep(float(os.environ["WAIT_SECONDS"]))
sys.exit(int(listener.counter == 0))
