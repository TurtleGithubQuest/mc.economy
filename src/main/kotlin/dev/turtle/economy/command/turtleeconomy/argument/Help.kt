package dev.turtle.economy.command.turtleeconomy.argument

import dev.turtle.turtlelib.TurtleCommand
import dev.turtle.turtlelib.TurtleSubCommand
import dev.turtle.turtlelib.util.MessageFactory

class Help(private val turtleCommand: TurtleCommand): TurtleSubCommand("help", turtleCommand) {
    val helpPlaceholders = mutableMapOf<String, HashMap<String, Any>>()
    var helpPage: List<MessageFactory.StylizedMessage>
    init {
        ArgumentData("subCommandName", turtleCommand.subCommands.keys.toList(), String::class, isRequired = false)
        helpPage = listOf(turtle.messageFactory.newMessage("").enablePrefix()) + turtleCommand.subCommands.map { (subCommandName, subCommand) ->
            helpPlaceholders[subCommandName.uppercase()] =
                hashMapOf(
                    "ARGUMENTS" to subCommand.argumentUsage,
                    "SUBCOMMAND" to subCommand.subCommandName,
                    "COMMAND" to turtleCommand.commandName,
                )
            turtle.messageFactory.newMessage("command.${turtleCommand.commandName}.$subCommandName.help-text")
                .fromConfig().placeholders(helpPlaceholders[subCommandName.uppercase()]!!)
        } + listOf(turtle.messageFactory.newMessage(""))
    }

    override fun onCommand(): Boolean {
        val subCommandName = getValue("subCommandName")?.toString() ?: run {
            helpPage.forEach { it.send(cs!!) }
            return true
        }
        turtleCommand.subCommands[subCommandName]?: run {
            turtle.messageFactory.newMessage("command.${turtleCommand.commandName}.subcommand-not-found").placeholder("subcommand", subCommandName).send(cs!!)
            return true
        }
        turtle.messageFactory.newMessage("command.${turtleCommand.commandName}.$subCommandName.help-text")
            .placeholders(helpPlaceholders[subCommandName.uppercase()]!!).fromConfig().send(cs!!)
        return true
    }
    override fun onSuggestion(argumentData: ArgumentData) {}
}