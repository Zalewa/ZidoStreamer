package eu.danman.zidostreamer.zidostreamer;

import java.util.ArrayList;
import java.util.List;

import android.content.SharedPreferences;

/**
 * Builds different ffmpeg command lines depending on selected streaming
 * settings.
 *
 * Path to ffmpeg executable is not prepended to the generated commands. The
 * caller must take care of that.
 *
 * @author Zalewa
 */
public class FfmpegCmdBuilder {

    public static List<String> streams(SharedPreferences settings) {
        return new FfmpegCmdBuilder().cmds(settings);
    }

    private List<String> cmds(SharedPreferences settings) {
        List<String> cmds = new ArrayList<String>();
        switch (settings.getString("stream_type", "1")){
            case "1":
                cmds.add(mpegts() + destUrl(settings));
                break;
            case "2":
                cmds.add(rtmp() + destUrl(settings));
                break;
            case "3":
                cmds.add(mpjpeg() + destUrl(settings));
                cmds.add(wav() + destUrl(settings, 1));
                break;
        }
        return cmds;
    }

    private String destUrl(SharedPreferences settings) {
        return destUrl(settings, 0);
    }

    private String destUrl(SharedPreferences settings, int portOffset) {
        String url = settings.getString("dest_url", "");
        if (portOffset != 0) {
            url = offsetPort(portOffset, url);
        }
        return url;
    }

    private String offsetPort(int portOffset, String url) {
        int idxPort = url.lastIndexOf(":");
        if (idxPort >= 0) {
            ++idxPort;
            int idxPortEnd = idxPort;
            while (idxPortEnd < url.length() && Character.isDigit(url.charAt(idxPortEnd))) {
                ++idxPortEnd;
            }
            String before = url.substring(0, idxPort);
            String port = url.substring(idxPort, idxPortEnd);
            String after = "";
            if (idxPortEnd < url.length()) {
                after = url.substring(idxPortEnd);
            }
            return before + Integer.parseInt(port) + portOffset + after;
        } else {
            throw new IllegalArgumentException(
                "this streaming setting requires port specification");
        }
    }


    private String mpegts() {
        return "-i - -codec:v copy -codec:a copy -bsf:v dump_extra -f mpegts ";
    }

    private String rtmp() {
        return "-i - -strict -2 -codec:v copy -codec:a aac -b:a 128k -f flv ";
    }

    private String mpjpeg() {
        return "-an -i - -codec:v mjpeg -qmin 1 -qmax 1 -f mpjpeg";
    }

    private String wav() {
        return "-vn -i - -f wav -codec:a pcm_s16le -f wav";
    }
}
