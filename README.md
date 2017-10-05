# Java chat project

### Protocol

```
Client -> Server                                Server -> Client
----------------                                ----------------
JOIN <user_name>                                J_OK
                                                J_ER <err_code>: <err_msg>
DATA <user_name>: <text>                        DATA <user_name>: <text>
IMAV                                            LIST <user_name1 user_name2 ...>
QUIT
```



## License

This project is MIT licensed.
Please see the LICENSE file for more information.
