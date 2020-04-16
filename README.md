# FTP
A Java implementation of the file transfer protocol.
Server can accept connections from multiple clients. Provides three functionalities:

  get(filename) - Client recieves the copy of the file from the server

  dir - List the files in the directory where the server class file is located.
  
  upload(filename) - Client upload one of its own file to the server.
 
A client after a successfull connection can send any of the three commands to server.
