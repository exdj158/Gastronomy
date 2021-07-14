package uk.joshiejack.gastronomy.crafting;

import net.minecraft.fluid.Fluid;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import uk.joshiejack.penguinlib.item.crafting.AbstractSimplePenguinRecipe;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class CookingRecipe extends AbstractSimplePenguinRecipe<ItemStack> {
    protected final NonNullList<RecipeIngredient<?>> ingredients;
    private final Supplier<IRecipeType<?>> recipeType;

    public CookingRecipe(Supplier<IRecipeType<?>> recipeType, IRecipeSerializer<?> serializer, ResourceLocation rl, NonNullList<RecipeIngredient<?>> ingredients, ItemStack output) {
        super(null, serializer, rl, null, output);
        this.ingredients = ingredients;
        this.recipeType = recipeType;
    }

    @Override
    public IRecipeType<?> getType() {
        return recipeType.get();
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return NonNullList.create(); //TODO: How?
    }

    private boolean matches(List<ItemStack> items, List<FluidStack> fluids) {
        if (items.isEmpty() && fluids.isEmpty()) return false;
        if (items.stream().anyMatch(inStack -> ingredients.stream()
                .filter(t -> t instanceof SolidIngredient).noneMatch(i -> ((SolidIngredient) i).test(inStack.getItem()))))
            return false;
        if (fluids.stream().anyMatch(inFluid -> ingredients.stream()
                .filter(t -> t instanceof FluidIngredient).noneMatch(i -> ((FluidIngredient) i).test(inFluid.getFluid()))))
            return false;

        //First let's convert the items and fluids to the recipe ingredients
        List<RecipeIngredient<?>> recipeIngredients = Stream.concat(SolidIngredient.toIngredients(items), FluidIngredient.toIngredients(fluids)).collect(Collectors.toList());
        //Now we need to check that we have everything in ingredients
        return ingredients.stream().allMatch(required -> recipeIngredients.stream().anyMatch(in -> in.matches(required)));
    }

    private List<ItemStack> items(IInventory inventory) {
        return IntStream.range(0, inventory.getContainerSize())
                .boxed().map(inventory::getItem)
                .filter(stack -> !stack.isEmpty())
                .collect(Collectors.toList());
    }

    private List<FluidStack> fluids(IInventory inventory) {
        List<FluidStack> fluids = new ArrayList<>();
        if (inventory instanceof TileEntity) {
            ((TileEntity) inventory).getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
                    .ifPresent(handler -> IntStream.range(0, handler.getTanks())
                            .forEach(i -> fluids.add(handler.getFluidInTank(i))));
        }

        return fluids;
    }

    @Override
    public boolean matches(IInventory inventory, @Nonnull World world) {
        return matches(items(inventory), fluids(inventory));
    }

    @Override
    public ItemStack assemble(@Nonnull IInventory inventory) {
        List<ItemStack> items = items(inventory);
        List<FluidStack> fluids = fluids(inventory);
        List<RecipeIngredient<?>> recipeIngredients = Stream.concat(SolidIngredient.toIngredients(items), FluidIngredient.toIngredients(fluids)).collect(Collectors.toList());
        //Now to determine how many we can make???
        int count = 0;
        for (int i = 0; i < 1000; i++) {
            if (ingredients.stream().allMatch(required -> {
                for (RecipeIngredient<?> ri : recipeIngredients) {
                    if (ri.matches(required)) {
                        ri.amount -= required.amount;
                        return true;
                    }
                }

                return false;
            })) count++;
        }

        ItemStack result = output.copy();
        result.setCount(output.getCount() * count);
        return result;
    }

    @Override
    public ItemStack getResultItem() {
        return output;
    }

    public abstract static class RecipeIngredient<T> {
        protected final ITag.INamedTag<T> tag;
        protected int amount;

        public RecipeIngredient(ITag.INamedTag<T> tag, int amount) {
            this.tag = tag;
            this.amount = amount;
        }

        public boolean matches(RecipeIngredient<?> required) {
            return required.tag.equals(tag) && amount >= required.amount;
        }
    }

    public static class SolidIngredient extends RecipeIngredient<Item> {
        public SolidIngredient(ITag.INamedTag<Item> tag, int amount) {
            super(tag, amount);
        }

        public static Stream<SolidIngredient> toIngredients(List<ItemStack> items) {
            List<SolidIngredient> ingredients = new ArrayList<>();
            items.forEach(stack -> stack.getItem().getTags()
                    .forEach(tag -> ingredients.add(new SolidIngredient(ItemTags.createOptional(tag), stack.getCount()))));
            return ingredients.stream();
        }

        private boolean test(Item item) {
            return item.is(tag);
        }
    }

    public static class FluidIngredient extends RecipeIngredient<Fluid> {
        public FluidIngredient(ITag.INamedTag<Fluid> tag, int amount) {
            super(tag, amount);
        }

        public static Stream<FluidIngredient> toIngredients(List<FluidStack> fluids) {
            List<FluidIngredient> ingredients = new ArrayList<>();
            fluids.forEach(stack -> stack.getFluid().getTags()
                    .forEach(tag -> ingredients.add(new FluidIngredient(FluidTags.createOptional(tag), stack.getAmount()))));
            return ingredients.stream();
        }

        private boolean test(Fluid fluid) {
            return fluid.is(tag);
        }
    }
}
