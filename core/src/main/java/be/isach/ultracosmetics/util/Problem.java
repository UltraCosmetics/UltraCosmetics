package be.isach.ultracosmetics.util;

import be.isach.ultracosmetics.UltraCosmeticsData;
import be.isach.ultracosmetics.config.MessageManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;

public enum Problem {
    // Fatal
    BAD_CONFIG(ProblemSeverity.FATAL, "config.yml is invalid, please run it through a YAML checker.", "invalid-config"),
    BAD_MESSAGES(ProblemSeverity.FATAL, " is invalid, please run it through a YAML checker.", "invalid-messages-file") {
        @Override
        public Component getSummary() {
            return Component.text(MessageManager.getLangFilename()).append(super.getSummary());
        }
    },

    // Non-fatal
    BAD_MC_VERSION(ProblemSeverity.WARNING, "This version of Minecraft is not fully supported, please check for an update.", "unsupported-mc-version"),
    NMS_LOAD_FAILURE(ProblemSeverity.WARNING, "Couldn't load support for this MC version, please check the logs for more info.", "nms-load-failure"),
    BAD_MAPPINGS_VERSION(ProblemSeverity.WARNING, "Server internals seem to have changed since this build was created, you will likely experience issues.", "bad-mappings-version") {
        @Override
        public Component getSummary() {
            return super.getSummary().append(Component.text(" (Expected version: " + UltraCosmeticsData.get().getServerVersion().canonicalName() + ")"));
        }
    },
    TALL_DISGUISES_DISABLED(ProblemSeverity.WARNING, "TallSelfDisguises is disabled in LibsDisguises self_disguise.yml. This may cause morph self view to not behave as intended.", "tall-disguises-disabled"),
    SQL_INIT_FAILURE(ProblemSeverity.WARNING, "SQL failed to connect, using flatfile support instead.", "sql-init-failure"),
    WORLDGUARD_HOOK_FAILURE(ProblemSeverity.WARNING, "Failed to hook into WorldGuard.", "worldguard-hook-failure"),
    SQL_MIGRATION_REQUIRED(ProblemSeverity.WARNING, "MySQL config settings have changed, please review them. SQL storage is disabled until then.", "sql-migration-required"),

    // Informational
    MOBCHIP_ERROR(ProblemSeverity.INFO, "MobChipLite does not support this version of Minecraft, pets will be disabled.", "mobchip-error"),
    ;

    private static final String PROBLEMS_WIKI = "https://github.com/UltraCosmetics/UltraCosmetics/wiki/Problems#";
    private final ProblemSeverity severity;
    private final String description;
    private final String wikiTitle;

    private Problem(ProblemSeverity severity, String description, String wikiTitle) {
        this.severity = severity;
        this.description = description;
        this.wikiTitle = wikiTitle;
    }

    public ProblemSeverity getSeverity() {
        return severity;
    }

    public String getDescription() {
        return description;
    }

    public String getWikiURL() {
        return PROBLEMS_WIKI + wikiTitle;
    }

    public Component getSummary() {
        return Component.text(description).clickEvent(ClickEvent.openUrl(getWikiURL()));
    }
}
