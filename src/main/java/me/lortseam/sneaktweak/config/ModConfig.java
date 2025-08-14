package me.lortseam.sneaktweak.config;

import com.google.gson.GsonBuilder;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.*;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import me.lortseam.sneaktweak.SneakTweak;
import net.minecraft.util.Identifier;

public class ModConfig {

    @SerialEntry
    private boolean smoothingEnabled = true;
    @SerialEntry
    private int speedPercentage = 100;
    @SerialEntry
    private SneakingEyeHeightType sneakingEyeHeightPreset = SneakingEyeHeightType.DEFAULT;
    @SerialEntry
    private float customSneakingEyeHeight = 1.27f;
    @SerialEntry
    private boolean modifyThirdPersonSneakingEyeHeight = false;

    public static ConfigClassHandler<ModConfig> HANDLER = ConfigClassHandler.createBuilder(ModConfig.class)
            .id(Identifier.of(SneakTweak.MOD_ID))
            .serializer(config -> GsonConfigSerializerBuilder.create(config)
                    .setPath(FabricLoader.getInstance().getConfigDir().resolve("sneaktweak.json5"))
                    .appendGsonBuilder(GsonBuilder::setPrettyPrinting)
                    .setJson5(true)
                    .build())
            .build();

    public static Screen createConfigScreen(Screen parent) {
        ModConfig config = HANDLER.instance();
        return YetAnotherConfigLib.createBuilder()
                .save(ModConfig::save)
                .title(Text.translatable("config.sneaktweak.title"))
                .category(ConfigCategory.createBuilder()
                        .name(Text.literal("Sneak Tweak")) // TODO: Localize but I really can't be bothered right now
                        .group(OptionGroup.createBuilder()
                                .name(Text.literal("Options")) // TODO: ditto
                                .option(Option.<Boolean>createBuilder()
                                        .name(Text.translatable("config.sneaktweak.smoothingEnabled"))
                                        .description(OptionDescription.of(
                                                Text.translatable("config.sneaktweak.smoothingEnabled." + (config.smoothingEnabled ? "true" : "false"))
                                        ))
                                        .binding(true, () -> config.smoothingEnabled, val -> config.smoothingEnabled = val)
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .option(Option.<Integer>createBuilder()
                                        .name(Text.translatable("config.sneaktweak.speedPercentage"))
                                        .description(OptionDescription.of(Text.translatable("config.sneaktweak.speedPercentage.description")))
                                        .binding(100, () -> config.speedPercentage, val -> config.speedPercentage = val)
                                        .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                                                .range(25, 300)
                                                .step(1)
                                                .formatValue(val -> Text.translatable(
                                                        "config.sneaktweak.speedPercentage.value",
                                                        val
                                                ))
                                        )
                                        .build())
                                .option(Option.<SneakingEyeHeightType>createBuilder()
                                        .name(Text.translatable("config.sneaktweak.sneakingEyeHeightPreset"))
                                        .description(OptionDescription.of(Text.translatable("config.sneaktweak.sneakingEyeHeightPreset.description")))
                                        .binding(SneakingEyeHeightType.DEFAULT, () -> config.sneakingEyeHeightPreset, val -> config.sneakingEyeHeightPreset = val)
                                        .controller(opt -> EnumControllerBuilder.create(opt)
                                                .enumClass(SneakingEyeHeightType.class)
                                                .formatValue(type -> switch (type) {
                                                    case DEFAULT -> Text.translatable("config.sneaktweak.sneakingEyeHeightPreset.default");
                                                    case PRE_1_14 -> Text.translatable("config.sneaktweak.sneakingEyeHeightPreset.pre114");
                                                    case PRE_1_9 -> Text.translatable("config.sneaktweak.sneakingEyeHeightPreset.pre19");
                                                    case CUSTOM -> Text.translatable("config.sneaktweak.sneakingEyeHeightPreset.custom");
                                                }))
                                        .build()
                                )
                                .build())
                        .option(Option.<Float>createBuilder()
                                .name(Text.translatable("config.sneaktweak.customSneakingEyeHeight"))
                                .description(OptionDescription.of(Text.empty()))
                                .binding(1.27f, () -> config.customSneakingEyeHeight, val -> config.customSneakingEyeHeight = val)
                                .controller(opt -> FloatSliderControllerBuilder.create(opt).range(0f, 1.8f).step(0.01f))
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Text.translatable("config.sneaktweak.modifyThirdPersonSneakingEyeHeight"))
                                .description(OptionDescription.of(Text.empty()))
                                .binding(false, () -> config.modifyThirdPersonSneakingEyeHeight, val -> config.modifyThirdPersonSneakingEyeHeight = val)
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .build())
                .build().generateScreen(parent);

    }

    public static boolean isSmoothingEnabled() {
        return HANDLER.instance().smoothingEnabled;
    }

    public static float getSpeedModifier() {
        return HANDLER.instance().speedPercentage / 100f;
    }

    public static float modifySneakingEyeHeight(float height) {
        if (MinecraftClient.getInstance().options.getPerspective().isFirstPerson() || HANDLER.instance().modifyThirdPersonSneakingEyeHeight) {
            return switch (HANDLER.instance().sneakingEyeHeightPreset) {
                case DEFAULT -> height;
                case PRE_1_14 -> 1.42f;
                case PRE_1_9 -> 1.54f;
                case CUSTOM -> HANDLER.instance().customSneakingEyeHeight;
            };
        }
        return height;
    }

    public static void save() {
        HANDLER.save();
    }

    public enum SneakingEyeHeightType {
        DEFAULT,
        PRE_1_14,
        PRE_1_9,
        CUSTOM
    }
}