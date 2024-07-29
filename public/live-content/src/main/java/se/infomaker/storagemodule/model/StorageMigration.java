package se.infomaker.storagemodule.model;

import java.util.UUID;

import io.realm.DynamicRealm;
import io.realm.DynamicRealmObject;
import io.realm.FieldAttribute;
import io.realm.RealmList;
import io.realm.RealmMigration;
import io.realm.RealmObjectSchema;
import io.realm.RealmSchema;

public class StorageMigration implements RealmMigration {

    public static final String TYPE_LOCATION = "location";

    @Override
    public void migrate(final DynamicRealm realm, long oldVersion, long newVersion) {
        if (oldVersion == 0) {
            RealmSchema schema = realm.getSchema();

            RealmObjectSchema keyValue = schema.create("KeyValue")
                    .addField("id", String.class, FieldAttribute.PRIMARY_KEY)
                    .addField("key", String.class, FieldAttribute.REQUIRED)
                    .addField("value", String.class);
            RealmObjectSchema subscription = schema.get("Subscription")
                    .addRealmListField("parameters", keyValue)
                    .addField("type", String.class, FieldAttribute.REQUIRED)
                    .transform(new RealmObjectSchema.Function() {
                        @Override
                        public void apply(DynamicRealmObject obj) {
                            obj.setString("type", TYPE_LOCATION);
                            double latitude = obj.getDouble("latitude");
                            double longitude = obj.getDouble("longitude");
                            float radius = obj.getFloat("radius");
                            RealmList<DynamicRealmObject> objects = new RealmList<>();
                            DynamicRealmObject lat = realm.createObject("KeyValue", UUID.randomUUID().toString());
                            lat.setString("key", "latitude");
                            lat.setString("value", Double.toString(latitude));
                            objects.add(lat);

                            DynamicRealmObject lon = realm.createObject("KeyValue", UUID.randomUUID().toString());
                            lon.setString("key", "longitude");
                            lon.setString("value", Double.toString(longitude));
                            objects.add(lon);

                            DynamicRealmObject rad = realm.createObject("KeyValue", UUID.randomUUID().toString());
                            rad.setString("key", "radius");
                            rad.setString("value", Float.toString(radius));
                            objects.add(rad);
                            obj.setList("parameters", objects);
                        }
                    })
                    .removeField("latitude")
                    .removeField("longitude")
                    .removeField("radius");
        }
        if (oldVersion == 1) {
            RealmSchema schema = realm.getSchema();
            RealmObjectSchema subscription = schema.get("Subscription")
                    .addField("pendingOperation", String.class, FieldAttribute.REQUIRED);
            oldVersion++;
        }
        if (oldVersion == 2) {
            RealmSchema schema = realm.getSchema();
            RealmObjectSchema subscription = schema.get("Subscription").addField("state", String.class);
            oldVersion++;
        }
        if (oldVersion == 3) {
            RealmSchema schema = realm.getSchema();
            RealmObjectSchema subscription = schema.get("Subscription")
                    .removeField("state")
                    .removeField("pendingOperation");
            oldVersion++;
        }
    }
}
