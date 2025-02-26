package github.kasuminova.ecoaeextension.client.gui.widget;

import github.kasuminova.mmce.client.gui.util.MousePos;
import github.kasuminova.mmce.client.gui.util.RenderPos;
import github.kasuminova.mmce.client.gui.util.RenderSize;
import github.kasuminova.mmce.client.gui.widget.base.DynamicWidget;
import github.kasuminova.mmce.client.gui.widget.base.WidgetGui;
import github.kasuminova.ecoaeextension.common.util.BiFunction2Bool;
import github.kasuminova.ecoaeextension.common.util.NumberUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Keyboard;

import java.util.List;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static github.kasuminova.mmce.client.gui.widget.MultiLineLabel.DEFAULT_FONT_HEIGHT;

public class InputBox extends DynamicWidget {

    protected final GuiTextField field = new GuiTextField(-1, Minecraft.getMinecraft().fontRenderer, 0, 0, 100, 14);

    protected InputType inputType = InputType.STRING;

    protected String prompt = "";

    protected BiFunction2Bool<InputBox, Character> onUserKeyTyped = null;
    protected BiFunction2Bool<InputBox, String> onUserConfirm = null;
    protected BiFunction2Bool<InputBox, Integer> onUserConfirmInt = null;
    protected BiFunction2Bool<InputBox, Long> onUserConfirmLong = null;
    protected BiFunction2Bool<InputBox, Double> onUserConfirmDouble = null;

    protected BiConsumer<InputBox, String> onContentChange = null;

    protected Function<InputBox, List<String>> tooltipFunction = null;

    @Override
    public void render(final WidgetGui gui, final RenderSize renderSize, final RenderPos renderPos, final MousePos mousePos) {
        int height = renderSize.height();
        int heightOffset = height > DEFAULT_FONT_HEIGHT ? (height - DEFAULT_FONT_HEIGHT) / 2 : 0;

        field.width = renderSize.width();
        field.height = Math.min(height, DEFAULT_FONT_HEIGHT);
        field.x = renderPos.posX();
        field.y = renderPos.posY() + heightOffset;
        field.drawTextBox();
        if (!field.isFocused() && field.getText().isEmpty()) {
            FontRenderer fr = Minecraft.getMinecraft().fontRenderer;
            fr.drawStringWithShadow(prompt, field.x, field.y, 0xFFFFFF);
        }
        GlStateManager.color(1F, 1F, 1F, 1F);
        GlStateManager.enableBlend();
    }

    @Override
    public boolean onKeyTyped(final char typedChar, final int keyCode) {
        if (!field.isFocused()) {
            return false;
        }

        String text = field.getText();
        if (!text.isEmpty() && keyCode == Keyboard.KEY_RETURN) {
            if (processUserConfirm(text)) {
                if (onContentChange != null) {
                    onContentChange.accept(this, field.getText());
                }
                return true;
            }
            return false;
        }

        if (GuiScreen.isKeyComboCtrlC(keyCode) ||
            GuiScreen.isKeyComboCtrlV(keyCode) ||
            GuiScreen.isKeyComboCtrlX(keyCode) ||
            GuiScreen.isKeyComboCtrlA(keyCode) ||
            keyCode == Keyboard.KEY_BACK ||
            keyCode == Keyboard.KEY_DELETE ||
            keyCode == Keyboard.KEY_LEFT ||
            keyCode == Keyboard.KEY_RIGHT ||
            keyCode == Keyboard.KEY_HOME ||
            keyCode == Keyboard.KEY_END)
        {
            final String prev = field.getText();
            final boolean success = field.textboxKeyTyped(typedChar, keyCode);
            if (success && !prev.equals(field.getText())) {
                if (onContentChange != null) {
                    onContentChange.accept(this, field.getText());
                }
            }
            return success;
        }

        if (onUserKeyTyped != null && onUserKeyTyped.apply(this, typedChar)) {
            return true;
        }

        if (switch (inputType) {
            case STRING -> field.textboxKeyTyped(typedChar, keyCode);
            case NUMBER -> {
                boolean result = processNumberTypeInput(typedChar, keyCode);
                String newText = field.getText();
                if (newText.isEmpty() || NumberUtils.canParse(newText)) {
                    field.setTextColor(0xFFFFFF);
                } else {
                    field.setTextColor(0xFF0000);
                }
                yield result;
            }
        }) {
            if (onContentChange != null) {
                onContentChange.accept(this, field.getText());
            }
            return true;
        }
        return false;
    }

    protected boolean processNumberTypeInput(final char typedChar, final int keyCode) {
        String text = field.getText();

        // 如果是负号且当前输入为空，允许输入
        if (typedChar == '-' && text.isEmpty()) {
            field.setText("-"); // 直接设置负号
            return true;
        }

        // 根据当前输入和进制规则判断字符是否有效
        String newInput;
        if (text.startsWith("-")) { // 处理负数情况
            newInput = text.substring(1); // 去掉负号进行检查
        } else {
            newInput = text;
        }

        // 判断进制并检查字符有效性
        boolean isValid = switch (getActiveBase(newInput)) {
            case 2 -> typedChar == '0' || typedChar == '1';
            case 8 -> Character.toString(typedChar).matches("[0-7]");
            case 10 -> Character.isDigit(typedChar) || typedChar == '.';
            case 16 -> Character.toString(typedChar).matches("[0-9a-fA-F]");
            // 如果无法识别进制，默认无效
            default -> false;
        };

        // 如果字符有效，更新输入框文本
        if (isValid) {
            return field.textboxKeyTyped(typedChar, keyCode);
        }

        // 字符无效，不改变输入框内容
        return false;
    }

