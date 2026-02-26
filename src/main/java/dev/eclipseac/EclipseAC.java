package dev.eclipseac;

import dev.eclipseac.managers.LicenseManager;
import dev.eclipseac.checks.combat.*;
import dev.eclipseac.checks.movement.*;
import dev.eclipseac.checks.player.*;
import dev.eclipseac.commands.EclipseACCommand;
import dev.eclipseac.listeners.PlayerListener;
import dev.eclipseac.managers.*;
import org.bukkit.plugin.java.JavaPlugin;

public class EclipseAC extends JavaPlugin {

    private static EclipseAC instance;

    private LicenseManager    licenseManager;
    private DataManager       dataManager;
    private ViolationManager  violationManager;
    private AlertManager      alertManager;
    private PunishmentManager punishmentManager;
    private NoFallCheck       noFallCheck;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        licenseManager = new LicenseManager(this);
        if (!licenseManager.validate()) {
            getLogger().severe("[EclipseAC] Disabling plugin due to invalid license.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        dataManager       = new DataManager(this);
        violationManager  = new ViolationManager(this);
        alertManager      = new AlertManager(this);
        punishmentManager = new PunishmentManager(this);

        registerChecks();

        getCommand("eclipseac").setExecutor(new EclipseACCommand(this));

        getLogger().info("EclipseAC v" + getDescription().getVersion() + " enabled. 25 checks loaded.");
    }

    @Override
    public void onDisable() {
        if (alertManager != null) alertManager.closeLog();
        if (dataManager  != null) dataManager.cleanup();
    }

    private void registerChecks() {
        var pm = getServer().getPluginManager();

        pm.registerEvents(new PlayerListener(this), this);

        pm.registerEvents(new KillAuraCheck(this),    this);
        pm.registerEvents(new ReachCheck(this),        this);
        pm.registerEvents(new AutoClickerCheck(this),  this);
        pm.registerEvents(new AimAssistCheck(this),    this);
        pm.registerEvents(new AntiVelocityCheck(this), this);
        pm.registerEvents(new CriticalsCheck(this),    this);
        pm.registerEvents(new SprintResetCheck(this),  this);
        pm.registerEvents(new NoSlowCheck(this),       this);

        pm.registerEvents(new FlightCheck(this),       this);
        pm.registerEvents(new SpeedCheck(this),        this);
        pm.registerEvents(new StepCheck(this),         this);
        pm.registerEvents(new TimerCheck(this),        this);
        pm.registerEvents(new ElytraFlightCheck(this), this);
        pm.registerEvents(new GroundSpoofCheck(this),  this);
        pm.registerEvents(new JesusCheck(this),        this);
        pm.registerEvents(new BoatFlyCheck(this),      this);
        pm.registerEvents(new StrafeCheck(this),       this);

        noFallCheck = new NoFallCheck(this);
        pm.registerEvents(noFallCheck, this);

        pm.registerEvents(new ScaffoldCheck(this),     this);
        pm.registerEvents(new FastplaceCheck(this),    this);
        pm.registerEvents(new InventoryCheck(this),    this);
        pm.registerEvents(new BadPacketsCheck(this),   this);
        pm.registerEvents(new NukerCheck(this),        this);
        pm.registerEvents(new DerpCheck(this),         this);
        pm.registerEvents(new PhaseCheck(this),        this);
    }

    public static EclipseAC getInstance() { return instance; }

    public LicenseManager    getLicenseManager()    { return licenseManager; }
    public DataManager       getDataManager()       { return dataManager; }
    public ViolationManager  getViolationManager()  { return violationManager; }
    public AlertManager      getAlertManager()      { return alertManager; }
    public PunishmentManager getPunishmentManager() { return punishmentManager; }
    public NoFallCheck       getNoFallCheck()       { return noFallCheck; }
}
