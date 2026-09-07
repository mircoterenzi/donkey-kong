---
layout: default
title: Report
nav_order: 2
---

# Donkey Kong: Rush

- [Foschi Giacomo](mailto:giacomo.foschi3@studio.unibo.it)
- [Terenzi Mirco](mailto:mirco.terenzi@studio.unibo.it)

### AI Disclaimer

During the preparation of this work, the authors used GitHub Copilot and DeepL to assist with writing the Javadoc and to
check the grammar of this report. Moreover, Copilot has been used as a review tool in some pull requests on GitHub.
After using this tool/service, the authors reviewed and edited the content as needed and take(s) full responsibility
for the content of the final report/artifact.

## Abstract

This report presents the "Donkey Kong: Rush" project, realized for the "Distributed System" course at the University of
Bologna. The project involves the development of a 2D multiplayer platform video game inspired by the
classic [Donkey Kong](https://en.wikipedia.org/wiki/Donkey_Kong_(1981_video_game)). The system is designed around an
Entity-Component-System (ECS) architecture written in Java, utilizing JavaFX for rendering. The main focus of the
project is the implementation of smooth multiplayer gameplay. The solution combines the Host's authority over the
deterministic environment with a reactive handling of local inputs, guaranteeing an experience free of blocking lag for
the players.

## 1. Concept

"Donkey Kong: Rush" is a desktop application featuring a graphical user interface (GUI). Specifically, it is a 2D
multiplayer platform video game, which takes inspiration from the
original [Donkey Kong](https://en.wikipedia.org/wiki/Donkey_Kong_(1981_video_game)) arcade game.

### 1.1 Use case description

The software provides a competitive multiplayer platforming experience. Users are located on separate desktop machines
connected over the same local area network. Thus, distribution is a fundamental requirement for this project to enable a
seamless multiplayer experience, as it allows players on physically separate machines across a network to connect,
interact, and share the same virtual space.

Interaction is continuous and in real-time during an active game session. The system captures inputs and exchanges state
updates across the network multiple times per second to maintain synchronization.

Players interact with the system via the GUI during setup or keyboard controls during the game. The game controls
consist of standard keybindings for movement (left/right, climbing ladders) and jumping.

The system does not require persistent, long-term data storage. All necessary data represents the state of the ongoing
match (such as player and barrel coordinates) and is kept strictly in RAM.

The game relies on multiple user roles:

- **Host (Player)**: Actively plays the game, processes their own local input, and acts as the local server. It
  possesses authority over the game environment (e.g., generating barrels) and broadcasts the world state to all
  connected clients.
- **Guest (Player)**: Actively plays the game by processing their own input locally while simultaneously receiving
  continuous updates from the Host regarding the rest of the world state.
- **Spectator**: A purely passive role that does not generate game input, but solely receives updates from the players
  to feed its local rendering system, allowing another user to watch the match in real-time.

### 2. Requirements Elicitation and Analysis

##### Glossary

| Term          | Definition                                                                                                                                                                    |
|---------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Host**      | A player who actively plays the game, processes local input, acts as the authoritative entity for game-world generation (e.g., barrels), and broadcasts the state to clients. |
| **Guest**     | A player who actively plays the game, processes local input, and receives continuous world state updates from the Host.                                                       |
| **Spectator** | A passive user who does not generate input but receives real-time updates to render and watch the match.                                                                      |
| **Lobby**     | The pre-game networking state where users connect and are assigned their respective roles (Host, Guest, or Spectator) before the match begins.                                |
| **Entity**    | Any distinct object in the game world.                                                                                                                                        |
| **Barrel**    | A dynamic entity that deals damage when a player comes into contact with it.                                                                                                  |
| **Ladder**    | A climbable entity in the game level that enables players to move vertically regardless of gravity.                                                                           |

##### Functional Requirements

| Description                                                                                                                                     | Acceptance Criterion                                                                                                                              |
|-------------------------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------|
| The system must allow users to join a lobby and automatically assign them a role (Host, Guest, or Spectator) based on join order or preference. | A user successfully connects to the server and receives a distinct role assignment (Host, Guest, or Spectator) before the game starts.            |
| The system must allow active players to move horizontally (left/right), jump, and climb ladders.                                                | Pressing the designated keys updates the player's position appropriately on the screen according to game physics (gravity, collision).            |
| The system must synchronize the game state (entities positions, and lives) between all connected clients in real time.                          | When the Host moves or a barrel spawns, the Guest and Spectators see the updated positions on their screens without noticeable desynchronization. |
| The system must detect collisions between players and damaging entities (e.g., barrels), deducting a life upon impact.                          | When a player's character intersects with a barrel, the player's life count decreases by 1, and the character respawns at the starting position.  |
| The system must declare a winner if a player reaches the goal (Pauline) or declare a loser if a player's lives reach zero.                      | The game transitions to a "Game Over" screen displaying the correct winner or loser when the goal is touched or 3 lives are lost.                 ||

##### Non-Functional Requirements

These requirements define the behavioral aspects and quality attributes of the system.

| Description                                                                                  | Acceptance Criterion                                                                                                                                                                                                                                                                      |
|----------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| The game must update and render at a consistent frame rate to ensure smooth gameplay.        | The local game loop executes consistently at the target of 60 Frames-Per-Second (FPS).                                                                                                                                                                                                    |
| State updates must be transmitted rapidly to prevent visual stuttering or unfair advantages. | State update payloads are serialized, transmitted over the local network, and deserialized by the receiving client in under 33 milliseconds (roughly 2 frames).                                                                                                                           |
| The system must ensure automatic recovery from disconnections for non-Host users.            | If a Guest or Spectator loses their network connection and subsequently reconnects to the server while the match is still active, the system automatically restores their role and resumes sending them real-time game state updates without requiring a full lobby reset.                |
| Code must be modular and documented to make extensions and changes easy.                     | Modularity is enforced by strictly decoupling data structures from execution logic, avoiding deep and rigid inheritance trees. Maintainability is verified by the presence of comprehensive Javadoc comments on all core API interfaces, ensuring new features can be integrated rapidly. |

### 2.1 Relevant Distributed System Features

* **Performance, concurrency, and communication efficiency:** Real-time games require extremely strict throughput and
  response times. The system must process player inputs, update physics, and broadcast network messages concurrently
  without blocking the main application thread. High communication efficiency is paramount to ensure state updates are
  delivered within the 33ms latency threshold to maintain a fair 60 FPS gameplay experience.

* **Evolvability and maintainability:** To ensure long-term maintainability and ease of updates, the system's
  architecture must strictly decouple game state data from execution logic. The design must allow new game mechanics or
  virtual objects to be added modularly, actively avoiding rigid, monolithic class hierarchies.

* **Fault tolerance, dependability, and availability:** While data integrity for long-term storage is irrelevant since
  all match state is kept in volatile memory, the system must gracefully handle network faults. If a player's connection
  drops, the central server must detect the failure and immediately broadcast a game-over message, resetting the lobby
  to prevent deadlocks and ensure continued availability for future matches.

* **Resource sharing:** The active game world acts as a synchronized shared resource. The Host is responsible for
  managing authoritative game mechanics and synchronizing this shared state with the Guest and Spectators via continuous
  network message broadcasts.

* **Transparency:** The system provides basic *location transparency*; clients seamlessly connect to the lobby without
  needing to know the physical network topology of the other players. However, *failure transparency* is intentionally
  absent; if a connection drops, the failure is explicitly exposed to the remaining users via the game over UI.

* **Scalability:** The game is explicitly bounded to a small, finite number of simultaneous connections (one Host, one
  Guest, and passive Spectators) on a local area network. It is not expected to scale horizontally to thousands of users
  or handle massive data growth over time.

* **Security and trust:** As a local LAN game without persistent user accounts or sensitive data storage, there is no
  need for cryptographic schemes, data encryption, or complex authentication mechanisms.

* **Openness and interoperability:** The project is a closed ecosystem. While it utilizes standardized text formats for
  message passing, it is not required to interact with external third-party systems or heterogeneous technological
  components.

* **Economy and costs:** Developed as an academic project running on local hardware, there are no cloud deployment
  budgets, operational costs, or strict economic constraints driving the architecture.

## Design

This chapter explains the strategies used to meet the requirements identified in the analysis.

### Architecture

The project follows the Model-View-Controller (MVC) and Entity-Component-System (ECS) architectural patterns. The MVC
pattern is used to separate the user interface from the game logic, while the ECS pattern is used to manage the game
entities and their behaviors.

For what concerns the distributed aspect of the project, a client-server architecture is used. The host player act as
both the client and the server, while the other player and any spectators act as clients. The server is responsible for
managing the game state and broadcasting updates to all connected clients.

This architecture was selected for its simplicity, efficiency, low latency, and ability to abstract complexity from the
user. A decentralized peer-to-peer alternative, where each user operates an interconnected server, was evaluated but
rejected due to high implementation complexity and network latency overhead. Its sole advantage, mitigating the single
point of failure if a central leader goes offline, did not justify the operational trade-offs.

### Infrastructure

- are there _infrastructural components_ that need to be introduced? _how many_?
  * e.g. _clients_, _servers_, _load balancers_, _caches_, _databases_, _message brokers_, _queues_, _workers_,
    _proxies_, _firewalls_, _CDNs_, _etc._

- how do components  _distribute_ over the network? _where_?
  * e.g. do servers / brokers / databases / etc. sit on the same machine? on the same network? on the same datacenter?
    on the same continent?

- how do components _find_ each other?
  * how to _name_ components?
  * e.g. DNS, _service discovery_, _load balancing_, _etc._

> Component diagrams are welcome here

- Each player, or spectator, of the game is a client and $N$ clients may join a game session. However, there is a single
  server per game, located on the host's machine.
- Data is not stored persistently, but rather exchanged in real-time between the server and clients, with the game state
  being replicated on each of the $N$ clients.
- A publish-subscribe pattern is used to both broadcast game state updates from the server to all clients and to send
  inputs from clients to the server. This means that each component (clients and server) are both publishers and
  subscribers
- Mutual trust is assumed between the host and the clients, thus, no authentication or authorization mechanisms are
  implemented.

To guarantee a smooth and responsive gaming experience, the game loop is performed on each client. However, since the
server is the authoritative source of truth, it is responsible for resolving any discrepancies in the game state and
periodically broadcasting alignment messages to the clients. This ensures that all clients have a consistent view of
the game state, even in the presence of network latency or packet loss.

### Modelling

- which __domain entities__ are there?
  * e.g. _users_, _products_, _orders_, _etc._

- how do _domain entities_ __map to__ _infrastructural components_?
  * e.g. state of a video game on central server, while inputs/representations on clients
  * e.g. where to store messages in an IM app? for how long?

- which __domain events__ are there?
  * e.g. _user registered_, _product added to cart_, _order placed_, _etc._

- which sorts of __messages__ are exchanged?
  * e.g. _commands_, _events_, _queries_, _etc._

- what information does the __state__ of the system comprehend
  * e.g. _users' data_, _products' data_, _orders' data_, _etc._

> Class diagram are welcome here

### Interaction

- how do components _communicate_? _when_? _what_?
- _which_ __interaction patterns__ do they enact?

> Sequence diagrams are welcome here

### Behaviour

- how does _each_ component __behave__ individually (e.g. in _response_ to _events_ or messages)?
  * some components may be _stateful_, others _stateless_

- which components are in charge of updating the __state__ of the system? _when_? _how_?

> State diagrams are welcome here

### Data and Consistency Issues

- Is there any data that needs to be stored?
  * _what_ data? _where_? _why_?

- how should _persistent data_ be __stored__?
  * e.g. relations, documents, key-value, graph, etc.
  * why?

- Which components perform queries on the database?
  * _when_? _which_ queries? _why_?
  * concurrent read? concurrent write? why?

- Is there any data that needs to be shared between components?
  * _why_? _what_ data?

### Fault-Tolerance

- Is there any form of data __replication__ / federation / sharing?
  * _why_? _how_ does it work?

- Is there any __heart-beating__, __timeout__, __retry mechanism__?
  * _why_? _among_ which components? _how_ does it work?

- Is there any form of __error handling__?
  * _what_ happens when a component fails? _why_? _how_?

### Availability

- Is there any __caching__ mechanism?
  * _where_? _why_?

- Is there any form of __load balancing__?
  * _where_? _why_?

- In case of __network partitioning__, how does the system behave?
  * _why_? _how_?

### Security

- Is there any form of __authentication__?
  * _where_? _why_?

- Is there any form of __authorization__?
  * which sort of _access control_?
  * which sorts of users / _roles_? which _access rights_?

- Are __cryptographic schemas__ being used?
  * e.g. token verification,
  * e.g. data encryption, etc.

---
<!-- Riparti da qui  -->

## Implementation

- which __network protocols__ to use?
  * e.g. UDP, TCP, HTTP, WebSockets, gRPC, XMPP, AMQP, MQTT, etc.
- how should _in-transit data_ be __represented__?
  * e.g. JSON, XML, YAML, Protocol Buffers, etc.
- how should _databases_ be __queried__?
  * e.g. SQL, NoSQL, etc.
- how should components be _authenticated_?
  * e.g. OAuth, JWT, etc.
- how should components be _authorized_?
  * e.g. RBAC, ABAC, etc.

### Technological details

- any particular _framework_ / _technology_ being exploited goes here

## Validation

### Automatic Testing

- how were individual components **_unit_-test**ed?
- how was communication, interaction, and/or integration among components tested?
- how to **_end-to-end_-test** the system?
  * e.g. production vs. test environment

- for each test specify:
  * rationale of individual tests
  * how were the test automated
  * how to run them
  * which requirement they are testing, if any

> recall that _deployment_ __automation__ is commonly used to _test_ the system in _production-like_ environment

> recall to test corner cases (crashes, errors, etc.)

### Acceptance test

- did you perform any _manual_ testing?
  * what did you test?
  * why wasn't it automatic?

## Release

- how where components organized into _inter-dependant modules_ or just a single monolith?
  * provide a _dependency graph_ if possible

- were modules distributed as a _single archive_ or _multiple ones_?
  * why?

- how were archive versioned?

- were archive _released_ onto some archive repository (e.g. Maven, PyPI, npm, etc.)?
  * how to _install_ them?

## Deployment

- should one install your software from scratch, how to do it?
  * provide instructions
  * provide expected outcomes

## User Guide

- how to use your software?
  * provide instructions
  * provide expected outcomes
  * provide screenshots if possible

## Self-evaluation

- An individual section is required for each member of the group
- Each member must self-evaluate their work, listing the strengths and weaknesses of the product
- Each member must describe their role within the group as objectively as possible.
  It should be noted that each student is only responsible for their own section
