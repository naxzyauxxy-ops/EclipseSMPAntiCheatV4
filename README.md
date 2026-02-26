<div align="center">

<img src="https://img.shields.io/badge/Paper-1.21.x-5865F2?style=for-the-badge&logo=minecraft&logoColor=white" alt="Paper 1.21.x"/>
<img src="https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java 21"/>
<img src="https://img.shields.io/badge/Version-1.0.0-a78bfa?style=for-the-badge" alt="Version"/>
<img src="https://img.shields.io/badge/License-MIT-60a5fa?style=for-the-badge" alt="MIT"/>

<br/><br/>

# ☀️ EclipseAC

**Lightweight, precise anticheat for Paper 1.21.x**

*Low false positives. Smart detection. Clean alerts.*

</div>

---

## ✨ Why EclipseAC?

Most anticheats either **miss cheaters** or **flag legit players constantly**. EclipseAC takes a different approach:

- 🎯 **Consecutive-hit thresholds** — requires multiple violations in a row before flagging (not a single laggy packet)
- ⏱️ **Grace tick system** — separate grace periods for teleports, damage/knockback, climbables, liquids, and jumps
- 📊 **Rolling averages** — Timer check uses a 3-second window so packet bursts don't instantly flag
- 🧠 **Environment-aware** — Speed check accounts for ice, soul sand, Speed potions, Slowness potions, slabs, and stairs
- 🔔 **Smart alert cooldown** — staff chat won't get spammed; alerts are rate-limited per check per player
- 📋 **Rich alert format** — every alert shows VL severity, ping, live TPS, and player health

---

## 📦 Checks

### ⚔️ Combat
| Check | What it detects | FP Mitigation |
|-------|----------------|---------------|
| **KillAura** | Attacks outside the player's FOV | Requires N consecutive bad-angle hits; aims at entity eye-height |
| **Reach** | Attacking beyond vanilla reach | Measures eye-to-hitbox-surface (not center); consecutive threshold |
| **AutoClicker** | High CPS or robotic click timing | Std-deviation analysis; min 12 samples before flagging |

### 🏃 Movement
| Check | What it detects | FP Mitigation |
|-------|----------------|---------------|
| **Flight** | Rising/hovering in air | Damage grace, climbable grace, liquid grace, bounce block exemption |
| **Speed** | Moving faster than vanilla allows | Accounts for Speed/Slowness potions, ice, soul sand, slabs, stairs, jump grace |
| **NoFall** | Landing without taking fall damage | Exempts water, honey, slime, cobweb, hay, slow falling potion |
| **Step** | Stepping up more than 0.6 blocks in one tick | Exempts slabs/stairs; requires horizontal movement |
| **Timer** | Too many movement packets per second | 3-second rolling average; high 25 pps threshold |

### 📦 Player
| Check | What it detects | FP Mitigation |
|-------|----------------|---------------|
| **Scaffold** | Bridging without looking down | Configurable streak (6 consecutive) before flagging |
| **FastPlace** | Block placements too fast | Configurable ms delay (default 80ms) |
| **Inventory** | Attacking while inventory is open | Close grace period; only flags if inventory is genuinely open |
| **BadPackets** | NaN positions, invalid pitch, impossible jumps | Teleport grace; loose delta threshold |

---

## 🔔 Alert Format

```
▸ Steve · KillAura (Combat)  VL:12  ⚑ MED
  angle=105.3°/90.0° streak=3  · ♥ 18.0  · ping 34ms  · tps 20.0
```

- **Color-coded VL** — gray (low) → yellow (medium) → red (high)
- **Severity tag** — `⚑ LOW` / `⚑ MED` / `⚑ HIGH` at a glance
- **Ping & TPS** — always visible so you know if lag is a factor
- **Alert cooldown** — same check on same player won't repeat within 1.5 seconds
- **Min VL threshold** — low-VL alerts suppressed by default (configurable)

---

## 🚀 Installation

### Requirements
- **Paper** 1.21.x (or forks: Purpur, Pufferfish, etc.)
- **Java** 21+

### Steps
1. Download the latest JAR from [Releases](../../releases)
2. Drop it in your `plugins/` folder
3. Restart your server
4. Configure `plugins/EclipseAC/config.yml`

---

## 🔨 Building from Source

```bash
git clone https://github.com/yourusername/EclipseAC.git
cd EclipseAC
mvn clean package
# Output: target/EclipseAC-1.0.0.jar
```

**Requires:** Java 21, Maven 3.8+

---

## ⚙️ Configuration Overview

