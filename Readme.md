# Project Information
 
* Project Name: <br/>
Factory Protocol
<br>

* Project Team: <br/>
Fadıl Şahin
<br>

* Project Start Date: <br/>
May 2021
<br>

* Project State And Duration: <br/>
Finished, 3 Days
<br>

* Project Description: <br/>
It is supposed to there is a factory and 3 different components in this factory system. To operate all these components together, a simple protocol is designed. Using this TCP protocol, all components, communication and functionalities are written in Java with using spring boot and jpa.

<br/>

# SETUP

To make the project running, there is some steps to do at first.

1. FactoryServer is using a MySQL8 database. In the macine using, there must be an MySQL 8 server and the application must be configured according to this server. If you don't want to deal with the configuration you can just install the MySQL8 database server to the local machine, add a database named `factorydb` and import the [factorydb.sql](factorydb.sql) dump file for this database. If you want to configure, In the [/src/main/resources/application.properties](src/main/resources/application.properties) file, `spring.datasource.*` configs must be appropriate with your database server's jdbc url, username and password.

2. Make sure, in users table in the database, there must be users to connect from [UserClient](src/main/java/socket/myfactory/user/UserClient.java) to [FactoryServer](src/main/java/socket/myfactory/server/MyFactoryServer.java). If you import the dump file, there will be users with username and password which are 'u' & '123', 'u2' & '123'.

3. It is ready to execute now.

<br>

# EXECUTION

To execute with eclipse, you can use Spring Tools Suite (STS) extension. To see all instances on eclipse, open debug tab next to the project explorer tab. If these are not exist, you can add from Window -> Show View in the top toolbar. After instances are created, It is possible to switch all running instances in the debug tab.   

## FactoryServer

For [FactoryServer](src/main/java/socket/myfactory/server/MyFactoryServer.java), at most one running instance can be exist. To create an instance of it, run [/src/main/java/socket/myfactory/ServerMain.java](src/main/java/socket/myfactory/ServerMain.java) file. This server handles all userclient and workcenter messages and uses the database mentioned. To stop the server, you can just write "stop" to the console and enter. It performs operations that users are sent, assign work orders to the workcenters connected.

## UserClient

For [UserClient](src/main/java/socket/myfactory/user/UserClient.java), mulitple running instance can be exist. To create an instance of it, run [/src/main/java/socket/myfactory/user/UserClient.java](src/main/java/socket/myfactory/user/UserClient.java) file. This client sends messages to and receives messages from the factoryserver. While connecting to factoryserver, an username and password record must exist in user tables in the database. At first UserClient must connect to the factoryserver using these username and passwords. To select operations, use the menu in the console that will be printed when connection is successful.

## WorkCenterClient

For [WorkCenterClient](src/main/java/socket/myfactory/workcenter/WorkCenterClient.java), mulitple running instance can be exist. To create an instance of it, run [/src/main/java/socket/myfactory/workcenter/WorkCenterClient.java](src/main/java/socket/myfactory/workcenter/WorkCenterClient.java) file. This client sends messages to and receives messages from the factoryserver. When an instance is created, workcenter name, worktype and speed must be given using the console. After that a connection to the server will be established. Then, it waits for work orders to be assigned itself.

# PROTOCOL DOCUMENT

This section is converted version of the [protocol document](Protocol%20Document/Protocol%20Document.pdf) to the markdown language and definition of the protocol used in communication between components. 

## COMPONENTS

In this protocol, there are three components works in a communication. These are factoryserver, userclient and workcenter. Factoryserver listens 9999 port and receives tcp messages on this port. Clients can listen on any port.

## MESSAGE TYPES

In message contents, parts in brackets are optional. According to situation, they can be empty. Parts in double quotes are constant parts that do not change and written directly to the message. Ellipsis show that repeating part of the message can continue. Other parts must be variable defined like below. Variables can consist of other variables and it will be shown later.

|MESSAGE TYPE|CONTENT|
|:-:|:-:|
|LOGIN|“LOGIN  “USERNAME”  ”PASSWORD”\n”|
|LOG|“LOG\n”|
|NLOG|“NLOG\n”|
|OP|“OP  “OPCODE”\n”[PARAMS”\n”]|
|RESULT|[RESULT-FIELDS]”\n”|
|RESULTSET|[ITEM-FIELDS”  “]”COUNT\n”[RESULT-FIELDS”\n”][RESULT-FIELDS”\n”]…|
|CON|“CON  “NAME”  ”WORKTYPE”  ”SPEED”\n”|
|NEW|“NEW “UNITAMOUNT”\n”|
|DONE|“DONE\n”|
|ERROR|“ERROR\n”|
|ILLEGAL|“ILLEGAL\n”|
|CLOSE|“CLOSE\n”|


## VARIABLES

`USERNAME:` Contains no space and end line characters. It is username of userclient component.

`PASSWORD:` Contains no space and end line characters. It is password of userclient component.

`OPCODE:` It is integer value. It is code of the operation will be performed.

`PARAMS:` Contains no end line character. It is seperated data with spaces is needed for the operation.

`RESULT-FIELDS:` Contains no end line character. It is seperated result objects.

`ITEM-FIELDS:` Contains no end line character. When a result returned for a specific object, seperated object fields with spaces is replaced with this.

