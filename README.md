# Distributed Content Searching
A simple overlay-based solution that allows a set of nodes to share content (e.g., music files) among each other. A set of nodes are connected via an overlay topology. Each node has a set of files that it is willing to share with other nodes. Suppose node ​`x` is interested in a file ​`f`.​ `​x` issues a search query to the overlay to locate at least one node ​`y` containing that particular file. Once the node is identified, the file `f` can be exchanged between ​`x` and `​y`​.

## Testing the System Implementation
1. Start the Bootstrap Server by running the `BootstrapServer` module.
2. Start the Client Nodes by running the `mrt.cse.msc.dc.cybertronez.test.StartClients` class in the `NodeServer` module.
3. Wait for the console output `Done...`.
4. To test a client node, run the `mrt.cse.msc.dc.cybertronez.test.TestClient` class in the `NodeServer` module.
5. To test the file search, run the `mrt.cse.msc.dc.cybertronez.test.TestSearch` class in the `NodeServer` module.
