package com.gregtechceu.gtceu.common.recipe.condition;

import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeCondition;
import com.gregtechceu.gtceu.api.recipe.condition.RecipeConditionType;
import com.gregtechceu.gtceu.common.data.GTRecipeConditions;

import com.gregtechceu.gtceu.utils.codec.GTCodecUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.HolderSetCodec;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

@NoArgsConstructor
public class BiomeCondition extends RecipeCondition<BiomeCondition> {

    // spotless:off
    public static final Codec<BiomeCondition> CODEC = RecordCodecBuilder.create(instance -> RecipeCondition.isReverse(instance).and(
            GTCodecUtils.lazyParsingCodec(HolderSetCodec.create(Registries.BIOME, RegistryFixedCodec.create(Registries.BIOME), true)).fieldOf("biomes").forGetter(val -> val.unresolvedBiomes)
    ).apply(instance, BiomeCondition::new));
    // spotless:on

    @Getter
    private @Nullable HolderSet<Biome> biomes;

    @Getter
    private Supplier<HolderSet<Biome>> unresolvedBiomes = HolderSet::direct;

    public BiomeCondition(boolean isReverse, Supplier<HolderSet<Biome>> biomes) {
        super(isReverse);
        this.unresolvedBiomes = biomes;
    }

    public BiomeCondition(Supplier<HolderSet<Biome>> biomes) {
        this(false, biomes);
    }

    @Override
    public RecipeConditionType<BiomeCondition> getType() {
        return GTRecipeConditions.BIOME;
    }

    @Override
    public boolean isOr() {
        return true;
    }

    @Override
    public Component getTooltips() {
        if (biomes == null) biomes = unresolvedBiomes.get();

        if (biomes.size() == 1) {
            var key = biomes.get(0).unwrapKey().orElseThrow();
            return Component.translatable("recipe.condition.biome.tooltip",
                    Component.translatableWithFallback(key.location().toLanguageKey("biome"),
                            key.location().toString()));
        }

        if (biomes instanceof HolderSet.Named<Biome> tag) {
            return Component.translatable("recipe.condition.biome.tooltip",
                    Component.translatableWithFallback(tag.key().location().toLanguageKey("biome"),
                            tag.key().location().toString()));
        }

        MutableComponent component = null;

        for (Holder<Biome> biome: biomes) {
            var key = biome.unwrapKey().orElseThrow();
            MutableComponent biomeLang = Component.translatableWithFallback(key.location().toLanguageKey("biome"),
                    key.location().toString());

            if (component != null) component.append(", ").append(biomeLang);
            else component = biomeLang;
        }

        return Component.translatable("recipe.condition.biome.tooltip", component);
    }

    @Override
    public boolean testCondition(@NotNull GTRecipe recipe, @NotNull RecipeLogic recipeLogic) {
        Level level = recipeLogic.getLevel();
        Holder<Biome> biome = level.getBiome(recipeLogic.getBlockPos());

        if (biomes == null) biomes = unresolvedBiomes.get();
        return biomes.contains(biome);
    }

    @Override
    public BiomeCondition createTemplate() {
        return new BiomeCondition();
    }
}
