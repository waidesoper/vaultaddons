package waidesoper.vaultaddons.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import iskallia.vault.item.CrystalData;
import iskallia.vault.item.ItemGiftBomb;
import iskallia.vault.item.ItemGiftBomb.Variant;
import iskallia.vault.item.ItemTraderCore;
import iskallia.vault.item.ItemTraderCore.CoreType;
import iskallia.vault.item.ItemVaultCrystal;
import iskallia.vault.command.GiveBitsCommand;
import iskallia.vault.util.EntityHelper;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.item.ItemStack;


public class addonsCommand extends Command {
    @Override
    public String getName() {
        return "internal";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) { 
        builder.then(
            Commands.literal("received_sub_gift")
                    .then(Commands.argument("actor", StringArgumentType.word())
                    .then(Commands.argument("amount", IntegerArgumentType.integer())
                    .then(Commands.argument("tier", IntegerArgumentType.integer(0,3))
                    .executes(context -> receivedSubGift(context)))))
        );
        builder.then(
            Commands.literal("received_donation")
                    .then(Commands.argument("actor", StringArgumentType.word())
                    .then(Commands.argument("amount", IntegerArgumentType.integer())
                    .executes(context -> receivedDonation(context))))
        );
        builder.then(
            Commands.literal("received_bit_donation")
                    .then(Commands.argument("actor", StringArgumentType.word())
                    .then(Commands.argument("amount", IntegerArgumentType.integer())
                    .executes(context -> receivedBitDonation(context))))
        );

        builder.then(
            Commands.literal("raffle")
                    .then(Commands.argument("actor", StringArgumentType.word())
                    .executes(context -> raffle(context)))
        );
    }

    private static int receivedSubGift(CommandContext<CommandSource> context) throws CommandSyntaxException {
        String actor = StringArgumentType.getString(context, "actor");
        int amount = IntegerArgumentType.getInteger(context,"amount");
        int tier = IntegerArgumentType.getInteger(context,"tier");
        int variant = 0;
        if(amount >=5){
            if(amount < 10)
                variant = 0;
            if(amount >= 10 && amount < 20)
                variant = 1;
            if(amount >= 20 && amount < 50)
                variant = 2;
            if(amount >= 50)
                variant = 3;
            ItemStack item = ItemGiftBomb.forGift(Variant.values()[variant], actor, amount);
            EntityHelper.giveItem(context.getSource().asPlayer(), item);
        }
        return 0;
    }
    
    private static int receivedDonation(CommandContext<CommandSource> context) throws CommandSyntaxException {
        String actor = StringArgumentType.getString(context, "actor");
        int amount = IntegerArgumentType.getInteger(context,"amount");
        boolean isMegaHead = amount >= 100;
        if(amount >= 25){
            ItemStack item = ItemTraderCore.generate(actor,amount,isMegaHead,CoreType.COMMON);
            EntityHelper.giveItem(context.getSource().asPlayer(), item);
        }
        return 0;
    }

    private static int receivedBitDonation(CommandContext<CommandSource> context) throws CommandSyntaxException {
        String actor = StringArgumentType.getString(context, "actor");
        int amount = IntegerArgumentType.getInteger(context,"amount");
        boolean isMegaHead = amount >= 10000;
        if(amount >= 2500){
            ItemStack item = ItemTraderCore.generate(actor, amount, isMegaHead, CoreType.COMMON);
            EntityHelper.giveItem(context.getSource().asPlayer(), item);
        }
        GiveBitsCommand.dropBits(context.getSource().asPlayer(), amount);
        return 0;
    }
    
    private static int raffle(CommandContext<CommandSource> context) throws CommandSyntaxException {
        String actor = StringArgumentType.getString(context, "actor");
        ItemStack item = ItemVaultCrystal.getCrystalWithBoss(actor);
        ItemVaultCrystal.getData(item).addModifier("Raffle", CrystalData.Modifier.Operation.ADD,1.0F);
        EntityHelper.giveItem(context.getSource().asPlayer(), item);
        return 0;
    }

    @Override
    public boolean isDedicatedServerOnly() {
        return false;
    }

}
