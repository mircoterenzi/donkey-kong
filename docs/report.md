# Project title

- [Foschi Giacomo](mailto:giacomo.foschi3@studio.unibo.it)
- [Terenzi Mirco](mailto:mirco.terenzi@studio.unibo.it)

### AI Disclaimer @TODO

```
"During the preparation of this work, the author(s) used [NAME TOOL /
SERVICE] to [REASON].
After using this tool/service, the author(s) reviewed and edited the
content as needed and take(s) full responsibility for the content of the
final report/artifact."
```

## Abstract @TODO

Brief description of the project, its goals, and its achievements.

## Concept

Distributed Donkey Kong is a real-time multiplayer game in which two players compete against each other to complete
a platforming level, drawing inspiration from the classic arcade
game [Donkey Kong (1981)](https://en.wikipedia.org/wiki/Donkey_Kong_(1981_video_game)). The developed product is a
desktop application that enables users to connect online and play against each other in real time. Players race to reach
the top of the level while avoiding obstacles, and the first player to do so is declared the winner.

### Use case collection

Users are geographically distributed and connect via a personal computer (PC, Mac or Linux). They interact with the
system via the GUI during setup or keyboard controls during the game. A game session is expected to last under two
minutes, during which time there will be continuous, high-frequency interaction as users send input commands to control
their characters in real time.

As the game does not require any persistent data, only temporary data for the duration of the match, the system does not
need to store any user data on a server or database. However, a large amount of data will be exchanged between players
during a game session, including information on player positions, updates on the game state, and input commands.

The system will comprise three main roles: host, client and spectator. The host is the player who creates the game
session, while the client is the player who joins it. Spectators are users who can watch the game session without
participating.

## Requirements

### Glossary

| Term   | Definition                                                                                          |
|:-------|:----------------------------------------------------------------------------------------------------|
| Barrel | A dynamic obstacle that deals damage when a player comes into contact with it.                      |
| Ladder | A climbable object in the game level that enables players to move vertically regardless of gravity. |

### Functional requirements

| ID  | Requirement                                                                                                                                                    | Acceptance Criteria |
|:----|:---------------------------------------------------------------------------------------------------------------------------------------------------------------|:--------------------|
| FR1 | The system must enable users to start a multiplayer session either by hosting or by connecting to an active game via a network address.                        | @TODO               |
| FR2 | The system must enable real-time, simultaneous control of two competing players (e.g., horizontal movement, jumping and climbing ladders).                     |                     |
| FR3 | The system must allow additional users to join an active session as spectators, providing them with a view, but preventing any interactive input.              |                     |
| FR4 | As long as the host remains online, the system must allow a disconnected player to attempt to rejoin the active session.                                       |                     |
| FR5 | The system must spawn and manage barrels, determining if a player contacts a barrel registering a lost life. If a player loses all lives, they are eliminated. |                     |
| FR6 | The system must track each player's progress, announce the winner when they complete a level, notify all connected users of the outcome, and end the session.  |                     |
| FR7 | The system must apply the same gravity model consistently to all players and dynamic entities, except when climbing ladders.                                   |                     |

### Non-functional requirements

| ID   | Requirement                                                                                                                                           | Acceptance Criteria                                                                                                                        |
|:-----|:------------------------------------------------------------------------------------------------------------------------------------------------------|:-------------------------------------------------------------------------------------------------------------------------------------------|
| NFR1 | The system must minimize the visual delay (input lag) between a player pressing a control key and the character beginning the action on their screen. | The visual delay between pressing a control key and the start of the character's movement must be below 50ms.                              |
| NFR2 | The system must enforce state consistency across all players, ensuring that the Host resolves and prevents critical discrepancies.                    | In a test scenario where network delay causes conflicting results, the host's determination of the winner is accepted by the other player. |
| NFR3 | The application must maintain a consistent and fully functional game experience regardless of the host operating system (Windows, macOS, or Linux).   | The game successfully builds and runs on all three target operating systems.                                                               |

## Design

This chapter explains the strategies used to meet the requirements identified in the analysis.
Ideally, the design should be the same, regardless of the technological choices made during the implementation phase.

> You can re-order the sections as you prefer, but all the sections must be present in the end

### Architecture

- Which architectural style?
    + why?

### Infrastructure

- are there _infrastructural components_ that need to be introduced? _how many_?
    * e.g. _clients_, _servers_, _load balancers_, _caches_, _databases_, _message brokers_, _queues_, _workers_, _proxies_, _firewalls_, _CDNs_, _etc._

- how do components	_distribute_ over the network? _where_?
    * e.g. do servers / brokers / databases / etc. sit on the same machine? on the same network? on the same datacenter? on the same continent?

- how do components _find_ each other?
    * how to _name_ components?
    * e.g. DNS, _service discovery_, _load balancing_, _etc._

> Component diagrams are welcome here

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
