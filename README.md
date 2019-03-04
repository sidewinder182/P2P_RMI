# 677 Lab 1

## Configuration File:
Contains the following parameters:<br />
N : Number of nodes in the system(including all buyers and sellers)<br />
Stock : The total stock that each seller is initialized with<br />
hop : The max hop count that each lookup can traverse before being returned to the origin of the lookup<br />
NodeId : The mapping of every NodeId to each peer's IP and port number in the following format - `IP:PortNumber`<br />

A sample configuration file with 6 nodes would look as below:<br />
```
N = 3
Stock = 5
hop = 2
1 = 192.168.0.9:8001
2 = 192.168.0.9:8002
3 = 192.168.0.9:8003
4 = 192.168.0.10:8901
5 = 192.168.0.10:8902
6 = 192.168.0.10:8903
```

## Compilation and setup:
The system is built in java 8 and hence the machine used for testing must have java 8 or higher installed.
Each machine must first compile the source files at the 'lab1-sp/src' location with the command
```
javac Peer.java
javac PeerInterface.java
javac NotReadyException.java
```

The configuration file on every machine must be identical and must be configured with the necessary parameters as mentioned in the configuration file section.
This includes setting the total number of nodes `N` in the system and initializing maximum `hop` count and `Stock`.
Then set the port number and IP for each node.

## Execution:
To start a node run `java Peer #` where `#` is the NodeId of the particular node(a number from 1-N). They can be started in any order but each node must have a unique ID.
