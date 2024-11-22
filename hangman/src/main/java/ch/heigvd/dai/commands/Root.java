
package ch.heigvd.dai.commands;

import picocli.CommandLine;

@CommandLine.Command(
        description = "HANGMAN G@MING over TCP.",
        version = "1.0.0",
        subcommands = {
                ClientCommand.class,
                ServerCommand.class,
        },
        scope = CommandLine.ScopeType.INHERIT,
        mixinStandardHelpOptions = true)
public class Root {}
