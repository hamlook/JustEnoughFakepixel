package com.jef.justenoughfakepixel.core.config.command;

import com.jef.justenoughfakepixel.core.JefConfig;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class JefCommand extends SimpleCommand {

    @Override
    public String getName() { return "jef"; }

    @Override
    public String getUsage() { return "/jef <category?>"; }

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("justenoughfakepixel");
    }

    @Override
    public void execute(ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 0) {
            JefConfig.openGui();
        } else {
            JefConfig.openCategory(StringUtils.join(args, " "));
        }
    }
}