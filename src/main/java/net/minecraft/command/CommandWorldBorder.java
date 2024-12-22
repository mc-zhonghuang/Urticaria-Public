package net.minecraft.command;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.MathHelper;
import net.minecraft.world.border.WorldBorder;

import java.util.List;

public class CommandWorldBorder extends CommandBase {
    /**
     * Gets the name of the command
     */
    public String getCommandName() {
        return "worldborder";
    }

    /**
     * Return the required permission level for this command.
     */
    public int getRequiredPermissionLevel() {
        return 2;
    }

    /**
     * Gets the usage string for the command.
     *
     * @param sender The {@link ICommandSender} who is requesting usage details.
     */
    public String getCommandUsage(final ICommandSender sender) {
        return "commands.worldborder.usage";
    }

    /**
     * Callback when the command is invoked
     *
     * @param sender The {@link ICommandSender sender} who executed the command
     * @param args   The arguments that were passed with the command
     */
    public void processCommand(final ICommandSender sender, final String[] args) throws CommandException {
        if (args.length < 1) {
            throw new WrongUsageException("commands.worldborder.usage");
        } else {
            final WorldBorder worldborder = this.getWorldBorder();

            if (args[0].equals("set")) {
                if (args.length != 2 && args.length != 3) {
                    throw new WrongUsageException("commands.worldborder.set.usage");
                }

                final double d0 = worldborder.getTargetSize();
                final double d2 = parseDouble(args[1], 1.0D, 6.0E7D);
                final long i = args.length > 2 ? parseLong(args[2], 0L, 9223372036854775L) * 1000L : 0L;

                if (i > 0L) {
                    worldborder.setTransition(d0, d2, i);

                    if (d0 > d2) {
                        notifyOperators(sender, this, "commands.worldborder.setSlowly.shrink.success", String.format("%.1f", Double.valueOf(d2)), String.format("%.1f", Double.valueOf(d0)), Long.toString(i / 1000L));
                    } else {
                        notifyOperators(sender, this, "commands.worldborder.setSlowly.grow.success", String.format("%.1f", Double.valueOf(d2)), String.format("%.1f", Double.valueOf(d0)), Long.toString(i / 1000L));
                    }
                } else {
                    worldborder.setTransition(d2);
                    notifyOperators(sender, this, "commands.worldborder.set.success", String.format("%.1f", Double.valueOf(d2)), String.format("%.1f", Double.valueOf(d0)));
                }
            } else if (args[0].equals("add")) {
                if (args.length != 2 && args.length != 3) {
                    throw new WrongUsageException("commands.worldborder.add.usage");
                }

                final double d4 = worldborder.getDiameter();
                final double d8 = d4 + parseDouble(args[1], -d4, 6.0E7D - d4);
                final long i1 = worldborder.getTimeUntilTarget() + (args.length > 2 ? parseLong(args[2], 0L, 9223372036854775L) * 1000L : 0L);

                if (i1 > 0L) {
                    worldborder.setTransition(d4, d8, i1);

                    if (d4 > d8) {
                        notifyOperators(sender, this, "commands.worldborder.setSlowly.shrink.success", String.format("%.1f", Double.valueOf(d8)), String.format("%.1f", Double.valueOf(d4)), Long.toString(i1 / 1000L));
                    } else {
                        notifyOperators(sender, this, "commands.worldborder.setSlowly.grow.success", String.format("%.1f", Double.valueOf(d8)), String.format("%.1f", Double.valueOf(d4)), Long.toString(i1 / 1000L));
                    }
                } else {
                    worldborder.setTransition(d8);
                    notifyOperators(sender, this, "commands.worldborder.set.success", String.format("%.1f", Double.valueOf(d8)), String.format("%.1f", Double.valueOf(d4)));
                }
            } else if (args[0].equals("center")) {
                if (args.length != 3) {
                    throw new WrongUsageException("commands.worldborder.center.usage");
                }

                final BlockPos blockpos = sender.getPosition();
                final double d1 = parseDouble((double) blockpos.getX() + 0.5D, args[1], true);
                final double d3 = parseDouble((double) blockpos.getZ() + 0.5D, args[2], true);
                worldborder.setCenter(d1, d3);
                notifyOperators(sender, this, "commands.worldborder.center.success", Double.valueOf(d1), Double.valueOf(d3));
            } else if (args[0].equals("damage")) {
                if (args.length < 2) {
                    throw new WrongUsageException("commands.worldborder.damage.usage");
                }

                if (args[1].equals("buffer")) {
                    if (args.length != 3) {
                        throw new WrongUsageException("commands.worldborder.damage.buffer.usage");
                    }

                    final double d5 = parseDouble(args[2], 0.0D);
                    final double d9 = worldborder.getDamageBuffer();
                    worldborder.setDamageBuffer(d5);
                    notifyOperators(sender, this, "commands.worldborder.damage.buffer.success", String.format("%.1f", Double.valueOf(d5)), String.format("%.1f", Double.valueOf(d9)));
                } else if (args[1].equals("amount")) {
                    if (args.length != 3) {
                        throw new WrongUsageException("commands.worldborder.damage.amount.usage");
                    }

                    final double d6 = parseDouble(args[2], 0.0D);
                    final double d10 = worldborder.getDamageAmount();
                    worldborder.setDamageAmount(d6);
                    notifyOperators(sender, this, "commands.worldborder.damage.amount.success", String.format("%.2f", Double.valueOf(d6)), String.format("%.2f", Double.valueOf(d10)));
                }
            } else if (args[0].equals("warning")) {
                if (args.length < 2) {
                    throw new WrongUsageException("commands.worldborder.warning.usage");
                }

                final int j = parseInt(args[2], 0);

                if (args[1].equals("time")) {
                    if (args.length != 3) {
                        throw new WrongUsageException("commands.worldborder.warning.time.usage");
                    }

                    final int k = worldborder.getWarningTime();
                    worldborder.setWarningTime(j);
                    notifyOperators(sender, this, "commands.worldborder.warning.time.success", Integer.valueOf(j), Integer.valueOf(k));
                } else if (args[1].equals("distance")) {
                    if (args.length != 3) {
                        throw new WrongUsageException("commands.worldborder.warning.distance.usage");
                    }

                    final int l = worldborder.getWarningDistance();
                    worldborder.setWarningDistance(j);
                    notifyOperators(sender, this, "commands.worldborder.warning.distance.success", Integer.valueOf(j), Integer.valueOf(l));
                }
            } else {
                if (!args[0].equals("get")) {
                    throw new WrongUsageException("commands.worldborder.usage");
                }

                final double d7 = worldborder.getDiameter();
                sender.setCommandStat(CommandResultStats.Type.QUERY_RESULT, MathHelper.floor_double(d7 + 0.5D));
                sender.addChatMessage(new ChatComponentTranslation("commands.worldborder.get.success", String.format("%.0f", Double.valueOf(d7))));
            }
        }
    }

    protected WorldBorder getWorldBorder() {
        return MinecraftServer.getServer().worldServers[0].getWorldBorder();
    }

    public List<String> addTabCompletionOptions(final ICommandSender sender, final String[] args, final BlockPos pos) {
        return args.length == 1 ? getListOfStringsMatchingLastWord(args, "set", "center", "damage", "warning", "add", "get") : (args.length == 2 && args[0].equals("damage") ? getListOfStringsMatchingLastWord(args, "buffer", "amount") : (args.length >= 2 && args.length <= 3 && args[0].equals("center") ? func_181043_b(args, 1, pos) : (args.length == 2 && args[0].equals("warning") ? getListOfStringsMatchingLastWord(args, "time", "distance") : null)));
    }
}
