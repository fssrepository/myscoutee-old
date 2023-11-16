package com.raxim.myscoutee.common;

import java.util.List;
import java.util.UUID;

import com.mongodb.client.model.geojson.Point;
import com.mongodb.client.model.geojson.Position;

public class AppTestConstants {
    public static final UUID UUID_PROFILE_OLIVER = UUID.fromString("534ccc6b-2547-4bf0-ad91-dca739943ea4");
    public static final UUID UUID_PROFILE_EMMA = UUID.fromString("fc4c72f8-a905-e1d1-05f8-c0d9914e158c");
    public final static UUID UUID_PROFILE_AVA = UUID.fromString("eb494fba-6c56-752f-5123-c414c8c86599");
    public static final UUID UUID_PROFILE_ETHAN = UUID.fromString("2f7e01ce-1336-37d4-e69e-efc88d2ee81a");
    public static final UUID UUID_PROFILE_LUCAS = UUID.fromString("694ef9b8-e486-bdf2-6510-7d05f19db486");
    public static final UUID UUID_PROFILE_LIAM = UUID.fromString("8f4bd3a8-1195-01b0-172a-a04f052f5982");
    public static final UUID UUID_PROFILE_SOPHIA = UUID.fromString("39402632-a452-57be-2518-53cc117b1abc");
    public final static UUID UUID_PROFILE_AMELIA = UUID.fromString("a84e4e95-d9c6-bd2c-2ed8-66e52cf2f5bb");
    public final static UUID UUID_PROFILE_MIA = UUID.fromString("4f837de6-5d83-2ddb-f545-c12539f490bc");
    public final static UUID UUID_PROFILE_CHARLOTTE = UUID.fromString("2b418324-7733-97ed-6730-bd1e3d589aa8");
    public final static UUID UUID_PROFILE_LILY = UUID.fromString("ed49d5e2-2227-681a-4017-2c21cb1c68bc");
    public final static UUID UUID_PROFILE_EVELYIN = UUID.fromString("428c6c68-23b9-07c7-71e7-720b001abdb7");

    public final static String NAME_PROFILE_OLIVER = "Oliver";
    public final static String NAME_PROFILE_AVA = "Ava";

    public final static UUID UUID_PROFILE_MASON_2 = UUID.fromString("eb494fba-6c56-752f-5123-c414c8c86599");

    public static final UUID UUID_EVENT_32 = UUID.fromString("15ad960f-b104-45ee-8510-ebf6ac78d3d8");
    public static final UUID UUID_EVENT_32_P = UUID.fromString("c20b958a-1daa-442c-9187-92de2c5b7806");
    public static final UUID UUID_EVENT_1 = UUID.fromString("1d03cf6d-a6bb-4ca0-92fa-251502bac825");
    public static final UUID UUID_EVENT_11 = UUID.fromString("43d37969-0bec-4c8c-a33b-69a149372573");
    public static final UUID UUID_EVENT_111 = UUID.fromString("b5aa652d-b55b-4861-b45b-0b3d58d16808");
    public static final String UUID_EVENT_111_FAKE_NAME = "FAKE_111";

    public static final UUID UUID_EVENT_ITEM_32_M1_S1 = UUID.fromString("541a9f25-7594-41e6-9707-0055be0a48f1");
    public static final UUID UUID_EVENT_ITEM_32_M1_S2 = UUID.fromString("e063f444-9881-45e0-9ee1-b67bef9431dd");
    public static final UUID UUID_EVENT_ITEM_33_M2_S1 = UUID.fromString("b5aa652d-b55b-4861-b45b-0b3d58d16808");

    public final static UUID UUID_GROUP_DATING = UUID.fromString("b33ec186-aea8-4636-b635-4a2f620a0c54");

    public final static Point LOCATION_PROFILE_LIAM = new Point(new Position(List.of(47.497912, 19.040235)));
    public final static Point LOCATION_PROFILE_OLIVER = new Point(new Position(List.of(47.497912, 19.040235)));

    public final static String DEVICE_OLIVER = "DV_Oliver";
    public final static String DEVICE_MASON = "DV_Mason";
    public final static String DEVICE_LUCAS = "DV_Lucas";

    public final static String MSG_1 = "msg_1";
    public final static String MSG_2 = "msg_2";
    public final static String MSG_3 = "msg_3";
}
