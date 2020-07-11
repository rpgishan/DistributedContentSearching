# Distributed Content Searching
A simple overlay-based solution that allows a set of nodes to share content (e.g., music files) among each other. A set of nodes are connected via an overlay topology. Each node has a set of files that it is willing to share with other nodes. Suppose node ​`x` is interested in a file ​`f`.​ `​x` issues a search query to the overlay to locate at least one node ​`y` containing that particular file. Once the node is identified, the file `f` can be exchanged between ​`x` and `​y`​.

## Testing the System Implementation
1. Start the Bootstrap Server by running the `BootstrapServer` module.
2. Start the Client Nodes by running the `mrt.cse.msc.dc.cybertronez.test.StartClients` class in the `NodeServer` module.
3. Wait for the console output `Done...`.
4. To test a client node, run the `mrt.cse.msc.dc.cybertronez.test.TestClient` class in the `NodeServer` module.
5. To test the file search, run the `mrt.cse.msc.dc.cybertronez.test.TestSearch` class in the `NodeServer` module.

## Running the System
### Step 1
Build the project using `mvn clean install`.
### Step 2
Run the Jar `BootstrapServer-1.0-SNAPSHOT.jar` created inside the `BootstrapServer/tyarget` to start the Bootstrap server.
### Step 3
Start the client nodes by running the following command providing the `ip address`, `port` and `username` for each node as arguments.
```sh
java -jar NodeServer-1.0-SNAPSHOT.jar localhost 8082 sachith
```
### Step 4
To perform a file search operation, establish a netcat connection using the following command.
```sh
nc -u <ip_address> <port>
```
Example:
```sh
nc -u 127.0.0.1 8082
```
### Step 5
Send the file search request using the following command.
```
<length> SER <IP> <port> <file_name> <search_word>
```
Example:
```sh
0034 SER localhost 8081 0 0 TINTIN
```

