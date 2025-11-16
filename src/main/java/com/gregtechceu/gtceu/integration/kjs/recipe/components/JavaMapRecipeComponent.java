package com.gregtechceu.gtceu.integration.kjs.recipe.components;

import com.gregtechceu.gtceu.GTCEu;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.kubejs.recipe.RecipeScriptContext;
import dev.latvian.mods.kubejs.recipe.component.*;
import dev.latvian.mods.kubejs.recipe.match.ReplacementMatchInfo;
import dev.latvian.mods.rhino.type.TypeInfo;

import java.util.HashMap;
import java.util.Map;

public record JavaMapRecipeComponent<K, V>(RecipeComponent<K> key, RecipeComponent<V> value)
        implements RecipeComponent<Map<K, V>> {

    @Override
    public Map<K, V> replace(RecipeScriptContext cx, Map<K, V> original, ReplacementMatchInfo match, Object with) {
        var map = original;

        for (Map.Entry<K, V> entry : original.entrySet()) {
            var r = value.replace(cx, entry.getValue(), match, with);
            if (r != entry.getValue()) {
                if (map == original) {
                    map = new HashMap<>(original);
                }
                map.put(entry.getKey(), r);
            }
        }

        return map;
    }

    @Override
    public String toString() {
        return "java_map{" + key + ":" + value + "}";
    }

    /**
     * see {@link ListRecipeComponent#TYPE}
     */
    public static final RecipeComponentType<?> TYPE = RecipeComponentType
            .<JavaMapRecipeComponent<?, ?>>dynamic(GTCEu.id("java_map"),
                    (type, ctx) -> RecordCodecBuilder.mapCodec(instance -> instance.group(
                            ctx.recipeComponentCodec().fieldOf("key").forGetter(JavaMapRecipeComponent::key),
                            ctx.recipeComponentCodec().fieldOf("value").forGetter(JavaMapRecipeComponent::value))
                            .apply(instance, JavaMapRecipeComponent::new)));

    @Override
    public RecipeComponentType<?> type() {
        return TYPE;
    }

    @Override
    public Codec<Map<K, V>> codec() {
        return Codec.unboundedMap(key.codec(), value.codec());
    }

    @Override
    public TypeInfo typeInfo() {
        return TypeInfo.RAW_MAP.withParams(key.typeInfo(), value.typeInfo());
    }
}
