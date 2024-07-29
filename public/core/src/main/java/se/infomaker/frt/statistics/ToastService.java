package se.infomaker.frt.statistics;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.util.Map;

public class ToastService implements StatisticsManager.StatisticsService {
    private Context context;
    private Handler handler;

    @Override
    public void init(Context context, Map<String, Object> config) {
        this.context = context;
        handler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void logEvent(StatisticsEvent event) {
        StringBuilder builder = new StringBuilder();
        builder.append("event:").append(event.getEventName()).append("\n")
                .append("---------").append("\n");
        for (String key : event.getAttributes().keySet()) {
            builder.append(key).append(":").append(event.getAttributes().get(key)).append("\n");
        }
        handler.post(() -> Toast.makeText(context, builder.toString(), Toast.LENGTH_SHORT).show());
    }

    @Override
    public String getIdentifier() {
        return "Toast";
    }

    @Override
    public void globalAttributesUpdated(@NonNull Map<String, Object> globalAttributes) {
        // NOP
    }
}
