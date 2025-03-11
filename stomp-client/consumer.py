import json
import stomp
import time

connection = stomp.Connection([('artemis', 61613)])
connection.connect('artemis', 'artemis', wait=True)
connection.set_listener('test', stomp.PrintingListener())
connection.subscribe("/topic/world", "test-sub")

time.sleep(1000)
