# p2pNetwork
p2p Network using sockets in java, to transfer data

The main project is in Peer,ActivePeer,Server files
the other files are for presenting the network.

The project works in such a way that each peer that connects to the network connects through the server that gives it the address of one of the already connected peers, and from there all the peers interact independently with each other without a server using sockets. And when any peer disconnects, it updates the server.

The project works in such a way that all the peers are connected to each other through linked lists which simulates graph theory. And the communication and information search works through an algorithm similar to the BFS algorithm.

The information that each peer saves is local and is not saved in any DB
