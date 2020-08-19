# Online-Typing-Test
Play against friends or by your self!

# What is this?
Well this repository includes both the client (Made in Unity) and server side and all you need to do is set it up and send the client to your friends :)

# How to setup the Server
0. Clone the Server branch into your folders and set it up in a IDE or command line
1. You must have Mysql installed already before continuing, there are many tutorials out there for this
2. Create a user for your Mysql
3. Create a database in Mysql, then create three tables (I will provide the SQL here)
  a. The first table: "CREATE TABLE 1PlayerGames (
  Game_ID INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  Player_One_WPM SMALLINT,
  Player_One_Accuracy SMALLINT,
  Player_One_Name VARCHAR(255),
  Game_Difficulty SMALLINT
);"
b. The second table: "CREATE TABLE 2PlayerGames (
  Game_ID INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  Player_One_WPM SMALLINT,
  Player_One_Accuracy SMALLINT,
  Player_One_Name VARCHAR(255),
  Player_Two_WPM SMALLINT,
  Player_Two_Accuracy SMALLINT,
  Player_Two_Name VARCHAR(255),
  Game_Difficulty SMALLINT
);"
c. The third table: "CREATE TABLE accounts (ID INT NOT NULL AUTO_INCREMENT PRIMARY KEY, Name VARCHAR(255));"
4. Go to the MysqlConn.java file and chane the "Username", "Password", and the "Database name" strings to your information so it can connect to your database.
# *OPTIONAL* You can change the port that the server will listen on in the Server.java file. OR you can just leave it at default (5014)

# How to setup the Client
0. Import the Unity folder from the client branch
# *OPTIONAL* If you changed the server port listener, then you must do so as well in this. The port number can be found in the ServerAPI class

# How to run the server
-If you are running the server in an IDE, then it should be as easy as pressing the run button.
-If you are compiling then running in the command line, then you must include the class path of the two files in the src folder
  Running from the Command Line Example: 
  1. Go to the src folder
  2. Type this first if you are in Linux: javac -cp java-json.jar:mysql-connector-java-8.0.21.jar ServerPackage/*.java
  3. Then to run the server in Linux: java -cp .:java-json.jar:mysql-connector-java-8.0.21.jar ServerPackage/Server

# Make sure to port forward
