# Contributing to EclipseAC

## Getting Started

1. Fork and clone the repo
2. Import into IntelliJ IDEA (recommended) or VS Code with Java Extension Pack
3. Ensure **Java 21** and **Maven** are installed
4. Run `mvn clean package` to confirm everything builds

## False Positive Philosophy

EclipseAC prioritises **accuracy over aggression**. Before submitting a check:

- Use **consecutive-hit thresholds** — at least 2–3 violations in a row
- Always respect `data.getTeleportTicks()`, `data.getDamageTicks()`, `data.getLiquidTicks()`, etc.
- Account for potions, surfaces, and edge cases (slabs, stairs, ice)
- Test against real players on a live server before submitting

## Adding a New Check

1. Choose the right package under `checks/`
2. Extend `Check`:

```java
public class MyCheck extends Check {
    public MyCheck(EclipseAC plugin) {
        super(plugin, "MyCheck", "Combat"); // configKey, category
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEvent(SomeEvent e) {
        if (!isEnabled()) return;
        Player player = ...;
        PlayerData data = plugin.getDataManager().getData(player);

        // Always check grace periods
        if (data.getTeleportTicks() > 0) return;
        if (data.getDamageTicks() > 0) return;

        if (badBehavior) {
            flag(player, "key=value expected=value");
        } else {
            reward(player);
        }
    }
}
```

3. Register in `EclipseAC.java`
4. Add config block to `config.yml`:

```yaml
  MyCheck:
    enabled: true
    my-setting: 10
    punishment-vl: 20
```

## Pull Request Rules

- One feature or fix per PR
- Write a clear description of what you changed and why it reduces false positives
- No breaking changes to the config format without a migration note
