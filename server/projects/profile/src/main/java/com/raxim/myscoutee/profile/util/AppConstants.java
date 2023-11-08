package com.raxim.myscoutee.profile.util;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

import org.bson.BsonType;
import org.bson.Document;
import org.bson.UuidRepresentation;
import org.bson.codecs.BsonTypeClassMap;
import org.bson.codecs.BsonValueCodecProvider;
import org.bson.codecs.Codec;
import org.bson.codecs.CollectionCodecProvider;
import org.bson.codecs.DocumentCodecProvider;
import org.bson.codecs.IterableCodecProvider;
import org.bson.codecs.MapCodecProvider;
import org.bson.codecs.ValueCodecProvider;
import org.bson.codecs.configuration.CodecRegistries;

public class AppConstants {
        public static final String WOMAN = "w";
        public static final String MAN = "m";

        public static final String MONTH = "3";
        public static final String WEEK = "2";
        public static final String DAY = "1";

        public static final String GROUP_NAME = "group";

        public static final String MONTH_FORMAT = "%Y-%m";
        public static final String WEEK_FORMAT = "%Y %U";
        public static final String DAY_FORMAT = "%Y-%m-%d";

        public static final LocalDate DATE_MIN = LocalDate.of(1900, 1, 1);

        public final static BsonTypeClassMap TYPE_MAP = new BsonTypeClassMap(Map.of(BsonType.BINARY, UUID.class));
        public static final Codec<Document> UUID_CODEC = CodecRegistries
                        .withUuidRepresentation(CodecRegistries.fromProviders(Arrays.asList(new ValueCodecProvider(),
                                        new CollectionCodecProvider(TYPE_MAP), new IterableCodecProvider(TYPE_MAP),
                                        new BsonValueCodecProvider(), new DocumentCodecProvider(TYPE_MAP),
                                        new MapCodecProvider(TYPE_MAP))),
                                        UuidRepresentation.JAVA_LEGACY)
                        .get(Document.class);

        public static final UUID UUID_SYSTEM = UUID.fromString("58d6abac-fd83-40a0-b339-14f7f009cd29");

        public static final String ERR_EVENT_FULL = "err.event_full";
        public static final String ERR_NOT_SELECTED_PROFILE = "err.not_selected_profile";
        public static final String ERR_ILLEGAL_ACCESS = "err.event_illegal_access";
        public static final String ERR_INVISIBLE_PROFILE = "err.invisible_profile";
        public static final String ERR_NO_PROFILE = "err.no_profile";
        public static final String ERR_NO_EDIT = "err.no_edit";
        public static final String ERR_SAVE = "err.save";
        public static final String ERR_FRIENDS_ONLY = "err.friends_only";

        public static final String RANK_NONE = "none";
        public static final String RANK_RATE = "rate";
        public static final String RANK_FIFA = "fifa";

        public static final String SCHEDULE_RANDOM_GROUP = "RANDOM_GROUP";

}