```yaml
alerts:
  min-vl-to-alert: 3         # Suppress spam at low VL
  alert-cooldown-ms: 1500    # Rate-limit per check per player
  show-ping: true            # Show ping in alert messages
  show-tps: true             # Show server TPS in alerts

checks:
  KillAura:
    max-attack-angle: 90.0
    consecutive-threshold: 3   # Require 3 bad hits in a row

  Reach:
    max-reach: 3.2             # Accounts for server-lag interpolation
    hitbox-buffer: 0.15        # Edge-of-hitbox buffer
    consecutive-threshold: 3

  Speed:
    max-speed-multiplier: 1.20 # 20% over vanilla sprint
    slab-multiplier: 1.30      # Extra room on slab surfaces

  Timer:
    max-packets-per-second: 25
    average-window-seconds: 3  # Rolling average, not instant spike
```

Full config with explanations is generated on first run.

---

## 🛠️ Commands

All commands have the alias `/eac` and `/eclipse`.

| Command | Description |
|---------|-------------|
| `/eac reload` | Reload config without restart |
| `/eac alerts` | Toggle alerts in your chat |
| `/eac info <player>` | View a player's violations (sorted by VL) |
| `/eac reset <player>` | Clear all VL for a player |
| `/eac kick <player>` | Manually kick a player |
| `/eac version` | Show plugin info |

---

## 🔑 Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `eclipseac.admin` | All commands | `op` |
| `eclipseac.alerts` | Receive alerts automatically | `op` |
| `eclipseac.bypass` | Exempt from all checks | `false` |

---

## 📁 Project Structure

```
EclipseAC/
├── src/main/java/dev/eclipseac/
│   ├── EclipseAC.java
│   ├── checks/
│   │   ├── Check.java                  ← Base class (flag, reward, grace checks)
│   │   ├── combat/
│   │   │   ├── KillAuraCheck.java
│   │   │   ├── ReachCheck.java
│   │   │   └── AutoClickerCheck.java
│   │   ├── movement/
│   │   │   ├── FlightCheck.java
│   │   │   ├── SpeedCheck.java
│   │   │   ├── NoFallCheck.java
│   │   │   ├── StepCheck.java
│   │   │   └── TimerCheck.java
│   │   └── player/
│   │       ├── ScaffoldCheck.java
│   │       ├── FastplaceCheck.java
│   │       ├── InventoryCheck.java
│   │       └── BadPacketsCheck.java
│   ├── commands/EclipseACCommand.java
│   ├── listeners/PlayerListener.java   ← Grace tick engine
│   └── managers/
│       ├── AlertManager.java           ← Cooldown, severity, format
│       ├── DataManager.java
│       ├── PlayerData.java             ← All grace tick fields
│       ├── ViolationManager.java
│       └── PunishmentManager.java
└── src/main/resources/
    ├── plugin.yml
    └── config.yml
```

---

## 🤝 Contributing

1. Fork the repo
2. Create a branch: `git checkout -b feature/my-check`
3. Implement your check by extending `Check`:
```java
public class MyCheck extends Check {
    public MyCheck(EclipseAC plugin) {
        super(plugin, "MyCheck", "Combat");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEvent(SomeEvent event) {
        if (!isEnabled()) return;
        PlayerData data = plugin.getDataManager().getData(player);
        if (data.getTeleportTicks() > 0) return; // always respect grace

        if (cheatDetected) {
            flag(player, "detail=value expected=value");
        } else {
            reward(player); // reduces VL for good behavior
        }
    }
}
```
4. Register it in `EclipseAC.java` → `registerChecks()`
5. Add config defaults to `config.yml`
6. Open a Pull Request

### False Positive Guidelines
When adding or tuning checks:
- Use **consecutive thresholds** rather than single-packet flags
- Always check `data.getTeleportTicks() > 0`, `data.getDamageTicks() > 0`
- Add environment-specific multipliers/exemptions where relevant
- Test on a real server before submitting

---

## 📋 Roadmap

- [ ] PacketEvents integration for true packet-level detection
- [ ] Velocity / AntiKnockback check
- [ ] AimAssist detection
- [ ] More movement: Strafe, GroundSpoof
- [ ] Discord webhook alerts
- [ ] Web dashboard for violation logs
- [ ] `/eac debug <player>` command for live per-check data

---

## 📄 License

[MIT](LICENSE) — free to use, modify, and distribute.

---

<div align="center">
Made with ☀️ for the Minecraft community<br/>
<a href="../../issues">Report a False Positive</a> · <a href="../../issues">Request a Feature</a>
</div>
