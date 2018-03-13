package com.robomwm.prettysimpleshop;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created on 3/10/2018.
 *
 * @author RoboMWM
 */
public class LazyUtil
{
    public static TextComponent getClickableCommand(String message, String command)
    {
        return getClickableCommand(message, command, command);
    }

    public static TextComponent getClickableCommand(String message, String command, String hover)
    {
        TextComponent textComponent = new TextComponent(message);
        textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
        if (hover != null)
            textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(hover)));
        return textComponent;
    }

    public static TextComponent getClickableSuggestion(String message, String suggestion, String hover)
    {
        TextComponent textComponent = new TextComponent(message);
        textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, suggestion));
        if (hover != null)
            textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(hover)));
        return textComponent;
    }

    public static BaseComponent[] buildPage(Object... strings)
    {
        List<BaseComponent> baseComponents = new ArrayList<>(strings.length);
        for (Object object : strings)
        {
            if (object instanceof BaseComponent)
                baseComponents.add((BaseComponent)object);
            else if (object instanceof String)
                baseComponents.addAll(Arrays.asList(TextComponent.fromLegacyText((String)object)));
        }
        return baseComponents.toArray(new BaseComponent[baseComponents.size()]);
    }

    public static List<BaseComponent> addLegacyText(String string, List<BaseComponent> baseComponents)
    {
        for (BaseComponent baseComponent : TextComponent.fromLegacyText(string))
            baseComponents.add(baseComponent);
        return baseComponents;
    }
}
