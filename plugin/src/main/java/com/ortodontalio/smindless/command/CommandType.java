package com.ortodontalio.smindless.command;

import java.util.Arrays;

public enum CommandType {
    RELOAD("reload", "r"),
    LIST("list", "l"),
    PLAYER_LIST("plist", "pl"),
    BAN("ban", "b"),
    UNBAN("unban", "ub"),
    GRANT("grant", "g"),
    UNGRANT("grant", "ug");

    private final String name;
    private final String alias;

    CommandType(String name, String alias) {
        this.name = name;
        this.alias = alias;
    }

    public String getName() {
        return name;
    }

    public String getAlias() {
        return alias;
    }

    public static CommandType getByNameOrAlias(String nameOrAlias) {
        return Arrays.stream(CommandType.values())
                .filter(cmd -> cmd.name.equals(nameOrAlias) || cmd.alias.equals(nameOrAlias))
                .findFirst()
                .orElse(null);
    }
}
