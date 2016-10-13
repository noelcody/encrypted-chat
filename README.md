# Summary

Basic encrypted chat between two clients. Uses an SSL-style asymmetric to symmetric key exchange to ensure that messages between clients can't be read by the server.

(Not actually secure...there's no server certificate verification, etc. Just made this to learn basics.)

# Client Handshake

Login, connect, and key exchange is completed as follows:

* Clients A and B log in by sending a screenname to the Server. Server stores screennames in its client directory.
* Clients generate a RSA public/private key pair.
* Client A sends request to connect to Client B.
* Server waits for B to connect and then forwards A's request.
* B receives A's request and responds back with a similar connect request containing B's public key (format `clientBScreenname:clientAScreenname:clientBPublicKey`)
* A generates a symmetric (AES) key, encrypts with B's public key, and sends back to B.
* B decrypts the symmetric key. Clients use the symmetric key to encrypt chat messages.