# Hangman - DAI Practical Work 2

### Gianni Cecchetto and Nathan Tschantz

This project is a hangman game which can be played in multiplayer. It uses `picocli` to make a command line interface with which you can start a server or a client. When a server is created, you can then connect to it with a client to play the game with your friend!

## Hangman protocol

You can read the [protocol](./Protocol.md) of our application Hangman to learn how we send data between a client and a server.

## Installation and Setup

### Prerequisites

Ensure you have Java temurin 21 installed on your setup.

### Build the Project

1. Clone the repository:

```sh
git clone git@github.com:GianniCecchetto/dai-practical-work-2-hangman.git
cd dai-practical-work-2-hangman
```

2. Build the project using Maven:

```sh
cd hangman
./mvnw dependency:resolve clean install package
```

This will download dependencies, compile the code, and create a JAR file in the target/ directory.

### Run the app as server

You can use the following command to start a server when you are in the base folder of the repository:

```sh
java -jar hangman/target/hangman-1.0-SNAPSHOT.jar server [-p=<port>]
```

Where the optional parameter `<port>` is the port on which the server will listen (1902 by default).

### Run the app as client

```sh
java -jar hangman/target/hangman-1.0-SNAPSHOT.jar client -H=<host> [-p=<port>]
```

Where `<host>` is the IP address of the server.

## Docker

### Publish

We published our docker application on GitHub Container Registry.

To do this, we renamed our docker imaged with the command `docker tag hangman ghcr.io/giannicecchetto/hangman:latest`.  

We then use the command `docker push ghcr.io/giannicecchetto/hangman:latest` to publish the image on GitHub Container Registry.

### Pull

You can check the container using this [link](https://github.com/users/GianniCecchetto/packages/container/package/hangman).  
If you want to pull the image, you just have to use the commandd `docker pull ghcr.io/giannicecchetto/hangman:latest`.

### Build

To build the app with the docker image, you can run the command `docker build -t hangman .` while you are in the folder: `dai-practical-work-2-hangman`.

### Run

> [!IMPORTANT]  
> The docker file is made to use port **1902**, by modifying the port when launching the game, the port wouldn't be exposed by docker, which results in the game not functionning.

To run the app, you can run the commad `docker run hangman`. By running this command, the application will run by default as a **server** using port **1902**.  
If you want to start a **server** with another port, use the command `docker run hangman server -p=<port>`. You can specify the option `-p=<port>` where `<port>` is the port number you want to use.

If you want to start a **client**, use the command `docker run hangman client -H=<host>` where `<host>` is the IP address of the **server**. You can also use a different port if needed by specifying the option `-p=<port>` just like the server.

#### Launch a server

You can run the app using this command if you pulled it from GitHub Container Registry:
```sh
docker run ghcr.io/giannicecchetto/hangman:latest server [-p=<port>]
```
or with this command if you've built using the `Dockerfile`:
```sh
docker run hangman server [-p=<port>]
```

#### Launch a client

You can run the app using this command if you pulled it from GitHub Container Registry:
```sh
docker run -i ghcr.io/giannicecchetto/hangman:latest client -H=<host> [-p=<port>]
```
or with this command if you've built using the `Dockerfile`:
```sh
docker run -i hangman client -H=<host> [-p=<port>]
```

#### Command options

* `-p, --port` : (Optional, default is 1902). Choose on which port the server listen, or the port to which the client will connect.
* `-H, --host` : Address IP which the client will use to connect to the server.

#### Example commands

1. Launch a server with default port (1902):
```sh
docker run hangman server
```

2. Start a client which connect to a server with IP address 192.168.0.40 and port 1902:
```sh
docker run hangman client -H=192.168.0.40
```

## UI Example

### Connecting to server

```sh
docker run --rm -i hangman client -H=172.17.0.2
[Client] Connecting to 172.17.0.2:1902...
[Client] Connected to 172.17.0.2:1902
CMD >
```

### Joining a game

```sh
CMD > join gege 0
=====HANGMAN=====
--------
Current room: 0
Current username: gege
--------


Opponent:
====)------------- * -------------(====

====)------------- * -------------(====

  +---+
  |   |
      |
      |
      |
      |
=========

Lives: 6
PROGRESS : null
CMD >
```

### Guess a letter

```sh
CMD > guess a
=====HANGMAN=====
--------
Current room: 0
Current username: gege
--------


Opponent:
====)------------- * -------------(====

====)------------- * -------------(====

  +---+
  |   |
      |
      |
      |
      |
=========

Lives: 6
PROGRESS : _A_A_A
CMD >
```

### Guess a word

```sh
CMD > guess banana
=====HANGMAN=====
--------
Current room: 0
Current username: gege
--------


Opponent:
====)------------- * -------------(====

====)------------- * -------------(====

You found the word
  +---+
  |   |
      |
      |
      |
      |
=========

Lives: 6
PROGRESS : banana
CMD >
```

### Leave a game

```sh
CMD > leave
CMD >
```

### Quitting a game

```sh
CMD > quit
Exiting the client. Goodbye!
Connexion to server lost : Socket closed
```

### Help menu

```sh
CMD > help
Usage:
  JOIN <name> <game_id> - Join the game with the id sent with a name.
  LISTGAMES - List all accessible games.
  GUESS <guess> - Submit the character or word you want to guess.
  LEAVE - Leave the current game.
  QUIT - Quit the client completely.
  HELP - Display this help message.
CMD >
```
