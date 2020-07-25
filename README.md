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

### Step 4
Start up the user interface by running the following commands in the `user-interface`
directory.
```sh
npm install
```
```sh
npm start
```

### Step 5
The user interface could be accessed using http://localhost:3000/.

Here you can type a search word for the file search and download the file using the file link if the file is available.

You can also monitor the status of the distributed network using http://localhost:3000/node-distribution. 