`NAME:` Contains no space and end line characters. It is name of the workcenter component.

`WORKTYPE:` Contains no space and end line characters. It is type of the work erformed by workcenter.

`SPEED:` Contains no space and end line characters. It is speed of performing a job of workcenter and integer value.

`ID:` Contains no space and end line characters. It is identification key of objects recorded in factoryserver component and integer value.

`COUNT:` Contains no space and end line characters. It indicates how many result field exists in a result list and integer value.

`STATUS:` Contains no space and end line characters. It is status of workcenter component. It is ‘EMPTY’ or‘BUSY’ values.

`ACTIVEORDERID:` Contains no space and end line characters. It is identification key for assigned job to the workcenter.

`UNITAMOUNT:` Contains no space and end line characters. It is unit amount of a job order.

## LOGIN MESSAGE
A userclient sends this message type to connect a factoryserver. After this operation, userclient is logged in factoryserver component, can perform operations defined in factoryserver and an id key is assigned it.

E.g.

LOGIN user 123

## LOG MESSAGE
Factoryserver inform the user which sends login message about it is logged in successfully and the user was registered.

E.g.

LOG

## NLOG MESSAGE
Factoryserver inform the user which sends login message about the login is unsuccessful and the user was not registered. 

E.g.

NLOG

## OP MESSAGE
A message type to perform an operation in factoryserver and is sended by userclient component. Opcode value is between 1-4 and it is used to select the operation. Params is parameters needed to perform selected operation in factoryserver. 

### OPERATION TYPES
`“OP 1\n”WORKTYPE”\n”`

Operation 1 is used to request workcenter list from factoryserver. WORKTYPE parameter is used to tell factoryserver which types will be in the list and does not contain space and end line characters.

E.g.

OP 1 cut

`“OP 2\n”ID”\n”`

Opreation 2 is used to request workcenter status and jobs finished from factoryserver. ID parameter is used to tell factoryserver which workcenter’s datas is requested and does not contain space and end line characters.

E.g.

OP 2 24

`“OP 3\n”`

Operation 3 is used to request job list seperated (ordered) by type which are not assigned for a workcenter to do.

E.g.

OP 3

`“OP 4\n”WORKTYPE”  “UNITAMOUNT”\n”`

Opreation 4 is used to generate new jobs saved in factoryserver. WORKTYPE  parameter indicates job type and UNITAMOUNT parameter indicates the job includes how many units. New job order is generated with these datas and these parameters do not contain space and end line characters. 

E.g.

OP 4 washing 40

## RESULT MESSAGE
It is the response message sent after userclient op messages that is wanted to be done by factoryserver. It is not a list but one line data. The message sent when operation 2 and 4 message is received.

### OPERATION 2
`“-1\n”`

For the workcenter whose ID is sent, if the workcenter is not found in records, the response message type is RESULT type.

### OPERATION 4
`ID”\n”`

It is used to sent ID value for the generated job.

E.g.

532

## RESULTSET MESSAGE

It is the response message sent after userclient op messages that is wanted to be done by factoryserver. It is a list of result data. The message sent when operation 1,2 and 3 message received.

### OPERATION 1

`COUNT”\n”ID”  “NAME”  “WORKTYPE”  “SPEED”  “STATUS”  “ACTIVEORDERID”\n”…`

There are as many workcenter records as the count value.

E.g.



3

1 wc1 a 2 EMPTY -1

52 wc2 b 1 BUSY 4242

35 wc3 a 1 EMPTY -1

### OPERASYON 2
`ID”  “NAME”  “STATUS”  “COUNT”\n”ID”  “WORKTYPE”  “UNITAMOUNT”\n”…`

Data in the first line is about to the workcenter has id value and sent to the factoryserver in operation 2 message.  In the other lines,  job orders done added to the message after the opration.

EX:

42 wc1 EMPTY 1

452 a 4

### OPERASYON 3
`COUNT”\n”ID”  “WORKTYPE”  “UNITAMOUNT”\n”…`

It lists unassigned job orders up to the COUNT.

EX:

3

1 a 1

4 a 2

6 b 5

## CON MESSAGE

For a workcenter, its name, type and unitamount is given, a connection is made and an ID key is assigned.

E.g.

CON wc1 a 4

## NEW MESSAGE

It is the message type that is sent to a workcenter by factoryserver and tells amount of the job.

E.g.

NEW 935

## DONE MESSAGE
It is the message type that is sent to a factoryserver by workcenter when it has done its job assigned before.

## ERROR MESSAGE

It is the message type that notifies the connection will be closed because of an error occured on factoryserver.

E.g.

ERROR

## ILLEGAL MESSAGE

It is the message that is sent when a message has unsupported syntax and 
unknown and unexpected message data.

E.g.

ILLEGAL

## CLOSE MESSAGE

It is the message type that is sent when a component wants to close the connection.

E.g.

CLOSE

## FINITE STATE MACHINE DIAGRAMS
The diagrams that show the defined protocol explained in this documentation and how all communication is carried out.

![server-fsm](Protocol%20Document/server-fsm.png)

![userclient-fsm](Protocol%20Document/userclient-fsm.png)

![workcenter-fsm](Protocol%20Document/workcenter-fsm.png)
