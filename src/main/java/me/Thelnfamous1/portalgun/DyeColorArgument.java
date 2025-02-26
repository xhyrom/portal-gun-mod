package me.Thelnfamous1.portalgun;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class DyeColorArgument implements ArgumentType<DyeColor> {
    private static final Collection<String> EXAMPLES = Arrays.asList("blue", "orange");
    public static final DynamicCommandExceptionType ERROR_INVALID_VALUE = new DynamicCommandExceptionType((o) -> Component.translatable("argument.color.invalid", o));

    private DyeColorArgument() {
    }

    public static DyeColorArgument color() {
        return new DyeColorArgument();
    }

    public static DyeColor getColor(CommandContext<CommandSourceStack> pContext, String pName) {
        return pContext.getArgument(pName, DyeColor.class);
    }

    public DyeColor parse(StringReader pReader) throws CommandSyntaxException {
        String s = pReader.readUnquotedString();
        Optional<DyeColor> dyeColor = Arrays.stream(DyeColor.values()).filter(dc -> dc.getName().equals(s)).findFirst();
        if (dyeColor.isPresent()) {
            return dyeColor.get();
        } else {
            throw ERROR_INVALID_VALUE.create(s);
        }
    }

    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> pContext, SuggestionsBuilder pBuilder) {
        return SharedSuggestionProvider.suggest(Arrays.stream(DyeColor.values()).map(DyeColor::getName), pBuilder);
    }

    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}