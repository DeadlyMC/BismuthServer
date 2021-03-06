package si.bismuth.commands;

import net.minecraft.block.BlockShulkerBox;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Slot;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemShulkerBox;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import si.bismuth.mixins.IBlockShulkerBoxMixin;

import java.util.HashMap;
import java.util.Map;

public class CommandStackBoxes extends CommandBismuthBase {
	@Override
	public String getName() {
		return "stackboxes";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "stackboxes";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (!(sender instanceof EntityPlayerMP)) {
			throw new CommandException("Unknown " + sender.getName() + " tried to run /stackboxes!");
		}

		Map<EnumDyeColor, Integer> boxesToStack = new HashMap<>();
		final EntityPlayerMP player = getCommandSenderAsPlayer(sender);
		for (final Slot slot : player.inventoryContainer.inventorySlots) {
			final Pair<EnumDyeColor, Integer> pair = getShulkerBoxColourAndAmour(slot.getStack());
			if (pair.getRight() > 0) {
				boxesToStack.merge(pair.getLeft(), pair.getRight(), Integer::sum);
				slot.putStack(ItemStack.EMPTY);
			}
		}

		for (Map.Entry<EnumDyeColor, Integer> entry : boxesToStack.entrySet()) {
			if (entry.getValue() > 0) {
				final ItemStack stack = new ItemStack(BlockShulkerBox.getBlockByColor(entry.getKey()), entry.getValue());
				if (!player.addItemStackToInventory(stack)) {
					player.dropItem(stack, false);
				}
			}
		}

		player.inventoryContainer.detectAndSendChanges();
	}

	private Pair<EnumDyeColor, Integer> getShulkerBoxColourAndAmour(final ItemStack stack) {
		if (stack.getItem() instanceof ItemShulkerBox) {
			NBTTagCompound cmp = this.getCompoundOrNull(stack);
			if (cmp == null || cmp.getTagList("Items", 10).isEmpty()) {
				final EnumDyeColor dye = ((IBlockShulkerBoxMixin) ((ItemShulkerBox) stack.getItem()).getBlock()).getColor();
				return new ImmutablePair<>(dye, stack.getCount());
			}
		}

		return new ImmutablePair<>(EnumDyeColor.WHITE, 0);
	}

	private NBTTagCompound getCompoundOrNull(final ItemStack stack) {
		final NBTTagCompound compound = stack.getTagCompound();
		if (compound != null && compound.hasKey("BlockEntityTag")) {
			return compound.getCompoundTag("BlockEntityTag");
		} else {
			return null;
		}
	}
}
