# Donkey Kong: Rush 🦍

*Donkey Kong: Rush* is a distributed, competitive 2D platform game inspired by the classic arcade era. Two players race
through a hazardous level, dodging barrels and climbing ladders to reach Pauline before their opponent. A third user can
join the session as a spectator to watch the match unfold in real-time.

## Key Features

* **Multiplayer:** Two-player platforming featuring lives, physical collisions, jumping, gravity, ladders, barrels, and
  a shared goal.
* **Networking:** Automatic LAN service discovery (UDP broadcast) eliminates the need for manual IP address entry. The
  system seamlessly handles roles assignment and connections.
* **Fault Tolerance:** Includes guest reconnection within a bounded recovery window and session cleanup after
  unrecoverable connection loss. In the event of a network partition, the match favors consistency over independent
  progress to prevent divergent states.
* **Architecture:** Built entirely on an Entity-Component-System (ECS) pattern with comprehensive automated testing.

## Getting Started

To run the game, you will need **Java 17** or newer. You can download the latest game release from
the [GitHub Releases page](https://github.com/mircoterenzi/donkey-kong/releases) and execute the provided JAR file by
double-clicking it or running the following command in your terminal:

```bash
java -jar DonkeyKong-Game.jar
```

Alternatively, you can clone the repository by running:

```bash
git clone https://github.com/mircoterenzi/donkey-kong.git
```

And build the project using Gradle. The repository includes a Gradle wrapper, so you don't need to install Gradle
globally. Based on your operating system, you can use `./gradlew run` (macOS/Linux) or `gradlew.bat run` (Windows) to
build the project.

## How to Play

1. **Main menu:** Upon launch, you have two options:

    - **Play:** Initiates the matchmaking process to either host a new game or join an existing one.
    - **Spectate:** Searches for an active game session to observe without participating.

   Note that, if the lobby is already full (i.e., two players are connected), the system will automatically redirect
   you to the spectate mode, ensuring that you can still enjoy the game as an observer.

2. **In-game controls:** Once the game has started, active players can interact with the environment using the following
   keyboard bindings:
    - **Movement:** `←` / `A` to move left, `→` / `D` to move right.
    - **Climbing:** `↑` / `W` to climb up ladders, `↓` / `S` to climb down.
    - **Jumping:** `spacebar` to jump over obstacles and gaps.

3. **Game objective:** Navigate your avatar from the bottom of the screen to the top to rescue Pauline. Avoid the
   rolling barrels spawned by Donkey Kong. If you collide with a barrel, you will lose a life and respawn at the bottom.
   The game ends when a player reaches Pauline or loses all three lives.

https://github.com/user-attachments/assets/feeb1bd4-61ed-423c-a3e7-045ff74e7d4e

## Architecture & Project Structure

The application operates on a distributed client-server model. The first player to create a session becomes the host,
running the authoritative shared simulation (including the environment and procedural barrel generation). The second
player joins as a guest, controlling their own avatar while syncing with the Host's environment. The game relies on UDP
broadcast for local network discovery and WebSockets over TCP for continuous, low-latency gameplay synchronization,
exchanging lightweight JSON payloads.

```text
.
├── docs/           # Architecture reports, UML, and design diagrams
└── src/
    ├── main/java/it/unibo/donkeykong/
    │   ├── core/       # World context and game bootstrap logic
    │   ├── ecs/        # Data-driven Components, stateless Entities, and Game Systems
    │   ├── network/    # UDP Discovery, TCP sessions, communication protocol, client/server verticles
    │   └── ui/         # JavaFX views, input handling, rendering, and scene management
    └── test/           # Unit and integration test suites
```

## Documentation

For a deep dive into the system's design, refer to
the [project report](https://mircoterenzi.github.io/donkey-kong/report). It details the academic requirements, software
architecture, ECS data-flow, communication protocols, fault-tolerance strategies, and CAP-theorem trade-offs applied
during development.

## Contributing

Contributions, bug reports, and optimizations are welcome. If you wish to contribute to the codebase:

1. Fork the repository and create a new feature branch.
2. Implement your changes, ensuring you follow the existing architectural patterns.
3. Ensure the project formatter is applied to maintain code style by running `./gradlew spotlessApply` in the terminal.
4. Run the test suite to ensure no existing logic is broken using `./gradlew test`.
5. Submit a pull request detailing your changes and the problem they solve.

## Acknowledgements

* The original *Donkey Kong* arcade game was created by Nintendo in 1981, designed by Shigeru Miyamoto. It is a classic
  platformer that has influenced countless games and remains a beloved title in gaming history.
* The sprites, textures, and other graphical assets are adapted from the original arcade releases. Credit goes to
  [The Spriters Resource](https://www.spriters-resource.com/arcade/dk/asset/252263/). They are used solely for
  non-commercial, educational purposes.
* The project was developed as part of a university course on distributed systems, and the design decisions were
  influenced by academic research on networking, fault tolerance, and real-time multiplayer game development.