    protected boolean processUserConfirm(final String text) {
        if (inputType == InputType.STRING) {
            if (onUserConfirm != null && onUserConfirm.apply(this, text)) {
                field.setText("");
                return true;
            }
        }

        if (onUserConfirmInt != null) {
            OptionalInt parsed = NumberUtils.tryParseInt(text);
            if (parsed.isPresent() && onUserConfirmInt.apply(this, parsed.getAsInt())) {
                field.setText("");
                return true;
            }
        }

        if (onUserConfirmLong != null) {
            OptionalLong parsed = NumberUtils.tryParseLong(text);
            if (parsed.isPresent() && onUserConfirmLong.apply(this, parsed.getAsLong())) {
                field.setText("");
                return true;
            }
        }

        if (onUserConfirmDouble != null) {
            OptionalDouble parsed = NumberUtils.tryParseDouble(text);
            if (parsed.isPresent() && onUserConfirmDouble.apply(this, parsed.getAsDouble())) {
                field.setText("");
                return true;
            }
        }

        return false;
    }

    // Text

    public String getText() {
        return field.getText();
    }

    // Input type

    public InputType getInputType() {
        return inputType;
    }

    public InputBox setInputType(final InputType inputType) {
        this.inputType = inputType;
        return this;
    }

    // Prompt

    public String getPrompt() {
        return prompt;
    }

    public InputBox setPrompt(final String prompt) {
        this.prompt = prompt;
        return this;
    }

    // Properties

    @Override
    public InputBox setWidth(final int width) {
        super.setWidth(width);
        field.width = width;
        return this;
    }

    @Override
    public InputBox setHeight(final int height) {
        super.setHeight(height);
        field.height = height;
        return this;
    }

    public InputBox setMaxStringLength(final int length) {
        field.setMaxStringLength(length);
        return this;
    }

    public InputBox setEnableBackground(final boolean enableBackground) {
        field.setEnableBackgroundDrawing(enableBackground);
        return this;
    }

    // Functions

    public InputBox setOnUserKeyTyped(final BiFunction2Bool<InputBox, Character> onUserKeyTyped) {
        this.onUserKeyTyped = onUserKeyTyped;
        return this;
    }

    public InputBox setOnUserConfirm(final BiFunction2Bool<InputBox, String> onUserConfirm) {
        this.onUserConfirm = onUserConfirm;
        return this;
    }

    public InputBox setOnUserConfirmInt(final BiFunction2Bool<InputBox, Integer> onUserConfirmInt) {
        this.onUserConfirmInt = onUserConfirmInt;
        return this;
    }

    public InputBox setOnUserConfirmLong(final BiFunction2Bool<InputBox, Long> onUserConfirmLong) {
        this.onUserConfirmLong = onUserConfirmLong;
        return this;
    }

    public InputBox setOnUserConfirmDouble(final BiFunction2Bool<InputBox, Double> onUserConfirmDouble) {
        this.onUserConfirmDouble = onUserConfirmDouble;
        return this;
    }

    public InputBox setOnContentChange(final BiConsumer<InputBox, String> onContentChange) {
        this.onContentChange = onContentChange;
        return this;
    }

    // Tooltip function

    public InputBox setTooltipFunction(final Function<InputBox, List<String>> tooltipFunction) {
        this.tooltipFunction = tooltipFunction;
        return this;
    }

    @Override
    public List<String> getHoverTooltips(final WidgetGui widgetGui, final MousePos mousePos) {
        if (tooltipFunction != null) {
            return tooltipFunction.apply(this);
        }
        return super.getHoverTooltips(widgetGui, mousePos);
    }

    @Override
    public boolean onMouseClick(final MousePos mousePos, final RenderPos renderPos, final int mouseButton) {
        if (!field.isFocused() && mouseButton == 0) {
            field.setFocused(true);
            return true;
        }
        if (field.isFocused() && mouseButton == 1) {
            field.setText("");
            if (onContentChange != null) {
                onContentChange.accept(this, "");
            }
            return true;
        }
        return super.onMouseClick(mousePos, renderPos, mouseButton);
    }

    @Override
    public void onMouseClickGlobal(final MousePos mousePos, final RenderPos renderPos, final int mouseButton) {
        if (field.isFocused()) {
            if (!isMouseOver(mousePos)) {
                field.setFocused(false);
            }
        }
        super.onMouseClickGlobal(mousePos, renderPos, mouseButton);
    }

    protected static int getActiveBase(String input) {
        if (input.startsWith("0x") || input.startsWith("0X")) return 16;
        if (input.startsWith("0b") || input.startsWith("0B")) return 2;
        if (input.startsWith("0o") || input.startsWith("0O")) return 8;
        return 10;
    }

    public enum InputType {
        NUMBER,
        STRING
    }

}
