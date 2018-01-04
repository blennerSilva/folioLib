package com.folioreader.ui.base;

import android.content.Context;

import com.folioreader.Config;
import com.folioreader.Constants;
import com.folioreader.R;

/**
 * @author gautam chibde on 14/6/17.
 */

public final class HtmlUtil {

    /**
     * Function modifies input html string by adding extra css,js and font information.
     *
     * @param context     Activity Context
     * @param htmlContent input html raw data
     * @return modified raw html string
     */
    public static String getHtmlContent(Context context, String htmlContent, Config config) {
        String cssPath =
                String.format(context.getString(R.string.css_tag), "file:///android_asset/css/Style.css");


        String jsPath = String.format(context.getString(R.string.script_tag),
                "file:///android_asset/js/jsface.min.js");
        jsPath =
                jsPath + String.format(context.getString(R.string.script_tag),
                        "file:///android_asset/js/jquery-3.1.1.min.js");

        jsPath =
                jsPath + String.format(context.getString(R.string.script_tag),
                        "file:///android_asset/js/rangy-core.js");
        jsPath =
                jsPath + String.format(context.getString(R.string.script_tag),
                        "file:///android_asset/js/rangy-highlighter.js");
        jsPath =
                jsPath + String.format(context.getString(R.string.script_tag),
                        "file:///android_asset/js/rangy-classapplier.js");
        jsPath =
                jsPath + String.format(context.getString(R.string.script_tag),
                        "file:///android_asset/js/rangy-serializer.js");
        jsPath =
                jsPath + String.format(context.getString(R.string.script_tag),
                        "file:///android_asset/js/rangy-serializer.js");
        jsPath =
                jsPath + String.format(context.getString(R.string.script_tag),
                        "file:///android_asset/js/Bridge.js");

        jsPath =
                jsPath + String.format(context.getString(R.string.script_tag),
                        "file:///android_asset/android.selection.js");
        jsPath =
                jsPath + String.format(context.getString(R.string.script_tag_method_call),
                        "setMediaOverlayStyleColors('#C0ED72','#C0ED72')");

        String toInject = "\n" + cssPath + "\n" + jsPath + "\n</head>";
        htmlContent = htmlContent.replace("</head>", toInject);

        String classes = "";
        switch (config.getFont()) {
            case Constants.FONT_ANDADA:
                classes = "andada";
                break;
            case Constants.FONT_LATO:
                classes = "lato";
                break;
            case Constants.FONT_LORA:
                classes = "lora";
                break;
            case Constants.FONT_RALEWAY:
                classes = "raleway";
                break;
            default:
                break;
        }

        if (config.isNightMode()) {
            classes += " nightMode";
        }

        if (config.getFontSize() > 0 && config.getFontSize() < 3) {
            classes += " textSizeOne";
        } else if (config.getFontSize() > 3 && config.getFontSize() < 6) {
            classes += " textSizeTwo";
        } else if (config.getFontSize() > 7 && config.getFontSize() < 10) {
            classes += " textSizeThree";
        } else if (config.getFontSize() > 11 && config.getFontSize() < 14) {
            classes += " textSizeFour";
        } else if (config.getFontSize() > 14 && config.getFontSize() < 20) {
            classes += " textSizeFive";
        }

        htmlContent = htmlContent.replace("<html ", "<html class=\"" + classes + "\" ");
        return htmlContent;
    }
}
