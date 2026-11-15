package com.ultima.elevators.item;

import com.ultima.elevators.block.entity.ControllerBlockEntity;
import com.ultima.elevators.block.entity.KioskBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Lift Linker: copies the dispatch group of one controller/kiosk and applies it to others,
 * building elevator banks for destination dispatch.
 */
public class LiftLinkerItem extends Item {
    public LiftLinkerItem(Properties props) {
        super(props);
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        Player player = ctx.getPlayer();
        if (player == null) return InteractionResult.PASS;
        ItemStack stack = ctx.getItemInHand();
        BlockEntity be = level.getBlockEntity(ctx.getClickedPos());

        if (!level.isClientSide) {
            if (be instanceof ControllerBlockEntity controller) {
                handleLink(stack, player, controller.getGroup(), controller::setGroup);
            } else if (be instanceof KioskBlockEntity kiosk) {
                handleLink(stack, player, kiosk.getGroup(), kiosk::setGroup);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private void handleLink(ItemStack stack, Player player, String currentGroup, java.util.function.Consumer<String> setter) {
        CompoundTag tag = stack.getOrCreateTag();
        String bound = tag.contains("group") ? tag.getString("group") : "";
        if (bound.isEmpty()) {
            String group = currentGroup.isEmpty() ? "BANK1" : currentGroup;
            tag.putString("group", group);
            setter.accept(group);
            player.displayClientMessage(Component.translatable("message.ultimaelevators.linker.copy", group), true);
        } else {
            setter.accept(bound);
            player.displayClientMessage(Component.translatable("message.ultimaelevators.linker.apply",
                    bound, Component.literal(bound).withStyle(ChatFormatting.AQUA)), true);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("group")) {
            tooltip.add(Component.translatable("tooltip.ultimaelevators.linker.group",
                    Component.literal(tag.getString("group")).withStyle(ChatFormatting.AQUA))
                    .withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.add(Component.translatable("tooltip.ultimaelevators.linker.empty").withStyle(ChatFormatting.GRAY));
        }
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
