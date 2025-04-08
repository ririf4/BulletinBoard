package net.ririfa.bulletinboard.command

import net.ririfa.bulletinboard.LM
import net.ririfa.bulletinboard.translation.BBMessageKey
import net.ririfa.bulletinboard.translation.adapt
import net.ririfa.bulletinboard.util.displayHelp
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

object CommandManager : CommandExecutor, TabCompleter {
    private val subCommandsList: List<String> = Commands.entries.map { it.name }

    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<String>
    ): Boolean {
        if (args.isEmpty() || subCommandsList.map { it.lowercase() }.contains(args[0]).not()) {
            if (sender is Player) {
                displayHelp(sender)
                return true
            } else {
                sender.sendMessage(LM.getMessage(BBMessageKey.Command.Other.PlayerOnly))
                return true
            }
        }

        val commandEnum = Commands.fromString(args[0])
        if (commandEnum != null && sender is Player) {
            with(commandEnum) {
                execute(sender, args)
                return true
            }
        } else {
            if (sender is Player) {
                val p = sender.adapt()
                sender.sendMessage(p.getMessage(BBMessageKey.Command.Other.UnknownCommand, Commands.HELP.name))
            } else {
                sender.sendMessage(LM.getMessage(BBMessageKey.Command.Other.UnknownCommand, Commands.HELP.name))
            }
            return true
        }
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): List<String?> {
        if (args.isEmpty()) {
            return subCommandsList.map { it.lowercase() }
        }

        if (args.size == 1) {
            return subCommandsList
                .map { it.lowercase() }
                .filter { it.startsWith(args[0].lowercase()) }
        }

        return listOf()
    }
}