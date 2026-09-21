# [1.4.0](https://github.com/mircoterenzi/donkey-kong/compare/v1.3.2...v1.4.0) (2026-09-21)


### Bug Fixes

* **network:** corrected player lives message in StateReceiverSystem ([a6739ee](https://github.com/mircoterenzi/donkey-kong/commit/a6739ee5ea61201fb5e0b821df7a8fdb56b7290c))
* removed pr title check ([6a54882](https://github.com/mircoterenzi/donkey-kong/commit/6a54882f9b08e7bc78313323339f2ef3bee735e6))
* **ui:** prevent null clientDeploymentId from causing UI disconnection issues ([7aa96c7](https://github.com/mircoterenzi/donkey-kong/commit/7aa96c7433573901f75276780ed0a41af8f9c8b2))


### Features

* **ci:** add ci check for build ([53c505e](https://github.com/mircoterenzi/donkey-kong/commit/53c505eb213ae5e7a047b96aebfabff4a723e49f))
* **entity:** refactor player and barrel creation for improved readability and maintainability ([ce9d2e0](https://github.com/mircoterenzi/donkey-kong/commit/ce9d2e01564a6a8cca17045a45327fece97b4720))
* **game:** implement barrel destruction handling and update health system callbacks ([1e679d7](https://github.com/mircoterenzi/donkey-kong/commit/1e679d77e32d6e0dc60721c499f8ebeb3c8682b0))
* **game:** implement guest reconnection handling and state restoration ([83033e6](https://github.com/mircoterenzi/donkey-kong/commit/83033e682a6278a22f2cc60abf366be6d1901f23))
* **game:** refactor entity creation and enhance health management with death callbacks ([029a364](https://github.com/mircoterenzi/donkey-kong/commit/029a364598708c28fe31e1fdd54404fceb266a49))
* **health:** integrate health component into player updates and rendering ([b0b9ea4](https://github.com/mircoterenzi/donkey-kong/commit/b0b9ea4a468e5f3c097d048d17b6af418ca6de0b))
* implement game hosting and joining functionality with Vert.x ([21ff920](https://github.com/mircoterenzi/donkey-kong/commit/21ff920be667f8eb9956ee55df27a8ba199bfbf5))
* **network:** add ClientVerticle and StateReceiverSystem for multiplayer game state management ([de218a9](https://github.com/mircoterenzi/donkey-kong/commit/de218a91355c1bc13759af000935dcfde5af0163))
* **network:** add createNetworkBarrel method and update StateReceiverSystem for barrel creation ([25b00f5](https://github.com/mircoterenzi/donkey-kong/commit/25b00f5c015d07f5a46f76a6ab0daaaef7de4d3c))
* **network:** add GoalReachedMessage and update MessageType for game events ([fc526fd](https://github.com/mircoterenzi/donkey-kong/commit/fc526fd31ba4c2b28c1879e846cd87305b09edff))
* **network:** add message types and data structures for game events ([f1ddb7d](https://github.com/mircoterenzi/donkey-kong/commit/f1ddb7d5fa582de81cbef339706876ba666c8561))
* **network:** enhance guest connection handling and implement reconnection logic ([b8235ee](https://github.com/mircoterenzi/donkey-kong/commit/b8235ee91f7f24f5b52978aa2164c2a363781210))
* **network:** implement LobbyVerticle for multiplayer game management ([c3dda71](https://github.com/mircoterenzi/donkey-kong/commit/c3dda71eb38b130865cf8f88e2091527212327dc))
* **network:** integrate LobbyVerticle and ClientVerticle for game hosting and joining ([bbce19c](https://github.com/mircoterenzi/donkey-kong/commit/bbce19c4ddc37080bc73a1ae5802bb207e25926f))
* **network:** update ClientVerticle and LobbyVerticle for role-based WebSocket connections ([1112dfa](https://github.com/mircoterenzi/donkey-kong/commit/1112dfa2345d5880e499091a77c2947f037a141a))
* **test:** added test for multiplayer connection ([ffacb56](https://github.com/mircoterenzi/donkey-kong/commit/ffacb56ea73f422c5f8188d5e94c1f275c5171b9))
* **ui:** add IP input field for host connection in game menu ([6de6a79](https://github.com/mircoterenzi/donkey-kong/commit/6de6a79ffa7cfb51f84a2bbd03448a863a6dae07))
* **ui:** enhance game loop management and add game over event handling ([ccfed12](https://github.com/mircoterenzi/donkey-kong/commit/ccfed12123696451b1151e9afe43b7ff5330a8d8))
* **ui:** enhance game over handling and improve network listener setup ([54ed7ae](https://github.com/mircoterenzi/donkey-kong/commit/54ed7aed3edd642b375263b2614db90681b63d98))
* **ui:** improve countdown text rendering and alignment ([3b11f58](https://github.com/mircoterenzi/donkey-kong/commit/3b11f5889ca292421863064b898fd553951afd96))
* **ui:** reorganize system initialization for improved game state management ([8edbdf1](https://github.com/mircoterenzi/donkey-kong/commit/8edbdf102e8d0373faedfe4c687689697edda8ef))
* **ui:** update countdown timer styling ([4f9576c](https://github.com/mircoterenzi/donkey-kong/commit/4f9576c5e2661ee15f2cba1e8d1e349ae601ddce))

## [1.3.2](https://github.com/mircoterenzi/donkey-kong/compare/v1.3.1...v1.3.2) (2026-08-27)


### Bug Fixes

* **dependabot:** use pull_request_target to access secrets ([3e23a4d](https://github.com/mircoterenzi/donkey-kong/commit/3e23a4da6b5aee9aa307367723361f067a5a5b68))

## [1.3.1](https://github.com/mircoterenzi/donkey-kong/compare/v1.3.0...v1.3.1) (2026-08-27)


### Bug Fixes

* **dependabot:** use PAT_TOKEN to trigger release workflow ([ac96d2f](https://github.com/mircoterenzi/donkey-kong/commit/ac96d2f4102c7e7495401794da6ab6b0030cc5dc))

# [1.3.0](https://github.com/mircoterenzi/donkey-kong/compare/v1.2.0...v1.3.0) (2026-08-27)


### Bug Fixes

* **dependabot:** remove approval step that fails with GitHub Actions token ([ed2ddf1](https://github.com/mircoterenzi/donkey-kong/commit/ed2ddf1338749bed402d7a53c071c723e969a16f))
* **dependabot:** wait for tests to pass before enabling auto-merge ([5050406](https://github.com/mircoterenzi/donkey-kong/commit/50504061906c44cf7163fbf99f268ff76feccc74))


### Features

* **dependabot:** use PAT token to trigger semantic-release after auto-merge ([ff115a6](https://github.com/mircoterenzi/donkey-kong/commit/ff115a61217446c729f72ef33e946cfc1890121c))

## [1.2.1](https://github.com/mircoterenzi/donkey-kong/compare/v1.2.0...v1.2.1) (2026-08-27)


### Bug Fixes

* **dependabot:** remove approval step that fails with GitHub Actions token ([ed2ddf1](https://github.com/mircoterenzi/donkey-kong/commit/ed2ddf1338749bed402d7a53c071c723e969a16f))

# [1.2.0](https://github.com/mircoterenzi/donkey-kong/compare/v1.1.0...v1.2.0) (2026-08-27)


### Features

* **dependabot:** add auto-merge workflow for Dependabot pull requests ([2f89323](https://github.com/mircoterenzi/donkey-kong/commit/2f89323abcd9d3351b609fc31d77383554d63644))

# [1.1.0](https://github.com/mircoterenzi/donkey-kong/compare/v1.0.1...v1.1.0) (2026-08-26)


### Features

* **game:** implement win condition and game loop in DonkeyKongRushUI ([b301a72](https://github.com/mircoterenzi/donkey-kong/commit/b301a72fa8192300e1e6388301336df90d002598))

## [1.0.1](https://github.com/mircoterenzi/donkey-kong/compare/v1.0.0...v1.0.1) (2026-01-20)


### Bug Fixes

* **ci:** update pull request permissions and release title ([a57634c](https://github.com/mircoterenzi/donkey-kong/commit/a57634c8d318b7c19d345cb999a04c6aeef71e33))
