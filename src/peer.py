#!/usr/bin/python

import rpyc
import sys
import socket
import struct
import threading
import time
import traceback
import json



class Peer(rpyc.Service):

	def exposed_buy(self,peerId):
		print('buy')


if __name__ == "__main__":
	with open('config.json', 'r') as f:
		config = json.load(f)

	numpeers = config['N']
	
	peerid = int(sys.argv[1])
	myipport = config["IPS"][str(peerid)]
	myip = myipport.split(":")[0]
	myport = int(myipport.split(":")[1])
	if peerid == 1:
		server = rpyc.utils.server.ThreadedServer(Peer,hostname = myip, port = myport)
		server.start()
		
	elif peerid < numpeers:
		ipport = config["IPS"][str(peerid - 1)]
		ip = ipport.split(":")[0]
		portnum = int(ipport.split(":")[1])
		c = rpyc.connect(ip, portnum,service = Peer)
		server = rpyc.utils.server.ThreadedServer(Peer,hostname = myip, port = myport)
		server.start()
	elif peerid == numpeers:
		ipport = config["IPS"][str(peerid - 1)]
		ip1 = ipport.split(":")[0]
		portnum1 = int(ipport.split(":")[1])
		
		ipport = config["IPS"][str(peerid - 1)]
		ip2 = ipport.split(":")[0]
		portnum2 = int(ipport.split(":")[1])

		c1 = rpyc.connect(ip1, portnum1,service = Peer)
		c2 = rpyc.connect(ip2, portnum2,service = Peer)
	else:
		print('Invalid peerid')
	
	print('Hello')

# def makesocket(port):
# 	s = socket.socket( socket.AF_INET, socket.SOCK_STREAM )
# 	s.setsockopt( socket.SOL_SOCKET, socket.SO_REUSEADDR, 1 )
# 	s.bind( ('', port ) )
# 	return s

# if __name__ == "__main__":
# 	port1 = int(sys.argv[1])
# 	# port2 = int(sys.argv[3])
# 	# s = socket.socket( socket.AF_INET, socket.SOCK_STREAM )
# 	# s.setsockopt( socket.SOL_SOCKET, socket.SO_REUSEADDR, 1 )
# 	# s.bind( ('', port ) )
# 	s = makesocket(port1)
# 	try:
# 		# s2 = makesocket(port)
# 		s.connect(('localhost',int(sys.argv[2])))
# 	except:# socket.timeout:
# 		print('No peer found at specified address')
# 	s.listen(10)
# 	print('here')
# 	while(1):
# 		print('here2')
# 		clientsock, clientaddr = s.accept()
# 		print(s.getsockname()[1])
# 		print(clientsock.getsockname()[1])
# 		print('here3')
# 		clientsock.settimeout(None)
# 		print('Connected ' + str(clientsock.getpeername()))