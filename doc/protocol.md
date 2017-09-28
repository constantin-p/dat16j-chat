# Protocol

### Summary

```
Client -> Server                                Server -> Client
----------------                                ----------------
JOIN <user_name>, <server_ip>: <server_port>    J_OK
                                                J_ER <err_code>: <err_msg>
DATA <user_name>: <text>                        DATA <user_name>: <text>
IMAV                                            LIST <user_name1 user_name2 ...>
QUIT
```

### Message description

#### 1. `JOIN <user_name>, <server_ip>: <server_port>` :

From client to server.

Meaning: | Notes:
---------|---------
The user name is given by the user. | Username is max **12 chars** long, only letters, digits, **‘-‘** and **‘_’** allowed.


#### 2. `J_OK` :

From server to client.

Meaning | Notes
--------|---------
The client is accepted. | -



#### 3. `J_ER <err_code>: <err_msg>` :

From server to client.

Meaning | Notes
--------|---------
The client not accepted. | Duplicate username, unknown command, bad command or any other errors.


#### 4. `DATA <user_name>: <text>` :

From client to server.
From server to all clients.

Meaning | Notes
--------|---------
Send a message to the server. | First part of message indicates from which user it is, the colon **‘:‘** indicates where the user message begins. Max **250 characters**.


#### 5. `IMAV` :

From client to server.

Meaning | Notes
--------|---------
Indicates that the client is active. | The client sends this heartbeat every **1 minute**.


#### 6. `QUIT` :

From client to server.

Meaning | Notes
--------|---------
Indicates that the client is closing down and leaving the group. | -


#### 7. `LIST <user_name1 user_name2 ...>` :

From server to client.

Meaning | Notes
--------|---------
List all active usernames. | A list of all active usernames is sent to all clients, each time the server detects a change.
