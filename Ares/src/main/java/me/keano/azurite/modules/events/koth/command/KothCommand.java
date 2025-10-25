package me.keano.azurite.modules.events.koth.command;

import me.keano.azurite.modules.commands.CommandManager;
import me.keano.azurite.modules.events.koth.command.args.*;
import me.keano.azurite.modules.framework.commands.Command;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Copyright (c) 2023. Keano
 * Use or redistribution of source or file is
 * only permitted if given explicit permission.
 */
public class KothCommand extends Command {

    public KothCommand(CommandManager manager) {
        super(
                manager,
                "koth"
        );
        this.setPermissible("azurite.koth");
        this.handleArguments(Arrays.asList(
                new KothClaimArg(manager),
                new KothCreateArg(manager),
                new KothDeleteArg(manager),
                new KothStartArg(manager),
                new KothEndArg(manager),
                new KothFocusArg(manager),
                new KothSetColorArg(manager),
                new KothSetMinArg(manager),
                new KothSetPointsArg(manager),
                new KothSetRemArg(manager),
                new KothListArg(manager),
                new KothRenameArg(manager),
                new KothEditLootArg(manager),
                new KothTpCapperArg(manager),
                new KothUnclaimArg(manager),
                new KothTeleportArg(manager),
                new KothLastCapperArg(manager)
        ));
    }

    @Override
    public List<String> aliases() {
        return Collections.emptyList();
    }

    @Override
    public List<String> usage() {
        return getLanguageConfig().getStringList("KOTH_COMMAND.USAGE");
    }
}