# Distributed Content Searching
A simple overlay-based solution that allows a set of nodes to share content (e.g., music files) among each other. A set of nodes are connected via an overlay topology. Each node has a set of files that it is willing to share with other nodes. Suppose node ​`x` is interested in a file ​`f`.​ `​x` issues a search query to the overlay to locate at least one node ​`y` containing that particular file. Once the node is identified, the file `f` can be exchanged between ​`x` and `​y`​.

## Running the System
### Step 1
Build the project using `mvn clean install` at the root directory.

### Step 2
Run the Jar `BootstrapServer-1.0-SNAPSHOT.jar` created inside the `BootstrapServer/target` to start the Bootstrap server.
```bash
java -jar BootstrapServer-1.0-SNAPSHOT.jar
```
### Step 3
Run the Jar `NodeServer-1.0-SNAPSHOT.jar` created inside the `NodeServer/target` to start the server nodes by running the following command providing the `port`, `bootstrap_ip_address` and `bootstrap_port` for each node as arguments.
```sh
java -jar NodeServer-1.0-SNAPSHOT.jar <node_port> <bootstrap_ip_address> <bootstrap_port>
```
**Note:** Recommened number of server nodes required for effective functioning of the system is around 10.

**Example:**

Node 1:
```bash
java -jar NodeServer-1.0-SNAPSHOT.jar 8081 localhost 55555
```
Node 2:
```bash
java -jar NodeServer-1.0-SNAPSHOT.jar 8082 localhost 55555
```
Node 3:
```bash
java -jar NodeServer-1.0-SNAPSHOT.jar 8083 localhost 55555
```
...

### Step 4 - User Interface
Start up the user interface by running the following commands in the `user-interface`
directory.
```sh
npm install
```
```sh
npm start
```
The user interface could be accessed using http://localhost:3000/.

Here you can type a search word for the file search and download the file using the file link if the file is available.

You can also monitor the status of the distributed network using http://localhost:3000/node-distribution. 

### Step 5 - Direct UDP

Alternative to the user interface, you can also perform the file search operation using a UDP request by following the steps given below.

Establish a netcat connection using the following command.
```bash
nc -u <ip_address> <port>
```
**Example:**
```bash
nc -u 127.0.0.1 8082
```
Send the file search request using the following command.
```bash
<length> SER <ip_address> <port> 0 <search_word>
```
**Example:**
```bash
0034 SER localhost 8081 0 TINTIN
```

## Developer Testing
1. Start the Bootstrap Server by running the `BootstrapServer` module.
2. Start the Client Nodes by running the `mrt.cse.msc.dc.cybertronez.test.StartServers` class in the `NodeServer` module.
3. Wait for the console output `Done...`.
4. To test a server node, run the `mrt.cse.msc.dc.cybertronez.test.TestClient` class in the `NodeServer` module.
5. To test the file search, run the `mrt.cse.msc.dc.cybertronez.test.TestSearch` class in the `NodeServer` module.
