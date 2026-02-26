# Changelog

## [1.0.0] - 2025-02-25

### Added
- Initial release as **EclipseAC**
- 12 checks: KillAura, Reach, AutoClicker, Flight, Speed, NoFall, Step, Timer, Scaffold, Fastplace, Inventory, BadPackets
- Grace tick engine: separate counters for teleport, damage/knockback, climbable, liquid, jump
- Rolling average Timer check (3-second window)
- Consecutive-threshold system for KillAura and Reach (reduces single-lag-packet false positives)
- AutoClicker standard deviation analysis
- Speed check: ice, soul sand, slabs, stairs, potions, jump grace
- NoFall: server-side fall distance tracking with proper exemptions (water, honey, slime, cobweb, hay)
- Redesigned alert format: severity tag (LOW/MED/HIGH), ping, TPS, player health
- Alert cooldown system (1.5s per check per player)
- Min VL threshold before alerts are broadcast (configurable)
- `/eclipseac` command with `/eac` alias
- GitHub Actions CI for automated builds and releases
